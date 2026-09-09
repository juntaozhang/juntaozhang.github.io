# HADOOP-15684 — triggerActiveLogRoll stuck on dead name node, when ConnectTimeoutException happens

## Commit

- **Hash**: `2f7222a27bf813b7fcb1e3baef19ed2af154709b`（branch-3.1，cherry-picked from trunk `7f9a89e1b54`，首次发布于 3.1.2）
- **Author**: Inigo Goiri（代码贡献者 Rong Tang）
- **Date**: 2018-09-19
- **涉及文件**:
  - `hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/ha/EditLogTailer.java`（+12/-34，净删减）
  - `hadoop-hdfs-project/hadoop-hdfs/src/test/java/org/apache/hadoop/hdfs/MiniDFSNNTopology.java`（+17）
  - `hadoop-hdfs-project/hadoop-hdfs/src/test/java/org/apache/hadoop/hdfs/server/namenode/ha/TestEditLogTailer.java`（+64/-40）

## Diff 机制分析

**根因**：HDFS-6440（支持多于 2 个 NameNode）引入的 `MultipleNameNodeProxy` 中，
`call()` 的循环只捕获 `RemoteException` 并仅对 `StandbyException` 换下一个 NN 重试；
其它异常（包括 `ConnectTimeoutException`、其它 IOException）直接从 `call()` 抛出，
**不会尝试下一个 NN**。当 Standby 的 `triggerActiveLogRoll` 持有的
`cachedActiveProxy` 恰好指向一台已经死掉的旧 Active NN 时：

- RPC 抛 `ConnectTimeoutException`，future 以 ExecutionException 结束；
- 外层 `triggerActiveLogRoll` 只对 StandbyException 特判跳过，其它只是 LOG.warn
  后吞掉——**不更新 cachedActiveProxy、不尝试其它 NN**，下个周期继续撞同一台死节点；
- Standby 的 edit log 滚动永远触发不了，tail 停滞，元数据落后。

**修复方式**：

1. `MultipleNameNodeProxy.call()` 把捕获范围从 `RemoteException` 扩大到
   `IOException`：任何 IO 错误（standby、连接超时、连接拒绝等）一律
   `cachedActiveProxy = null; nnLoopCount++` 换下一个 NN 重试。
2. 删除 `triggerActiveLogRoll` 里的 StandbyException 特判逻辑（不再需要），
   也删除了不再使用的 `RemoteException`/`StandbyException` import。
3. `MultipleNameNodeProxy` 改为 package-private + `@VisibleForTesting`，
   测试可以直接驱动多 NN 拓扑（MiniDFSNNTopology 增加相应支持）。

Release note 原话："either the remote NN is standby or IO Exception happens,
A should continue to try next NN, instead of getting stuck on the problematic one."

## JIRA 页面信息

- **Affects Version/s**: 3.0.0-alpha1（HDFS-6440 起，3.x 全系列含 3.1.1 未修复）
- **Fix Version/s**: 3.0.4, **3.1.2**, 3.2.0（branch-2 另有 HDFS-14397 回移）
- **Priority**: **Critical**（component: HA，label: multi-sbnn）
- **报告人/Assignee**: Rong Tang
- **触发场景**（报告人原话）："take down the active NN (don't let it come back during
  test), and then the stand by NNs will keep trying dead (old active) NN, because it is
  the cached one." 日志表现为反复 `Unable to trigger a roll of the active NN`
  + ConnectTimeoutException。
- **Issue Links**: Duplicates HDFS-13900；Caused by HDFS-6440（多 NN HA 场景）；
  相关 HDFS-14397（branch-2 回移）。

## 影响评估

**高**（多 NameNode HA 部署）/ 中（标准 2-NN 部署）。

- 对 NN RPC 可用性的影响是间接但严重的：Standby/Observer 停止 tail edits 后
  元数据持续落后，一旦 Active 故障触发 failover，落后的新 Active 可能丢失已确认
  的元数据操作（客户端已收到成功响应），属于可用性 + 正确性双重风险。
- 2-NN 部署下 cachedActiveProxy 错误也有概率自愈（周期性 doWork 路径），
  但死节点场景仍会显著拉长 tail 停滞窗口；3+ NN 部署（HDFS-6440 引入）下
  必现 stuck。
- 不影响 Active NN 自身 RPC 服务，只影响 standby 侧追平能力。

## 是否值得 cherry-pick 到 3.1.1

**值得**。

- 主代码 + 测试经 `git apply -3` 实测全部**干净应用**到 3.1.1。
- 改动是净删减（-34/+12 于主类），行为更简单（任何 IO 错误换 NN），
  引入回归的概率低。
- 3.1.1 用户在 NN 故障切换场景（正是 HA 存在的意义）会踩中；
  Priority 为 Critical，上游评估一致。
