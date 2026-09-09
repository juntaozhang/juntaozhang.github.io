# HADOOP-16161. NetworkTopology#getWeightUsingNetworkLocation return unexpected result

## Commit
- Hash: f1fe3abac9c（2019-05-13）
- 文件: hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/net/NetworkTopology.java, TestDatanodeManager.java, TestNetworkTopology.java

## Diff 机制分析
`getWeightUsingNetworkLocation` 计算节点权重时未正确排除目标节点自身/空值分支，导致返回意外结果。patch 修正权重计算逻辑，并补充单测覆盖原来出错的分支（含存储类型相关的 chooseRandomWithStorageType 权重比较）。

## 影响评估
中。该方法是 DN 下线/维护（maintenance）状态下块放置权重计算的路径，错误结果会导致 NN 在选副本目标时做出错误决策，可能造成放置不均或无法满足 storage policy 的放置。影响块放置正确性而非崩溃，建议 cherry-pick。

## apply 检查
clean
