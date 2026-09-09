# HADOOP-16461. Regression: FileSystem cache lock parses XML within the lock

## Commit
- Hash: b9dc2c17de5（2019-07-26）
- 文件: hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/fs/FileSystem.java

## Diff 机制分析
FileSystem CACHE 的 `getInternal` 在持全局 CACHE 锁的情况下调用 `createFileSystem`，其中会解析 `core-default.xml` 相关默认值（DefaultMetricsSystem/initialize 路径）。patch 把该初始化移出临界区（+6/-1），避免在全局锁内做 XML 解析。

## 影响评估
中。这是一个回归：第一次并发创建 FileSystem 实例时，所有线程在 CACHE 锁上排队，锁内还做 XML 解析，可造成秒级卡顿（客户端任务启动、NN/DN 内部创建 FS 均受影响）。修复后消除该锁竞争尖峰，建议 cherry-pick。

## apply 检查
clean
