# Hadoop branch-3.1 基线 RPC 性能/稳定性 fix 全量复审

**基线**：`680dac26d80`（branch-3.1）→ `a3d8749b8ca`（branch-3.1 tip）
**范围**：`range_jira.txt` 822 个 HDFS-/HADOOP- commit（已去文档/构建/Ozone/Revert）。**覆盖核验**：与 `git log 680dac26..a3d8749b` 实际区间比对，range 文件缺 13 个——去重/构建（Yetus×2）/测试×2/Revert×1/Distcp CLI×1/HDFS-14305 等价 hash×1 后，实补 **5 个**（HDFS-14403、14476、14557、15062、14084），清单 172→**177**

## 方法（两步）

1. **标题人工标注** → `triage_step1.tsv`：KEEP 232 / DROP 590（misc 266、feat 98、cli 66、test 61、sec 46、fs 49、cpp 4）
2. **patch 分析** → `triage_step2.tsv`：RELEVANT **177** / DROP 47 / KNOWN 13。RELEVANT 均写 `<JIRA>.md` 分析，并在基线 worktree（`/tmp/hadoop-base` @680dac26）实测 `git apply --check`

## 最终清单（177 个 RELEVANT，按评级排序）

> **类型标签**：`bug` 缺陷修复 / `performance` 性能优化 / `support` 可观测性·诊断；`rpc` = 修复点落在 RPC 调用路径（ipc 框架、服务端请求处理及其锁、DataXceiver 数据面、心跳/块汇报等 RPC 客户端行为、RPC 可观测性）。后台线程自转、fsimage/edit log 存取、启动停机、HTTP/WebHDFS、客户端本地缓存管理不算。

| 评级 | JIRA | 组件 | 类型 | commit | apply 到基线 | 说明 |
|---|---|---|---|---|---|---|
| 高 | [HADOOP-16385](HADOOP-16385.md) | Common | `bug` | `3ab475377a9` | clean | RedundancyMonitor 线程在块重建/选择副本目标时调用 NetworkTopology，`getNod… |
| 高 | [HADOOP-17068](HADOOP-17068.md) | Common | `bug` `rpc` | `1fe15113cf4` | clean | IPC Client 建立连接时对 `UnresolvedAddressException`（NN 换 IP / 域… |
| 高 | [HDFS-10240](HDFS-10240.md) | NN+DN | `bug` `rpc` | `865650052b0` | clean | 客户端 `close()`（`complete` RPC，提交最后一块）与租约超时触发的 `recoverLease… |
| 高 | [HDFS-10477](HDFS-10477.md) | NN | `bug` `rpc` | `084fb9de22a` | 冲突:BlockManager.java | 对一整架 DN 做 decommission 时，`DatanodeManager.refreshNodes` → … |
| 高 | [HDFS-12914](HDFS-12914.md) | NN | `bug` `rpc` | `026cc2647d4` | clean | Block report lease（BR lease）机制：DN 申请 full block report lea… |
| 高 | [HDFS-13101](HDFS-13101.md) | NN | `bug` | `d18b720c705` | clean | 快照目录/文件的 diff list 在 save/load fsimage 时，snapshot 引用计数与 di… |
| 高 | [HDFS-13359](HDFS-13359.md) | DN | `bug` `rpc` | `f2007818fc9` | clean | getBlockInputStream` 在持有 FsDatasetImpl 全局读锁的情况下打开块文件输入流；打开… |
| 高 | [HDFS-14429](HDFS-14429.md) | NN+DN | `bug` `rpc` | `608e7044a53` | 冲突:DataNodeTestUtils.java | 块处于 COMMITTED（写流水线已提交、等待最低副本数报告）期间若有 DN 开始 decommission，co… |
| 高 | [HDFS-14941](HDFS-14941.md) | NN | `bug` | `c6cbf67b82c` | 冲突:NameNodeAdapter.java | Standby tail editlog 时 `OP_SET_GENSTAMP_V2` 直接提升全局 generat… |
| 高 | [HDFS-15012](HDFS-15012.md) | NN | `bug` `rpc` | `280cd6dd1ee` | 冲突:DirectoryWithSnapshotFeature.java | HDFS-13101 改动后，快照目录清理（cleanDirectory）在销毁 createdList 前只看 `… |
| 高 | [HDFS-15210](HDFS-15210.md) | Client | `bug` `rpc` | `379a7a57267` | clean | EC 写路径上，admin 用 `shutdownDatanode` 关闭 DN 时 DN 会等待重启（datano… |
| 高 | [HDFS-15323](HDFS-15323.md) | NN+Client | `bug` `rpc` | `8358d7c1759` | 冲突:QuorumJournalManager.java、TestStandbyInProgressTail.java | QJM 每次 RPC tail edit 最多取 5000 条（`dfs.ha.tail-edits.qjm.rpc… |
| 高 | [HDFS-15421](HDFS-15421.md) | NN | `bug` | `d26dcf66869` | 冲突:TestAddBlockTailing.java | Standby NN tail edit log 时，truncate 相关 op（updateBlock）应用后没… |
| 高 | [HDFS-15641](HDFS-15641.md) | DN | `bug` `rpc` | `eb909fd726a` | clean | refreshNameNode`（动态增删 NN 地址，例如 observer 上线）与 BPServiceActo… |
| 高 | [HDFS-15644](HDFS-15644.md) | DN | `bug` `rpc` | `7f3645ec5ba` | clean | getBlockReports` 遍历 replica map 时，遇到所属 volume 已失败/正在移除的 re… |
| 高 | [HDFS-15704](HDFS-15704.md) | NN | `bug` `rpc` | `a23faaec184` | 冲突:TestDeleteRace.java、LeaseManager.java | LeaseManager 原来维护 `leases`（TreeMap）+ `sortedLeases`（TreeSe… |
| 高 | [HDFS-15719](HDFS-15719.md) | Common | `bug` `performance` | `13768fe6f0c` | 冲突:CommonConfigurationKeysPublic.java、HttpServer2.java | 把 `hadoop.http.idle_timeout.ms` 的默认值从 1000ms（HttpServer2 内… |
| 高 | [HDFS-15915](HDFS-15915.md) | NN | `bug` `rpc` | `49490af23c7` | 冲突:NameNodeAdapter.java、TestEditLogRace.java | 核心是把 txid 的分配/记录收回到 `FSEditLog.logEdit` 的 synchronized 临界区… |
| 中 | [HADOOP-15696](HADOOP-15696.md) | Common | `bug` `performance` | `d1ea7df43d6` | clean | Hadoop 3 的 HttpServer2 从老的 Jetty 6/7 迁到 Jetty 9 后，KMS/Http… |
| 中 | [HADOOP-15865](HADOOP-15865.md) | Common | `bug` | `72ef7528763` | clean | Configuration.overlay()` 遍历 overlay 配置时直接操作被迭代的集合，patch 改为… |
| 中 | [HADOOP-16028](HADOOP-16028.md) | Common | `bug` `rpc` | `3dd40008846` | clean | NetworkTopology.chooseRandom(scope, excludedScope, exclude… |
| 中 | [HADOOP-16127](HADOOP-16127.md) | Common |  `bug` `performance` `rpc`  | `ce804be72e1` | 冲突:Client.java | Client.stop()` 与 `getConnection()` 存在竞态：stop 把 running 置 f… |
| 中 | [HADOOP-16161](HADOOP-16161.md) | NN | `bug` `rpc` | `f1fe3abac9c` | clean | getWeightUsingNetworkLocation` 计算节点权重时未正确排除目标节点自身/空值分支，导致返… |
| 中 | [HADOOP-16192](HADOOP-16192.md) | Common |  `bug` `performance` `rpc`  | `d4fbbc83ad8` | clean | RPC server 的客户端 backoff 机制（`ipc.<port>.backoff.enable`，与 F… |
| 中 | [HADOOP-16248](HADOOP-16248.md) | Common |  `bug` `performance` `rpc`  | `55cc35c0e14` | clean | MutableQuantiles`（RpcQueueTime/RpcProcessingTime 等分位数指标的采样… |
| 中 | [HADOOP-16435](HADOOP-16435.md) | Common |  `bug` `performance` `rpc`  | `4662b13b264` | clean | RPC server 关闭时 `RpcMetrics.shutdown()`/`RpcDetailedMetrics… |
| 中 | [HADOOP-16459](HADOOP-16459.md) | NN+Common |  `support` `rpc`  | `7b8f08f59e5` | 冲突:Server.java | 引入 `ProcessingDetails` 把一次 RPC 的处理时间拆成 queueTime、processin… |
| 中 | [HADOOP-16461](HADOOP-16461.md) | Common |  `bug` `performance`  | `b9dc2c17de5` | clean | FileSystem CACHE 的 `getInternal` 在持全局 CACHE 锁的情况下调用 `creat… |
| 中 | [HADOOP-16580](HADOOP-16580.md) | Common |  `bug` `rpc`  | `f8c06f5a87d` | clean | FailoverOnNetworkExceptionRetry.shouldRetry` 的异常分派没有 Acces… |
| 中 | [HADOOP-16677](HADOOP-16677.md) | Common | `bug` `rpc` | `b4f39c2a8ac` | clean | SocketIOWithTimeout.select()` 循环里用入参 `timeout` 原地扣减，但中断检查 … |
| 中 | [HADOOP-16868](HADOOP-16868.md) | Common | `bug` `rpc` | `b28347d2824` | clean | Server.Connection` 复用同一个 4 字节 `dataLengthBuffer` 读取每个 RPC … |
| 中 | [HADOOP-17052](HADOOP-17052.md) | Common | `bug` `rpc` | `2c40f20debd` | clean | NetUtils.connect` 对 unresolved 地址抛 UnresolvedAddressExcept… |
| 中 | [HADOOP-17342](HADOOP-17342.md) | Common | `bug` `rpc` | `4cec4bebd9e` | clean | AbstractDelegationTokenIdentifier` 构造时调用的 toString/equals … |
| 中 | [HADOOP-17346](HADOOP-17346.md) | Common | `bug` `rpc` | `c7845dc574c` | 冲突:DecayRpcScheduler.java、Server.java | FairCallQueue + DecayRpcScheduler 的公平性以"用户 principal"为身份维度… |
| 中 | [HDFS-10659](HDFS-10659.md) | NN | `bug` `support` `rpc` | `dd795edcc74` | clean | QJM 的恢复协议（paxos-like recovery）需要读写 `$JN/current/paxos/<seg… |
| 中 | [HDFS-12703](HDFS-12703.md) | NN | `bug` | `fe40fbbd4d4` | clean | decommissioning 监控线程（DatanodeAdminManager 内部循环）中任何 DN 的异常（… |
| 中 | [HDFS-12748](HDFS-12748.md) | NN+Client | `bug` `performance` | `9c3806cf1b7` | clean | WebHdfs 的 GETHOMEDIRECTORY 每次请求都在 NN 上新建一个带独立 UGI/缓存的 File… |
| 中 | [HDFS-12749](HDFS-12749.md) | DN | `bug` `support` `rpc` | `362657c1a35` | clean | NN 重启后 DN `register()` 重试循环：EOFException/SocketTimeout 时睡 … |
| 中 | [HDFS-13027](HDFS-13027.md) | NN |  `bug` `rpc`  | `65977e5d812` | clean | 多个 NN 内部路径遍历块时，块可能已被并发删除（`BlockInfo.delete()` 把 `bcId` 置 0… |
| 中 | [HDFS-13566](HDFS-13566.md) | NN+Client+Common |  `performance` `rpc`  | `3835a6c4e82` | 冲突:Server.java | 给 ipc.Server 增加"附加监听器"支持（可绑定额外端口复用同一 handler），NN 据此暴露辅助 RP… |
| 中 | [HDFS-13677](HDFS-13677.md) | DN |  `bug` `rpc`  | `7b51edbab9f` | clean | DN 支持热插拔/动态增删数据盘（`reconfigure` 或 `dfs.datanode.failed.volu… |
| 中 | [HDFS-13694](HDFS-13694.md) | NN | `bug` | `7c7907f756a` | clean | NN 加载 fsimage 后再计算整图 md5 校验和（单线程顺序读），大 image 上这是分钟级的串行开销。 |
| 中 | [HDFS-13709](HDFS-13709.md) | DN | `bug` `rpc` | `fc062b6fef9` | clean | DN 间块传输（balance/recovery 读源块）遇到磁盘 EIO 时，原来只把异常抛给调用方，不报告坏块，… |
| 中 | [HDFS-13840](HDFS-13840.md) | NN | `bug` `rpc` | `02f8b5da47c` | clean | DN 报告的块状态中 RBW/RWR（写进行中/恢复中）的处理：`checkReplicaCorrupt` 原来对"… |
| 中 | [HDFS-13941](HDFS-13941.md) | DN | `bug` `rpc` | `8548864ccb5` | clean | HDFS 分阶段升级/旧客户端场景：NN 颁发的 block token 里可能不带 storageId（旧版本 t… |
| 中 | [HDFS-13977](HDFS-13977.md) | NN+Client |  `bug` `performance` `rpc`  | `9dc921f5e5d` | 冲突:QuorumJournalManager.java | EditLogOutputStream.shouldForceSync()` 默认实现让 FSEditLog 认为 … |
| 中 | [HDFS-14048](HDFS-14048.md) | Client | `bug` `rpc` | `f65324aa47e` | clean | DataStreamer.createBlockOutputStream` 建管道成功后，原代码重置了 `error… |
| 中 | [HDFS-14074](HDFS-14074.md) | DN | `bug` `rpc` | `529d0955f5a` | 冲突:ThrottledAsyncChecker.java | ThrottledAsyncChecker.schedule()` 是 DN 磁盘健康异步检查（`VolumeChe… |
| 中 | [HDFS-14146](HDFS-14146.md) | NN+Common | `bug` `rpc` | `6c1bf616285` | 冲突:Server.java | 两处修复：1) `Call.doResponse(Throwable)` 拆出 `doResponse(Throwa… |
| 中 | [HDFS-14305](HDFS-14305.md) | NN+DN | `bug` `rpc` | `8fc4e406ad5` | 冲突:BlockTokenSecretManager.java | BlockTokenSecretManager 的 key serial number 由 NN 本地时间戳派生，H… |
| 中 | [HDFS-14308](HDFS-14308.md) | Client | `bug` `performance` | `44ca2fe4db3` | clean | unbuffer()`（HBase/预读释放用）在 DFSStripedInputStream 未覆写，只释放父类的… |
| 中 | [HDFS-14313](HDFS-14313.md) | DN | `performance` `support` | `3aea2bcf120` | 冲突:BlockPoolSlice.java | 新增 `ReplicaCachingGetSpaceUsed`/`FSCachingGetSpaceUsed`：DN… |
| 中 | [HDFS-14314](HDFS-14314.md) | DN |  `bug` `rpc`  | `d951497f57c` | clean | DN 的 `BPServiceActor` 用 `fullBlockReportLeaseId` 走 BR leas… |
| 中 | [HDFS-14317](HDFS-14317.md) | NN | `bug` `support` `rpc` | `b0b80002402` | 冲突:EditLogTailer.java、TestEditLogTailer.java | Standby NN 用 `EditLogTailer` 周期触发 active 滚动 edit log（`dfs.… |
| 中 | [HDFS-14333](HDFS-14333.md) | DN | `bug` | `327e57ae6e4` | 冲突:FsVolumeList.java、AddBlockPoolException.java | DN 向 NN 注册前 `initBlockPool` → `FsVolumeList.getAllVolumesM… |
| 中 | [HDFS-14366](HDFS-14366.md) | NN |  `bug` `performance` `rpc`  | `80732146c6f` | clean | HDFS append 需要 `BlockManager.isSufficientlyReplicated(last… |
| 中 | [HDFS-14370](HDFS-14370.md) | NN |  `performance` `rpc`  | `f6ce2f4a508` | 冲突:EditLogTailer.java | SBN/Observer 的 EditLogTailer 在没有新事务时按固定周期向 JN 发起 selectInp… |
| 中 | [HDFS-14384](HDFS-14384.md) | DN+Client |  `bug` `performance` `rpc`  | `4690142e86b` | 冲突:TestBlockToken.java | 追加写文件的读，读到最后一个正在写的 block 时，由于块未 complete，重拉 locatedBlocks … |
| 中 | [HDFS-14403](HDFS-14403.md) | Common |  `performance` `rpc`  | `96ea7f05623` | 冲突:DecayRpcScheduler.java | 为 FairCallQueue 的 DecayRpcScheduler 增加 cost-based 调度维度：新接口… |
| 中 | [HDFS-14459](HDFS-14459.md) | DN | `bug` | `163fb887a63` | 冲突:AddBlockPoolException.java | DN 启动 addBlockPool 时，某块卷 addBlockPool 抛 ClosedChannelExcep… |
| 中 | [HDFS-14465](HDFS-14465.md) | NN | `bug` | `b3e0430c3be` | clean | DN 进入 maintenance 前，NN 的 `DatanodeAdminManager` 要确认该 DN 上所… |
| 中 | [HDFS-14476](HDFS-14476.md) | DN |  `performance` `rpc`  | `e978c6c9ed8` | clean | DirectoryScanner.reconcile() 对 diff 中每个块连续调 `dataset.check… |
| 中 | [HDFS-14492](HDFS-14492.md) | NN |  `bug` `performance` `rpc`  | `a3493fe3288` | clean | 快照删除/清理后，INode 上的 DirectoryWithSnapshotFeature/FileWithSna… |
| 中 | [HDFS-14497](HDFS-14497.md) | NN | `bug` `rpc` | `c61c114a3db` | 冲突:FSNamesystem.java | FSNamesystem.metaSave()` 原本持有 写锁 做全量元数据 dump（遍历所有 inode/bl… |
| 中 | [HDFS-14497](HDFS-14497.md) | NN | `bug` `rpc` | `d91c68729c1` | 冲突:FSNamesystem.java | FSNamesystem.metaSave()` 原本持有 写锁 做全量元数据 dump（遍历所有 inode/bl… |
| 中 | [HDFS-14498](HDFS-14498.md) | NN | `bug` `rpc` | `b6115597557` | clean | create 失败的文件可能在 namesystem 里留下半初始化状态（INode 存在但 lease/block… |
| 中 | [HDFS-14498](HDFS-14498.md) | NN | `bug` `rpc` | `0bfd1b5a945` | clean | create 失败的文件可能在 namesystem 里留下半初始化状态（INode 存在但 lease/block… |
| 中 | [HDFS-14523](HDFS-14523.md) | Common |  `performance` `rpc`  | `41c0757a1f5` | clean | getNode`/`countNumOfAvailableNodes` 等只读方法在持有 NetworkTopolo… |
| 中 | [HDFS-14527](HDFS-14527.md) | NN | `bug` `rpc` | `0160e2c97f7` | clean | 全部 DN 停止/失联瞬间（如集群维护同时停 DN），`getMaxNodesPerRack()` 中 `numOf… |
| 中 | [HDFS-14547](HDFS-14547.md) | NN |  `performance` `rpc`  | `a59553b5e6b` | clean | QuotaCounts` 对 storage type 配额一律分配 EnumCounters[StorageTyp… |
| 中 | [HDFS-14557](HDFS-14557.md) | NN | `bug` | `45ba41570b0` | 冲突:TestJournal.java | HDFS 升级前 2.x 时代的 edit log 是 pre-transactional 格式（无 txid 头）… |
| 中 | [HDFS-14582](HDFS-14582.md) | DN | `bug` | `c2aeeb01fa6` | 冲突:BlockPoolSlice.java | 块使用 NULL checksum（不校验，部分测试/特殊场景）时，BlockPoolSlice 计算块大小/校验和… |
| 中 | [HDFS-14610](HDFS-14610.md) | NN | `bug` `rpc` | `d95dd33ea11` | clean | DatanodeDescriptor.storageMap` 通常由 storageMap 对象自身的 synchr… |
| 中 | [HDFS-14618](HDFS-14618.md) | NN | `bug` `rpc` | `ab7ecd6a0f1` | clean | PendingReconstructionBlocks.pendingReconstructions` 内部有一个以… |
| 中 | [HDFS-14655](HDFS-14655.md) | NN+Client | `bug` `rpc` | `5c058549b71` | 冲突:DFSConfigKeys.java | 虽然挂在 [SBN Read] 名下，实际修复的是 QJM client：某台 JN 宕机后，`QuorumCall… |
| 中 | [HDFS-14687](HDFS-14687.md) | NN | `bug` `rpc` | `92c3a581ec0` | clean | SBN 处于 safemode 时，DN 的 block report/增量报告中 EC 相关消息被 `Pendin… |
| 中 | [HDFS-14697](HDFS-14697.md) | NN | `bug` | `69e3ac5cbb2` | 冲突:FSImage.java | NN shutdown 与正在进行的 saveNamespace/checkpoint 并发时，FSImage 保存… |
| 中 | [HDFS-14706](HDFS-14706.md) | DN+Client | `bug` `rpc` | `63b18d764ae` | 冲突:DataNode.java | 块 meta 文件小于 7 字节（版本+checksum头最小长度）时，原代码读取 header 后直接走"无 ch… |
| 中 | [HDFS-14852](HDFS-14852.md) | NN | `bug` `rpc` | `0f234667dde` | clean | LowRedundancyBlocks.remove` 系列方法在按队列优先级查找块时，若块实际位于其它优先级队列（… |
| 中 | [HDFS-14853](HDFS-14853.md) | NN | `bug` `rpc` | `8cc702ad38f` | clean | chooseRandomWithStorageType` 在排除节点不在拓扑中时，`excludedNode` 对应… |
| 中 | [HDFS-14861](HDFS-14861.md) | NN | `bug` `rpc` | `8aaa8d1b712` | 冲突:DFSConfigKeys.java、TestLowRedundancyBlockQueues.java | LowRedundancyBlocks.chooseLowRedundancyBlocks()` 用 bookmar… |
| 中 | [HDFS-15036](HDFS-15036.md) | NN | `bug` `support` | `ea0eeb8f1ab` | 冲突:StandbyCheckpointer.java、ImageServlet.java | Standby 周期性把 fsimage 上传给 Active。 |
| 中 | [HDFS-15052](HDFS-15052.md) | NN+Client | `bug` `performance` | `8095e3b149f` | 冲突:DFSUtilClient.java、DistributedFileSystem.java | WebHDFS 处理 `GETTRASHROOT` 时，NN 端 Jersey 资源 `getTrashRoot()… |
| 中 | [HDFS-15086](HDFS-15086.md) | NN |  `bug`  | `6a4297adc47` | clean | NN 调度重建时 `blocksScheduled` 计数（DN 维度的待复制上限，防热点）只在副本 上报（addB… |
| 中 | [HDFS-15119](HDFS-15119.md) | Client |  `performance` `rpc`  | `7dd43cec7f7` | clean | DFSInputStream 打开后 locatedBlocks 缓存终身不变：long-running reade… |
| 中 | [HDFS-15170](HDFS-15170.md) | NN |  `bug` `rpc`  | `a09d636e2cd` | clean | EC block group 在 failover 与 pipeline recovery 并发时，DN 上报的旧 … |
| 中 | [HDFS-15187](HDFS-15187.md) | NN |  `bug` `rpc`  | `a68ca4a292a` | clean | Standby NN 在 failover 前以延迟队列处理 corrupt 上报，切换 Active 时队列中的块… |
| 中 | [HDFS-15199](HDFS-15199.md) | DN | `bug` `support` `rpc` | `64eb5b27094` | 冲突:BlockSender.java | BlockSender.sendPacket()` 捕获 IOException 后对 `e.getMessage(… |
| 中 | [HDFS-15250](HDFS-15250.md) | Client | `bug` `rpc` | `86c15b82874` | clean | 开启 `dfs.client.use.datanode.hostname`（跨网段/多机房客户端按主机名连 DN）后… |
| 中 | [HDFS-15386](HDFS-15386.md) | DN |  `bug` `rpc`  | `f701d7bdd18` | clean | 一次性移除多个数据目录（volume）时，`removeVolumes` 的副本清理循环在第一个缺失 replica… |
| 中 | [HDFS-15403](HDFS-15403.md) | DN | `bug` `support` `rpc` | `c2f908482f2` | 冲突:FileIoProvider.java | DN 发送 block 数据（transferTo 零拷贝路径）时，`transferToSocketFully` … |
| 中 | [HDFS-15415](HDFS-15415.md) | DN | `bug` `performance` | `4a8b59df595` | 冲突:DirectoryScanner.java | DirectoryScanner 的扫描分三步：磁盘枚举（reconcile 前）、与内存 replica map … |
| 中 | [HDFS-15422](HDFS-15422.md) | NN |  `bug` `rpc`  | `bf229135caf` | clean | Standby NN 处理增量块报告（IBR/FBR）时，遇到"out-of-date generation sta… |
| 中 | [HDFS-15621](HDFS-15621.md) | DN | `bug` `performance` | `71a9885c978` | 冲突:FsVolumeImpl.java | FsVolumeSpi.ScanInfo` 原来存的是 block/meta 文件的完整路径字符串（`blockSu… |
| 中 | [HDFS-15622](HDFS-15622.md) | NN |  `bug` `performance` `rpc`  | `7a3085d552c` | 冲突:LowRedundancyBlocks.java、TestLowRedundancyBlockQueues.java | 文件被删除后其 block 应从低冗余队列（corrupt/underReplicated 等 5 个优先级队列）中… |
| 中 | [HDFS-15694](HDFS-15694.md) | NN |  `bug` `performance` `rpc`  | `2331f86dfba` | clean | DatanodeDescriptor` 内部多处调用 `updateHeartBeatState` 时隐式携带/更新… |
| 中 | [HDFS-15703](HDFS-15703.md) | NN | `bug` `support` `rpc` | `521d5b381a9` | clean | setPermission`/`setOwner` 即使权限/属主未发生变化（no-op）也照常写一条 edit l… |
| 中 | [HDFS-15709](HDFS-15709.md) | DN | `bug` `rpc` | `173d298c637` | 冲突:StripedBlockChecksumReconstructor.java | EC 文件的 striping checksum 计算（`getFileChecksum` 路径，客户端对 EC 文… |
| 中 | [HDFS-15725](HDFS-15725.md) | NN | `bug` `rpc` | `e7df47c45c0` | 冲突:FSNamesystem.java、TestLeaseRecovery.java | 租约恢复（lease recovery）流程中，块已 COMMITTED 但 DN 因故障从未 finalize 该… |
| 中 | [HDFS-15875](HDFS-15875.md) | NN+DN |  `bug` `rpc`  | `7250f0e3273` | 冲突:DataNodeFaultInjector.java、TestFileTruncate.java | 对处于 being-truncated 状态（已有 `truncateBlock` UC 状态、lease reco… |
| 中 | [HDFS-8178](HDFS-8178.md) | NN | `bug` `support` | `e3c01e174c8` | 冲突:JNStorage.java | 当 NN 在向某目录写 edits_inprogress 时崩溃/故障转移，留下 stale inprogress … |
| 低 | [HADOOP-15864](HADOOP-15864.md) | NN | `bug` `rpc` | `ef9f8ca13d8` | clean | HA 集群里，客户端持有逻辑 URI 的 delegation token；`SecurityUtil.setTok… |
| 低 | [HADOOP-16013](HADOOP-16013.md) | Common | `bug` `rpc` | `7b4f44573f6` | clean | DecayRpcScheduler 的 decay Timer 用 `new Timer()`（非 daemon）。 |
| 低 | [HADOOP-16126](HADOOP-16126.md) | Common |  `performance` `rpc`  | `8d71b508abc` | clean | Client.stop() 里等待所有 connection 关闭的循环使用 `Thread.sleep(100)`… |
| 低 | [HADOOP-16208](HADOOP-16208.md) | Common |  `support` `rpc`  | `41f1bc20566` | clean | Client.call()` 发送 RPC 请求时，若线程在 `sendRpcRequest` 等待连接可写时被中断… |
| 低 | [HADOOP-16307](HADOOP-16307.md) | Common |  `performance` `rpc`  | `cf0d5a0e6ee` | clean | PBHelper.convert(FileStatusProto)` 把 protobuf 的 `FileStatu… |
| 低 | [HADOOP-16341](HADOOP-16341.md) | Common | `bug` `performance` | `2c4d8c343ea` | 冲突:ShutdownHookManager.java | HADOOP-15679 后 hooks 存储结构改为按优先级链表/数组，remove 操作变成 O(n) 扫描且 … |
| 低 | [HADOOP-16345](HADOOP-16345.md) | Common |  `bug` `rpc`  | `cf3c1b9ffc8` | clean | FairCallQueue.MetricsProxy` 通过静态 `getInstance(namespace)` … |
| 低 | [HADOOP-16662](HADOOP-16662.md) | Common |  `rpc`  | `9434f36b436` | clean | add(Node)` 中维护 depthOfAllLeaves（全叶子同深度假设用的缓存）时先 `if (!(nod… |
| 低 | [HADOOP-16683](HADOOP-16683.md) | Common |  `bug` `rpc`  | `3a09593a73c` | 冲突:RetryPolicies.java、UnreliableInterface.java | FailoverOnNetworkExceptionRetry` 只对顶层 `AccessControlExcept… |
| 低 | [HADOOP-16700](HADOOP-16700.md) | Common | `bug` `rpc` | `23f25418e82` | 冲突:Server.java | Server.Call.timestampNanos` 一个字段两用：入队时是"接收时间"，异步响应 （handle… |
| 低 | [HADOOP-16705](HADOOP-16705.md) | Common | `bug` `performance` `support` | `50d98a6568c` | clean | MBeanInfoBuilder.get()` 每次构建 MBeanInfo 都执行 `attrs.toString… |
| 低 | [HADOOP-17127](HADOOP-17127.md) | Common | `bug` `support` `rpc` | `0de48933c50` | 冲突:DecayRpcScheduler.java、RpcScheduler.java | DecayRpcScheduler/FairCallQueue 计算 call 的 cost（queueTime +… |
| 低 | [HADOOP-17130](HADOOP-17130.md) | Common | `bug` | `cb3e3787820` | clean | getValByRegex` 在遍历 properties 的同时向结果 map 写匹配项——若正则恰好匹配结果 m… |
| 低 | [HADOOP-17358](HADOOP-17358.md) | Common | `bug` `performance` | `487eaa6dea1` | clean | Configuration` 子类（如 `HdfsConfiguration`/`YarnConfiguration… |
| 低 | [HADOOP-17362](HADOOP-17362.md) | Common | `bug` `performance` | `118d8f5aa16` | clean | HarFileSystem`（hadoop archive）的 `listStatus`/目录遍历原本对 HAR 内… |
| 低 | [HADOOP-9844](HADOOP-9844.md) | Common |  `bug` `support` `rpc`  | `1b7c7dc860d` | clean | SASL RPC 协商失败构建错误响应时，某字段（errorMessage/exception 类名）为 null，… |
| 低 | [HDFS-11719](HDFS-11719.md) | DN | `bug` `rpc` | `bff22305911` | clean | BlockSender.readChecksum` 读校验和出错且 `corruptChecksumOk`（下游允许… |
| 低 | [HDFS-12487](HDFS-12487.md) | DN | `bug` `support` | `4507c2f3a5d` | clean | branch-3.1 backport 只含 DiskBalancer 部分：`DiskBalancer.getBl… |
| 低 | [HDFS-12999](HDFS-12999.md) | Client |  `performance` `rpc`  | `354e63b366e` | clean | DFSStripedOutputStream.writeChunk()` 在写完一个 stripe 时调用 `flu… |
| 低 | [HDFS-13693](HDFS-13693.md) | NN | `bug` `performance` | `8ce5015e94b` | 冲突:FSImageFormatPBINode.java、INodeDirectory.java | fsimage 加载时 `addToParent` 调 `parent.addChild(child)`，后者在子节… |
| 低 | [HDFS-13721](HDFS-13721.md) | DN | `bug` `support` | `7d1e2e1fbbe` | clean | diskBalancer` 字段在 `initBlockPool` 成功后才赋值。 |
| 低 | [HDFS-13730](HDFS-13730.md) | Client | `bug` `support` | `fc8983a5b8c` | clean | 远程读失败时，`BlockReaderRemote.sendReadResult(...)` 会记录诊断日志，日志参… |
| 低 | [HDFS-13735](HDFS-13735.md) | NN+Client | `bug` `support` | `e05b38f1322` | clean | QJM 通过 HTTP 从 JN 下载 edit log 段（`URLImageViewer`/log segmen… |
| 低 | [HDFS-13736](HDFS-13736.md) | NN | `bug` `rpc` | `9956d18129c` | clean | favored node 路径（客户端 `setFavoredNodes`）复用 `chooseLocalStora… |
| 低 | [HDFS-13758](HDFS-13758.md) | NN | `bug` `rpc` | `7a115e8de47` | clean | DatanodeManager.getBlockRecoveryCommand` 从 DN 取待恢复块构造 `Blo… |
| 低 | [HDFS-13789](HDFS-13789.md) | NN+Client | `support` `rpc` | `d96ddb3b873` | 冲突:QuorumJournalManager.java | selectInputStreams` 在 tail 循环中每轮对无法提供 edits 的 JN 打 warn，SB… |
| 低 | [HDFS-13791](HDFS-13791.md) | NN+Client | `support` | `91803c82167` | 冲突:LogThrottlingHelper.java、QuorumJournalManager.java | NN 启动/failover 恢复加载 edits 时，"无法从某流读取"等 warn 日志按事务数逐条打印，大 e… |
| 低 | [HDFS-13831](HDFS-13831.md) | NN |  `performance` `rpc`  | `2cbc3c7d43c` | clean | FSNamesystem.removeBlocks` 在 NN 写锁内每批删 `BLOCK_DELETION_INC… |
| 低 | [HDFS-13901](HDFS-13901.md) | NN | `bug` `rpc` | `8ef6c0aab24` | 冲突:TestDeleteRace.java | getBlockLocations 先在读锁内解析路径返回结果，之后为更新 atime 又在写锁段 二次 resol… |
| 低 | [HDFS-14027](HDFS-14027.md) | Client | `bug` `support` `rpc` | `399645ebc16` | clean | EC（条带化）写流的 `hflush()`/`hsync()` 是 no-op（EC 尚不支持 hsync）。 |
| 低 | [HDFS-14043](HDFS-14043.md) | NN | `bug` `performance` | `9bf4f3d6140` | clean | PersistentLongFile.readFile` 读取写入 `seen_txid` 等持久化长整型文件。 |
| 低 | [HDFS-14084](HDFS-14084.md) | Common | `bug` `support` `rpc` | `e8e55839a0c` | clean | 给 DFSClient 侧 RPC 增加细化统计：ProtobufRpcEngine 在 call 抛出异常时按异常… |
| 低 | [HDFS-14171](HDFS-14171.md) | NN |  `performance` `rpc`  | `f24684ca9cd` | clean | （backport 到 branch-3.1 时只带了 safemode 部分）`BlockManagerSafeM… |
| 低 | [HDFS-14235](HDFS-14235.md) | DN | `bug` `support` | `1030ac85bbf` | clean | slowDiskDetectionDaemon 线程统计慢盘时，磁盘 IO 统计数组在并发更新下长度/索引不一致，导… |
| 低 | [HDFS-14247](HDFS-14247.md) | NN | `bug` `rpc` | `90b88db35d4` | clean | DN 注册路径中 `nodeDesc` 被重复加入 NetworkTopology（第二次 add 触发内部 rem… |
| 低 | [HDFS-14263](HDFS-14263.md) | DN | `bug` `rpc` | `bd9e2826fec` | clean | FsDatasetImpl.getBlockInputStream`（DataXceiver 读 block 路径）… |
| 低 | [HDFS-14276](HDFS-14276.md) | NN | `performance` `support` | `900384cf57a` | clean | EditLogTailer 的 doWork 循环每次迭代都调用 `updateNameDirSize()`（统计所… |
| 低 | [HDFS-14311](HDFS-14311.md) | DN | `bug` | `7a1f508f582` | clean | DN 启动时多个 block pool 并行加载 storage，`layoutVersion` 静态/共享字段的读… |
| 低 | [HDFS-14535](HDFS-14535.md) | Client |  `performance` `rpc`  | `b082628e5a3` | clean | Short-circuit read（短路本地读）建立共享内存通道时，客户端要向 DN 通过 Unix Domain… |
| 低 | [HDFS-14537](HDFS-14537.md) | NN |  `bug` `rpc`  | `3ba7826d41e` | 冲突:Journal.java | JN 被重新 format 后，JournaledEditsCache（Edit Tail Fast Path 的内… |
| 低 | [HDFS-14541](HDFS-14541.md) | Client | `bug` `performance` | `aaf74ea5d73` | clean | ShortCircuitCache 驱逐线程对 `evictableMmapped`/`evictable` 队列轮… |
| 低 | [HDFS-14553](HDFS-14553.md) | NN |  `bug` `rpc`  | `03ec4c4c7c1` | 冲突 | NN 的异步块报告处理线程 `BlockReportProcessingThread`（`BlockManager`… |
| 低 | [HDFS-14578](HDFS-14578.md) | NN | `bug` `rpc` | `12b258115b4` | clean | AvailableSpaceBlockPlacementPolicy 本应优先把副本放到磁盘最空的节点，但其继承的 … |
| 低 | [HDFS-14631](HDFS-14631.md) | DN | `bug` `performance` | `d66ee4bf611` | clean | DirectoryScanner 发现块文件放在错误目录（如 finalized 块出现在 rbw）时，原逻辑只做记… |
| 低 | [HDFS-14632](HDFS-14632.md) | NN | `performance` | `2e1afa9a867` | clean | SafeModeMonitor 每次运行（默认 3s 周期）重复调用 `getNumLiveDataNodes` 多… |
| 低 | [HDFS-14637](HDFS-14637.md) | NN | `bug` | `71dcb8f6d40` | clean | 原逻辑对"副本数已达标但违反放置策略"的块一律只补 1 个副本（注释写死 "Needed on a new rack… |
| 低 | [HDFS-14720](HDFS-14720.md) | NN | `bug` `rpc` | `91790463684` | clean | 块删除但重建任务已在队列时，块 numBytes 被置为 `BlockCommand.NO_ACK` （Long.M… |
| 低 | [HDFS-14731](HDFS-14731.md) | NN | `performance` `rpc` | `4af188587fa` | 冲突:FSDirectory.java、TestBlocksWithNotEnoughRacks.java | FSDirectory 长期持有一把独立的公平 ReentrantReadWriteLock（dirLock），而所… |
| 低 | [HDFS-14806](HDFS-14806.md) | NN | `bug` | `76ca8644067` | clean | hdfs namenode -bootstrapStandby` 默认沿用 `dfs.ha.tail-edits.i… |
| 低 | [HDFS-14859](HDFS-14859.md) | NN |  `performance` `rpc`  | `a3599c7e705` | 冲突:BlockManagerSafeMode.java | areThresholdsMet()` 每次 safemode 检查（每个 block report/心跳后都可能触… |
| 低 | [HDFS-14909](HDFS-14909.md) | NN | `bug` `rpc` | `14beaae65e6` | clean | chooseRandomWithStorageType 遍历 excludedNodes 扣减 availableC… |
| 低 | [HDFS-14913](HDFS-14913.md) | NN | `bug` `rpc` | `9067a11b0d0` | 冲突:DFSNetworkTopology.java | chooseRandomWithStorageType 计算候选 availableCount 时，对 exclud… |
| 低 | [HDFS-14915](HDFS-14915.md) | NN |  `performance` `rpc`  | `4d4f5325bde` | 冲突:FSNamesystem.java | createEncryptionZone`/`listEncryptionZones`/`listReencrypt… |
| 低 | [HDFS-14952](HDFS-14952.md) | NN | `bug` `rpc` | `d101b748142` | 冲突:TestHASafeMode.java、BlockManagerSafeMode.java | 全新 NN（首次格式化、无 block report）blockTotal=0，但 safemode 状态机仍会进 … |
| 低 | [HDFS-14986](HDFS-14986.md) | DN | `bug` `performance` `support` | `f1a19b7a3f5` | 冲突:FsDatasetSpi.java、FsDatasetImpl.java | 根因是 DN 磁盘用量统计的三处缺陷叠加： 1. `CachingGetSpaceUsed.init()` 在 `u… |
| 低 | [HDFS-15010](HDFS-15010.md) | DN | `bug` | `5eb21b83dfd` | 冲突:BlockPoolSlice.java、TestFsVolumeList.java | initializeAddReplicaPool` 是实例方法但初始化的是 static 的 addReplicaT… |
| 低 | [HDFS-15032](HDFS-15032.md) | Common | `bug` `rpc` | `b06965ae32c` | 冲突:ProxyCombiner.java、TestBalancerWithHANameNodes.java | ProxyCombiner` 的 CombinedProxyInvocationHandler.invoke 用反射… |
| 低 | [HDFS-15062](HDFS-15062.md) | DN | `bug` `support` `rpc` | `27555620fae` | clean | DN 的增量块汇报（IBR）批量发送失败时原来静默吞掉（只走正常重试路径），无任何日志。 |
| 低 | [HDFS-15074](HDFS-15074.md) | DN | `bug` `support` | `55510d32bf0` | 冲突:DataNode.java | DataTransfer.run()` 只 catch `IOException`；block 复制/重建传输中若抛… |
| 低 | [HDFS-15161](HDFS-15161.md) | Client | `bug` `performance` `support` | `83ab8537338` | clean | ShortCircuitCache.close()` 用 `while(true) { try { firstKey… |
| 低 | [HDFS-15174](HDFS-15174.md) | DN | `performance` `support` | `268e35d3ae7` | 冲突:ReplicaCachingGetSpaceUsed.java | ReplicaCachingGetSpaceUsed.refresh()` 累加副本大小时调用 `replicaIn… |
| 低 | [HDFS-15281](HDFS-15281.md) | NN | `bug` `rpc` | `68dcdad6662` | clean | 多网卡机器上 ZKFC 向 ZK 注册/检查 NN 健康时绑定地址不按 `dfs.namenode.rpc-addr… |
| 低 | [HDFS-15293](HDFS-15293.md) | NN | `bug` `support` | `8f53545f9d1` | 冲突:ImageServlet.java、TestCheckpoint.java | Active NN 接收 SBN/2NN 上传 fsimage 的校验中，"距上次 checkpoint 的时间差"… |
| 低 | [HDFS-15295](HDFS-15295.md) | NN |  `performance` `rpc`  | `6f7a30fd716` | 冲突:AvailableSpaceBlockPlacementPolicy.java | 基于可用空间的选择策略每选一个 block 目标都做一次全机架随机选取 + 排序比较；trunk 上 `Networ… |
| 低 | [HDFS-15406](HDFS-15406.md) | DN | `bug` `performance` | `9d891f08c28` | clean | VolumeScanner 读取 block 做校验时按固定小缓冲区 + 节流 sleep 推进；修复调整读循环的缓… |
| 低 | [HDFS-15446](HDFS-15446.md) | NN | `bug` `support` | `1e0424f39a5` | clean | 对 `/.reserved/raw` 下路径创建的 snapshot，其 CreateSnapshotOp 记录在 … |
| 低 | [HDFS-15574](HDFS-15574.md) | DN | `bug` | `43b113de69a` | clean | DirectoryScanner 拿到 FsDataset 的 replica 列表后先整体排序再逐项比对，而后续比… |
| 低 | [HDFS-15583](HDFS-15583.md) | DN |  `bug` `performance` `rpc`  | `27b2c1ea7bb` | 冲突:DirectoryScanner.java | 把 trunk 上三个 DirectoryScanner 修复打包回移：HDFS-14476（扫描跳过正在写/rep… |
| 低 | [HDFS-15618](HDFS-15618.md) | DN | `bug` `performance` | `33db7c140b3` | 冲突:DFSConfigKeys.java、MiniDFSCluster.java | DN 优雅停机时要等 VolumeScanner 当前扫描文件收尾，而大文件扫描 + 节流读使停机被拖到分钟级。 |
| 低 | [HDFS-15623](HDFS-15623.md) | Common | `bug` `rpc` | `3443d759468` | clean | RPC.setProtocolEngine` 无条件用传入值覆盖 `rpc.engine.<protocol>` 配… |
| 低 | [HDFS-15633](HDFS-15633.md) | Client | `bug` `rpc` | `8354ce680b8` | clean | DFSClient.getDiskStatus()`（`df` 命令 / `Filesystem.getStatus… |
| 低 | [HDFS-15734](HDFS-15734.md) | DN | `performance` | `09bbbc8b866` | 冲突:DirectoryScanner.java | DirectoryScanner 扫描循环里原本对每个 ScanInfo 都查 `vol.getStorageTyp… |
| 低 | [HDFS-15783](HDFS-15783.md) | NN |  `performance` `rpc`  | `33a71c8b9e5` | clean | verifyBlockPlacement` 在 EC + RackFaultTolerant 策略下校验 block… |
| 低 | [HDFS-15826](HDFS-15826.md) | NN | `bug` | `cab9172d2e3` | clean | 加载 FsImage 恢复 delegation token 时，各 token 的 sequence number… |
| 低 | [HDFS-16040](HDFS-16040.md) | NN+Common |  `bug` `support` `rpc`  | `a357d4ce607` | 冲突:Server.java、TestConsistentReadsObserver.java | IPC Server 的 Handler 循环里，当 call 因一致性读取（observer/msync）原因被 … |
| 低 | [HDFS-16233](HDFS-16233.md) | NN |  `performance` `rpc`  | `cd7c34f9b40` | 冲突:QuotaCounts.java | QuotaCounts` 内部维护一组 `EnumCounters<Quota>`（storage space / … |

## 之前验证清单在新基线下的状态（KNOWN 13 个）

**新基线 680dac26 仍缺失，恢复 pick 资格**：HADOOP-10219、HDFS-13051、HADOOP-15684、HDFS-13768+13962、HDFS-15211、HDFS-15219、HDFS-14973、HDFS-15068、HDFS-14920、HDFS-15115、HADOOP-16531（+ 之前 trunk 侧 HADOOP-15340、15611、15742、HDFS-13833 不在本 range，结论不变）

**已在基线**：HDFS-13792（patch 等价存在于 branch-3.1）

## 注意事项

- **HDFS-14497**：主修复 + addendum 两个 commit 须一起 pick
- **HDFS-14498**：两个 hash（`b6115597557`/`0bfd1b5a945`）同 diff，取一
- **HDFS-14909 + HDFS-14913**：同改 `chooseRandomWithStorageType`，按序合并
- **HDFS-15295**：pick 前确认基线有 `chooseRandomWithStorageTypeTwoTrial`
- **HDFS-16233**：用户点名（QuotaCounts 去异常驱动 COW），apply 冲突 `QuotaCounts.java`，需手工合
- **HDFS-13813**（`b38649c59a7`）：当前 HEAD 已 revert，跳过
- **HADOOP-16459 + 16531**：慢 RPC 计时指标，前置+本体配对 pick
- 评级分布：高 18 / 中 80 / 低 74；apply clean 约半，冲突多集中在测试文件或与基线漂移的大文件（DirectoryScanner、DecayRpcScheduler、BlockManager 等），主代码 hunk 大多可手工合
