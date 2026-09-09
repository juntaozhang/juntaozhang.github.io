# HADOOP-16531 — Log more timing information for slow RPCs

## Commit

- **Hash**: `5e56914650a3ecbc39c3d4c19a59eb52f78fba55`（branch-3.1，cherry-picked from trunk `a23417533e1e`，首次发布于 3.1.4）
- **Author**: Erik Krogen（代码贡献者 Chen Zhang）
- **Date**: 2019-09-06
- **涉及文件**:
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Server.java`（+11/-5，唯一改动文件）

## Diff 机制分析

改动非常小，只涉及 slow RPC 的日志输出，**不改变任何检测逻辑**：

1. `logSlowRpcCalls` 方法签名从 `(String methodName, Call call, long processingTime)`
   改为 `(String methodName, Call call, ProcessingDetails details)`；processingTime 改为
   在方法体内通过 `details.get(Timing.PROCESSING, RpcMetrics.TIMEUNIT)` 取出。
2. 警告日志从
   `Slow RPC : {} took {} {} to process from client {}`
   扩展为追加 `the processing detail is {}`，即输出整个 `ProcessingDetails.toString()`。
3. 调用点 `updateMetrics` 处把原来的 `processingTime` 实参换成 `details`。
4. 检测阈值逻辑原样保留：3-sigma 规则（processing mean + 3 × stddev）、
   minSampleSize=1024、`incrSlowRpc()` 计数均不变。

`ProcessingDetails` 是 HADOOP-16459（HADOOP-16266 的 branch-3.1 backport，
`7b8f08f59e5`，19 个文件、约 500 行）引入的，按 `Timing` 枚举细分各阶段耗时
（队列等待、锁等待、读写锁持有、实际处理、响应等），
本 JIRA 只是把这些已有的细分计时打印到 slow RPC 日志里。

**与 SlowRpcBehavior 的关系**：`SlowRpcBehavior`（调用队列层面的慢 RPC 行为分析类）
在 3.1.1 中**不存在**（全树 grep 无结果，是 3.2 才引入的特性），因此不存在兼容性
问题；本改动只作用于 `logSlowRPC=true` 时的 warn 日志路径，与调用队列插入行为
（FairCallQueue / DecayRpcScheduler 等）完全正交。3.1.1 中 slow RPC 日志通过
`Server.setLogSlowRPC()` 开关控制（测试接口），生产上默认关。

## JIRA 页面信息

- **标题**: Log more detail for slow RPC
- **类型**: Improvement；**Status**: Resolved
- **Affects Version/s**: 未填写
- **Fix Version/s**: 3.3.0, 3.2.2, **3.1.4**
- **Priority**: Major
- **报告人/Assignee**: Chen Zhang
- **动机**：报告人原话——现有 slow RPC 日志只有 processing time，
  "We need to log more details to help us locate the problem
  (eg. how long it take to request lock, holding lock, or other things)"，
  即无法区分慢在等锁还是慢在处理。
- **关联**：本 JIRA 被标记为 HADOOP-16310（要求 slow RPC 日志带上请求参数）的
  duplicate；页面无重要评论，附件仅一个 2KB 的 .001.patch。

## 影响评估

**中**。

- 纯可观测性增强，不改变 RPC 服务端任何行为/性能路径（仅多一次 toString 拼接，
  且只在已判定为 slow 的 RPC 上触发）。
- 对 NN RPC 排障价值高：NN 的慢调用往往慢在 FSNamesystem 锁竞争，
  细分计时（lock wait / lock held / processing）能把"慢 RPC"定位从猜测变为数据。
- 不影响稳定性；不开 slow RPC 日志的用户完全无感知，因此单项影响面有限。

## 是否值得 cherry-pick 到 3.1.1

**值得但成本偏高（依赖前置补丁）**。

- 经 `git apply -3` 在 rel/release-3.1.1 上实测：**冲突**（Server.java）。
  根因是 3.1.1 的 `logSlowRpcCalls` 还是旧签名 `(String methodName, int processingTime)`，
  还没有 `Call` 参数和 `ProcessingDetails`，本补丁所依赖的
  HADOOP-16459（细粒度 RPC 计时，`7b8f08f59e5`）不在 3.1.1 中。
- 冲突难度：高（直接 pick）——需先 backport HADOOP-16459（19 文件、约 500 行，
  含 FSNamesystemLock、RpcMetrics、两个 RpcEngine 及协议 proto 改动）；
  若只做最小手工移植（在低版本 Server.java 里把 queue time 等已有计时拼进
  warn 日志），难度中等，但拿不到 lock wait/hold 细分数据，价值大打折扣。
- 建议：若集群已饱受 NN 慢 RPC 困扰且计划停留 3.1.1 较长时间，连同
  HADOOP-16459 一起 pick（顺带获得 p50/p75/p90/p95/p99 processing time 指标）；
  否则可缓。
