# HADOOP-16013 — DecayRpcScheduler decay thread should run as a daemon

## Commit

- **Hash**: `7b4f44573f62bacc88392363b57a11f5f31ec382`
- **Date**: 2019-01-11
- **涉及文件**: `hadoop-common/.../ipc/DecayRpcScheduler.java`

## Diff 机制分析

DecayRpcScheduler 的 decay Timer 用 `new Timer()`（非 daemon）。启用
DecayRpcScheduler（如 NN 用公平调度权重）时，该 timer 阻止 JVM 退出：
`hadoop dfsadmin`/CLI 等短进程执行完业务后挂住不退，只能 kill。
修复：`new Timer(true)` 设为 daemon。

## 影响评估

**低** — 使用 DecayRpcScheduler 的进程退出挂起问题，一行修复。

## apply 检查结果

**clean**。
