# HADOOP-16341. ShutDownHookManager: Regressed performance on Hook removals after HADOOP-15679

## Commit
- Hash: 2c4d8c343ea（2019-07-17）
- 文件: ShutdownHookManager.java, TestShutdownHookManager.java

## Diff 机制分析
HADOOP-15679 后 hooks 存储结构改为按优先级链表/数组，remove 操作变成 O(n) 扫描且 shutdown 阶段倒序执行删除时退化为 O(n²)。NN/DN/客户端 shutdown 时注册 hook 多的进程（YARN AM、Spark driver 更明显）关闭明显变慢。patch 调整 remove 实现（+21/-9）。

## 影响评估
低。只影响进程退出耗时，但滚动重启频繁的大集群会累积可观的停机时间。修复无行为变化，冲突在 ShutdownHookManager.java，需手工合并。

## apply 检查
conflict: ShutdownHookManager.java
