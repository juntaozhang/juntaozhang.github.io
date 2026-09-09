# HADOOP-16580 — Disable retry of FailoverOnNetworkExceptionRetry on AccessControlException

## Commit

- **Hash**: `f8c06f5a87d5dbddc57d2b49c4178f06088c60c5`
- **Date**: 2019-10-16
- **涉及文件**: `hadoop-common/.../io/retry/RetryPolicies.java`（+测试）

## Diff 机制分析

`FailoverOnNetworkExceptionRetry.shouldRetry` 的异常分派没有 AccessControlException
分支，权限错误被 SocketException/通用 IOException 分支兜住，按"网络异常可重试"
处理——HA 客户端对 standby 反复 failover 重试直至 maxRetries，权限问题被掩盖
且每次都要等 failover 睡眠，报错延迟被放大到分钟级。
修复：显式 `e instanceof AccessControlException → FAIL("Access denied")`。
（HADOOP-16683 是其后续：处理被包装在异常链里的 ACE。）

## 影响评估

**中** — HA 客户端重试正确性：无权限访问在 HA 集群上表现为长时间挂起后
报笼统错误，修复后即刻准确失败；影响所有走 HA proxy 的服务。

## apply 检查结果

**clean**。
