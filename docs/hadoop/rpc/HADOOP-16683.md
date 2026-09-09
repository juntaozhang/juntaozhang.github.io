# HADOOP-16683 — Disable retry of FailoverOnNetworkExceptionRetry on wrapped AccessControlException

## Commit

- **Hash**: `3a09593a73c1c919bebf344bd4a2afdddcbb85c9`
- **Date**: 2020-01-14
- **涉及文件**: `hadoop-common/.../io/retry/RetryPolicies.java`（+测试）

## Diff 机制分析

`FailoverOnNetworkExceptionRetry` 只对顶层 `AccessControlException` 判 FAIL；
当 ACE 被包装在 RemoteException/InvocationTargetException 等异常链里时（例如
经过 HA proxy 包装），误判为可重试，客户端对两个 NN 反复重试直至次数耗尽，
把权限错误放大成多次无用 RPC 和 failover 尝试。修复：新增
`hasWrappedAccessControlException()` 沿 cause 链查找 ACE。

## 影响评估

**低** — HA 客户端重试正确性：仅权限被拒场景，减少无谓重试与延迟，报错更快更准。

## apply 检查结果

**冲突**：`RetryPolicies.java`、`UnreliableInterface.java`（3.1.1 的 shouldRetry
上下文不同，需手工合并）。
