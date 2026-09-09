# HADOOP-17068 — Client fails forever when namenode ipaddr changed

## Commit

- **Hash**: `1fe15113cf4`
- **日期**: 2020-06-23
- **涉及文件**: `hadoop-common/.../ipc/Client.java`（+11/-2）

## Diff 机制分析

IPC Client 建立连接时对 `UnresolvedAddressException`（NN 换 IP / 域名解析失败/变更）按普通异常直接标记连接失败并不再重试，导致客户端永久失败——即使 DNS 已恢复。修复把 UnresolvedAddressException 纳入可重试范畴，配合现有 retry 机制在地址重新可解析后恢复。

## 影响评估

**高**。运维变更（NN 换机/换 IP、DNS 迁移）后全体客户端永久挂死是重大可用性事故；改动小、只放宽重试条件，风险低。所有依赖 IPC 客户端的组件（HDFS/YARN 客户端、DN、ZKFC）均受益。

## apply 检查结果

**clean**。
