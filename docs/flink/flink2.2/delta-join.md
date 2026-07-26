# Delta Join
![delta-join.png](https://nightlies.apache.org/flink/flink-docs-master//fig/table-streaming/delta_join.png)
Flink 2.x 中的 Delta Join 实现，本质上是将 Flink 算子变成了一个带智能缓存的双向异步 Lookup 代理。它通过移除本地全量状态，解决了传统双流 Join 的状态膨胀问题，代价是引入了对外部存储的高并发依赖。

https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/tuning/#delta-joins
StreamingDeltaJoinOperator
AsyncDeltaJoinRunner
