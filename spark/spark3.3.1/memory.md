# Spark Memory Management

## Spark 默认内存管理？

**默认使用 UnifiedMemoryManager（统一内存管理）**

- ✅ **动态边界**: Execution和Storage内存之间可以相互借用
- ✅ **软边界**: 不是硬性分割，而是动态调整的边界
- ✅ **默认启用**: Spark 1.6+后的默认选择，替代了StaticMemoryManager

**内存总体划分**
```text
┌─────────────────────────────────────────────────────────────┐
│                    JVM Heap (例如: 1GB)                      │
├─────────────────────────────────────────────────────────────┤
│ Reserved Memory (300MB)                                     │ 40%
│ - 系统元数据、内部数据结构、OOM保护                              │
├─────────────────────────────────────────────────────────────┤
│ Unified Memory Region (420MB = (1024-300) * 0.6)            │ 60%
│ ┌─────────────────┬─────────────────────────────────────┐   │
│ │ Storage Memory  │ Execution Memory                    │   │
│ │ (210MB默认)      │ (210MB默认)                         │   │
│ │ ↕ 可借用         │ ↕ 可借用                             │   │
│ └─────────────────┴─────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

例如：
[memory-example.md](memory-example.md)

**关于User Memory与memoryOverhead区分：在spark 管理之外，一般on-heap是user memory，off-heap是overhead**
```text
Container Total Memory
├── JVM Heap Memory (spark.executor.memory=4GB)
│   ├── Reserved Memory (300MB)
│   ├── Unified Memory Pool (2.4GB) - Spark管理
│   └── User Memory (1.4GB) - JVM堆内，Spark不管理
│
└── MemoryOverhead (spark.executor.memoryOverhead=1GB) - 非堆内存
    ├── Metaspace: 类加载器、方法区元数据
    ├── Code Cache: JIT编译后的本地代码缓存
    ├── GC工作内存: 垃圾回收器自身的工作内存
    ├── Parquet/ORC向量化缓冲区
    ├── Netty Direct Memory: Shuffle拉取缓冲区(maxSizeInFlight) + Netty Arena Pool
    └── 线程栈空间：on-heap（非用户内存）
```

## Spark 分配策略是什么？

**动态借用策略(不对称竞争机制)**

场景1: Shuffle密集型任务 (Execution优势)
```text
初始状态 (总内存420MB, storageRegionSize=210MB):
Storage Memory  : █████████░░░  250MB (超出storageRegionSize 40MB)
Execution Memory: █████░░░░░░░  170MB

Shuffle操作需要100MB:
Storage Memory  : ██████████░░░  210MB (被驱逐到storageRegionSize边界)
Execution Memory: ██████████░░░  210MB (获得足够内存进行Shuffle)
```

场景2: Cache密集型任务 (Storage劣势)
```text
初始状态 (总内存420MB, storageRegionSize=210MB):
Storage Memory: ████████░░░░░░  180MB
Execution Memory: ██████████░░  240MB (正在执行Shuffle)

Cache操作需要100MB:
Storage Memory: ████████░░░░░░  180MB (只能等待Execution释放空闲内存)
Execution Memory: ██████████░░  240MB (不能被驱逐，继续运行)

如果Execution释放60MB空闲:
Storage Memory: █████████░░░░  220MB (借用60MB空闲内存)
Execution Memory: ████████░░░░░  180MB (保留运行所需内存)
```

## Execution Memory
Spark 为任务执行过程中计算密集型操作分配的内存区域，
专门用于存储 Shuffle、Join、Sort、Aggregation 等计算过程中产生的临时数据与中间数据结构。
例如：
1. Shuffle Write流程
   - 流程1: UnsafeShuffleWriter路径
     ```text
     UnsafeShuffleWriter.write()
       -> insertRecord() [UnsafeShuffleWriter.java:250]
         -> ShuffleExternalSorter.insertRecord() [ShuffleExternalSorter.java:408]
           -> acquireNewPageIfNecessary() [ShuffleExternalSorter.java:395]
             -> allocatePage(required) [ShuffleExternalSorter.java:399]
     ```
   - 流程2: SortShuffleWriter路径
     ```text  
     SortShuffleWriter.write() [SortShuffleWriter.scala:63]
       -> ExternalSorter.insertAll() [ExternalSorter.scala:185]
         -> map.changeValue() / buffer.insert() [ExternalSorter.scala:200,208]
           -> maybeSpillCollection() [ExternalSorter.scala:201,209]
             -> maybeSpill() [ExternalSorter.scala:223,228]
               -> Spillable.acquireMemory() [向TaskMemoryManager申请execution内存]
     ```

2. ExternalAppendOnlyMap
   - RDD Aggregation流程
   - CoGroupedRDD join流程
  ```text
     ExternalAppendOnlyMap.insertAll() [ExternalAppendOnlyMap.scala:143]
    -> currentMap.changeValue() [ExternalAppendOnlyMap.scala:164]
      -> maybeSpill() [ExternalAppendOnlyMap.scala:161]
        -> Spillable.acquireMemory() [向TaskMemoryManager申请execution内存]
  ```

3. SQL Tungsten Aggregation流程
  ```text
  TungstenAggregationIterator.processInputs()
    -> UnsafeFixedWidthAggregationMap.append() [UnsafeFixedWidthAggregationMap.java:135]
      -> BytesToBytesMap.append() [BytesToBytesMap.java:758]
        -> acquireNewPage() [BytesToBytesMap.java:780]
          -> allocatePage(required) [BytesToBytesMap.java:842]
   ```
4. BlockStoreShuffleReader有 sort 时使用 Execution Memory
   - 有排序时：创建ExternalSorter进行排序，通过insertAllAndUpdateMetrics使用Execution Memory
   - 无排序时：直接返回聚合后的迭代器，不使用Execution Memory
5. BypassMergeSortShuffleWriter 不使用 Execution Memory
6. Shuffle Read(shuffleClient.fetchBlocks): 使用Netty Direct Memory，不是Execution Memory

### Unroll是什么？
- Unroll 是将Iterator数据逐步展开到内存的过程
- 默认先申请1MB内存(`spark.storage.unrollMemoryThreshold`)
- 在不知道数据总大小的情况下，渐进式地评估是否能完全缓存到内存

**Unroll过程**: Iterator数据 → 预先申请1MB → 逐步unroll → 检查大小 → 决定继续或降级

具体 Unroll 流程：
```mermaid
sequenceDiagram
    autonumber
    participant Task as Task
    participant BM as BlockManager
    participant MS as MemoryStore
    participant UMM as UnifiedMemoryManager
    participant Disk as DiskStore

    Note over Task,Disk: Unroll过程开始

%% 1. 开始展开
    Task->>+BM: getOrElseUpdate(blockId, level, iterator)
    BM->>+MS: [deserialized] putIteratorAsValues()
    MS->>MS: putIterator()

%% 2. 逐步展开并申请内存
    MS->>UMM: reserveUnrollMemoryForThisTask()
    UMM->>UMM: acquireUnrollMemory(blockId, memory, memoryMode)
    UMM-->>MS: success: true/false

    alt 初始内存申请失败
        MS-->>BM: Left(PartiallyUnrolledIterator)
    else 内存申请成功
        Note over MS: dd
        loop 逐步展开数据
            MS->>MS: storeValue()
            alt 触发内存扩容（估计值）
                MS->>UMM: reserveUnrollMemoryForThisTask(additionalMemory)
            end
        end

    %% 3. 展开结果处理
        alt 展开成功（根据精确值判断）
            Note over MS: 转换unroll memory为storage memory
            MS->>UMM: releaseUnrollMemoryForThisTask(unrollMemory)
            MS->>UMM: acquireStorageMemory()
            MS->>MS: entries.put(blockId, entry)
            MS-->>BM: Right(storedSize)
        else 真实的内存占用大于申请内存，重新申请失败
            MS-->>BM: Left(PartiallyUnrolledIterator)
        end
    end

    BM-->>-Task: Left(BlockResult) or Right(iterator)

    Note over Task,Disk: Unroll过程结束
```
## Storage Memory
缓存 Spark 计算过程中需要重复使用的数据
- RDD 缓存：通过 rdd.persist() 或 rdd.cache() 缓存的数据
- 广播变量：通过 sc.broadcast() 创建的广播变量
- DataFrame/Dataset 缓存：通过 df.cache() 缓存的数据

### StorageLevel设置成MEMORY_AND_DISK，如果一个block unroll失败会怎么样？

**Unroll失败的情况**:
- 内存不足无法完成unroll过程
- 数据大小超过可用内存限制
- Storage内存被Execution借用导致空间不够

**失败后的自动降级策略** (源码 `MemoryStore.scala`):
1. **检查StorageLevel**: 如果设置了`useDisk = true`
2. **降级到磁盘**: 自动调用DiskStore进行磁盘存储
3. **保证可用性**: 后续访问时从磁盘读取(性能较低但数据不丢失)

**完整降级流程**:
RDD.persist(MEMORY_AND_DISK) → 尝试内存缓存 → [内存不足] → 自动降级到磁盘存储 → 保证数据可用性

```mermaid
  sequenceDiagram
    participant Caller as 调用方
    participant BM as BlockManager
    participant MS as MemoryStore
    participant DS as DiskStore
    participant Serializer as SerializerManager

    Note over Caller,Serializer: doPutIterator - MEMORY_AND_DISK模式

%% 1. 开始unroll过程
    Caller->>+BM: doPutIterator(blockId, iterator, level)

%% 2. 尝试内存存储
    BM->>BM: 检查level.useMemory && level.deserialized

    activate MS
    alt deserialized模式
        BM->>MS: putIteratorAsValues(blockId, iterator, memoryMode, classTag)
    else 序列化模式
        BM->>MS: putIteratorAsBytes(blockId, iterator, classTag, memoryMode)
    end

%% 3. 内存展开过程
    Note over MS: 逐步展开iterator数据

%% 4. 内存不足，返回失败
    Note over MS: 内存不足，unroll失败
    MS-->>-BM: Left(PartiallyUnrolledIterator) or Left(PartiallySerializedBlock)

%% 5. MEMORY_AND_DISK降级处理
    alt level.useDisk (MEMORY_AND_DISK)
        Note over BM: 降级到磁盘存储

        alt deserialized模式
            BM->>+DS: diskStore.put(blockId) { channel =>
            DS->>+Serializer: dataSerializeStream(blockId, out, iter)
            Serializer-->>-DS: 序列化完成
            DS-->>-BM: 写入完成
        else 序列化模式
            BM->>+DS: diskStore.put(blockId) { channel =>
            DS->>+Serializer: partiallySerializedValues.finishWritingToStream(out)
            Serializer-->>-DS: 序列化完成
            DS-->>-BM: 写入完成
        end

        Note over BM: 获取磁盘存储大小，标记成功，更新状态和报告

    else level.useDisk == false (MEMORY_ONLY)
        Note over BM: 无磁盘降级选项，完全失败
        BM->>BM: iteratorFromFailedMemoryStorePut = Some(iter)
        BM->>BM: blockWasSuccessfullyStored = false
    end

%% 6. 返回成功
    BM-->>-Caller: None (表示成功)

    Note over Caller,Serializer: MEMORY_AND_DISK降级成功完成
```

**用途**: 通过渐进式unroll避免盲目消耗内存，通过降级策略确保数据在内存不足时仍然可用。
### RDD MEMORY_ONLY，如果一个block unroll失败？会怎么样？
- 当MEMORY_ONLY模式下的unroll失败时，Spark不会保存该block(partition)任何部分数据
- iteratorFromFailedMemoryStorePut被返回给调用者，但这个迭代器在当前任务使用完后就被丢弃了
- 没有任何数据被缓存到内存或磁盘中


- 只重新计算缓存失败的特定partition
- 其他partition如果缓存成功，直接从缓存读取
- 从未被访问过的partition不会被动计算
- 每个partition的生命周期完全独立

具体流程如图：
```mermaid
  sequenceDiagram
      participant Task as Task
      participant RDD as RDD.getOrCompute
      participant BM as BlockManager
      participant MS as MemoryStore
      participant Compute as Compute函数

      Note over Task,Compute: RDD分区缓存访问 - MEMORY_ONLY模式

      %% 1. 尝试获取缓存
      Task->>+RDD: iterator(partition)
      RDD->>+BM: getOrElseUpdate(blockId, MEMORY_ONLY, classTag, computeClosure)

      %% 2. 尝试获取现有缓存
      BM->>BM: getLocalValues(blockId)
      Note over BM: 缓存不存在，需要计算

      %% 3. 执行计算闭包
      BM->>+Compute: computeClosure() -> computeOrReadCheckpoint()
      Compute-->>-BM: Iterator[数据]  // 生成原始数据

      %% 4. 尝试缓存到内存 (MEMORY_ONLY)
      BM->>+BM: doPutIterator(blockId, iterator, MEMORY_ONLY)
      BM->>+MS: putIteratorAsValues(blockId, iterator, memoryMode, classTag)

      %% 5. 内存展开过程
      Note over MS: 逐步展开iterator数据到内存
      MS->>MS: reserveUnrollMemoryForThisTask()

      %% 6. 内存不足，展开失败
      Note over MS: 内存不足，unroll失败
      MS-->>-BM: Left(PartiallyUnrolledIterator)  // 返回部分数据

      %% 7. MEMORY_ONLY模式处理 (无磁盘降级)
      BM->>BM: 检查 level.useDisk == false
      Note over BM: 无法降级到磁盘，完全失败
      BM->>BM: iteratorFromFailedMemoryStorePut = Some(iter)

      %% 8. 清理并返回失败
      BM->>BM: removeBlockInternal(blockId, tellMaster = false)
      BM-->>-RDD: Right(PartiallyUnrolledIterator)

      %% 9. getOrElseUpdate返回结果
      RDD->>RDD: 匹配 Right(iter) 分支
      RDD-->>-Task: InterruptibleIterator(未缓存的原始数据)

      Note over Task,Compute: 任务正常执行，但数据未缓存
```
- MEMORY_ONLY：失败时数据完全丢失，下次需要完全重新计算该partition的全部数据
- MEMORY_AND_DISK：失败时会spill到磁盘，下次可以从磁盘读取，避免重新计算



### MEMORY_ONLY_SER 与 MEMORY_ONLY 的性能权衡是什么？

**MEMORY_ONLY (反序列化存储)**：
- ✅ **访问速度快**: 对象直接存储在堆内存中，无需反序列化
- ✅ **CPU开销低**: 直接访问Java对象，无额外计算
- ❌ **内存占用大**: Java对象有对象头、指针等额外开销
- ❌ **GC压力大**: 大量对象增加垃圾回收负担

**MEMORY_ONLY_SER (序列化存储)**：
- ❌ **访问速度慢**: 每次访问需要反序列化操作
- ❌ **CPU开销高**: 序列化/反序列化消耗CPU资源
- ✅ **内存占用小**: 序列化后数据更紧凑，可节省2-5倍内存
- ✅ **GC友好**: 减少堆中对象数量，降低GC压力

## User Memory
Spark无法追踪User Memory使用，完全由用户代码控制
`User Memory = maxHeapMemory × (1 - spark.memory.fraction)`，


1. UDF内存使用
    ```scala
    // 例子: 用户UDF中的对象创建
    val myUDF = udf((data: String) => {
      val largeList = data.split(",").map(_.trim)  // 每行都创建新数组
      largeList.filter(_.length > 5)               // 创建过滤后的新数组
    })
    ```

2. Driver端内存
   - 配置加载: SparkConf对象和配置文件解析
   - SparkContext: 大量的内部对象和状态维护
   - DAGScheduler: 任务调度相关的数据结构

3. 第三方库内存
   - JDBC连接池 - Connection、Statement等Java对象
   - Kryo序列化 - ByteArrayOutputStream、byte[]等缓冲区
   - Kafka客户端对象 - KafkaConsumer、ProducerRecord等
   - Protobuf对象 - Java对象和内部字节数组

## memoryOverhead
`MemoryOverhead = Executor总内存 - Spark直接管理的内存`,
Executor进程中不被Spark直接管理的内存，包括JVM固有开销、系统级开销、第三方库分配的内存等。\
含普通JVM Direct Memory (用户代码、部分第三方库)

1. JVM非堆内存开销
   - Metaspace: 类加载器、方法区元数据
   - Code Cache: JIT编译后的本地代码缓存
   - 普通JVM Direct Memory: java.nio.ByteBuffer.allocateDirect()
2. Spark内部非堆内存
   - Parquet/ORC缓冲区
   - 网络通信: **Netty**的DirectByteBuffers和事件循环缓冲区
     - Kafka网络缓冲区 - 底层网络通信的off-heap缓冲区
     - 其他网络通信 - Netty Direct Memory用于数据传输
3. 系统级开销
   - 线程栈: 每个Executor线程的栈空间 (默认1MB/线程)
   - GC开销: 垃圾回收器自身的工作内存
   - 本地库: JNI调用时的本地内存分配

## OOM 常见场景


| 内存类型             | 场景                          | RCA                                                                                                                         |
|------------------|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Execution Memory | Driver 拉取大量数据               | 使用 collect()、take()、count() 等操作将大数据集拉取到 driver，Driver 内存不足以容纳所有收集的数据	                                                       | 
| Execution Memory | sortByKey中 Shuffle 操作       | ExternalSorter， 估算不准 → 溢写不及时 → PartitionedAppendOnlyMap/PartitionedPairBuffer 持续增长 → OOM                                    | 
| Execution Memory | reduceByKey、groupByKey、join | ExternalAppendOnlyMap, 阈值过高：currentMap 内存映射可能占用超过申请Execution Memory                                                         | 
| Execution Memory | Hash Join                   | 在内存中创建大哈希表的 Hash Join，build side使用BytesToBytesMap中的LongArray无法溢写导致的内存压力                                                     | 
| Execution Memory | SMJ，数据倾斜                    | bufferMatchingRows() 方法在找到匹配的 key 时，会批量调用 bufferedMatches.add() 来添加所有匹配的 records，如果匹配的 records数量巨大，在达到溢写阈值之前，内存会持续累积并最终 OOM | 
| Storage Memory   | Broadcast 大量数据              | 使用 broadcast() 向所有 executors 广播大数据集，广播变量太大，超出可用内存	                                                                          | 
| User Memory      | mapPartitions 操作            | 使用 mapPartitions() 函数在迭代器中缓存中间结果                                                                                            | 
| Memory Overhead  | Netty shuffle 缓存            | 通过一个 executor 多个task fetch data 接近 maxRemoteBlockSizeFetchToMem=200m，会占用大量的堆外内存                                             |



## ExternalSorter


map 和 buffer 虽然是 JVM 堆内存，但逻辑内存申请属于 Execution Memory，这个管理的值是估计值，不是实际内存；
如果在 spill 时能多申请到内存，就不溢出磁盘了。
内存管理和实际的 map(PartitionedAppendOnlyMap) / buffer(PartitionedPairBuffer) 扩充管理是分离的。
- 估算值过低 → 不及时溢写 → map/buffer 在 JVM 堆中持续增长 → JVM 堆 OOM

使用在需要sort的场景：
- shuffle read(BlockStoreShuffleReader)
- shuffle write(SortShuffleWriter)


### 非聚合模式 (PartitionedPairBuffer)
```mermaid
sequenceDiagram
    autonumber
    participant TaskContext
    participant ExternalSorter
    participant PartitionedPairBuffer
    participant Spillable
    participant DiskBlockObjectWriter

    Note over ExternalSorter: 初始化 ExternalSorter[K,V,V] without Aggregator
    ExternalSorter->>PartitionedPairBuffer: 创建内存缓冲区

    loop 处理非聚合数据记录
        TaskContext->>ExternalSorter: insertAll(Iterator[Product2[K,V]])
        alt 无聚合器
            ExternalSorter->>PartitionedPairBuffer: insert(partition, key, value)
            PartitionedPairBuffer->>JVM: JVM 堆内存增长存储原始键值对
            ExternalSorter->>ExternalSorter: estimateSize() - 估算内存使用
            ExternalSorter->>Spillable: maybeSpill(buffer, estimatedSize)

            alt 估算内存 >= 阈值
                Spillable->>ExternalSorter: spill()
                ExternalSorter->>DiskBlockObjectWriter: spillMemoryIteratorToDisk()
                DiskBlockObjectWriter->>Disk: 写入临时文件
                ExternalSorter->>PartitionedPairBuffer: 创建新缓冲区
            else 估算内存 < 阈值
                ExternalSorter->>ExternalSorter: 继续在内存中缓冲
            end
        end
    end
```

## ExternalAppendOnlyMap
ExternalAppendOnlyMap Spark 中一个外部溢写映射类，专门用于处理大规模聚合操作，当内存不足时会将排序后的内容溢写到磁盘。

1. 聚合操作的内存管理：对输入的键值对进行聚合
2. 内存溢写机制：当内存不足时自动溢写到磁盘，(阈值过高：内存映射可能占用超过可用内存，导致 OOM)
3. 外部排序合并：将内存和磁盘中的多个有序流进行合并

在 CoGroupedRDD 中，ExternalAppendOnlyMap 被用于多 RDD 协分组（cogroup）操作,
多个 RDD 中的相同键需要被合并到一个元组中,
比如 join，`(k, a) cogroup (k, b)` produces k -> Array(ArrayBuffer as, ArrayBuffer bs).


## BytesToBytesMap
典型使用场景ShuffledHashJoinExec，buildHashedRelation会build 如UnsafeHashedRelation, 
binaryMap为BytesToBytesMap 结构：

LongArray 只存储在内存(数据量太大会OOM)：
- key 的内存地址编码（不是 key 本身，而是指向 dataPages 中的位置）
- key 的完整 hashcode（用于快速比较）

DataPages 存储在MemoryBlock，可以溢出磁盘:
- 存储实际的 key-value 对数据格式: [total size][key size][key data][value data][next pointer]

**结构图**
```mermaid
graph TB
    subgraph "BytesToBytesMap 整体结构"
        LongArray[LongArray 元数据<br/>key地址和hashcode<br/>无法溢写]
        DataPages[DataPages 实际数据<br/>key-value对<br/>可溢写到磁盘]
    end

    subgraph "LongArray 结构 (元数据)"
        LA0["[0] key地址1"]
        LA1["[1] hashcode1"]
        LA2["[2] key地址2"] 
        LA3["[3] hashcode2"]
        LA4["[4] key地址3"]
        LA5["[5] hashcode3"]
        LA6["[6] key地址4"]
        LA7["[7] hashcode4"]
    end

    subgraph "DataPages 存储实际数据 (可溢写)"
        subgraph "Page 1 内存页"
            P1R1["Record 1<br/>[总长度][key长][key数据][value数据][next_ptr]"]
            P1R2["Record 2<br/>[总长度][key长][key数据][value数据][next_ptr]"]
            P1R3["Record 3 (冲突)<br/>[总长度][key长][key数据][value数据][next_ptr]"]
        end
        
        subgraph "Page 2 内存页"
            P2R1["Record 4<br/>[总长度][key长][key数据][value数据][0]"]
        end
    end

    LongArray --> LA0 & LA1 & LA2 & LA3 & LA4 & LA5 & LA6 & LA7
    DataPages --> Page1 & Page2
    Page1[Page 1] --> P1R1 & P1R2 & P1R3
    Page2[Page 2] --> P2R1

    LA0 -.-> P1R1
    LA2 -.-> P1R2  
    LA4 -.-> P1R3
    LA6 -.-> P2R1

    P1R3 --> P2R1
```

**查找过程示例**

```mermaid
sequenceDiagram
    participant User as 用户请求
    participant Map as BytesToBytesMap  
    participant LongArray as LongArray
    participant DataPage as Data Page
    participant Disk as 磁盘文件

    User->>+Map: lookup("key2", hash=34567)
    Map->>+LongArray: 查找 hashcode=34567
    LongArray-->>-Map: 返回地址addr1和hashcode
    Map->>+DataPage: 读取addr1处的key和value
    alt 在内存中
        DataPage-->>-Map: 返回key2和value2
    else 在磁盘中
        Map->>+Disk: 从磁盘读取
        Disk-->>-Map: 返回key2和value2
    end
    Map-->>-User: 返回结果[value2, value4]
```
## ExternalAppendOnlyUnsafeRowArray
SortMergeJoin执行流程中，当streamed side遍历到特定key时，
`ExternalAppendOnlyUnsafeRowArray` 作为build side的匹配缓冲区，
临时存储该key对应的所有build side记录，用于完成join匹配。

```mermaid
sequenceDiagram
    participant SortMergeJoinScanner
    participant ExternalAppendOnlyUnsafeRowArray
    participant ArrayBuffer
    participant UnsafeExternalSorter
    participant TaskMemoryManager
    participant Disk

    Note over ExternalAppendOnlyUnsafeRowArray: 初始化，numRowsInMemoryBufferThreshold=128
    Note over ExternalAppendOnlyUnsafeRowArray: 当前 numRows=0

    loop Add 操作循环
        SortMergeJoinScanner->>ExternalAppendOnlyUnsafeRowArray: add(unsafeRow)
        
        ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: 检查 numRows < numRowsInMemoryBufferThreshold?
        
        alt numRows < 128 (内存模式)
            ExternalAppendOnlyUnsafeRowArray->>ArrayBuffer: inMemoryBuffer.add(unsafeRow.copy())
            ArrayBuffer->>ExternalAppendOnlyUnsafeRowArray: 添加成功
            ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: numRows += 1
            ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: modificationsCount += 1
        else numRows >= 128 (溢写模式)
            Note over UnsafeExternalSorter: 首次进入溢写模式
            ExternalAppendOnlyUnsafeRowArray->>UnsafeExternalSorter: 创建 UnsafeExternalSorter 实例
            UnsafeExternalSorter->>TaskMemoryManager: 请求内存资源
            TaskMemoryManager->>UnsafeExternalSorter: 分配内存
            
            Note over ExternalAppendOnlyUnsafeRowArray: 迁移现有数据
            ExternalAppendOnlyUnsafeRowArray->>ArrayBuffer: 遍历 inMemoryBuffer
            ArrayBuffer->>UnsafeExternalSorter: spillableArray.insertRecord(existingRow...)
            ExternalAppendOnlyUnsafeRowArray->>ArrayBuffer: inMemoryBuffer.clear()
            ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: numFieldsPerRow = unsafeRow.numFields()
            
            Note over UnsafeExternalSorter: 添加新数据
            ExternalAppendOnlyUnsafeRowArray->>UnsafeExternalSorter: spillableArray.insertRecord(unsafeRow.getBaseObject, ...)
            UnsafeExternalSorter->>TaskMemoryManager: 检查内存使用
            opt 内存压力检测
                TaskMemoryManager->>UnsafeExternalSorter: 内存不足，触发溢写
                UnsafeExternalSorter->>Disk: 溢写数据到磁盘文件
                Disk->>UnsafeExternalSorter: 溢写完成
                UnsafeExternalSorter->>TaskMemoryManager: 更新内存状态
            end
            UnsafeExternalSorter->>ExternalAppendOnlyUnsafeRowArray: 插入记录成功
            ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: numRows += 1
            ExternalAppendOnlyUnsafeRowArray->>ExternalAppendOnlyUnsafeRowArray: modificationsCount += 1
        end
    end
```

**SortMergeJoinScanner inner join流程**

```mermaid
sequenceDiagram
    autonumber
    participant TaskContext
    participant SortMergeJoinExec
    participant SortMergeJoinScanner
    participant ExternalArray as ExternalAppendOnlyUnsafeRowArray (bufferedMatches)

    Note over TaskContext: SortMergeJoinExec (InnerLike) 执行
    Note over SortMergeJoinScanner: SortMergeJoinScanner 处理左右侧数据
    
    TaskContext->>SortMergeJoinExec: execute()
    SortMergeJoinExec->>SortMergeJoinExec: 获取 build side 和 stream side 数据流
    SortMergeJoinExec->>SortMergeJoinScanner: new SortMergeJoinScanner(streamedIter, bufferedIter, ...)
    
    Note over ExternalArray: 初始化 bufferedMatches (ExternalAppendOnlyUnsafeRowArray)
    Note over SortMergeJoinScanner: bufferedMatches 缓冲 build side 匹配的行
    Note over SortMergeJoinScanner: streamedIter 是 stream side 的迭代器
    Note over SortMergeJoinScanner: bufferedIter 是 build side 的迭代器

    loop findNextInnerJoinRows()
        SortMergeJoinScanner->>SortMergeJoinScanner: advancedStreamed() - 移动 stream 侧指针
        alt stream 侧行的键没有 null
            SortMergeJoinScanner->>SortMergeJoinScanner: 比较 stream 和 buffered 键
            alt 键相等 (匹配)
                SortMergeJoinScanner->>SortMergeJoinScanner: bufferMatchingRows()
                SortMergeJoinScanner->>ExternalArray: loop -> bufferedMatches.add() 
                ExternalArray->>ExternalArray: 可能触发内存模式到溢写模式切换
            else 键不等
                SortMergeJoinScanner->>SortMergeJoinScanner: 移动指针寻找匹配行
            end
        end
    end
    
    Loop Join 结果生成
        SortMergeJoinScanner->>ExternalArray: getBufferedMatches() - 获取缓冲的匹配行
        ExternalArray->>SortMergeJoinScanner: 返回 ExternalAppendOnlyUnsafeRowArray
        SortMergeJoinScanner->>SortMergeJoinScanner: 生成匹配结果 (streamed row × bufferedMatches)
    end
    
    SortMergeJoinExec->>SortMergeJoinScanner: 执行完成
    SortMergeJoinScanner->>ExternalArray: clear() - 清理 bufferedMatches 资源
    ExternalArray->>TaskContext: 释放内存资源
    SortMergeJoinExec->>TaskContext: Inner Join 完成
```

### UnsafeExternalSorter
numElementsForSpillThreshold = Integer.MAX_VALUE，通常设置得很高，
在达到溢写阈值之前，内存会持续累积并最终超过 JVM 堆内存限制导致 OOM。
