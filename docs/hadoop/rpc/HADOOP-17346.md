# HADOOP-17346 — Fair call queue is defeated by abusive service principals

## Commit

- **Hash**: `c7845dc574c`
- **日期**: 2020-11-23
- **涉及文件**: `CallQueueManager.java`、`DecayRpcScheduler.java`、`RPC.java`、`Server.java`、`UserIdentityProvider.java`、`SecurityUtil.java`、`ServiceAuthorizationManager.java`（+172/-30）

## Diff 机制分析

FairCallQueue + DecayRpcScheduler 的公平性以"用户 principal"为身份维度；但服务间调用（如 RM/NM、HiveServer 代表提交）在 ServiceAuthorizationManager 层完成鉴权后，调用者实际身份是服务 principal，scheduler 无法区分终端用户，单一服务账号的高负载会打爆整个 call queue（"defeated by abusive service principals"）。修复引入按 UGI 静态优先级的机制：`DecayRpcScheduler` 增加 `staticPriorities` 映射和 `getPriorityLevel(UGI)/setPriorityLevel(UGI,int)`，`CallQueueManager`/`RPC`/`Server` 暴露相应入口，`SecurityUtil`/`ServiceAuthorizationManager` 在鉴权路径上把真实终端用户身份传递进来，使服务账号下的调用可按终端用户分摊成本。

## 影响评估

**中**。对启用 FairCallQueue 的 NN（大共享集群常见）是关键公平性修复：没有它，某个服务账号的风暴会挤垮所有其它租户；同时提供按用户静态优先级的能力。改动面较大，合入需充分回归 RPC 相关测试。

## apply 检查结果

**冲突**：`DecayRpcScheduler.java`、`Server.java` 等 patch does not apply（与 3.1.1 的 DecayRpcScheduler 实现漂移，需人工合入）。
