# HADOOP-16208 — IPC Client 不应记录 InterruptedException

## Commit

- **Hash**: `41f1bc20566`（branch-3.1，2019-04-04，David Mollitor）
- **涉及文件**（1 个，+5/-3）：`Client.java`（hadoop-common ipc）

## Diff 机制分析

`Client.call()` 发送 RPC 请求时，若线程在 `sendRpcRequest` 等待连接可写时被中断，原代码 `LOG.warn("interrupted waiting to send rpc request to server", e)` 每次中断都打一条 warn 且带完整堆栈。中断是客户端正常生命周期的一部分（关闭、取消、超时线程打断），高并发客户端（NM 上数百个 container、HBase RS）在集群抖动时会产生海量 warn 日志，淹没日志并增加 I/O。

修复：不再打日志，改为构造 `InterruptedIOException`（带 cause）直接抛出，保留中断标记。

代码路径：所有 ipc Client.call 发送路径（全集群客户端共享）。

## 影响评估

**评级：低**。纯日志降噪 + 更规范的异常类型；无性能/正确性变化，但改善故障定位时的日志可用性。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
