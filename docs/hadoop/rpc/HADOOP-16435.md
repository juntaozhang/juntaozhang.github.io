# HADOOP-16435. RpcMetrics should not retained forever

## Commit
- Hash: 4662b13b264（2019-07-29）
- 文件: RpcDetailedMetrics.java, RpcMetrics.java, TestRpcMetrics.java

## Diff 机制分析
RPC server 关闭时 `RpcMetrics.shutdown()`/`RpcDetailedMetrics.shutdown()` 只置 shutdown 标志，未从 MetricsSystem 注销注册，metrics source 永远驻留；反复创建/销毁 RPC server（如 NN 多次 failover、测试、内部 minicluster、YARN 组件）会累积 MBean 与内存。patch 在 shutdown 时调用 unregister。

## 影响评估
中。长期运行的 NN（failover 演练频繁）或其他 daemon 内存在 MBean/指标累积，表现为 Metaspace/old gen 缓慢增长。小修复，建议 cherry-pick。

## apply 检查
clean
