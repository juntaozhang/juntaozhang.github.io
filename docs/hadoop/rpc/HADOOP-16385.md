# HADOOP-16385. Namenode crashes with 'RedundancyMonitor thread received Runtime exception'

## Commit
- Hash: 3ab475377a9（2019-07-02）
- 文件: NetworkTopology.java（+5/-4）

## Diff 机制分析
RedundancyMonitor 线程在块重建/选择副本目标时调用 NetworkTopology，`getNode`/`sortByDistance` 路径上对空父节点/未注册节点解引用抛 RuntimeException，而 RedundancyMonitor 的未捕获异常处理是终止线程（"received Runtime exception" 后整个监控线程退出）。patch 修 NetworkTopology 的空值防护。

## 影响评估
高。RedundancyMonitor 线程死后 NN 失去块副本补足能力，集群进入慢性数据不安全状态且需重启 NN 恢复。一行级防护修 NN 崩溃级后果，必须 cherry-pick。

## apply 检查
clean
