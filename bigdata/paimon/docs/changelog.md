
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


## lookup
source 是不完整的 CDC 流, 缺少 `-U`，每次提交前，通过“维表 Join”的方式查询旧状态，生成 `-U` 和 `+U`。
lookup 相比 Flink Normalize Operator 有以下优势：
- 将状态管理从Flink转移到Paimon的存储层
- 使用内存+磁盘的二级缓存机制
- Paimon在存储层为主键构建了高效索引，支持O(log n)的查找性能
- 相比 Normalize 状态存储在单个节点，可能成为性能瓶颈，lookup 可以利用多个线程同时进行查找和变更日志生成

## full-compaction
在 full-compaction 模式下，通过全量合并（Compaction）对比新旧数据，生成 `-U` 和 `+U`。
实时性要求不高的场景下使用
性能开销少
