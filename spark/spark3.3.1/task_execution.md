# Task 在 Executor 中的运行流程
## 代码示例
```scala
val textFile = sc.textFile("hdfs://...")           // Stage 0 开始
val words = textFile.flatMap(_.split(" "))         // 仍在 Stage 0 (narrow dependency)
val pairs = words.map(word => (word, 1))           // 仍在 Stage 0 (narrow dependency)
val counts = pairs.reduceByKey(_ + _)               // Stage 0 结束，Stage 1 开始 (shuffle dependency)
counts.collect()                                    // Stage 1：ResultStage
```

## Task 执行完整流程
```mermaid
graph TD
    subgraph Driver["Driver 端"]
        A[TaskScheduler 接收 TaskSet] --> B[选择 Executor]
        B --> C[序列化 Task 和依赖]
        C --> D[发送 LaunchTask 消息]
    end

    subgraph Executor["Executor 端"]
        D --> E[CoarseGrainedExecutorBackend<br/>接收 LaunchTask]
        E --> F[Executor.launchTask]

        subgraph TaskLaunch["Task 启动阶段"]
            F --> G[创建 TaskRunner]
            G --> H[提交到 ThreadPool]
            H --> I[TaskRunner.run 开始执行]
        end

        subgraph TaskRun["Task 运行阶段"]
            I --> J[反序列化 Task 对象]
            J --> K{Task 类型判断}

            K -->|ShuffleMapTask| L[ShuffleMapTask 执行]
            K -->|ResultTask| M[ResultTask 执行]

            subgraph ShuffleMapExec["ShuffleMapTask 执行"]
                L --> N[反序列化 RDD 和 ShuffleDependency]
                N --> O[调用 RDD.iterator 计算数据]
                O --> P[ShuffleWriter 写入 Shuffle 文件]
                P --> Q[返回 MapStatus<br/>记录 Shuffle 文件位置和大小]
            end

            subgraph ResultTaskExec["ResultTask 执行"]
                M --> R[反序列化 RDD 和 Func]
                R --> S{需要 Shuffle Read?}
                S -->|是| T[ShuffleReader 读取上游数据]
                S -->|否| U[直接读取本地数据]
                T --> V[调用 RDD.iterator 计算数据]
                U --> V
                V --> W[应用 ResultFunction]
                W --> X[返回计算结果]
            end
        end

        subgraph TaskComplete["Task 完成阶段"]
            Q --> Y[序列化 Task 结果]
            X --> Y
            Y --> Z{结果大小判断}
            Z -->|小于阈值| AA[DirectTaskResult<br/>直接返回]
            Z -->|大于阈值| BB[存储到 BlockManager<br/>返回 IndirectTaskResult]
            AA --> CC[发送结果给 Driver]
            BB --> CC
        end
    end

    subgraph Driver2["Driver 端结果处理"]
        CC --> DD[TaskResultGetter 接收结果]
        DD --> EE[反序列化结果]
        EE --> FF[更新 TaskSetManager]
        FF --> GG[通知 DAGScheduler]
    end

    style L fill:#ffeb3b
    style M fill:#4caf50
    style P fill:#ff9800
    style T fill:#2196f3
    style AA fill:#e8f5e8
    style BB fill:#fff3e0

    classDef driverStyle fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef executorStyle fill:#f1f8e9,stroke:#388e3c,stroke-width:2px
    classDef shuffleStyle fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class Driver,Driver2 driverStyle
    class Executor executorStyle
    class ShuffleMapExec,ResultTaskExec shuffleStyle
```

## ShuffleMapTask
- **继承关系**: `extends Task[MapStatus]`
- **职责**: 处理 Shuffle Write 操作，将 RDD 数据按分区器分桶写入磁盘
- **关键参数**:
  - `taskBinary: Broadcast[Array[Byte]]` - 广播的 RDD 和 ShuffleDependency
  - `partition: Partition` - 要处理的 RDD 分区
  - `mapId: Long` - Map 任务的唯一标识

**Stage 0: ShuffleMapTask 执行流程**

```mermaid
sequenceDiagram
    participant SMT as ShuffleMapTask
    participant RDD1 as textFile(HadoopRDD)
    participant RDD2 as words(MapPartitionsRDD)
    participant RDD3 as pairs(MapPartitionsRDD)
    participant SW as ShuffleWriter
    participant Disk as 磁盘文件系统

    Note over SMT: runTask() 开始执行
    SMT->>SMT: 反序列化 (textFile.flatMap().map(), ShuffleDependency)

    Note over SMT: 调用 rdd.iterator() - 管道化执行
    SMT->>RDD3: iterator(partition, context)
    RDD3->>RDD2: iterator(partition, context)
    RDD2->>RDD1: iterator(partition, context)

    Note over RDD1: 读取 HDFS 文件块
    RDD1-->>RDD1: 读取数据: ["hello world", "spark scala"]
    RDD1-->>RDD2: Iterator[String]

    Note over RDD2: flatMap(_.split(" "))
    RDD2-->>RDD2: 应用用户函数: ["hello", "world", "spark", "scala"]
    RDD2-->>RDD3: Iterator[String]

    Note over RDD3: map(word => (word, 1))
    RDD3-->>RDD3: 应用用户函数: [("hello",1), ("world",1), ("spark",1), ("scala",1)]
    RDD3-->>SMT: Iterator[(String, Int)]

    Note over SMT: 获取 ShuffleWriter 开始写入
    SMT->>SW: shuffleManager.getWriter(handle, mapId, context)
    SMT->>SW: write(iterator) - 按分区器分桶

    loop 对每个 (key, value) 对
        SW->>SW: partitioner.getPartition(key)
        Note over SW: "hello" -> partition 0<br/>"world" -> partition 1<br/>"spark" -> partition 0<br/>"scala" -> partition 1
    end

    SW->>Disk: 写入 shuffle_0_0.data (分区0数据)
    SW->>Disk: 写入 shuffle_0_0.index (分区边界)
    SW->>SMT: stop(success=true) -> MapStatus

    Note over SMT: 返回 MapStatus(文件位置和大小)
```

**关键特性**:
- **数据分桶**: 根据 `Partitioner.getPartition(key)` 将数据分配到不同的输出分区
- **文件格式**: 生成 `.data` 文件（数据）和 `.index` 文件（分区边界索引）
- **Push-based Shuffle**: 支持主动推送数据到 Merge 服务器减少 Shuffle Read 开销
- **度量收集**: 通过 `ShuffleWriteMetricsReporter` 收集写入指标

## ResultTask
- **继承关系**: `extends Task[U]`
- **职责**: 执行最终的结果计算，可能包含 Shuffle Read
- **关键参数**:
  - `taskBinary: Broadcast[Array[Byte]]` - 广播的 RDD 和执行函数
  - `outputId: Int` - 在 Job 中的输出索引

**Stage 1: ResultTask 执行流程**
```mermaid
sequenceDiagram
    participant RT as ResultTask
    participant SR as ShuffleReader
    participant BM as BlockManager
    participant ES as ExternalSorter
    participant RDD4 as ShuffledRDD
    participant CF as collect函数

    Note over RT: runTask() 开始执行
    RT->>RT: 反序列化 (ShuffledRDD, collect函数)

    Note over RT: 调用 rdd.iterator() - Shuffle Read
    RT->>RDD4: iterator(partition, context)
    RDD4->>SR: shuffleManager.getReader(handle, partition, context)

    Note over SR: 读取多个 ShuffleMapTask 的输出
    SR->>BM: getBlockData(shuffle_0_0, partition_0)
    BM-->>SR: 从 Executor-1 获取数据
    SR->>BM: getBlockData(shuffle_0_1, partition_0)
    BM-->>SR: 从 Executor-2 获取数据
    SR->>BM: getBlockData(shuffle_0_N, partition_0)
    BM-->>SR: 从 Executor-N 获取数据

    Note over SR: 合并来自多个 Map 的数据
    SR->>ES: 创建 ExternalSorter
    ES->>ES: 插入数据: [("hello",1), ("hello",1), ("spark",1), ...]
    ES->>ES: 按 key 分组并聚合: reduceByKey(_ + _)
    ES-->>SR: 聚合结果: [("hello",5), ("spark",3), ...]

    SR-->>RDD4: Iterator[(String, Int)] - 已聚合数据
    RDD4-->>RT: Iterator[(String, Int)]

    Note over RT: 应用最终函数
    RT->>CF: func(context, iterator) - collect()
    CF->>CF: 将所有数据收集到数组
    CF-->>RT: Array[String, Int] - 最终结果

    Note over RT: 返回结果给 Driver
```

**关键特性**:
- **Shuffle Read**: 自动处理从多个 ShuffleMapTask 读取和合并数据
- **网络传输**: 通过 BlockManager 获取远程 Shuffle 数据
- **内存管理**: 使用 ExternalSorter 进行基于磁盘的排序合并
- **容错处理**: Shuffle Read 失败时会重新获取数据

## SortShuffleManager
- registerShuffle(): 注册 ShuffleHandle 任务，初始化相关数据结构。
- getReader(): 返回 ShuffleReader 实例，负责读取排序后的数据。
- getWriter(): 返回 ShuffleWriter 实例，负责将数据排序后写入磁盘。

## ShuffleHandle 类型详解
```mermaid
flowchart TD
    A[registerShuffle] --> B{需要聚合?}
    B -->|否| C{分区数 ≤ 200?}

    C -->|是| E[BypassMergeSortShuffleHandle<br/>🚀 直接写入，最快]
    C -->|否| F{支持序列化器对象重定位?}

    F -->|是| G[SerializedShuffleHandle<br/>⚡ 高性能指针排序]
    F -->|否| D
    B -->|是| D[BaseShuffleHandle<br/>🔧 支持聚合+排序]

    style E fill:#e8f5e8
    style G fill:#fff3e0
    style D fill:#e3f2fd
```

**性能对比**:

| 特性 | BypassMergeSortShuffleHandle | SerializedShuffleHandle | BaseShuffleHandle |
|------|----------------------------|------------------------|------------------|
| **写入速度** | 🚀 最快 (直接写入) | ⚡ 快 (指针排序) | 🐌 较慢 (完整排序) |
| **内存使用** | 📈 高 (多文件缓冲区) | 📉 低 (序列化数据) | 📊 中等 (ExternalSorter) |
| **CPU开销** | 🔥 最低 (无排序) | ⚡ 低 (只排序指针) | 💪 高 (完整排序+聚合) |
| **适用场景** | 小分区数重分区 | 大数据Shuffle | 聚合操作 |
| **限制条件** | 🔒 苛刻 (分区≤200 + 无聚合) | ⚠️ 中等 (Kryo + 无聚合 + 分区≤1677万) | ✅ 无限制 |
| **网络传输** | 原数据量 | 原数据量 | 聚合后减少 |
| **Reduce端压力** | 🔥 高 (需完整排序) | ⚡ 中等 (需key排序) | 📉 低 (只需归并) |
| **典型用例** | `repartition(100)` | `sortByKey()`, `repartition(1000)` | `reduceByKey()`, `aggregateByKey()` |

**选择建议**:
- **小分区重分区**: BypassMergeSortShuffleHandle (如100个分区的repartition)
- **大数据排序**: SerializedShuffleHandle + Kryo (如TB级数据的sortByKey)
- **聚合计算**: BaseShuffleHandle (如reduceByKey、groupByKey)

Spark 3.3.1 中的 `SortShuffleManager.registerShuffle()` 根据不同条件选择最优的 ShuffleHandle 类型:

### 1. BypassMergeSortShuffleHandle
绕过合并排序**特性**:
- 绕过排序: 每个分区直接写入独立文件，避免排序开销
- 适用场景: 分区数少且无聚合需求的操作（如 `repartition`, `partitionBy`）
- 性能: 最快的写入速度，但会产生大量小文件

**选择条件**:
1. mapSideCombine = false (不需要 Map 端预聚合)
2. partitioner.numPartitions <= bypassMergeThreshold (分区数 ≤ 阈值，默认200)

**使用示例**:
```scala
// 触发 BypassMergeSortShuffleHandle
rdd.repartition(100)  // 分区数 < 200，无聚合
rdd.partitionBy(customPartitioner)
```

### 2. SerializedShuffleHandle
**选择条件**:
1. serializer.supportsRelocationOfSerializedObjects = true (支持序列化对象重定位，如Kryo) 
2. dependency.mapSideCombine = false (不需要 Map 端聚合)
3. partitioner.numPartitions <= MAX_SHUFFLE_OUTPUT_PARTITIONS (分区数 ≤ 16777216)

**特性**:
- **序列化排序**: 直接操作序列化后的二进制数据，避免反序列化开销
- **内存效率**: 使用 `UnsafeShuffleWriter` 进行基于指针的排序
- **高性能**: 适用于大数据量的 Shuffle 操作
- **排序支持**: 完全支持排序操作，通过 map 端 partition 排序 + reduce 端 key 排序实现

**使用示例**:
```scala
// 配置 Kryo 序列化器
spark.conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

// 以下操作都可能触发 SerializedShuffleHandle
rdd.sortByKey()               // ✅ 完全支持排序
rdd.repartition(1000)         // ✅ 支持重分区
rdd.partitionBy(partitioner)  // ✅ 支持分区操作
```


### 3. BaseShuffleHandle (通用兜底方案)
**选择条件**:
当不满足上述两种特化条件时的兜底选择：
1. 需要聚合操作 (mapSideCombine = true)
2. 分区数超大 (> 16777216)
3. 不支持序列化对象重定位 (非Kryo序列化器)

**特性**:
- **完整排序能力**: 使用 `SortShuffleWriter` 在 map 端完成 partition + key 双重排序
- **Map端预聚合**: 支持 combiner 函数，显著减少网络传输和 reduce 端计算压力
- **ExternalSorter**: 内存+磁盘的外部排序，支持大数据量处理和自动 spill
- **通用兼容**: 支持所有 Shuffle 操作，包括复杂聚合、排序、分组
- **内存管理**: 动态内存分配，避免 OOM，但内存使用相对较高


## BypassMergeSortShuffleWriter
BypassMergeSortShuffleWriter 采用最简单的写入策略：
1. 每个 reduce 分区对应一个独立的临时文件
2. 数据直接写入对应分区的文件，无需排序或合并
3. 最终将所有临时文件按顺序合并为单一的 .data 文件，生成 .index 文件记录分区边界


## UnsafeShuffleWriter
UnsafeShuffleWriter 采用"指针排序"替代"数据排序"的策略：
1. 数据与索引分离：序列化数据连续存储在内存页中，排序操作只针对轻量级指针数组
2. 压缩指针设计：将 partition ID、page 号、页内偏移量打包进单个64位指针
3. 局部排序策略：仅按 partition ID 排序，实现数据按分区聚集，避免昂贵的 key 比较
4. 零拷贝输出：按排序后的指针顺序直接拷贝序列化数据，无需反序列化

这种设计在需要按partition分组但不需要key排序的场景下，比传统方式节省50%以上的内存并显著提高排序性能。\
但代价是无法进行key级别的排序，这就是为什么sortByKey操作仍然需要在reduce端完成。

假设有以下5条记录需要shuffle，使用3个reduce分区(partition 0,1,2)：
```text
// 为每条记录创建指针
Record 0: ("hello", 1)  → partition=2, pageId=0, offset=0
Record 1: ("world", 2)  → partition=1, pageId=0, offset=8
Record 2: ("spark", 3)  → partition=0, pageId=0, offset=16
Record 3: ("java", 4)   → partition=1, pageId=0, offset=24
Record 4: ("scala", 5)  → partition=2, pageId=0, offset=32

打包成64位long指针：

指针数组（排序前）：
Index 0: 0x0000020000000000  // partition=2, pageId=0, offset=0  ("hello")
Index 1: 0x0000010000000008  // partition=1, pageId=0, offset=8  ("world")
Index 2: 0x0000000000000010  // partition=0, pageId=0, offset=16 ("spark")
Index 3: 0x0000010000000018  // partition=1, pageId=0, offset=24 ("java")
Index 4: 0x0000020000000020  // partition=2, pageId=0, offset=32 ("scala")

排序过程：

排序前的指针数组：
[0x0000020000000000, 0x0000010000000008, 0x0000000000000010, 0x0000010000000018, 0x0000020000000020]
对应partition:  [2,                1,                0,                1,                2]
对应数据:       [("hello",1),       ("world",2),      ("spark",3),      ("java",4),       ("scala",5)]

排序后的指针数组：
[0x0000000000000010, 0x0000010000000008, 0x0000010000000018, 0x0000020000000000, 0x0000020000000020]
对应partition:  [0,                1,                1,                2,                2]
对应数据:       [("spark",3),       ("world",2),      ("java",4),       ("hello",1),      ("scala",5)]

最终输出文件结构：
Shuffle Output File:
┌─────────────────────────────────────┐
│ Partition 0: [spark,3]              │
│ Partition 1: [world,2][java,4]      │
│ Partition 2: [hello,1][scala,5]     │
└─────────────────────────────────────┘
```

## SortShuffleWriter

**Map端工作流程**:
```
数据处理流程:
输入记录 → ExternalSorter →
├─ 内存缓冲区 (聚合+排序) →
├─ Spill到磁盘 (按需) →
└─ 最终归并输出 → 已排序的partition文件

优势: Reduce端只需简单归并，压力分散
劣势: Map端内存和CPU开销较大
```

**性能特点**:
- **Map端开销**: 较高内存和CPU使用，但数据处理完整
- **Reduce端轻松**: 只需归并已排序流，内存和计算压力小
- **网络传输**: 聚合操作能大幅减少传输数据量
- **容错性**: 强大的spill机制，适合内存受限环境

## ShuffleReader
**统一的读取接口**：虽然不同的ShuffleHandle产生不同格式的数据，但Spark使用统一的`BlockStoreShuffleReader`来处理所有shuffle读取操作。

```mermaid
sequenceDiagram
    participant RT as ResultTask
    participant BSR as BlockStoreShuffleReader
    participant SBFI as ShuffleBlockFetcherIterator
    participant BM as BlockManager
    participant ES as ExternalSorter

    Note over RT: read() 开始
    RT->>BSR: 调用 read()

    Note over BSR: 1. 获取 Shuffle 数据
    BSR->>SBFI: 创建数据获取迭代器
    SBFI->>BM: 从多个Executor获取shuffle blocks
    BM-->>SBFI: 返回序列化数据流
    SBFI-->>BSR: wrappedStreams

    Note over BSR: 2. 反序列化数据
    BSR->>BSR: serializerInstance.deserializeStream()
    BSR->>BSR: recordIter (原始K-V对)

    Note over BSR: 3. 聚合处理 (如果需要)
    alt dep.aggregator.isDefined
        alt dep.mapSideCombine
            Note over BSR: Map端已聚合，Reduce端合并
            BSR->>BSR: combineCombinersByKey()
        else
            Note over BSR: Map端未聚合，Reduce端聚合
            BSR->>BSR: combineValuesByKey()
        end
    end

    Note over BSR: 4. 排序处理 (如果需要)
    alt dep.keyOrdering.isDefined
        BSR->>ES: 创建 ExternalSorter
        ES->>ES: insertAllAndUpdateMetrics()
        Note over ES: 根据不同ShuffleHandle数据状态<br/>排序压力差异巨大
        ES-->>BSR: 排序后的数据
    end

    BSR-->>RT: 最终结果迭代器
```

| ShuffleHandle | 网络传输量 | 反序列化开销 | 聚合开销 | 排序开销 | ResultTask总压力 |
|--------------|----------|------------|---------|---------|----------------|
| **BypassMergeSortShuffleHandle** | 🔥 原始数据量 | ⚡ 标准 | 🔥 **完整聚合** | 🔥 **完整排序** | 🔥 **高** |
| **SerializedShuffleHandle** | 🔥 原始数据量 | ⚡ 标准 | 🔥 **完整聚合** | 🔥 **完整排序** | 🔥 **高** |
| **BaseShuffleHandle** | ✅ 聚合后数据 | ⚡ 标准 | ✅ 简单合并 | ✅ **归并操作** | ✅ **最低** |

### BypassMergeSortShuffleHandle 和 SerializedShuffleHandle 读取

相同点

- 输出结果: 都生成单个.data文件 + .index文件，文件格式完全一致
- 排序程度: Map端都只按partition分组，partition内数据无序
- ResultTask压力: 由于数据无序，都需要完整的key排序和聚合处理

不同点

- 写入策略: BypassMergeSortShuffleHandle采用多文件并行写入后合并；SerializedShuffleHandle使用指针数组排序直接写入
- 内存消耗: BypassMergeSortShuffleHandle需要为每个分区维护独立缓冲区，内存要求更高；SerializedShuffleHandle基于序列化
  数据和轻量指针，内存效率更佳

### BaseShuffleHandle 读取

**性能影响**: ✅ **最轻计算负荷**
- 归并操作：已排序数据的简单合并
- 低内存压力：数据已聚合，量小
- 低CPU开销：主要是流式归并

### 批量读取优化 (Batch Fetch)
是什么？
```text
  对每个Executor分别发送多个小请求

  Executor-1:
  ├─ 请求 block_1_0 → HTTP请求1
  ├─ 请求 block_1_1 → HTTP请求2
  └─ 请求 block_1_2 → HTTP请求3

  Executor-2:
  ├─ 请求 block_2_0 → HTTP请求4
  ├─ 请求 block_2_1 → HTTP请求5
  └─ 请求 block_2_2 → HTTP请求6

  总HTTP请求数: 6次 (每个block一个请求)
  TCP连接数: 仍然是20个 (每个Executor一个)

  ✅ 批量读取优化:
  对每个Executor发送一个批量请求

  Executor-1:
  └─ 批量请求 [block_1_0, block_1_1, block_1_2] → 1个HTTP请求

  Executor-2:
  └─ 批量请求 [block_2_0, block_2_1, block_2_2] → 1个HTTP请求
```

**批量读取条件**:
- 支持序列化对象重定位 (Kryo)
- 压缩编解码器支持流拼接
- 未启用IO加密
- 未使用旧版fetch协议

## ResultTask 读取 Shuffle Block 的详细流程

### Part 1: Reduce端 - Iterator驱动的懒加载流程

ShuffleBlockFetcherIterator：将复杂的分布式数据获取操作包装成简单的Iterator接口，
多源数据统一获取器。
- 本地块: fetchLocalBlocks(localBlocks)
- Host-Local: fetchAllHostLocalBlocks()
- 远程块: 通过网络从其他Executor获取
- Push-Merged: pushBasedFetchHelper.fetchAllPushMergedLocalBlocks()

```mermaid
sequenceDiagram
    participant RT as ResultTask
    participant SRDD as ShuffledRDD
    participant BSR as BlockStoreShuffleReader
    participant SBFI as ShuffleBlockFetcherIterator
    participant UR as 用户函数<br/>(如collect/foreach)

    Note over RT: ResultTask.runTask() 开始

    %% 1. 反序列化和初始化
    RT->>RT: 反序列化 taskBinary<br/>得到 (rdd, func)
    RT->>SRDD: rdd.iterator(partition, context)
    SRDD->>SRDD: compute() 被调用

    %% 2. 创建ShuffleReader
    SRDD->>BSR: shuffleManager.getReader(handle, partitionId)
    BSR->>BSR: read() 方法被调用

    %% 3. 创建ShuffleBlockFetcherIterator (立即初始化)
    BSR->>SBFI: new ShuffleBlockFetcherIterator()
    Note over SBFI: 构造函数调用initialize()
    SBFI->>SBFI: initialize() → fetchUpToMaxBytes()
    Note over SBFI: 立即发起网络请求！

    %% 4. 创建数据处理管道 (仍然是懒加载)
    BSR->>BSR: 创建 recordIter, metricIter<br/>aggregatedIter, resultIter
    BSR-->>SRDD: 返回 Iterator[Product2[K, C]]
    SRDD-->>RT: 返回 partition 迭代器

    %% 5. 用户函数开始消费数据 (触发实际执行)
    RT->>UR: func(context, iterator)
    UR->>BSR: iterator.hasNext() 第一次调用
    Note over BSR: 懒加载被触发！

    %% 6. ShuffleBlockFetcherIterator 开始工作
    BSR->>SBFI: wrappedStreams.hasNext()
    SBFI->>SBFI: 检查本地blocks
    SBFI->>SBFI: initialize() 初始化远程请求

    %% 7. 发起网络请求 (第一次真正的网络调用)
    SBFI->>SBFI: fetchUpToMaxBytes()
    SBFI->>SBFI: sendRequest(fetchRequest)
    Note over SBFI: 向Map端发起网络请求<br/>获取shuffle blocks

    %% 8. 等待和接收数据
    SBFI->>SBFI: 等待网络响应<br/>数据存储到 results 队列

    %% 9. 返回第一个数据块
    SBFI-->>BSR: (blockId, inputStream)
    BSR->>BSR: 反序列化数据
    BSR-->>UR: 第一批数据记录

    %% 10. 后续数据消费 (迭代过程)
    loop 用户函数继续迭代
        UR->>BSR: iterator.next()
        BSR->>SBFI: wrappedStreams.next()

        alt 当前block已消费完
            SBFI->>SBFI: 检查 results 队列
            alt 队列为空且有更多blocks
                SBFI->>SBFI: fetchUpToMaxBytes()<br/>发起新的网络请求
                Note over SBFI: 按需获取更多blocks
            end
        end

        SBFI-->>BSR: 下一个 (blockId, inputStream)
        BSR->>BSR: 反序列化 + 聚合 + 排序
        BSR-->>UR: 下一批数据记录
    end

    Note over UR: 数据消费完成
    Note over RT: Task 完成

    rect rgb(255, 245, 245)
        Note over RT, BSR: Reduce端执行流程
    end

    rect rgb(245, 255, 245)
        Note over UR: 用户函数驱动的懒加载
    end
```

### Part 2: 网络协议回调链详细时序图

专门展示OneForOneBlockFetcher中callback和listener的调用流程，分为大文件(stream)和小文件(fetchChunk)两种策略：

#### ExternalShuffleService 策略时序图

**策略原理**: 连接到独立的ExternalShuffleService进程(端口7337)，由该服务读取磁盘上的shuffle文件。优势是Executor崩溃后数据仍可访问，支持Dynamic Allocation。

**使用场景**: `spark.shuffle.service.enabled=true`

```mermaid
sequenceDiagram
    autonumber
    participant SBFI as ShuffleBlockFetcherIterator
    participant ESC as ExternalBlockStoreClient
    participant OOFBF as OneForOneBlockFetcher
    participant NET as Netty客户端

    participant ESS as ExternalShuffleService<br/>(端口7337)
    participant EBH as ExternalBlockHandler
    participant CFRH as ChunkFetchRequestHandler
    participant ISB as IndexShuffleBlockResolver
    participant FSMB as FileSegmentManagedBuffer

    rect rgb(245, 255, 255)
        Note over SBFI, NET: ResultTask端 (客户端)
    end

    rect rgb(255, 248, 220)
        Note over ESS, FSMB: ExternalShuffleService端 (服务端)
    end

    Note over SBFI: ExternalShuffleService策略

    %% 1. 批量读取优化判断
    SBFI->>SBFI: initialize() 划分FetchRequest
    Note over SBFI:  根据节点地址分组 blocks，<br/>然后按targetRemoteRequestSize=maxBytesInFlight/5将同一节点的多个 blocks 划分为多个FetchRequest，<br/>但大于targetRemoteRequestSize的块会被单独形成一个FetchRequest

    %% 2. 阈值判断和fetchBlocks调用
    SBFI->>+SBFI: fetchUpToMaxBytes
    Note over SBFI: isRemoteBlockFetchable()<br/>确保FetchRequest内异步请求，FetchRequest之间串行
    SBFI->>SBFI: sendRequest(req) 开始
    alt req.size > 200MB
        Note over SBFI: 大请求策略
        SBFI->>ESC: fetchBlocks(host, 7337, execId, blockIds, listener, this)
        Note over ESC: downloadFileManager = this (启用磁盘写入)
    else req.size ≤ 200MB
        Note over SBFI: 小请求策略
        SBFI->>ESC: fetchBlocks(host, 7337, execId, blockIds, listener, null)
        Note over ESC: downloadFileManager = null (内存处理)
    end
    deactivate SBFI

    %% 3. 第一阶段：RPC获取StreamHandle
    ESC->>OOFBF: new OneForOneBlockFetcher(..., downloadFileManager)
    OOFBF->>NET: 📡 sendRpc(FetchShuffleBlocks)
    NET->>ESS: RPC请求到ExternalShuffleService
    ESS->>EBH: receive() 处理RPC请求
    EBH->>EBH: 解析blockIds并创建StreamHandle

    loop 处理每个block
        EBH->>ISB: getBlockData(blockId)
        ISB->>ISB: 读取索引文件，定位数据段
        ISB->>FSMB: new FileSegmentManagedBuffer(dataFile, offset, length)
        FSMB-->>EBH: 返回数据引用
    end

    EBH->>NET: 返回StreamHandle{streamId, numChunks}
    NET->>OOFBF: RpcResponseCallback.onSuccess()

    %% 4. 第二阶段：数据传输
    loop 对每个chunk (i=0 to numChunks-1)，异步请求数据传输
        alt downloadFileManager != null (大文件)
            OOFBF->>NET: 🌊 stream(StreamRequest)
            NET->>ESS: StreamRequest到ExternalShuffleService
            ESS->>CFRH: 处理StreamRequest
            CFRH->>CFRH: 流式传输数据
            CFRH->>NET: 返回数据流 StreamResponse
            NET->>OOFBF: DownloadCallback.onData() + onComplete()
            Note over SBFI: listener=ShuffleBlockFetcherIterator.BlockFetchingListener
            OOFBF->>SBFI: onBlockFetchSuccess(blockId, buffer)
        else downloadFileManager == null (小文件)
            OOFBF->>NET: 📦 fetchChunk(ChunkFetchRequest)
            NET->>ESS: ChunkFetchRequest到ExternalShuffleService
            ESS->>CFRH: 处理ChunkFetchRequest
            CFRH->>CFRH: 读取chunk数据
            CFRH->>NET: 返回ManagedBuffer
            NET->>OOFBF: ChunkCallback.onSuccess()
            OOFBF->>SBFI: onBlockFetchSuccess(blockId, buffer)
        end
    end
```

##### stream vs fetchChunk

| 方法                  | 数据处理                | 内存使用    | 适用场景        |
  |---------------------|---------------------|---------|-------------|
| client.stream()     | 流式写入临时文件            | 低（流式处理） | 大文件(>200MB) |
| client.fetchChunk() | 直接内存中的ManagedBuffer | 高（全部加载） | 小文件(≤200MB) |


##### 大文件策略 (req.size > 200MB) - stream() + DownloadCallback
ResultTask client端
```mermaid
sequenceDiagram
    autonumber
    participant SBFI as ShuffleBlockFetcherIterator
    participant BFL as BlockFetchingListener
    participant OOFBF as OneForOneBlockFetcher
    participant Client as Netty客户端
    participant RpcCB as RpcResponseCallback
    participant DownCB as DownloadCallback<br/>(extends StreamCallback)
    participant Channel as DownloadFileWritableChannel

    Note over OOFBF: 大文件流程开始

    %% 第一次交互：RPC获取StreamHandle
    OOFBF->>Client: 📡 sendRpc(FetchShuffleBlocks, RpcResponseCallback)
    Note over Client: RPC Event: FetchShuffleBlocks
    Client->>RpcCB: onSuccess(ByteBuffer response)
    RpcCB->>RpcCB: 解析StreamHandle{streamId, numChunks}

    %% 第二次交互：Stream数据传输
    loop 对每个chunk (i=0 to numChunks-1)
        RpcCB->>OOFBF: downloadFileManager != null
        OOFBF->>Client: 🌊 stream(genStreamChunkId(streamId, i), DownloadCallback(i))
        Note over Client: StreamRequest: genStreamChunkId(streamId, i)
        Note over DownCB: 构造函数: createTempFile() + openForWriting()

        Client->>DownCB: onData(streamId, ByteBuffer buf)
        DownCB->>Channel: channel.write(buf)
        Note over Channel: 流式写入临时磁盘文件

        Client->>DownCB: onComplete(streamId)
        DownCB->>Channel: channel.closeAndRead()
        Channel-->>DownCB: ManagedBuffer
        DownCB->>BFL: listener.onBlockFetchSuccess(blockId, buffer)
        BFL->>SBFI: 通知数据就绪
    end
```

MapShuffleTask Server端
```mermaid
  sequenceDiagram
    autonumber
    participant Client as 客户端
    participant Pipeline as Netty Pipeline<br/>(TransportChannelHandler)
    participant TRH as TransportRequestHandler<br/>(extends MessageHandler)
    participant RP as RpcHandler<br/>(NettyBlockRpcServer)
    participant SCM as StreamManager<br/>(OneForOneStreamManager)


%% 第一阶段：RPC请求处理
    Note over Client,SCM: 阶段1: RPC获取StreamHandle
    Client->>Pipeline: sendRpc(FetchShuffleBlocks, callback)
    Pipeline->>TRH: handle(RpcRequest)
    TRH->>RP: receive(reverseClient, ByteBuffer, RpcResponseCallback)
    Note over RP: ExternalShuffleService处理
    RP->>SCM: registerStream and get streamId
    SCM->>SCM: 创建ChunkStreamHandle{streamId, numChunks}
    SCM->>RP: 返回StreamHandle
    RP->>TRH: callback.onSuccess(ByteBuffer with StreamHandle)
    TRH->>Pipeline: respond(RpcResponse)
    Pipeline->>Client: RpcResponse{StreamHandle}

%% 第二阶段：Stream请求处理
    Note over Client,SCM: 阶段2: StreamRequest传输
    Client->>Pipeline: stream(StreamRequest, DownloadCallback)
    Pipeline->>TRH: handle(StreamRequest)
    TRH->>TRH: processStreamRequest()

    TRH->>SCM: openStream(streamId)
    Note over SCM: 读取文件创建ManagedBuffer
    SCM-->>TRH: FileSegmentManagedBuffer(extends ManagedBuffer 文件引用)

    Note over TRH: 创建StreamResponse
    TRH->>Pipeline: respond(StreamResponse{streamId, byteCount, ManagedBuffer})

    Note over Pipeline: MessageEncoder包装为MessageWithHeader
    Pipeline->>Pipeline: new MessageWithHeader(header, ManagedBuffer)
    Note over Pipeline: extends AbstractFileRegion -> zero-copy

    Pipeline->>Client: MessageWithHeader.transferTo()
    Note over Client: 底层调用FileChannel.transferTo()

%% 完成回调
    Pipeline->>TRH: addListener(future) -> streamSent()
    TRH->>SCM: streamSent(streamId)
```
- OneForOneStreamManager 是 Spark 中用于管理点对点流数据传输的组件。
- FileSegmentManagedBuffer 是 “文件流” 的封装，适合大文件的分段传输，核心是减少内存占用。



##### 小文件策略 (req.size ≤ 200MB) - fetchChunk() + ChunkCallback
ResultTask client端
```mermaid
sequenceDiagram
    autonumber
    participant SBFI as ShuffleBlockFetcherIterator
    participant BFL as BlockFetchingListener
    participant OOFBF as OneForOneBlockFetcher
    participant Client as Netty客户端
    participant RpcCB as RpcResponseCallback
    participant ChunkCB as ChunkCallback<br/>(extends ChunkReceivedCallback)

    Note over OOFBF: 小文件流程开始

    %% 第一次交互：RPC获取StreamHandle
    OOFBF->>Client: 📡 sendRpc(FetchShuffleBlocks, RpcResponseCallback)
    Note over Client: RPC Event: FetchShuffleBlocks
    Client->>RpcCB: onSuccess(ByteBuffer response)
    RpcCB->>RpcCB: 解析StreamHandle{streamId, numChunks}

    %% 第二次交互：Chunk数据获取
    loop 对每个chunk (i=0 to numChunks-1)
        RpcCB->>OOFBF: downloadFileManager == null
        OOFBF->>Client: 📦 fetchChunk(streamId, i, ChunkCallback)
        Note over Client: ChunkFetchRequest: (streamId, chunkIndex=i)

        Client->>ChunkCB: onSuccess(chunkIndex, ManagedBuffer buffer)
        Note over ChunkCB: 直接获得完整的ManagedBuffer
        ChunkCB->>BFL: listener.onBlockFetchSuccess(blockIds[chunkIndex], buffer)
        BFL->>SBFI: 通知数据就绪
    end
```

MapShuffleTask Server端
```mermaid
  sequenceDiagram
    autonumber
    participant Client as 客户端
    participant Pipeline as Netty Pipeline<br/>(TransportChannelHandler)
    participant TRH as TransportRequestHandler<br/>(extends MessageHandler)
    participant SCM as StreamManager<br/>(OneForOneStreamManager)
    participant ChunkHandler as ChunkFetchRequestHandler<br/>(extends SimpleChannelInboundHandler)
    
    Client->>Pipeline: channelRead0 ChunkFetchRequest(streamChunkId)
    Pipeline->>TRH: handle(ChunkFetchRequest)
    TRH->>ChunkHandler: processFetchRequest(ChunkFetchRequest)
    ChunkHandler->>SCM: checkAuthorization() + getChunk()
    SCM-->>ChunkHandler: BlockManagerManagedBuffer(extends ManagedBuffer)
    ChunkHandler->>Pipeline: respond(ChunkFetchSuccess(streamChunkId, ManagedBuffer))
    Pipeline->>Client: ChunkFetchSuccess{ManagedBuffer}
    ChunkHandler->>SCM: addListener -> chunkSent()
```
BlockManagerManagedBuffer 是 “内存块” 的封装，适合内存中数据的快速访问，核心是提供高效的数据操作接口。

BlockData data 存储在内存
- On-Heap 内存：不支持零拷贝，需先将堆内数据拷贝到 DirectByteBuffer，“堆内 → 直接内存”
- Off-Heap 内存：属于内核态可访问内存，无需 JVM 堆拷贝，从直接内存拷贝到网卡缓冲区，无用户态 → 内核态的拷贝
  - 只有当 ByteBuffer 是 DirectByteBuffer 时，`chunks.length == 1` 才能在网络传输场景下实现真正的
    zero-copy


**核心回调链总结**：
- **第一阶段**: `sendRpc(FetchShuffleBlocks)` → `RpcResponseCallback.onSuccess()` → 解析`StreamHandle`
- **第二阶段 - stream流程**: `stream(StreamRequest)` → `DownloadCallback.onData()` → `DownloadCallback.onComplete()` → `BlockFetchingListener.onBlockFetchSuccess()`
- **第二阶段 - fetchChunk流程**: `fetchChunk(ChunkFetchRequest)` → `ChunkCallback.onSuccess()` → `BlockFetchingListener.onBlockFetchSuccess()`

#### NettyBlockTransferService 策略时序图

**策略原理**: 直接连接到目标Executor的BlockManager(动态端口)，从该Executor获取shuffle数据。优势是减少中间层开销，但要求目标Executor必须存活。

**使用场景**: `spark.shuffle.service.enabled=false` (默认)

```mermaid
sequenceDiagram
    autonumber
    participant SBFI as ShuffleBlockFetcherIterator
    participant NBTS as NettyBlockTransferService
    participant OOFBF as OneForOneBlockFetcher
    participant NET as Netty客户端
    participant TBM as 目标Executor的<br/>BlockManager
    participant ISB as IndexShuffleBlockResolver
    participant FSMB as FileSegmentManagedBuffer

    Note over SBFI: NettyBlockTransferService策略

    %% 1. 批量读取优化判断
    SBFI->>SBFI: initialize() 划分FetchRequest

    %% 2. 阈值判断和fetchBlocks调用
    SBFI->>+SBFI: fetchUpToMaxBytes
    Note over SBFI: isRemoteBlockFetchable()
    SBFI->>SBFI: sendRequest(req) 开始
    alt req.size > 200MB
        Note over SBFI: 大请求策略
        SBFI->>NBTS: fetchBlocks(host, executorPort, execId, blockIds, listener, this)
        Note over NBTS: downloadFileManager = this (启用磁盘写入)
    else req.size ≤ 200MB
        Note over SBFI: 小请求策略
        SBFI->>NBTS: fetchBlocks(host, executorPort, execId, blockIds, listener, null)
        Note over NBTS: downloadFileManager = null (内存处理)
    end
    deactivate SBFI

    %% 3. NettyBlockTransferService处理
    NBTS->>OOFBF: new OneForOneBlockFetcher(..., downloadFileManager)
    OOFBF->>NET: 创建到目标Executor的连接
    Note over OOFBF: 根据numChunks，异步请求
    NET->>TBM: 连接到目标Executor BlockManager (动态端口)

%% 4. 目标Executor的BlockManager处理
    TBM->>TBM: 处理block请求
    loop 处理每个block
        TBM->>ISB: getBlockData(blockId)
        ISB->>ISB: 读取本地索引文件，定位数据段
        ISB->>FSMB: new FileSegmentManagedBuffer(localDataFile, offset, length)
        Note over FSMB: 每次读取重新打开文件句柄<br/>这是并发瓶颈！
        FSMB-->>TBM: 返回数据引用
    end

    TBM->>NET: 开始传输数据，服务器并发传输数据

    %% 5. 数据传输和回调 (详细回调链见上方专门时序图)
    loop 传输每个chunk
        alt downloadFileManager != null (大文件)
            NET->>OOFBF: stream() 传输完成
            Note over OOFBF: DownloadCallback处理
            OOFBF->>SBFI: onBlockFetchSuccess(blockId, buffer)
        else downloadFileManager == null (小文件)
            NET->>OOFBF: fetchChunk() 传输完成
            Note over OOFBF: ChunkCallback处理
            OOFBF->>SBFI: onBlockFetchSuccess(blockId, buffer)
        end
    end

    rect rgb(240, 255, 240)
        Note over TBM, FSMB: 目标Executor本地处理
    end
```

ExternalShuffleService vs NettyBlockTransferService 对比：

| 配置项 | ExternalShuffleService | NettyBlockTransferService |
|--------|----------------------|---------------------------|
| `spark.shuffle.service.enabled` | ✅ true | ❌ false |
| 目标服务 | ExternalShuffleService进程 | 目标Executor |
| 端口 | `spark.shuffle.service.port` (7337) | Executor的BlockManager端口 |
| 容错性 | ✅ Executor崩溃后仍可用 | ❌ 依赖Executor存活 |
| Dynamic Allocation | ✅ 完全支持 | ⚠️ 受限 |






# Q&A
## 什么是serializer.supportsRelocationOfSerializedObjects？
序列化器对象重定位: 能够重新排列序列化流中对象的字节顺序，而不影响反序列化的正确性。\

1. JavaSerializer - ❌ 不支持
2. KryoSerializer - ⚠️ 条件支持(必须启用 auto-reset 功能)
   ```scala
   private[spark] override lazy val supportsRelocationOfSerializedObjects: Boolean = {
     newInstance().asInstanceOf[KryoSerializerInstance].getAutoReset()
   }
   ```

比如[JavaSerializationTest](../../algorithm/src/test/java/cn/juntaozhang/jdk/JavaSerializationTest.java)中的测试：
```text
--- 测试正常顺序反序列化 ---
正常顺序 - 成功读取:
  第一个对象: Person{name='Alice', age=25}
  第二个对象: Person{name='Bob', age=30}

--- 测试重新排列后反序列化 ---
重新排列 - 反序列化失败:
  错误: StreamCorruptedException: invalid stream header: 7371007E

--- 字节内容分析 ---
第一个对象 字节分析:
  前20个字节: AC ED 00 05 73 72 00 2F 63 6E 2E 6A 75 6E 74 61 6F 7A 68 61 
  包含Java序列化魔法数字: true
  包含类名信息: true

第二个对象 字节分析:
  前20个字节: 73 71 00 7E 00 00 00 00 00 1E 74 00 03 42 6F 62 
  包含Java序列化魔法数字: false
  包含类名信息: false
```
\
假设我们有：

- obj1 = "Hello"
- obj2 = "World"

✅ 支持重定位的序列化器示例：
```text
// 1. 打开序列化输出流
serOut.open()
position = 0

// 2. 写入第一个对象
serOut.write("Hello")
serOut.flush()
position = 5  // "Hello" 占用5个字节
obj1Bytes = output[0:4] = [0x48, 0x65, 0x6c, 0x6c, 0x6f]  // "Hello"的字节

// 3. 写入第二个对象
serOut.write("World")
serOut.flush()
position2 = 10  // 总共10个字节
obj2Bytes = output[5:9] = [0x57, 0x6f, 0x72, 0x6c, 0x64]  // "World"的字节

// 4. 原始序列化流
原始流: [0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x57, 0x6f, 0x72, 0x6c, 0x64]
|----------obj1Bytes----------|----------obj2Bytes----------|

// 5. 关键测试：重新排列字节块
重排列流: [obj2Bytes] concatenate [obj1Bytes]
= [0x57, 0x6f, 0x72, 0x6c, 0x64, 0x48, 0x65, 0x6c, 0x6c, 0x6f]
= [----------obj2Bytes----------][----------obj1Bytes----------]

// 6. 反序列化重排列的流
serIn.open(重排列流)
result = serIn.readObjects()
result should be ("World", "Hello")  // 对应 (obj2, obj1)
```
❌ 不支持重定位的序列化器示例：
```text
// Java序列化的情况
serOut.open()

// 写入第一个对象
serOut.write("Hello")
// 实际写入：[String类定义][长度信息][0x48, 0x65, 0x6c, 0x6c, 0x6f]
obj1Bytes = [类定义+长度+"Hello"]

// 写入第二个对象
serOut.write("World")
// 实际写入：[String类引用][长度信息][0x57, 0x6f, 0x72, 0x6c, 0x64]
obj2Bytes = [类引用+长度+"World"]  // 注意：这里没有完整类定义！

// 原始流能正常反序列化
原始流: [类定义+长度+"Hello"][类引用+长度+"World"] ✅

// 重排列后就失败了
重排列流: [类引用+长度+"World"][类定义+长度+"Hello"]
// 反序列化时：❌ 找不到类引用指向的类定义！
```
## Kryo Auto-Reset 的核心机制
- 默认情况：Kryo 的 auto-reset 是启用的
- 启用 Auto-Reset：每个对象后立即清空引用表 → 无跨对象依赖 → 支持重定位
- 禁用 Auto-Reset：维护全局引用表优化重复对象 → 存在跨对象引用 → 不支持重定位

原理：每个对象序列化后立即清空引用表
```text
序列化过程:
obj1 → [完整数据1] → reset() → 引用表清空
obj2 → [完整数据2] → reset() → 引用表清空
obj1 → [完整数据1] → reset() → 引用表清空

重排序后:
[完整数据2][完整数据1][完整数据1] → ✅ 反序列化成功
```

❌ Auto-Reset 禁用 = 不支持重定位
```text
重排序后:
[引用id=1][完整数据1][完整数据2]
反序列化时: 先遇到id=1的引用，但引用表中还没有id=1 → ❌ 失败
```


## ShuffleExternalSorter: UnsafeShuffleWriter基于指针排序，最后输出非连续的跳跃访问，spark 如何优化这个问题？
排序类：`ShuffleExternalSorter`\
排序仅限part维度, part内的数据相对位置不变，比如A在B左边，如果它们最终输出相同part时A仍然在B左边。

假设我们有 9 条记录，分布在 3 个内存页，2 个 partition：
```text
  Cache限制: 只能缓存1个页面
  Memory Pages:
  ┌─────────────────────────────────────────┐
  │ Page 0: [R0(p=0), R1(p=1), R2(p=0)]     │
  │ Page 1: [R3(p=1), R4(p=0), R5(p=1)]     │
  │ Page 2: [R6(p=0), R7(p=1), R8(p=0)]     │
  └─────────────────────────────────────────┘

  原始记录顺序:    R0, R1, R2, R3, R4, R5, R6, R7, R8
  对应partition:  0,  1,  0,  1,  0,  1,  0,  1,  0

  按partition分组:
  - Partition 0: R0(Page0), R2(Page0), R4(Page1), R6(Page2), R8(Page2)
  - Partition 1: R1(Page0), R3(Page1), R5(Page1), R7(Page2)

  合并处理顺序:  R0→R2→R4→R6→R8 →R1→R3→R5→R7
  Page Access: P0→P0→P1→P2→P2 →P0→P1→P1→P2
```
每个 partition 内部的记录仍然保持原始顺序，减少了随机访问开销。\
在写入磁盘时，`ShuffleExternalSorter` 会按 partition 顺序将数据写入输出文件，\
确保每个 partition 的数据是连续存储的，从而优化了磁盘 I/O 性能。

每个partition 理论上load一次Page0～N到内存，有多少partition就load多少遍page0～N到内存

## 传统Pull模式 vs 新Push模式 Shuffle数据传输对比？

### 传统Pull模式（现有Spark Shuffle）
```mermaid
graph TB
    subgraph "Reduce阶段 (1000个Reduce Tasks)"
        R1[Reduce-1<br/>需要partition-1数据]
        R500[Reduce-500<br/>需要partition-500数据]
        R1000[Reduce-1000<br/>需要partition-1000数据]
    end

    subgraph "Map阶段 (1000个Map Tasks)"
        M1[Map-1<br/>写partition 0-999]
        M500[Map-500<br/>写partition 0-999]
        M1000[Map-1000<br/>写partition 0-999]
    end

    %% 显示部分连接表示连接爆炸
    M1 -.->|网络拉取| R1
    M500 -.->  R1


    M1  -.->  R500
    M1000 -.-> R500

    M500 -.-> R1000
    M1000 -.-> R1000

    style R1 fill:#ffcccc
    style R500 fill:#ffcccc
    style R1000 fill:#ffcccc

    classDef mapStyle fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef reduceStyle fill:#fff3e0,stroke:#e65100,stroke-width:2px

    class M1,M2,M500,M1000 mapStyle
    class R1,R2,R500,R1000 reduceStyle
```
现有优化措施：
- 文件合并: 每个Map将多个分区合并为单个.data文件
- 批量读取: ShuffleBlockBatchId将连续block合并为单次请求
- 连接复用: 连接池 + 并发控制
- 流控优化: maxBytesInFlight, maxReqsInFlight等限制

### Push模式（Spark 3.2+）
```mermaid
graph TB
    subgraph "Map阶段 (1000个Map Tasks)"
        M1[Map-1<br/>产生partition 0-999数据]
        M500[Map-500<br/>产生partition 0-999数据]
        M1000[Map-1000<br/>产生partition 0-999数据]
    end

    subgraph "MergeServer集群 (5个服务器)"
        MS1[MergeServer-1<br/>负责partition 0-199]
        MS2[MergeServer-2<br/>负责partition 200-399]
        MS3[MergeServer-3<br/>负责partition 400-599]
        MS5[MergeServer-5<br/>负责partition 800-999]
    end

    subgraph "Reduce阶段 (1000个Reduce Tasks)"
        R1[Reduce-1<br/>只需partition-1]
        R250[Reduce-250<br/>只需partition-250]
        R500[Reduce-600<br/>只需partition-600]
        R1000[Reduce-1000<br/>只需partition-1000]
    end

    %% Map推送阶段 - 每个Map连接所有MergeServer
    M1 -->|推送各分区数据| MS1
    M1 --> MS2
    M1 --> MS3

    M500 --> MS1
    M500 --> MS2
    M500 --> MS5

    M1000 --> MS2
    M1000 --> MS3
    M1000 --> MS5

    %% Reduce拉取阶段 - 每个Reduce只连接对应MergeServer
    MS1 -.-> R1
    MS2 -.-> R250
    MS5 -.-> R500
    MS5 -.-> R1000

    style MS1 fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style MS2 fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style MS3 fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style MS5 fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px

    classDef mapStyle fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef reduceStyle fill:#fff3e0,stroke:#e65100,stroke-width:2px

    class M1,M2,M500,M1000 mapStyle
    class R1,R250,R500,R1000 reduceStyle
```

| 对比维度  | 传统Pull模式     | Push模式           | 改进倍数                    |
  |-------|--------------|------------------|-------------------------|
| 网络连接对 | M×R (100万)   | M×S + R×1 (6000) | 167倍，pull优化会减少但是不会改变数量级 |
| 文件碎片化 | 每Map产生R个段    | 预合并大文件           | 显著改善                    |
| 容错成本  | Map失败重算全部分区  | 增量恢复             | 大幅降低                    |
| 网络热点  | 集中在热门Reducer | 分散到MergeServer   | 负载均衡                    |


## Push-Based Shuffle 完整分析(TODO)

```mermaid
  sequenceDiagram
      participant Map as Map Task
      participant Pusher as ShuffleBlockPusher
      participant Merger as RemoteBlockPushResolver
      participant Reduce as Reduce Task
      participant ESS as ExternalShuffleService

      Note over Map, ESS: Push-Based Shuffle 完整流程

      %% Phase 1: Map端写入和推送
      Map->>Map: 执行shuffle write
      Map->>Map: 生成shuffle blocks
      Map->>Pusher: 创建ShuffleBlockPusher

      Note over Pusher: 推送阶段
      Pusher->>ESS: 查找merger locations
      Pusher->>Merger: push blocks (异步)
      Merger->>Merger: 合并blocks到merged file
      Merger-->>Pusher: push confirmation

      %% Phase 2: Reduce端读取
      Note over Reduce: 读取阶段
      Reduce->>ESS: 请求shuffle data
      alt 有merged data
          ESS->>Merger: 读取merged shuffle file
          Merger-->>Reduce: 返回merged data
      else 无merged data
          ESS->>ESS: 读取原始shuffle blocks
          ESS-->>Reduce: 返回原始data
      end

      Note over Map, ESS: 混合模式: merged + original blocks
```

YARN实现特点：
- ✅ 内置支持：Spark 3.2+版本内置，无需额外组件
- ✅ NodeManager集成：作为YARN NodeManager的辅助服务运行
- ✅ 生产就绪：经过充分测试，稳定性好
- ⚠️ 功能有限：基础的merge功能，优化空间有限

Kubernetes + Celeborn的优势：
- ✅ 专业化设计：Celeborn专门为大规模shuffle优化
- ✅ 更好性能：分层存储、智能分区策略
- ✅ 云原生友好：更适合Kubernetes环境
- ✅ 功能丰富：支持更多高级特性

实现对比表

| 特性    | YARN + RemoteBlockPushResolver | Kubernetes + Celeborn |
  |-------|--------------------------------|-----------------------|
| 集成方式  | Spark内置                        | 外部服务                  |
| 部署复杂度 | 🟢 低（NodeManager自带）            | 🟡 中等（独立部署）           |
| 性能优化  | 🟡 基础优化                        | 🟢 深度优化               |
| 扩展性   | 🟡 受限于NodeManager              | 🟢 独立伸缩               |
| 资源隔离  | 🟡 与NodeManager共享              | 🟢 独立资源池              |
| 故障恢复  | 🟡 基础容错                        | 🟢 高级容错机制             |

## 为什么 BypassMergeSortShuffleWriter 最后要拼接成一个文件？
1000 Map × 20个reduce task并发，很多地方说的不接成一个文件会导致"Too many open files"，在默认配置下防止文件句柄爆炸， 目前版本不存在该问题。

真相是：BypassMergeSortShuffleWriter的文件合并主要是为了架构统一性，而不是解决任何实际的性能或稳定性问题。
```text
  ┌─ IndexShuffleBlockResolver ─┐
  │ 期望: 单个.data + .index文件  │
  │ 接口: getBlockData(offset)   │
  └─────────────────────────────┘
             ↑ 必须兼容
  ┌─ BypassMergeSortShuffleWriter ─┐
  │ 自然产出: 200个分散的小文件       │
  │ 架构要求: 必须合并成单文件        │
  └───────────────────────────────┘
```


## IndexShuffleBlockResolver作用？

IndexShuffleBlockResolver是Spark Shuffle架构的关键抽象层，它：

1. 统一了接口：让不同的ShuffleWriter可以无缝协作
2. 简化了Reader：所有shuffle读取都通过相同的逻辑
3. 优化了访问：支持单分区和批量分区的高效访问
4. 保证了一致性：提供原子性写入和容错机制

## 如果 某个 Executor 所在节点宕机（并运行着 Map Task），那么 Spark 是否需要把整个 ShuffleMapStage 全部重算，还是只重算部分？
```text
  场景A: 启用External Shuffle Service

  ✅ 最佳情况 - 无需重算:
  ┌─────────────────┐         ┌──────────────────────┐
  │ Executor-1 宕机  │  ────→  │ External Shuffle     │ ← 数据仍然可访问
  │ (已完成Map Task) │         │ Service (独立进程)    │
  └─────────────────┘         └──────────────────────┘
                                        ↓
                              ResultTask可以正常读取shuffle数据
                              ⟹ 无需重算任何Map Task


  场景B: 未启用External Shuffle Service

  🔥 需要部分重算:
  ┌─────────────────┐   shuffle数据   ┌──────────────────────┐
  │ Executor-1 宕机  │  ──────X────→  │ Executor-1本地磁盘    │ ← 数据丢失
  │                 │                 │ (随executor一起宕机) │
  └─────────────────┘                 └──────────────────────┘
                                              ↓
                                     只重算Executor-1上的Map Tasks
                                     其他Executor的Map Tasks保持不变

  工作原理:
  MapOutputTracker维护的映射关系:
  ┌─────────────────────────────────────────────────────────┐
  │ ShuffleMapStage-0:                                      │
  │ ├─ Partition-0 → Executor-1 ✅ (保留)                   │
  │ ├─ Partition-1 → Executor-2 ❌ (宕机,移除)              │
  │ ├─ Partition-2 → Executor-3 ✅ (保留)                   │
  │ ├─ Partition-3 → Executor-2 ❌ (宕机,移除)              │
  │ └─ Partition-4 → Executor-1 ✅ (保留)                   │
  └─────────────────────────────────────────────────────────┘

  重算范围: 只有Partition-1和Partition-3需要重新执行
  
  
```