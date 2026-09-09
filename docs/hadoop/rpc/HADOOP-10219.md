# HADOOP-10219 — ipc.Client.setupIOstreams() needs to check for ClientCache.stopClient requested shutdowns

## Commit

- **Hash**: `9e96ac666d783376a8cdea9c3cc84098c5bdcb56`（trunk，2018-09-04，Steve Loughran 提交，Kihwal Lee / Lukas Majercak 贡献）
- **Fix Version**: 2.9.2 / 2.10.0 / 3.0.4 / 3.1.2 / 3.2.0（官方已 backport 到 3.1.2）
- **branch-3.1 backport**: `142d878c902`（实测可干净 apply 到 rel/release-3.1.1）
- **涉及文件**（2 个，+59/-0）：
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Client.java`（+14）
  - `hadoop-common-project/hadoop-common/src/test/java/org/apache/hadoop/ipc/TestIPC.java`（+45，新测试 `testSetupConnectionShouldNotBlockShutdown`）

## Diff 机制分析

问题：`Client.stop()`（如 `FileSystem.cache.closeAll()` 关闭钩子、NameNode failover 场景）调用时，如果某个连接正处于 `setupIOstreams()`/`setupConnection()` 的 socket connect 重试循环中，`stop()` 只能 `conn.interrupt()` 打断 Connection 线程本身，而实际阻塞在 `socket.connect()` 的是**发起调用的业务线程**。业务线程要等 `ConnectTimeoutException` 按 `ipc.client.connect.max.retries` 重试策略耗尽才退出，`Client.stop()` 随之挂起——严重时 JVM 无法退出或 NN 切换 active 时 hang。

改动（全部在 `Client.Connection` 内）：

1. 新增 `AtomicReference<Thread> connectingThread`，`setupIOstreams()` 进入连接流程时 `set(Thread.currentThread())`，finally 中置 null。
2. 新增 `interruptConnectingThread()`：取到正在连接的线程并 `interrupt()`。
3. `Client.stop()` 在遍历 `conn.interrupt()` 之外追加调用 `conn.interruptConnectingThread()`，使阻塞在 socket connect 的业务线程立即被中断，跳出重试循环，连接被 markClosed。

效果：`stop()` 不再被进行中的连接建立阻塞（测试断言：mock 的 createSocket 只被调用 1 次，stop 立即返回，不再重试 5 次）。

代码路径：`hadoop-common` IPC 客户端核心路径 `Client.stop()` → `Connection.setupIOstreams()`，所有基于 Hadoop IPC 的客户端（HDFS NameNode/DataNode、YARN RM 等）均受益。

## JIRA 页面信息

- **Affects**: 2.2.0 / 2.6.0；**Priority**: Major；**Reporter**: Steve Loughran；**Assignee**: Kihwal Lee
- **描述要点**：`ClientCache.stopClient()` 时，被 connectivity 问题阻塞的 client 要到 timeout 策略耗尽才退出，可在 `FileSystem.cache.closeAll()` 关闭钩子里 hang；NN failover（Standby→Active 切换）期间 `Client.stop()` 同样受影响，后果严重、可造成停机。
- **性能数据**：无 benchmark 数字；测试中间接体现（sleep MIN_SLEEP_TIME*5 × 最多 45 次重试的阻塞被消除）。
- **关联**：Breaks HDFS-5755（FileSystem close 阻塞 SystemExit）；Duplicated by HADOOP-15683；Related HADOOP-12464、HADOOP-12950。补丁历史：Kihwal Lee 2015 年初版，Lukas Majercak 2018 年 v2–v4 最终版。

## 对 RPC 性能的影响评估

**评级：中**。不是吞吐/延迟优化，而是**关闭/failover 路径的 RPC 可用性修复**：

- NameNode failover 时 `Client.stop()` 从"可能 hang 到连接超时耗尽（默认重试可达数分钟）"变为立即中断，显著缩短 HA 切换期间 RPC 客户端卡死的窗口——对生产 RPC 尾延迟（failover 场景）影响显著。
- 正常 RPC 调用路径零开销（仅多一个 AtomicReference set/clean 和一次 interrupt 调用，纳秒级）。
- 对 JVM 平滑退出（shutdown hook）也有直接帮助。

## 是否值得 cherry-pick 到 3.1.1

**值得，强烈推荐（官方 3.1.2 已包含）**。

- 冲突难度：**极低**。diff 仅 14 行生产代码、单文件 `Client.java` + 一个测试文件；用 branch-3.1 backport `142d878c902` 实测 apply 到 rel/release-3.1.1 **clean**。
- 该 bug 自 2.2.0 起存在，影响所有 HA 场景与进程退出路径，修复无副作用（不改变正常连接建立语义）。
