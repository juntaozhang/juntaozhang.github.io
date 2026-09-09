# HADOOP-16192 — CallQueue backoff 两个 bug

## Commit

- **Hash**: `d4fbbc83ad8`（branch-3.1，2019-03-14，Erik Krogen）
- **涉及文件**（4 个，+28/-4）：`CallQueueManager.java`、`Server.java` + 2 测试文件

## Diff 机制分析

RPC server 的客户端 backoff 机制（`ipc.<port>.backoff.enable`，与 FairCallQueue 配合，拥塞时让慢客户端退避）有两个 bug：

1. **`add()` 路径不检查 backoff**：`CallQueueManager.put()` 检查了 backoff，但 `add()`（`BlockingQueue.add` 语义入口，也被 `putRef` 切换后的队列使用）完全没做 backoff 检查，客户端请求从 add 路径进来时退避机制形同虚设。
2. **refresh 后 backoff 开关丢失**：`Server.refreshCallQueue()`（`dfsadmin -refreshCallQueue` 动态生效）重建队列后没有重新设置 `clientBackoffEnabled`，backoff 被静默关闭。

修复：`add()` 委托到新增的 `addInternal(e, checkBackoff=true)`（put 路径传 false 避免重复检查）；`refreshCallQueue` 末尾补 `callQueue.setClientBackoffEnabled(...)`。

代码路径：所有开启 backoff 的 RPC server 端（NN/DataNode/RM）的 Listener 线程入队 + 动态刷新调用队列。

## 影响评估

**评级：中**。修复 ipc 框架拥塞控制功能的实际生效问题；对启用 FCQ+backoff 的大集群，影响拥塞时的 RPC 隔离与尾延迟。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
