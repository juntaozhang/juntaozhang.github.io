
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

StreamExec`ChangelogNormalize `will translateToPlanInternal => ChangelogNormalize which is expensive.
- KeyedProcessOperator: first will shuffle
- ProcTimeDeduplicateKeepLastRowFunction: will have a large state to store history to gen -D/-U/+U
- DeduplicateFunctionHelper

ChangelogNormalize 对于保证数据准确性是必不可少的，但它被称为 Flink CDC 作业中的“重量级”算子。
在很多生产环境中，它是导致作业反压（Backpressure）和 Checkpoint 超时的首要原因

## input 
source 是完整的 CDC 流，直接输出 `+I -U +U -D`。


## [lookup](lookup.md)

source 是不完整的 CDC 流, 缺少 `-U`，每次提交前，通过“维表 Join”的方式查询旧状态，生成 `-U` 和 `+U`。
lookup 相比 Flink Normalize Operator 有以下优势：
- 将状态管理从Flink转移到Paimon的存储层
- 使用内存+磁盘的二级缓存机制
- Paimon在存储层为主键构建了高效索引，支持O(log n)的查找性能
- 相比 Normalize 状态存储在单个节点，可能成为性能瓶颈，lookup 可以利用多个线程同时进行查找和变更日志生成


整体而言比 full 资源开销小

## full-compaction
在 full-compaction 模式下，通过全量合并（Compaction）对比新旧数据，生成 `-U` 和 `+U`。\
实时性要求不高的场景下使用\
相比 `lookupLevels.lookup()`查找历史值, 直接在合并时就可以读到历史记录，操作简单\


1. 如果 full-compaction 也是每次 checkpoint 都执行（delta-commits = 1）
    <details>

    下游看到的最终数据状态一样，changelog 流也基本一样。
    
    假设 key=1 连续更新 v1 → v2 → v3：
    
    | checkpoint | lookup 输出      | full-compaction 输出 |
      |------------|----------------|--------------------|
    | cp2        | -U(v1), +U(v2) | -U(v1), +U(v2)     |
    | cp3        | -U(v2), +U(v3) | -U(v2), +U(v3)     |
    </details>
2. lookup vs lookup-compaction 性能完全不同
    <details>

   lookup: 只 compact level 0 和少量高层文件，重写数据量小
     - LookupMergeTreeCompactRewriter
     - 需要 LookupLevels 做随机 lookup 查旧值
       - 可能是 level 0 → level 1
       - 也可能是 level 0 + level 1 → level 2
       - 甚至 level 0 + level 1 + level 2 → level 3
       - 如果 compaction 在 level 0 → level 1 之间，没有找到旧值，会去 level 2,3,4,5 查旧值。(LookupChangelogMergeFunctionWrapper#getResult)
       - 如果 compaction 在 level 0 → level 2 之间，没有找到旧值，会去 level 3,4,5 查旧值
     - 在一个 checkpoint 周期内，lookup 可能会因 minor compact 产出多次 changelog
   
   full-compaction：重写整个 bucket 的所有层级文件，IO 开销大得多
     - FullChangelogMergeTreeCompactRewriter#rewriteChangelog
       - 所有层级（0~4）的文件全部参与合并
       - 输出到 max level（比如 level 5）
       - 对比的是上次 full compaction 留在 max level 的快照 vs 本次新生成的 max level 快照
     - **#upgradeStrategy 为什么NO_REWRITE**？
       - 新增文件直接从 level 0 跳跃到 max level，只需要修改 metadata，不需要重新写文件，但是需要读一遍这个文件，产出它对应的 changelog（INSERT）
       - 这是一种性能优化，避免了对所有层级文件的重写
     - 在一个 checkpoint 周期内，只在最后 full compact 到 L5 时产出一次 changelog
       </details>
3. 如果 full-compaction 也是每次 checkpoint 都执行（delta-commits > 1）
    <details>
    delta-commits 是每 N 个 checkpoint 触发一次 full compaction的计数器，中间 checkpoint 的数据以中间层文件的形式存在，可读但无
      full-compaction changelog
    </details>
