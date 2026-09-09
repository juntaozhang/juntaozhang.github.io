# HADOOP-16705 — MBeanInfoBuilder puts unnecessary memory pressure with a debug log

## Commit

- **Hash**: `50d98a6568c6662d2757cf73cb9e066b4f808c75`
- **Date**: 2019-11-14
- **涉及文件**: `hadoop-common/.../metrics2/impl/MBeanInfoBuilder.java`

## Diff 机制分析

`MBeanInfoBuilder.get()` 每次构建 MBeanInfo 都执行 `attrs.toString()`，
而该行仅作为 debug 日志参数——即使 debug 关闭，toString() 也已求值，
对包含大量属性的 metrics source（NN 上动辄数千指标）每次注册/刷新都产生
大字符串垃圾，加重 GC。修复：改用参数化日志 `LOG.debug("{}", attrs)`，
debug 关闭时零开销。

## 影响评估

**低** — metrics 热路径上的内存/GC 微优化，一行改动，风险为零。

## apply 检查结果

**clean**。
