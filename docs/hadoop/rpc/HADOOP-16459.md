# HADOOP-16459. Add more fine-grained processing time metrics to the RPC layer（HADOOP-16266 backport）

## Commit
- Hash: 7b8f08f59e5（2019-04-29）
- 文件: ipc/Server.java（+152）, RpcMetrics.java, RpcScheduler.java, ProcessingDetails.java（新增）, ProtobufRpcEngine.java, WritableRpcEngine.java, DecayRpcScheduler.java 等（17 个文件）

## Diff 机制分析
引入 `ProcessingDetails` 把一次 RPC 的处理时间拆成 queueTime、processingTime、responseTime、lockWaitTime、lockSharedHoldTime、lockExclusiveHoldTime 等阶段，Server 在各阶段埋点并经 RpcMetrics 暴露。同时把响应序列化从 handler 计时中分离。KNOWN 清单中 HADOOP-16531（slow RPC 详细计时日志）依赖本提交的 ProcessingDetails 基础设施。

## 影响评估
中。本身是 metrics 增强（每 RPC 增加几个 System.nanoTime 调用，纳秒级开销），但它为 RPC 慢请求诊断提供唯一的数据源，且是已确认要 cherry-pick 的 HADOOP-16531 的前置依赖。冲突在 ipc/Server.java，需与 HDFS-14146 的 doResponse 改动协调，建议与 HADOOP-16531 一起移植。

## apply 检查
conflict: hadoop-common ipc/Server.java
