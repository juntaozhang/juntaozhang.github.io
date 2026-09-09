# HADOOP-15865. ConcurrentModificationException in Configuration.overlay() method

## Commit
- Hash: 72ef7528763（2019-08-01）
- 文件: hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java

## Diff 机制分析
`Configuration.overlay()` 遍历 overlay 配置时直接操作被迭代的集合，patch 改为对要修改的集合做拷贝或在迭代期间避免结构性修改（+4/-2）。任何客户端/服务端组件热加载配置时都可能触发。

## 影响评估
中。Configuration 是所有 Hadoop 进程的基础类，CMF/热加载 overlay 并发场景下可稳定抛 CME 导致配置加载失败或任务初始化失败。单点小补丁，建议 cherry-pick。

## apply 检查
clean
