# Spark Memory Management: Execution vs Storage 详细分析

## 📊 基础参数

### 数据规模
- **数据总量**: 200GB + 50GB (Parquet压缩后)
- **解压后数据**: 1000GB + 250GB = 1250GB
- **Shuffle输出**: 600GB (LZ4压缩后，膨胀因子0.5)
- **Join结果因子**: 1.2x
- **Shuffle分区数**: 500
- **每个Executor并发任务数**: 3

### 每个Task数据负载
- **输入数据**: 1250GB ÷ 500 = 2.5GB/Task
- **Shuffle输出**: 600GB ÷ 500 = 1.2GB/Task

## 🏗️ Spark内存管理架构

### 核心配置参数源码依据

```scala
// package.scala:389-397
private[spark] val MEMORY_FRACTION = ConfigBuilder("spark.memory.fraction")
  .doc("Fraction of (heap space - 300MB) used for execution and storage")
  .createWithDefault(0.6)

private[spark] val MEMORY_STORAGE_FRACTION = ConfigBuilder("spark.memory.storageFraction")
  .doc("Amount of storage memory immune to eviction, expressed as a fraction of the size of the region set aside by spark.memory.fraction")
  .createWithDefault(0.5)

// UnifiedMemoryManager.scala:198
private val RESERVED_SYSTEM_MEMORY_BYTES = 300 * 1024 * 1024  // 300MB保留内存
```

### 内存分配计算逻辑

```scala
// UnifiedMemoryManager.scala:232-235
private def getMaxMemory(conf: SparkConf): Long = {
  val usableMemory = systemMemory - reservedMemory  // 4GB - 300MB = 3712MB
  val memoryFraction = conf.get(config.MEMORY_FRACTION)  // 默认0.6
  (usableMemory * memoryFraction).toLong  // 3712MB × 0.6 = 2227MB
}
```

## 💾 MemoryOverhead 详细计算

### JVM非堆内存组件

| 组件                       | 源码依据                        | 计算方式                   | 内存占用  |
|--------------------------|-----------------------------|------------------------|-------|
| **Metaspace**            | JVM规范                       | Spark 3.x类加载优化         | 256MB |
| **Code Cache**           | JVM规范                       | JIT编译缓存                | 128MB |
| **线程栈空间**                | Executor.scala:113-119      | 25线程×1MB               | 25MB  |
| **GC工作内存**               | package.scala:335-339       | GC算法开销，与堆大小成比例         | 80MB  |
| **Parquet向量化缓冲区**        | SQLConf.scala:1036          | 4096行×250字节×3tasks     | 3MB   |
| **Netty Arena Pool 内存池** | NettyUtils.java:129         | min(3核, 2 * 3)×16MB    | 48MB  |
| **Shuffle拉取缓冲区**         | reducer.maxSizeInFlight=48m | 3tasks×48MB            | 144MB |

**线程栈详细分析** (基于Executor.scala:113-119):
```scala
// Executor任务线程池 - newCachedThreadPool (动态扩展)
private[executor] val threadPool = Executors.newCachedThreadPool(threadFactory)

// Task reaper线程池 - 监督任务取消
private val taskReaperPool = ThreadUtils.newDaemonCachedThreadPool("Task reaper")

// NettyRpcEnv RPC线程
val timeoutScheduler = ThreadUtils.newDaemonSingleThreadScheduledExecutor("netty-rpc-env-timeout")
private[netty] val clientConnectionExecutor = ThreadUtils.newDaemonCachedThreadPool("netty-rpc-client")

// NettyUtils.java:129 - Netty I/O线程
NettyUtils.createEventLoop(IOMode.NIO, 3, "shuffle-chunk-fetch-handler")
```

**线程数计算**:
- 任务线程池: 最多3个并发任务 = 3线程
- Task reaper池: cached线程池，默认 < 5线程 = 5线程
- Netty RPC: 1个timeout scheduler + cached client连接池 = 6线程
- Netty I/O: 3个事件循环线程
- 系统线程: GC、监控等 ≈ 8线程
- **总计**: 3 + 5 + 6 + 3 + 8 = **25线程**

**Parquet读取缓冲**
   ```text
   VectorizedParquetRecordReader 读取数据到内存 columnarBatch-> OnHeapColumnVector[]
   向量化批次: 4096行 × 250字节 = 1MB/批次 PARQUET_VECTORIZED_READER_BATCH_SIZE
   ```
- 向量化缓冲: 1MB

**Netty Arena Pool 内存池**

源码依据: Netty的内部配置（Spark中没有直接配置）
Netty PooledByteBufAllocator 默认配置：
- spark.network.sharedByteBufAllocators.enabled=true
- 每个 Arena: 16MB (Chunk size)
- 默认 Arena 数量: min(availableProcessors=3, 3 × 2)
- 总内存: Arena数量 × 16MB = 48MB

### **最终MemoryOverhead总计**
```
MemoryOverhead Total = 256 + 128 + 25 + 80 + 3 + 48 + 144 ≈ 1GB
推荐配置: spark.executor.memoryOverhead=768m
```

**GC内存使用说明**：
- **GC工作内存**：包含GC线程栈、算法工作区（如G1的Remembered Sets、CMS的Mark Stack等）
- **与堆大小成比例**：堆越大，GC越复杂，需要的non-heap内存越多
- **Spark设计考虑**：`spark.executor.memoryOverheadFactor=0.1`默认10%考虑了GC开销增长

## 🗄️ Storage Memory详细组件

**配置依据** (package.scala:389-397):
```scala
private[spark] val MEMORY_FRACTION = ConfigBuilder("spark.memory.fraction")
  .createWithDefault(0.6)  // 默认值

private[spark] val MEMORY_STORAGE_FRACTION = ConfigBuilder("spark.memory.storageFraction")
  .createWithDefault(0.5)  // 默认值
```

**Storage Memory实际使用**: 没有cache实际使用0MB，全部1139MB供Execution Memory借用

## ⚙️ Execution Memory详细组件

### Spark Execution Memory的动态申请机制

Execution Memory采用**动态申请 + Spill机制**，无法预先精确计算！

#### 阶段1: ShuffleMapTask - Shuffle写端内存动态申请

**源码依据**: ExternalAppendOnlyMap.scala + TaskMemoryManager.java
```scala
// ExternalAppendOnlyMap.scala:29-30 - MemoryConsumer机制
abstract class Spillable[C](taskMemoryManager: TaskMemoryManager)
  extends MemoryConsumer(taskMemoryManager, MemoryMode.ON_HEAP)

// TaskMemoryManager.java:306-323 - 动态页面分配
public MemoryBlock allocatePage(long size, MemoryConsumer consumer) {
  long acquired = acquireExecutionMemory(size, consumer);  // 动态申请Execution Memory
  if (acquired <= 0) {
    return null;  // 申请失败，会触发spill到磁盘
  }
  page = memoryManager.tungstenMemoryAllocator().allocate(acquired);
}
```

**ShuffleMapTask内存使用**：

2. **Shuffle写端内存** - **真正的Execution Memory用户**
   ```scala
   // MemoryManager.scala:254-273 - 页面大小计算
   val pageSizeBytes = 32MB  // 系统自动计算

   // ExternalAppendOnlyMap通过TaskMemoryManager动态申请
   // 插入数据时调用acquirePage(32MB)
   ```
   - **申请机制**: 按需申请32MB页面，申请失败就spill
   - **最大限制**: 1139MB基础配额 + 可借用Storage空间
   - **实际占用**: **无法预先精确计算**，完全按需分配

**ShuffleMapTask总内存**: **动态申请，不超过配额限制**

#### 阶段2: ResultTask - SortMergeJoin处理内存

**源码依据**: SortMergeJoinExec.scala:148 + ExternalAppendOnlyUnsafeRowArray.scala:135
```scala
// SortMergeJoinExec.scala:148
private[this] var currentRightMatches: ExternalAppendOnlyUnsafeRowArray = _

// ExternalAppendOnlyUnsafeRowArray.scala:135-144 - 内存存储策略
if (numRows < initialSizeOfInMemoryBuffer) {
  // 前128行存储在内存ArrayBuffer中
  inMemoryBuffer.asInstanceOf[ArrayBuffer[UnsafeRow]] += row
} else {
  // 超过128行，使用UnsafeExternalSorter（需要Execution Memory）
  // 数据溢出到磁盘，通过iterator按需读取
}
```

**ResultTask内存使用真实情况**：

**SortMergeJoin缓冲**
   - **初始缓冲**: 128行ArrayBuffer (约12.8KB)
   - **超过128行**: 使用UnsafeExternalSorter动态申请Execution Memory
   - **Spill机制**: 内存不足时溢出到磁盘
     - 默认情况下spillableArray.insertRecord 触发split
     - UnsafeExternalSorter 默认numElementsForSpillThreshold=Integer.MAX_VALUE
     - insertRecord split 主要触发依靠`growPointerArrayIfNecessary`中的catch代码块来split

**ResultTask总内存**: 无法预先精确计算，取决于数据匹配模式，动态申请，初始极小，增长时触发spill

#### 阶段3: Executor并发内存分析

- **ShuffleMapTask**: 3个Task动态申请Execution Memory，共享1139MB配额
- **ResultTask**: 3个Task动态申请，初始占用极小
- **并发限制**: 总占用不超过1139MB基础配额 + 可借用Storage空间

**借用Storage源码依据**: UnifiedMemoryManager.scala:232-235 + TaskMemoryManager.java
```scala
// UnifiedMemoryManager.scala:232-235 - 内存配额计算
private def getMaxMemory(conf: SparkConf): Long = {
  val usableMemory = systemMemory - reservedMemory  // 4096MB - 300MB = 3796MB
  val memoryFraction = conf.get(config.MEMORY_FRACTION)  // 默认0.6
  (usableMemory * memoryFraction).toLong  // 3796MB × 0.6 = 2278MB
}

// TaskMemoryManager.java - 动态申请机制
public long acquireExecutionMemory(
    long numBytes, MemoryConsumer consumer) {
  // 1. 先从Execution Memory池申请
  // 2. 不够时，借用Storage空闲内存
  // 3. 仍然不够时，返回0，触发spill
}
```
### Execution Memory分配表

| 阶段 | 组件 | 源码依据 | 真实机制 | 内存占用 |
|------|------|----------|----------|----------|
| **ShuffleMapTask** | Shuffle写端 | ExternalAppendOnlyMap + TaskMemoryManager | 动态申请32MB页面，失败则spill | **无法预先精确计算** |
| **ResultTask** | SortMergeJoin | ExternalAppendOnlyUnsafeRowArray | 128行初始缓冲，超出用UnsafeExternalSorter | **无法预先精确计算** |
| **并发限制** | 所有Task | TaskMemoryManager | 共享1139MB配额 + 可借用Storage | **不超过配额限制** |
| **基础配额** | UnifiedMemoryManager | spark.memory.fraction=0.6 | 2278MB×0.5 | 1139MB |
| **动态借用** | ExecutionMemoryPool | 可借用Storage空闲内存 | 最大1139MB | **动态计算** |


## 📋 最终内存分配详情

```text
Executor Memory: 4096MB
├── 系统保留内存 (RESERVED_SYSTEM_MEMORY_BYTES): 300MB
├── 用户代码预留 (40%): 1638MB
├── Unified Memory Pool (60%): 2278MB
│   ├── Storage Memory (基础配额50%): 1139MB
│   │   └── 可用借用空间: 1139MB (全部供Execution借用)
│   │
│   ├── Execution Memory (基础配额50%): 1139MB
│   │   ├── ShuffleMapTask阶段: 动态申请，失败则spill
│   │   ├── ResultTask阶段: 动态申请，初始极小
│   │   └── 最大可用: 1139MB + 可借用Storage空间
│   │
│   └── 动态借用机制: Execution可借用Storage空闲内存
│
└── MemoryOverhead: 1GB
    ├── Metaspace: 256MB
    ├── Code Cache: 128MB
    ├── 线程栈空间: 25MB
    ├── GC工作内存: 80MB
    ├── Parquet缓冲: 3MB
    ├── Netty Arena Pool: 48MB
    ├── Shuffle缓冲: 144MB
    └── 其他native开销: 36MB
```

## 🎯 配置参数
```bash
# Executor内存配置
spark.executor.memory=4g
spark.executor.memoryOverhead=768m
spark.executor.cores=3

# 内存管理配置 (使用默认值，适合SMJ场景)
spark.memory.fraction=0.6           # 默认值，60%用于Unified Memory Pool
spark.memory.storageFraction=0.5     # 默认值，SMJ场景中Storage使用很少，大部分可借用

# 针对Join优化
spark.sql.sortMergeJoinExec.buffer.spill.threshold=1000000
spark.sql.autoBroadcastJoinThreshold=10m
spark.shuffle.file.buffer=256k
spark.reducer.maxSizeInFlight=48m     # 使用默认值

# Parquet优化
spark.sql.parquet.enableVectorizedReader=true
spark.sql.parquet.columnarReaderBatchSize=4096
```