# HADOOP-15340 — Provide meaningful RPC server name for RpcMetrics

## Commit

- **Hash**: 8ab776d61e569c12ec62024415ff68e5d3b10141
- **Author**: Xiaoyu Yao（补丁贡献者：Elek Marton）
- **Date**: 2018-04-10
- **涉及文件**（6 个，+117/-12）:
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/RPC.java`
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Server.java`
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/ProtobufRpcEngine.java`
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/WritableRpcEngine.java`
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/metrics/RpcMetrics.java`
  - `hadoop-common-project/hadoop-common/src/test/java/org/apache/hadoop/ipc/TestRPC.java`

## Diff 机制分析

背景：一个 JVM（如 NameNode）里通常有多个 RPC server，但 `RpcMetrics` 只按端口区分（registry 名 `RpcActivityForPort<port>`），可观测性差；`Server` 构造函数其实接受 `serverName` 参数但从未保存使用。

改动内容：

1. **`Server.java`**: 新增 `private final String serverName` 字段并在构造函数中赋值；新增 `getServerName()` getter。不改构造函数签名，完全向后兼容。
2. **`RPC.java`**: 把旧的 `classNameBase(String className)` 替换为 `serverNameFromClass(Class<?> clazz)`。旧实现只取类全名的最后一段，对 protobuf 生成的内部类/匿名类会得到类似 `TestRPC$10` 的无意义名字。新实现用正则 `COMPLEX_SERVER_NAME_PATTERN`（`(?:[^\$]*\$)*([A-Za-z][^\$]+)(?:\$\d+)?`）提取有意义的类名：普通类取 simple name；内部类取内部类名；工厂方法/匿名类（`...$TestClass$2`、`...$10`）回溯到父类名。
3. **`ProtobufRpcEngine` / `WritableRpcEngine`**: 两处 `Server` 子类构造函数由 `classNameBase(protocolImpl.getClass().getName())` 改为 `serverNameFromClass(protocolImpl.getClass())`。
4. **`RpcMetrics.java`**: MetricsRegistry 上新增 tag `serverName`（"Name of the RPC server"），值为 `server.getServerName()`；并新增 `@VisibleForTesting` 的 `getTag(String)` 便于测试断言。
5. **测试**: `TestRPC.testServerNameFromClass` 覆盖 4 种命名场景；`testCallsInternal` 断言 metrics 的 `port` 和 `serverName`（`TestProtobufRpcProto`）tag。

本质：不是性能优化，而是**可观测性修复**——让多 RPC server 的 metrics 可以按 serverName 维度区分/过滤。

## JIRA 页面信息

- **标题**: Provide meaningful RPC server name for RpcMetrics
- **类型/状态**: Bug，Resolved/Fixed
- **Affects Version/s**: 3.2.0
- **Fix Version/s**: 3.2.0
- **Priority**: Major
- **Reporter / Assignee**: Marton Elek
- **报告人描述要点**: 同一 JVM 中多个 RPC server 的 metrics 无法区分，唯一标识是端口号；`Server` 构造函数虽有 serverName 参数但未被使用。补丁方案：保存 serverName 字段（不改构造签名）、RpcMetrics 增加 serverName tag、改进无名字时的回退命名（旧逻辑对 protobuf 生成的内部类会得到"脏"名字甚至匿名内部类名）。
- **Benchmark/性能数据**: 无。
- **讨论**: 页面无可见评论；2018-03-23 至 04-09 间上传了 3 版补丁（.001–.003），显示命名规则经过迭代打磨。
- **兼容性**: 描述明确指出改动兼容——因为旧代码里这个名字"完全没被用过"。

## 对 RPC 调优/排障的价值评估：**高**

理由：

- 生产 NN/JN/RM 等节点一个 JVM 内常有 5+ 个 RPC server（client RPC、service RPC、lifeline、health check 等），只靠端口号区分 metrics 在排障时非常低效，且端口可能变化。
- 有了 `serverName` tag 后，RpcQueueTime、RpcProcessingTime、callQueueLength 等关键指标可按 server 维度拆分，是 RPC 慢调用定位、容量规划的基础数据。
- 改动纯增字段+tag，零运行时开销，零行为变化，风险极低。

## 是否值得 cherry-pick 到 3.1.1：**值得，冲突难度极低**

- `git apply --check` 实测对 rel/release-3.1.1（2b9a8c1d3a2）**干净通过**，6 个文件全部无冲突。
- 无 API/行为变更（serverName 原本就是死参数），不涉及协议兼容；回归风险集中在 metrics 命名变化（监控端若按旧命名做告警需微调，但旧名字本就无法区分 server，实际影响为正）。
- 唯一注意点：下游若有代码自己也扩展了 `org.apache.hadoop.ipc.Server` 并依赖 `classNameBase`（该方法是 package 内 static，影响面很小）。
