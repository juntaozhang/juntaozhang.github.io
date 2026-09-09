# HADOOP-16345 — FairCallQueue metrics 实例化潜在 NPE

## Commit

- **Hash**: `cf3c1b9ffc8`（branch-3.1，2019-06-07，Erik Krogen）
- **涉及文件**（1 个，+16/-2）：`FairCallQueue.java`（hadoop-common ipc）

## Diff 机制分析

`FairCallQueue.MetricsProxy` 通过静态 `getInstance(namespace)` 懒加载创建。`delegate` 是 `WeakReference<FairCallQueue>`，**只在 `setDelegate()` 被调用后才非 null**。JMX/metrics 线程可能在 `setDelegate` 之前（或 delegate 被 GC 回收后）调用 `getQueueSizes()` / `getOverflowedCalls()`，原代码直接 `this.delegate.get()` → NPE。

修复：抽取 `getCallQueue()`，先判 `ref == null` 返回 null（后续已有 null 检查返回空数组），消除 NPE。

代码路径：任何启用 FairCallQueue 的 RPC server（NN/DataNode/KMS 等，配置 `ipc.<port>.callqueue.impl=org.apache.hadoop.ipc.FairCallQueue`）启动早期 + JMX 轮询线程。

## 影响评估

**评级：低-中**。仅在使用 FairCallQueue（大集群 RPC 隔离常用）时存在启动窗口 NPE；修复消除 ipc 框架在调度策略下的启动竞态。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
