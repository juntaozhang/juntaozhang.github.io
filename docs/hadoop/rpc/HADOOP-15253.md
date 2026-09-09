# HADOOP-15253 — Should update maxQueueSize when refresh call queue

## Commit

- **Hash**: `acfd764fcc9990e507c0e7cea746652375aaa632`（rel/release-3.1.1..rel/release-3.2.0 区间）
- **作者**: Konstantin V Shvachko（提交），补丁作者 Tao Jie
- **日期**: 2018-03-30
- **3.1.1 中的对应提交**: `4ce45a8ec7b`（已在 branch-3.1）
- **涉及文件**（2 个，+18/-2）:
  - `hadoop-common-project/hadoop-common/.../ipc/Server.java`（+3 行核心改动）
  - `hadoop-hdfs-project/hadoop-hdfs/.../TestRefreshCallQueue.java`（测试）

## Diff 机制分析

核心改动在 `org.apache.hadoop.ipc.Server.refreshCallQueue(Configuration conf)`（hadoop-common，所有 Hadoop RPC Server 共用，包括 NameNode/DataNode/ResourceManager 的 RPC server）：

```java
public synchronized void refreshCallQueue(Configuration conf) {
    String prefix = getQueueClassPrefix();
+   this.maxQueueSize = handlerCount * conf.getInt(
+       CommonConfigurationKeys.IPC_SERVER_HANDLER_QUEUE_SIZE_KEY,
+       CommonConfigurationKeys.IPC_SERVER_HANDLER_QUEUE_SIZE_DEFAULT);
    callQueue.swapQueue(getSchedulerClass(prefix, conf),
        getQueueClass(prefix, conf), maxQueueSize, prefix, conf);
}
```

- **改动前**：`refreshCallQueue` 在 swap 新 CallQueue 时复用 Server 启动时计算好的旧 `maxQueueSize`，即 `dfsadmin -refreshCallQueue` 修改 `ipc.server.handler.queue.size` 后新队列仍用旧容量。
- **改动后**：swap 之前按当前 conf 重新计算 `maxQueueSize = handlerCount × ipc.server.handler.queue.size`。
- 语义影响：切到 `FairCallQueue` 时（HADOOP-9640 引入，按优先级拆分队列），每个子队列长度为原长度的 1/priorityLevels。修复前若管理员期望 refresh 后按新值调整容量（例如扩容后调大队列），实际不会生效，导致队列比预期小——RPC 高峰期更容易出现 callQueue full 拒绝/阻塞。
- 测试 `TestRefreshCallQueue.testRefreshCallQueueWithFairCallQueue` 增加断言：refresh 前 queue size = DEFAULT×serviceHandlerCount，改 conf 为 150 后 refresh，断言变为 150×serviceHandlerCount。

## JIRA 页面信息

- **标题**: Should update maxQueueSize when refresh call queue
- **类型/优先级**: Improvement / **Minor**，Affects Version: 2.8.2
- **Fix Version/s**: 2.8.4, 2.9.1, 2.10.0, 3.0.3, **3.1.1**, 3.2.0
- **报告人/Assignee**: Tao Jie，2018-02-23 创建；patch 迭代 4 版（001–004）
- **性能数据**: 无 benchmark。
- **讨论要点**: 描述指出 FairCallQueue 各子队列长度 = 原长度 1/priorityLevels，refresh 时不更新 maxQueueSize 会导致容量配置不生效。属正确性/可运维性修复，不是性能优化 commit。

## 对 RPC 性能的影响评估

**低**。

- 正常运行期（不执行 refreshCallQueue）零影响；仅在管理员执行 `dfsadmin -refreshCallQueue` 调整队列类型的运维操作时才体现差异。
- 修复的是"容量配置不生效"这一正确性问题，间接避免 FairCallQueue 场景下队列过小导致的吞吐下降，但属于边界场景。Priority 为 Minor 也印证了这点。

## 是否值得 cherry-pick 到 3.1.1

**不需要**：已随 rel/release-3.1.1 发布（branch-3.1 上的 `4ce45a8ec7b`）。若需自行移植，冲突难度极低：主代码 3 行，触及 1 个主代码文件。
