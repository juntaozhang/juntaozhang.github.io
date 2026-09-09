# HADOOP-16677 — Recalculate remaining timeout millis correctly in SocketIOWithTimeout

## Commit

- **Hash**: `b4f39c2a8acbb3620fece0e141ae1a9ad1f4319a`
- **Date**: 2019-11-13（GitHub PR #1687）
- **涉及文件**: `hadoop-common/.../net/SocketIOWithTimeout.java`（+测试）

## Diff 机制分析

`SocketIOWithTimeout.select()` 循环里用入参 `timeout` 原地扣减，但中断检查
（isInterrupted 抛 InterruptedIOException）在扣减之前——中断异常消息里的
"millis timeout left" 是初始值而非剩余值；更关键的是 `timeout -= ...` 与
`if (timeout <= 0) return 0` 的顺序在 select 被假唤醒（返回 0）时存在路径
让循环用已经耗尽的 timeout 再 select 一次，理论上 select(<=0) 行为不确定。
修复：引入局部 `timeoutLeft` 正确扣减并 clamp 到 0，中断检查移到扣减之后，
`timeoutLeft==0` 才返回。

## 影响评估

**中** — 所有带超时的 socket IO（IPC 客户端、DataTransfer 等广泛复用）的
超时语义正确性：避免超时失真导致的多等/早退/中断信息错误。

## apply 检查结果

**clean**。
