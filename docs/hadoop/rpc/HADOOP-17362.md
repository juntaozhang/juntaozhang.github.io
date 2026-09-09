# HADOOP-17362 — Reduce RPC calls doing ls on HAR file

## Commit

- **Hash**: `118d8f5aa16`
- **日期**: 2020-11-13
- **涉及文件**: `hadoop-common/.../fs/HarFileSystem.java`（+38/-39）；测试 `TestHarFileSystemBasics`

## Diff 机制分析

`HarFileSystem`（hadoop archive）的 `listStatus`/目录遍历原本对 HAR 内每个目录项都发起一次底层 FS 的 RPC 查询 master index/索引文件，ls 一个 HAR 目录产生 O(条目数) 次 RPC。修复改为批量读取索引、在客户端本地解析目录项，把 ls 的 RPC 次数降为常数级。

## 影响评估

**低**。只在访问 HAR 归档时生效；对大量归档目录的集群 ls 延迟从秒级降到亚秒级。改动限于 HarFileSystem，不影响普通 HDFS 路径。

## apply 检查结果

**clean**。
