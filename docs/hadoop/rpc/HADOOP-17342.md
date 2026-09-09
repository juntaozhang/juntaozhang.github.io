# HADOOP-17342 — Creating a token identifier should not do kerberos name resolution

## Commit

- **Hash**: `4cec4bebd9e`
- **日期**: 2020-11-05
- **涉及文件**: `hadoop-common/.../security/token/delegation/AbstractDelegationTokenIdentifier.java`（+1/-1）

## Diff 机制分析

`AbstractDelegationTokenIdentifier` 构造时调用的 toString/equals 路径会触发 `KerberosName` 解析（DNS/kerberos realm 查找）。token 的创建、序列化、在 NN 认证热路径（getBlockLocations 之前的身份校验）都会 new identifier。一行修复：把会触发 kerberos 名称解析的调用换成纯字符串形式。

## 影响评估

**中**。delegation token 校验在每次新连接的 RPC 认证路径上；kerberos 名称解析涉及 DNS 查询，慢且可超时（曾造成 NN 认证阶段偶发长尾）。一行改动、风险极低、收益直接落在 RPC 认证热路径上。

## apply 检查结果

**clean**。
