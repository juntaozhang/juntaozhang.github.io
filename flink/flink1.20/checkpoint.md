# Checkpoint
## 是什么？

Flink 的 Checkpoint 机制基于 Chandy-Lamport 算法（全局一致性快照算法）， 可在算子故障时将整个job恢复到故障前的一个状态，来实现**全局一致性**。

### 什么是 [State](state.md)？
### 什么是 Barrier？

它是一种特殊的事件，用于将数据流分成多个部分，每个部分都可以独立地进行 Checkpoint。

checkpoint configuration:
```java
conf.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
conf.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "file:///tmp/checkpoints/CheckpointExample");
```
- [CheckpointExample](https://github.com/juntaozhang/flink/tree/release-1.20-study/flink-examples/flink-examples-streaming/src/main/java/org/apache/flink/streaming/examples/my/checkpoint/CheckpointExample.java)

## Checkpoint 原理

- 创建：在 ExecutionGraph 创建的同时，在 DefaultExecutionGraph 中创建一个 CheckpointCoordinator
- 周期性运行：
  - CheckpointCoordinator getTasksToTrigger(SourceSteamTask)
  - SourceSteamTask 接收到 triggerCheckpointAsync 开始执行 checkpoint， 在 SubtaskCheckpointCoordinatorImpl.checkpointState 执行：
    - broadcast CheckpointBarrier 到所有下游 task
    - 执行 Operator 中的 checkpointState
    - ack to CheckpointCoordinator
  - 其他 StreamTask 在接收到所有上游的 Barrier 后，与 SourceSteamTask 一样
    - aligned-checkpointing 需要等待所有上游算子的 Barrier 对齐（SourceSteamTask 没有上游不需要等待）
  - 所有算子快照完成后会向 CheckpointCoordinator 上报 ACK 消息；
    CheckpointCoordinator 收到所有算子的 ACK 后，标记该 Checkpoint 成功；
    - 通知所有 tasks notifyCheckpointOnComplete

### [Aligned Checkpointing](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/concepts/stateful-stream-processing/#checkpointing)
- 什么是barrier对齐？
  - 是指在 Checkpoint 过程中，上游多个channel 中 barrier 不是同时到达，快barrier要等慢barrier的过程
  - Operator 会等待所有上游算子的 Barrier 对齐，然后再执行 checkpointState。
- Barrier 对齐的性能影响？
  - 对齐成本 = 最大的等待时间（所有上游算子的最大延迟时间）
  - 反压（backpressure）：阻塞了较快的流；Checkpoint 延迟增加；

![stream_aligning](https://nightlies.apache.org/flink/flink-docs-release-1.20/fig/stream_aligning.svg)

#### Exactly Once vs. At Least Once
- Exactly Once
  - Flink 默认通过 Checkpoint + Barrier 机制来实现 Exactly Once，语义强度取决于：状态一致性 + source 可靠性 + sink 端支持。

- At Least Once
  - Barrier 机制未对齐，快 barrier 不等慢 barrier，导致慢 barrier 到达的时候 operator的 state包含了一些 不应该被记录的状态
    - 导致异常恢复的时候 Operator State 会重新消费快 barrier 的那条流的数据，导致 state 重复处理
  - sink 非幂等性，同一条数据可能被处理两次（重发或分区 skew）
  - sink 不支持事务
- At Most Once
  - 没有 checkpoint 应该就是 At Most Once

## [Unaligned Checkpointing](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/concepts/stateful-stream-processing/#unaligned-checkpointing)
当对齐成本太高时，Flink 任务会因为 checkpoint 对齐而阻塞，导致性能下降。

- savepoints will always be aligned
- in-flight data
  - input/output buffer 中的数据
  - 上游 operator 正在处理的数据 d（这里无法画的很准确）
    - 如果上游 Operator 正在处理一个事件，它会先完成当前事件的处理然后再处理作为优先级元素的 barrier，这是因为 Flink 无法中断处理过程
    - input buffer 的 in-flight data 怎么持久化的见：ChannelStatePersister(TODO)
- 最大的 cost 是 in-flight data 持久化的成本
 
![unaligned_checkpointing](https://nightlies.apache.org/flink/flink-docs-release-1.20/fig/stream_unaligning.svg)

## checkpoint 与 savepoint
- checkpoint 是为了故障恢复而设计的，而 savepoint 是为了应用升级而设计的
- checkpoint 是 Flink 自动触发的，而 savepoint 是手动触发的

## 什么是两阶段提交？
- 在 Flink 的 Exactly Once 语义下，Sink 的 invoke(() 只负责写入“事务上下文”或“临时区域”，真正的提交（commit()）只有在 checkpoint 成功（即 notifyCheckpointComplete() 被调用）之后才发生。这是端到端一致性的核心保障机制。

- Flink 中 sink 输出是否“对下游系统可见”，完全取决于 Flink checkpoint 是否成功。只有 checkpoint 成功之后，sink 的数据才能被 commit()，从而“真正写入”或“对外部可见”。

- 过程：
  - 阶段 1：预提交（Pre-commit）
    - Task 正在运行期间
        - beginTransaction() 创建事务
        - invoke() 不断写入数据（保存在 buffer / 临时事务中）
        - preCommit() 在 CheckpointedFunction.snapshotState 时将写入状态挂起
  - 阶段 2：真正提交（Commit）
    - JobManager 通知 checkpoint 成功
      - notifyCheckpointComplete()
      - commit() 提交该 checkpoint 对应事务
    - 如果checkpoint 太慢 transaction 就会越慢，延时越高
  - abort() - 出错时回滚事务
    - [Checkpoint 失败 / 作业取消 / 重启] 撤销该事务（如 rollback、delete、关闭连接）
    - 如果checkpoint成功，commit 失败 / 丢失
      - commit 在 snapshotState 中执行的 preCommit 才是容易异常点，commit 只是简单 finalize
      - 提交所有 checkpointId ≤ 当前 checkpointId 的事务
    - “Dangling Transaction”（悬挂事务）（TODO）
      - 故障恢复的时候，commit 没有被 checkpoint 提交的事务
      - 在金融或严格场景下，不能完全信任 Flink 的 2PC
    - 幂等性写入 (Idempotency)

## Incremental Checkpoints
- 解决问题：状态太大导致 upload state 耗时太长，进而导致 checkpoint 超时
- Incremental Checkpoints 的核心思想是借鉴了数据库的 WAL（Write-Ahead Log，预写日志） 机制：
  - Changelog State Backend
    - Snapshot → Upload log files
      - Flink 只需要确认增量的部分，而不需要再传输全量数据
      - 频次可以比较高，开销小
    - Periodic Materialization (定期物化)
      - Flink 会将当前的完整状态（State Table）生成一个快照并上传
      - 使得在它之前的 Changelog 在恢复时不再需要被重放
      - 频次低，开销大
    - Delete log files
      - 在完成一次 Materialization（全量快照）之后，Flink 会清理（Delete/Truncate）那些已经被包含在全量快照中的旧 Changelog 文件

## [Task-Local Recovery](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/ops/state/large_state_tuning/#task-local-recovery)
Task-Local Recovery 通过在任务所在的 TaskManager 本地磁盘上保存状态的辅助副本，实现了：

- 状态存储机制
   - 主副本：仍然保存在远程分布式存储中（确保可靠性和一致性）
   - 辅助副本：保存在任务所在的 TaskManager 本地磁盘上（提高恢复速度）
- 智能恢复策略
   - 恢复时优先使用本地副本，减少网络传输
   - 如果本地副本不可用或恢复失败，自动透明地回退到远程副本
- 关键优势
   - 显著减少恢复时间：本地磁盘读取比网络传输快得多
   - 降低网络压力：减少远程存储的读取流量
- 适用场景
   - 具有大型状态的 Flink 作业
   - 对恢复时间敏感的生产环境
   - 网络带宽有限或远程存储延迟较高的场景
- Kubernetes 环境
  - K8s会重新创建新的Pod，新Pod的本地磁盘是全新的，原来的本地状态副本会丢失
  - 如果PVC使用了 hostPath local PV，Task-Local Recovery会有效果，除此之外理论上就是累赘


## ForSt (State Backend) TODO

## Reference
- [Restart Pipelined Region Failover Strategy](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/ops/state/task_failure_recovery/#failover-strategies) TODO
- [Tuning Checkpoints and Large State](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/ops/state/large_state_tuning/) TODO
