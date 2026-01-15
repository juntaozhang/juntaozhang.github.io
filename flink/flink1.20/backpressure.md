# Backpressure
## 什么是反压？

- 当下游算子处理速度赶不上上游产生数据的速度时，所产生的一种“反馈式阻塞机制”。
- 以一个简单的Source -> Sink作业为例。如果您看到关于Source的警告，这意味着Sink消耗数据的速度慢于Source生成数据的速度。Sink正在对上游算子Source施加反压。

### credit-based backpressure

- 下游会告诉上游：“我还有 N 个空位，你可以发 N 条数据过来”
- 上游每发送一条数据，就消耗一个 Credit

## Metrics

空闲任务为蓝色，完全背压任务为黑色，完全繁忙任务为红色。介于这些状态之间的所有值都用这三种颜色之间的色调表示。\
![back_pressure_job_graph](https://nightlies.apache.org/flink/flink-docs-release-1.20/fig/back_pressure_job_graph.png)

![back_pressure_subtasks](https://nightlies.apache.org/flink/flink-docs-release-1.20/fig/back_pressure_subtasks.png)
Flink Web UI 的 Backpressure  Status页面显示 “HIGH”， 查看Backpressured / Idle / Busy 这几个指标：
- Backpressured：任务因输出缓冲区不足而被阻塞的时间比例（Output Buffer 满 / 无 Credit 时判定为反压）
  - 当前算子如果背压说明是下游算子处理能力不足
  - 如果下游算子因为内外部资源导致处理不过来，它本身是不会有反压的
  - Backpressured + Idle + Busy = 100% 
  - source Busy 为NaN
- Idle：任务等待数据输入的时间比例（无输入数据且不反压时判定为空闲）（Input Gate 空）
- Busy：任务实际处理数据的时间比例（既不空闲也不反压时判定为繁忙）
  - CPU处理时间
  - 外部网络请求等待时间（如HTTP请求、API调用）
  - 文件I/O操作时间
  - 数据库I/O操作时间

状态指标采用5秒更新一次、60秒滑动窗口的计算方式
通过 TimerGauge 记录状态持续时间，支持处理跨周期的持续状态，并将结果以每秒平均时间的形式展示

状态互斥性：在 StreamTask.java 的 processInput() 方法中，三个状态是严格互斥的，顺序是：Backpressured -> Idle -> Busy

Backpressured / Idle / Busy = 80% / 0% / 20%
- 处理速度（Busy 20%）跟不上数据流入的速度
  - 它的输出缓冲区（Output Buffer）一直被填满 --> 说明下游反压

Backpressured / Idle / Busy = 80% / 20% / 0%
- 过程：0%/60%/40% -> 40%/40%/20% -> 80%/20%/0% -> 最后状态：100%/0%/0%
- 20% Idle：因为某种原因停止了发数据
- 80% Backpressured：它有 80% 时间在等待输出缓冲区可用

TODO 这个指标只是一个周期窗口的变化值，主要还是看趋于稳定后的值，而非变化过程值
## RCA
- data skew
- cpu I/O 瓶颈
- Checkpoint对齐阻塞
    - Checkpoint Barrier 的对齐机制要求所有输入通道（Input Channels）都必须收到 Barrier 才能继续。
    - 快流反压是表象：它是因为慢流没跟上节奏而被阻塞的
- 严重的Checkpoint超时 （TODO）


# Reference
- https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/ops/monitoring/back_pressure/