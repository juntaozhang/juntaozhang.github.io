# HADOOP-15864 — SBN 域名无法解析导致 Job submitter/executor 失败

## Commit

- **Hash**: `ef9f8ca13d8`（branch-3.1，2018-10-25，He Xiaoqiao）
- **涉及文件**（2 个，+28/-4）：`SecurityUtil.java`、测试

## Diff 机制分析

HA 集群里，客户端持有逻辑 URI 的 delegation token；`SecurityUtil.setTokenService`/`buildTokenService` 负责把 `InetSocketAddress` 编码成 token service 名。当配置了 `hadoop.security.token.service.use_ip`（用 IP 做 service 名）而**standby NN 的域名暂时无法解析**时（DNS 抖动、只配了 active），`buildTokenService` 直接 `throw IllegalArgumentException(UnknownHostException)`——这个异常发生在 token 获取/设置的通用路径上，导致作业提交器/执行器整体失败，即使客户端本来只需要连 active。

修复：`buildTokenService` 遇到 unresolved 地址时 warn 并返回 null；`setTokenService` 判 `service != null` 才设置，token 保留原 service，后续按原逻辑解析。

代码路径：客户端（MR AM、JobSubmitter、任何 DFSClient）获取/复制 delegation token → `SecurityUtil.buildTokenService`（HA 下 cloneDelegationTokenForLogicalUri 会遍历所有 NN 地址）。

## 影响评估

**评级：低**。修复 HA + use_ip 配置下 SBN DNS 解析抖动引起客户端任务失败的边角场景；改动小、行为更宽容。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
