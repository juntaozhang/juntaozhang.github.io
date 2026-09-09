# HADOOP-17358 — Improve excessive reloading of Configurations

## Commit

- **Hash**: `487eaa6dea1`
- **日期**: 2020-11-11
- **涉及文件**: `hadoop-common/.../conf/Configuration.java`（+34/-8）；测试 `TestConfigurationSubclass`

## Diff 机制分析

`Configuration` 子类（如 `HdfsConfiguration`/`YarnConfiguration`）静态初始化期间会重复 loadResources 多次（每次 new 都触发全量 default 资源重载），应用启动阶段产生大量重复解析与临时对象。修复在 `Configuration` 加载路径上加缓存/去重，同一类加载器内只加载一次静态默认资源。

## 影响评估

**低**。收益集中在进程启动阶段（NN/DN/客户端启动更快、临时内存更少），对稳态 RPC 性能无影响；但对短生命周期客户端（CLI、container 任务进程）有普遍收益。

## apply 检查结果

**clean**。
