# HADOOP-15611 — Log more details for FairCallQueue

## Commit

- **Hash**: 9089790cabb4771198be0fe64c1317a3ff1c80f1
- **Author**: Yiqun Lin（补丁贡献者：Ryan Wu）
- **Date**: 2018-07-26
- **涉及文件**（2 个，+11/-0，纯新增日志）:
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/DecayRpcScheduler.java`
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/WeightedRoundRobinMultiplexer.java`

## Diff 机制分析

纯 debug 级别日志增强，不改任何逻辑：

1. **`DecayRpcScheduler.decayCurrentCounts()`**（定时衰减用户调用计数的核心方法，决定 FairCallQueue 的调度权重）新增 4 条 `LOG.debug`:
   - 进入方法时："Start to decay current counts."
   - 逐用户衰减时：打出 user、decayedCount、rawCount；
   - 某用户 decayedCount 归零被清理时：打出该 user；
   - 完成后：打出 totalDecayedCount、totalRawCallCount。
2. **`WeightedRoundRobinMultiplexer.moveToNextQueue()`**（按权重轮询选择子队列）新增 1 条 `LOG.debug`：打出从哪个 queue index 移到哪个 index、当前队列剩余请求数（requestsLeft，即权重）。

机制价值：FairCallQueue 的公平性完全由这两个内部状态机决定——衰减结果决定各用户权重，multiplexer 决定请求落在哪个优先级子队列。此前这两处完全没有日志，用户开启 FairCallQueue 后无法验证"用户分级是否符合预期""权重是否按配置生效"。

## JIRA 页面信息

- **标题**: Log more details for FairCallQueue
- **类型/状态**: Improvement，Resolved/Fixed（2018-07-26），Hadoop Flags: Reviewed
- **Affects Version/s**: 3.1.0
- **Fix Version/s**: 3.2.0
- **Priority**: Minor
- **Reporter / Assignee**: Ryan Wu
- **报告人描述要点**: "使用 FairCallQueue 时发现缺少关键日志，打印的日志很少，导致难以学习和调试该特性。"建议至少在 `DecayRpcScheduler#decayCurrentCounts` 和 `WeightedRoundRobinMultiplexer#moveToNextQueue` 加日志——与最终补丁落点完全一致。
- **Benchmark/性能数据**: 无。
- **讨论**: 页面无可见评论；Ryan Wu 在 2018-07-20 至 07-26 间迭代上传 4 版补丁（.001–.004）。

## 对 RPC 调优/排障的价值评估：**中**

理由：

- FairCallQueue（配合 DecayRpcScheduler）是 Hadoop RPC 最重要的 QoS/隔离开关，但历来"黑盒"——调度不公、用户被错分级这类问题此前只能靠 JMX 指标间接推断。这些 debug 日志是验证 `callqueue.<ns>.schedule.weights` 等权重配置是否生效的直接手段。
- 评级不到"高"的原因：(1) 全部是 `LOG.debug`，生产默认级别（INFO）下不可见，需要显式开 debug 才有效；(2) `decayCurrentCounts` 默认每秒执行一次，逐用户打日志在高负载+多用户场景下量不小，开 debug 本身有开销，仅适合排障期临时开启；(3) 不改任何行为，只是可观测性。

## 是否值得 cherry-pick 到 3.1.1：**值得，冲突难度极低**

- `git apply --check` 实测对 rel/release-3.1.1 **干净通过**（2 个文件）。
- 纯新增 debug 日志、零逻辑改动，回归风险几乎为零。
- 适合与 HADOOP-15742 一起作为"FairCallQueue 可观测性"组合补丁引入 3.1.1。
