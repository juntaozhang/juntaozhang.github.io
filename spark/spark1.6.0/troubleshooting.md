# troubleshooting

## java.util.NoSuchElementException: key not found: 356

```text

18/06/27 11:57:33 INFO InMemoryColumnarTableScan: Predicate (userid#5803 = 354_5b32f89077bf7efe33b9d1b1) generates partition filter: ((userid.lowerBound#6191 <= 354_5b32f89077bf7efe33b9d1b1) && (354_5b32f89077bf7efe33b9d1b1 <= userid.upperBound#6190))
18/06/27 11:57:33 INFO SparkContext: Starting job: rdd at OutputUtils.scala:64
18/06/27 11:57:33 INFO DAGScheduler: Got job 25 (rdd at OutputUtils.scala:64) with 1200 output partitions
18/06/27 11:57:33 INFO DAGScheduler: Final stage: ResultStage 308 (rdd at OutputUtils.scala:64)
18/06/27 11:57:33 INFO DAGScheduler: Parents of final stage: List(ShuffleMapStage 302, ShuffleMapStage 307)
18/06/27 11:57:33 INFO DAGScheduler: Missing parents: List()
18/06/27 11:57:33 INFO DAGScheduler: Submitting ResultStage 308 (MapPartitionsRDD[426] at rdd at OutputUtils.scala:64), which has no missing parents
18/06/27 11:57:33 INFO MemoryStore: Block broadcast_78 stored as values in memory (estimated size 58.2 KB, free 5.0 MB)
18/06/27 11:57:33 INFO MemoryStore: Block broadcast_78_piece0 stored as bytes in memory (estimated size 24.1 KB, free 5.0 MB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on 10.6.160.213:45603 (size: 24.1 KB, free: 9.6 GB)
18/06/27 11:57:33 INFO SparkContext: Created broadcast 78 from broadcast at DAGScheduler.scala:1006
18/06/27 11:57:33 INFO DAGScheduler: Submitting 1200 missing tasks from ResultStage 308 (MapPartitionsRDD[426] at rdd at OutputUtils.scala:64)
18/06/27 11:57:33 INFO YarnClusterScheduler: Adding task set 308.0 with 1200 tasks
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-019.yzdns.com:28492 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd15-161-106:36922 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-017.yzdns.com:37954 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-020.yzdns.com:61136 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd15-161-106:21701 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-037.yzdns.com:44483 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-031.yzdns.com:40602 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-026.yzdns.com:27867 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-038.yzdns.com:19196 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd15-161-107:25332 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-013.yzdns.com:39283 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-018.yzdns.com:32081 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-011.yzdns.com:24708 (size: 24.1 KB, free: 3.5 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-026.yzdns.com:40094 (size: 24.1 KB, free: 3.5 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-030.yzdns.com:13952 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-005.yzdns.com:45131 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-018.yzdns.com:27394 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 INFO BlockManagerInfo: Added broadcast_78_piece0 in memory on bd041-025.yzdns.com:14438 (size: 24.1 KB, free: 3.4 GB)
18/06/27 11:57:33 WARN Accumulators: Ignoring accumulator update for unknown accumulator id 356
18/06/27 11:57:33 ERROR DAGScheduler: Failed to update accumulators for ResultTask(308, 35)
java.util.NoSuchElementException: key not found: 356
	at scala.collection.MapLike$class.default(MapLike.scala:228)
	at scala.collection.AbstractMap.default(Map.scala:58)
	at scala.collection.mutable.HashMap.apply(HashMap.scala:64)
	at org.apache.spark.scheduler.DAGScheduler$$anonfun$updateAccumulators$1.apply(DAGScheduler.scala:1085)
	at org.apache.spark.scheduler.DAGScheduler$$anonfun$updateAccumulators$1.apply(DAGScheduler.scala:1081)
	at scala.collection.mutable.HashMap$$anonfun$foreach$1.apply(HashMap.scala:98)
	at scala.collection.mutable.HashMap$$anonfun$foreach$1.apply(HashMap.scala:98)
	at scala.collection.mutable.HashTable$class.foreachEntry(HashTable.scala:226)
	at scala.collection.mutable.HashMap.foreachEntry(HashMap.scala:39)
	at scala.collection.mutable.HashMap.foreach(HashMap.scala:98)
	at org.apache.spark.scheduler.DAGScheduler.updateAccumulators(DAGScheduler.scala:1081)
	at org.apache.spark.scheduler.DAGScheduler.handleTaskCompletion(DAGScheduler.scala:1151)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.doOnReceive(DAGScheduler.scala:1639)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1601)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1590)
	at org.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:48)
18/06/27 11:57:33 WARN Accumulators: Ignoring accumulator update for unknown accumulator id 356
18/06/27 11:57:33 ERROR DAGScheduler: Failed to update accumulators for ResultTask(308, 45)
java.util.NoSuchElementException: key not found: 356
	at scala.collection.MapLike$class.default(MapLike.scala:228)
	at scala.collection.AbstractMap.default(Map.scala:58)
	at scala.collection.mutable.HashMap.apply(HashMap.scala:64)
	at org.apache.spark.scheduler.DAGScheduler$$anonfun$updateAccumulators$1.apply(DAGScheduler.scala:1085)
	at org.apache.spark.scheduler.DAGScheduler$$anonfun$updateAccumulators$1.apply(DAGScheduler.scala:1081)
	at scala.collection.mutable.HashMap$$anonfun$foreach$1.apply(HashMap.scala:98)
	at scala.collection.mutable.HashMap$$anonfun$foreach$1.apply(HashMap.scala:98)
	at scala.collection.mutable.HashTable$class.foreachEntry(HashTable.scala:226)
	at scala.collection.mutable.HashMap.foreachEntry(HashMap.scala:39)
	at scala.collection.mutable.HashMap.foreach(HashMap.scala:98)
	at org.apache.spark.scheduler.DAGScheduler.updateAccumulators(DAGScheduler.scala:1081)
	at org.apache.spark.scheduler.DAGScheduler.handleTaskCompletion(DAGScheduler.scala:1151)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.doOnReceive(DAGScheduler.scala:1639)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1601)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1590)
	at org.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:48)
18/06/27 11:57:33 WARN Accumulators: Ignoring accumulator update for unknown accumulator id 356

```