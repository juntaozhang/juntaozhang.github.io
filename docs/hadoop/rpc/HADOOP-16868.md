# HADOOP-16868 — ipc.Server readAndProcess threw NullPointerException

## Commit

- **Hash**: `b28347d282474ec1bd6b978b290873d05d1bd320`
- **Date**: 2020-02-18
- **涉及文件**: `hadoop-common/.../ipc/Server.java`

## Diff 机制分析

`Server.Connection` 复用同一个 4 字节 `dataLengthBuffer` 读取每个 RPC 帧长度。
`close()` 里把它置 null（本意是释放引用），但连接对象可能随后被 Reader 线程再次
用于 `readAndProcess`（如半关闭竞态、未从 selector 及时摘除），对 null buffer
做 `put()` 即 NPE，Reader 线程抛异常并可能拖垮该 Reader 上所有连接的处理。
修复：`dataLengthBuffer` 改 `final`，`close()` 不再置 null（4 字节缓冲无释放必要）。

## 影响评估

**中** — IPC 框架 Reader 线程稳定性：竞态窗口虽窄，但命中时 Reader 崩溃会影响
该 Reader 承载的全部连接，属于典型的"小改动防大故障"。

## apply 检查结果

**clean**。
