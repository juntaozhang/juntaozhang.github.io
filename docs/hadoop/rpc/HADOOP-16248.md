# HADOOP-16248 — MutableQuantiles 高负载下内存泄漏

## Commit

- **Hash**: `55cc35c0e14`（branch-3.1，2019-05-17，Alexis Daboville）
- **涉及文件**（1 个，+1/-1）：`MutableQuantiles.java`（hadoop-common metrics2）

## Diff 机制分析

`MutableQuantiles`（RpcQueueTime/RpcProcessingTime 等分位数指标的采样器）用共享 scheduler 每 `interval` 秒跑一次 `RolloverSample` 任务把采样快照交给 `SampleQuantiles`。

原实现用 `scheduleAtFixedRate`：**固定速率调度**。如果某次 rollover 任务执行耗时超过 interval（NN 高负载时 `estimator.quantiles()` 计算 + 监听器回调可能很慢），下一次执行会"补跑"立即触发，形成恶性循环——任务越积压调度越密集，采样队列无限增长 → heap 膨胀（生产观察到 NN 因之 OOM 的案例）。

修复：一行改动改为 `scheduleWithFixedDelay`——每次执行完才重新计 delay，慢一次不会引发追赶式连续执行。

代码路径：NN/DN/RM 每个启用 quantile metrics 的 MetricsSystem → `MutableQuantiles` 定时 rollover（服务端进程全局共享一个 scheduler）。

## 影响评估

**评级：中**。高负载 NN 上由 metrics 调度引发的内存增长/OOM，属于服务端全局内存/GC 热路径修复；一行改动、风险极低。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
