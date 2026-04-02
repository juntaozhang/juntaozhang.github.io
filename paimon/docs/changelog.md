
# ChangeLog
输入增删改，paimon 表提供标准的 changelog （`+I -U +U -D`）输出。

| 模式 (Mode) | 作用原理 | 输出效果 | 适用场景 |
| :--- | :--- | :--- | :--- |
| none (默认) | 不额外生成日志。流读时依赖 Flink State 补全 `-U`。 | 不完整。如果不开启 Normalize 算子，可能只有 `+I`, `+U`, `-D`，缺少 `-U`。 | 仅用于批读，或下游不需要完整 Changelog 的场景。 |
| input | 直接双写输入流。要求源数据本身就是完整的 CDC 流。 | 完整。直接输出源数据的 `+I -U +U -D`。 | 源数据就是 CDC 数据（如 Debezium 格式）。 |
| lookup | 每次提交前，通过“维表 Join”的方式查询旧状态。 | 完整。能精确生成 `-U` 和 `+U`。 | 对实时性要求高，且输入数据不是完整 CDC 的场景。 |
| full-compaction | 通过全量合并（Compaction）对比新旧数据。 | 完整。生成标准的 Changelog。 | 对实时性要求不高，但需要准确变更日志的场景。 |

## None
意味着没有额外生成 changelog
后续 Flink Job 需要该表的 changelog，需要开启 Normalize 算子

ChangelogNormalize 对于保证数据准确性是必不可少的，但它被称为 Flink CDC 作业中的“重量级”算子。
在很多生产环境中，它是导致作业反压（Backpressure）和 Checkpoint 超时的首要原因

## input 
source 是完整的 CDC 流，直接输出 `+I -U +U -D`。


## [lookup](compact.md#lookup-compaction)

```java
public LookupStrategy lookupStrategy() {
    return LookupStrategy.from(
            mergeEngine().equals(MergeEngine.FIRST_ROW),
            changelogProducer().equals(ChangelogProducer.LOOKUP),
            deletionVectorsEnabled(),
            options.get(FORCE_LOOKUP));
}
```
如果是这几种情况，就会启动lookup，lookup 会查找旧状态，这个查询是通过触发compaction来实现的。\
每次简单insert，产生2个sort_run，产生两个snapshot，一个是append，一个是commit


source 是不完整的 CDC 流, 缺少 `-U`，每次提交前，通过“维表 Join”的方式查询旧状态，生成 `-U` 和 `+U`。
lookup 相比 Flink Normalize Operator 有以下优势：
- 将状态管理从Flink转移到Paimon的存储层
- 使用内存+磁盘的二级缓存机制
- Paimon在存储层为主键构建了高效索引，支持O(log n)的查找性能
- 相比 Normalize 状态存储在单个节点，可能成为性能瓶颈，lookup 可以利用多个线程同时进行查找和变更日志生成

### Sorted String Table

SST File 中查找一个 key 时：
```text

  步骤 1: 读取 Footer (36 bytes)
          → 获取 Index Block 的位置

  步骤 2: 读取 Index Block
          → 在 Index Block 内部二分查找（可能是 Aligned 或 Unaligned）
          → 找到目标 Data Block 的 BlockHandle (offset, size)

  步骤 3: 读取目标 Data Block
          → 在 Data Block 内部二分查找（可能是 Aligned 或 Unaligned）
          → 找到目标 Key-Value
```

SST File Layout
```text
  ┌─────────────────────────────────────────────────────────────┐
  │                    SST File Layout                          │
  ├─────────────────────────────────────────────────────────────┤
  │                                                             │
  │  +-------------------------------------------------------+  │
  │  │ Data Block 1                                          │  │
  │  │   [key1, value1] (e.g., user:001, Alice)              │  │
  │  │   [key2, value2] (e.g., user:002, Bob)                │  │
  │  │   [key3, value3] (e.g., user:003, Charlie)            │  │
  │  │   + Restart Positions (optional)                      │  │
  │  │   + Block Trailer (5 bytes: CRC32C + Compression)     │  │
  │  +-------------------------------------------------------+  │
  │  +-------------------------------------------------------+  │
  │  │ Data Block 2                                          │  │
  │  │   [key4, value4]                                      │  │
  │  │   ...                                                 │  │
  │  │   + Block Trailer                                     │  │
  │  +-------------------------------------------------------+  │
  │  +-------------------------------------------------------+  │
  │  │ ... more Data Blocks ...                              │  │
  │  +-------------------------------------------------------+  │
  │                                                             │
  │  +-------------------------------------------------------+  │
  │  │ Bloom Filter (optional)                               │  │
  │  │   Bit array for fast existence check                  │  │
  │  +-------------------------------------------------------+  │
  │                                                             │
  │  +-------------------------------------------------------+  │
  │  │ Index Block                                           │  │
  │  │   [lastKey1, BlockHandle1 → offset/size of Block 1]   │  │
  │  │   [lastKey2, BlockHandle2 → offset/size of Block 2]   │  │
  │  │   ...                                                 │  │
  │  │   + Block Trailer                                     │  │
  │  +-------------------------------------------------------+  │
  │                                                             │
  │  +-------------------------------------------------------+  │
  │  │ Footer (36 bytes)                                     │  │
  │  │   BloomFilterHandle (offset, size, numEntries)        │  │
  │  │   IndexBlockHandle (offset, size)                     │  │
  │  │   Magic Number (0x5850454c)                           │  │
  │  +-------------------------------------------------------+  │
  │                                                             │
  └─────────────────────────────────────────────────────────────┘
```

#### Data Block
用二分查找，快速根据key检索到value：

| 类型        | 核心特点    | 查找方式           | 备注                  |
|-----------|---------|----------------|---------------------|
| Aligned   | 记录等长    | 位置 = 记录号 × 大小  | key 和 value 都要求长度一致 |
| Unaligned | 记录不等长   | 位置 = 索引数组[记录号] | 二次内存随机定位            |

- AlignedBlockReader
```text
  +------------------ Block Start ----------------+
  | Record 0: [key|value]  (20 bytes)             |
  +-----------------------------------------------+
  | Record 1: [key|value]  (20 bytes)             |
  +-----------------------------------------------+
  | Record 2: [key|value]  (20 bytes)             |
  +-----------------------------------------------+
  | Record 3: [key|value]  (20 bytes)             |
  +-----------------------------------------------+
  | recordSize: 20 bytes   (4 bytes)              |
  | aligned: 0x01          (1 byte)               |
  +------------------ Block End ------------------+
```

- UnalignedBlockReader\
**TODO：** Index[i] 有没有等价值key转换, 直接可以排序，去掉一次内存随机查找？
```text
  +------------------ Block Start ----------------+
  | Record 0: [key|value]  (17 bytes)             |
  +-----------------------------------------------+
  | Record 1: [key|value]  (20 bytes)             |
  +-----------------------------------------------+
  | Record 2: [key|value]  (15 bytes)             |
  +-----------------------------------------------+
  | Record 3: [key|value]  (22 bytes)             |
  +-----------------------------------------------+
  | Index[0]: 0           (4 bytes)               |
  | Index[1]: 17          (4 bytes)               |
  | Index[2]: 37          (4 bytes)               |
  | Index[3]: 52          (4 bytes)               |
  +-----------------------------------------------+
  | count: 4              (4 bytes)               |
  | aligned: 0x00         (1 byte)                |
  +------------------ Block End ------------------+
```

### compaction
完整流程图
```text
  Checkpoint 触发
      ↓
  prepareCommit()
      ↓
  flushWriteBuffer() → 生成 Level 0 文件
      ↓
  triggerCompaction()
      ↓
  ForceUpLevel0Compaction.pick() → 选择压缩任务
      ↓
  提交压缩任务到线程池
      ↓
  MergeTreeCompactTask.doCompact()
      ↓
  ChangelogMergeTreeRewriter.rewrite()
      ↓
  rewriteOrProduceChangelog(produceChangelog=true)
      ↓
  创建 LookupChangelogMergeFunctionWrapper
      ├─ mergeFunction (合并逻辑)
      └─ lookup = key -> lookupLevels.lookup(key, outputLevel+1)
      ↓
  遍历所有记录:
      ├─ mergeFunction.getResult() → 合并记录
      │  └─ 如果没有高层级记录
      │     └─ lookupLevels.lookup() → 查找历史值
      │        ├─ 从 outputLevel+1 查到最高层
      │        └─ 可能需要从 .lookup 索引文件读取
      │
      ├─ 生成 Changelog
      │  ├─ 对比 before (历史值) 和 after (新值)
      │  └─ 生成 INSERT/UPDATE/DELETE
      │
      └─ 写入文件
         ├─ compactFileWriter → data-N-new.dat
         └─ changelogFileWriter → changelog-N-new.cl
      ↓
  CompactResult(before, after, changelog)
      ↓
  levels.update(before, after)
      ├─ 删除旧文件
      ├─ 添加新文件
      └─ 触发回调 → 删除旧文件的 Lookup 索引
      ↓
  返回 CompactIncrement 给 commit
```
整体而言比 full 资源开销小

## full-compaction
在 full-compaction 模式下，通过全量合并（Compaction）对比新旧数据，生成 `-U` 和 `+U`。\
实时性要求不高的场景下使用\
相比 `lookupLevels.lookup()`查找历史值, 直接在合并时就可以读到历史记录，操作简单\
