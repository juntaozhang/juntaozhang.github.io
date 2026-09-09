# HADOOP-17127 — Use RpcMetrics.TIMEUNIT to initialize rpc queueTime and processingTime

## Commit

- **Hash**: `0de48933c50`
- **日期**: 2020-07-15
- **涉及文件**: `hadoop-common/.../ipc/DecayRpcScheduler.java`、`RpcScheduler.java`；测试 `TestRPC`

## Diff 机制分析`

DecayRpcScheduler/FairCallQueue 计算 call 的 cost（queueTime + processingTime）时硬编码了毫秒假设，而 RpcMetrics 的 TIMEUNIT 可配置（`rpc.metrics.timeunit` 改微秒/纳秒后），导致 cost 数量级错误、调度优先级错乱。修复统一用 `RpcMetrics.TIMEUNIT` 换算。

## 影响评估

**低**。仅当显式把 rpc.metrics.timeunit 调成非毫秒值时才触发错误调度；默认配置下无影响。指标/调度一致性修复。

## apply 检查结果

**冲突**：`DecayRpcScheduler.java`、`RpcScheduler.java` patch does not apply（需人工合入）。
