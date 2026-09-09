# HADOOP-16700 — Track response time separately in Server.Call to avoid negative RpcQueueTime

## Commit

- **Hash**: `23f25418e822f90aa6bd0daa0ba65ab548b05eb1`
- **Date**: 2019-11-20
- **涉及文件**: `hadoop-common/.../ipc/Server.java`

## Diff 机制分析

`Server.Call.timestampNanos` 一个字段两用：入队时是"接收时间"，异步响应
（handler 处理完交给 Responder）时又被覆写为"响应开始发送时间"。后果：
- `doPurge` 用 `timestampNanos + PURGE_INTERVAL` 判废连接，比较基准被污染；
- 队列时间指标计算出现负值/异常（RpcQueueTime 可能为负）。
修复：拆成 `timestampNanos`（接收，不再变）与 `responseTimestampNanos`
（异步发送时更新），doPurge 与指标各用其位。

## 影响评估

**低** — IPC 框架指标与连接清理正确性：修复负 queue time 指标及 purge 判断
基准污染（极端情况下影响半关闭连接的及时清理）。

## apply 检查结果

**冲突**：`Server.java`（Call 字段区上下文与 3.1.1 有差异，需手工合并）。
