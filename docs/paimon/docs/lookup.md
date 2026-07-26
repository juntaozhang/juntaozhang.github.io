# lookup

## Compaction Lookup

在 compaction 时按 key 查找高层级旧值的机制

### when need lookup
[CoreOptions.lookupStrategy()](../paimon-api/src/main/java/org/apache/paimon/CoreOptions.java)

| case             | conf                            |
|------------------|---------------------------------|
| produceChangelog | changelog-producer = lookup     |
| deletionVector   | deletion-vectors.enabled = true |
| isFirstRow       | merge-engine = first_row        |
| forceLookup      | force-lookup = true             |

example 见：[LocalTest.scala#test("lookup: first-row")](../paimon-spark/paimon-spark-ut/src/test/scala/org/apache/paimon/spark/sql/my/LocalTest.scala)

Changelog Lookup\
这是 LookupMergeFunction / LookupMergeTreeCompactRewriter 中的 lookup，解决的是主键表在 compaction 时如何正确产出 changelog 的问题。
(source 是不完整的 CDC 流, 缺少 `-U`，每次提交前，通过“维表 Join”的方式查询旧状态，生成 `-U` 和 `+U`。)

<details>
<summary>在关闭changelog，为什么 engine 中只有 first_row 才会强制 lookup？</summary>

技术上确实可以做一个 lazy 去重 的 FIRST_ROW：让重复 key 暂时存在于多个层级，读取时取 seq 最小。\
但 Paimon 选择 eager 去重（通过 lookup 在 compaction 时彻底丢弃）

DEDUPLICATE/PARTIAL_UPDATE/AGGREGATE 都顺着 LSM 的数据流向设计，只有 FIRST_ROW 是逆着来的，旧数据在高层，需要往上查来压制新数据。

目前 FIRST_ROW Level > 0 是不会重复的。\
如果采用 lazy 方式，Level > 0 会存在重复 key 的情况。
</details>

<details>
<summary>Lookup 为什么必须强制 Compaction？</summary>

lookup 会查找旧状态，这个查询是通过触发compaction来实现的。\
每次简单insert，产生2个sort_run，产生两个snapshot，一个是append，一个是compact

write compaction 策略
- lookup-compact ：`MergeTreeCompactManagerFactory.java#createCompactStrategy`
  - radical: 强制合并 Level 0 到更高层级
    - ForceUpLevel0Compaction 的核心行为：只要 Level 0 有文件，就强制把它们 compact 到更高层级。
  - gentle: 仅在 Level 0 满足条件的时候合并，否则不合并
    - max(lookup-compact.max-interval, num-sorted-run.compaction-trigger)

read scan: 过滤掉level0，如果没有compaction，新数据不会被检索
- DataTableBatchScan
    - `snapshotReader.withLevelFilter(level -> level > 0).enableValueFilter();`
    - SnapshotReader.withLevelFilter: `deletionVectorsEnabled() || mergeEngine() == FIRST_ROW;`
    - SparkCatalog.loadTable: FROM_SNAPSHOT: scan.version -> 3

- [Lookup Compaction](https://paimon.apache.org/docs/master/primary-key-table/compaction/#lookup-compaction)

</details>

<details>
<summary>为什么每次 Insert 产生两个 Snapshot？</summary>

这是 lookup 表的标准行为：

| 阶段      | Snapshot 类型 | 内容           |
|---------|---------|--------------|
| 写入      | APPEND  | 新数据写入 Level 0 |
| 提交后立刻触发 | COMPACT | ForceUpLevel0Compaction 把 Level 0 合并到 Level 1+ |

流程：
1. MergeTreeWriter 写入数据到 Level 0
2. prepareCommit() 调用 getCompactionResult(true) 等待 compaction
3. compactNotCompleted() 返回 true（Level 0 还有文件）
4. 触发 ForceUpLevel0Compaction，把 Level 0 文件合并到更高层级
5. 在 compaction 过程中，LookupMergeFunction 通过 LookupLevels 查找旧值
6. 产出 merge 后的新文件 + changelog 文件
7. 最终 commit 生成 COMPACT 类型的 snapshot
</details>

### LookupLevels
LookupLevels 是 Paimon 为每个数据文件构建的本地点查索引，它的作用是把远程/本地的 Parquet/ORC 数据文件转换成内存映射友好的 SSTable 格式，从而在 compaction 时实现 O(logN) 的 key 点查，而不是全表扫描。

```text
LookupLevels 的查找是从 startLevel 开始逐层向下的：
Level 0 → Level 1 → Level 2 → ... → Level N
 ↓         ↓         ↓
命中？     命中？     命中？
是 → 返回   是 → 返回   是 → 返回

Level 0 查找（遍历文件）
    Level 0 的文件无序且可能重叠，只能逐个检查 minKey/maxKey 范围

Level > 0 查找（二分定位文件）
    Level > 0 的文件不重叠且按 maxKey 有序，先二分定位文件：
```

SST（Sorted String Table） File 中查找一个 key 时：
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

#### SST File Layout
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
BlockReader 是 SSTable Data Block 的读取器，Paimon 支持两种格式：
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

- UnalignedBlockReader: 更普遍
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

#### Q&A
<details>
<summary>Index[i] 有没有等价值key转换, 直接可以排序，去掉一次内存随机查找？</summary>

目前 Paimon 的 BlockIterator.seekTo 确实需要两次间接访问：`BlockIterator.java:65`。\
如果 Index[i] 直接存储 key（或 key 前缀），确实可以省掉 readEntry() 的随机内存访问。
```text
  +-----------------------------------------------+
  | Index[0]: short-key,0      (8+4 bytes)        |
  | Index[1]: short-key,17     (8+4 bytes)        |
  | Index[2]: short-key,37     (8+4 bytes)        |
  | Index[3]: short-key,52     (8+4 bytes)        |
  +-----------------------------------------------+
```

如果要优化，常见做法是存 short key 前缀（如 8 bytes），先比较前缀，前缀命中后再解析完整 record。这是 LevelDB/RocksDB 的 restart interval 优化思路，但 Paimon 当前实现选择了更简洁的"纯偏移索引"方案。

原因是：\
Block 通常很小：默认 4KB/16KB，即使一次随机 read 也在 [CPU cache line 64kb](juntaozhang.github.io/algorithm/src/test/java/cn/juntaozhang/design/README.md#Latency-numbers) 范围内，收益有限。

SstFileWriter 按照 blockSize（`CoreOptions.cachePageSize` 默认 64 kb） 写入 Data Block。\
cache-page-size 只在 write 侧作为目标 block 大小出现，但 Reader 读取的是写入完成后文件中内嵌的 BlockHandle。Reader 代码里看不到这个配置，但它每次读取的 I/O 块大小，恰好就是写入时由 cache-page-size 决定的那个值。
</details>

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

### [deletion-vectors](deletion-vectors.md#mow-merge-on-write)

## Flink SQL 层面的 Lookup Join

Lookup Join 做了什么

Paimon 提供 LookupTable 接口，Flink 会在运行时通过 `FileStoreLookupFunction` 按 key 对 Paimon 表进行点查：
详细见：`LookupTableTest`

实现包括：

| 实现                           | 说明                 |
|------------------------------|--------------------|
| PrimaryKeyLookupTable        | 主键表点查，直接定位到 bucket |
| SecondaryIndexLookupTable    | 非主键点查（需要二级索引）      |
| FullCacheLookupTable         | 全量缓存到内存/本地磁盘，定期刷新  |
| PrimaryKeyPartialLookupTable | 部分缓存，按需加载          |

解决了什么

- 大维度表关联：不需要全量广播，按 key 按需查
- 实时更新感知：refresh() 机制定期加载 Paimon 的新 snapshot
- 减少状态存储：Paimon 本身作为状态后端，Flink 无需维护关联状态


https://paimon.apache.org/docs/master/flink/sql-lookup/