# Support UPDATE/DELETE by _ROW_ID for row tracking

![RowWriter-fields-inconsistent](rowwriter-fields-inconsistent.png)

https://github.com/apache/paimon/pull/6335


> DELETE FROM t WHERE _ROW_ID = 2;
```text
Job aborted due to stage failure: Task 0 in stage 24.0 failed 4 times, most recent failure: Lost task 0.3 in stage 24.0 (TID 27) (10.1.3.90 executor 1): java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Long (java.lang.String and java.lang.Long are in module java.base of loader 'bootstrap')
	at scala.runtime.BoxesRunTime.unboxToLong(BoxesRunTime.java:107)
	at org.apache.spark.sql.Row.getLong(Row.scala:253)
	at org.apache.spark.sql.Row.getLong$(Row.scala:253)
	at org.apache.spark.sql.catalyst.expressions.GenericRow.getLong(rows.scala:27)
	at org.apache.paimon.spark.SparkRow.getLong(SparkRow.java:115)
	at org.apache.paimon.format.parquet.writer.ParquetRowDataWriter$LongWriter.write(ParquetRowDataWriter.java:263)
	at org.apache.paimon.format.parquet.writer.ParquetRowDataWriter$RowWriter.write(ParquetRowDataWriter.java:604)
	at org.apache.paimon.format.parquet.writer.ParquetRowDataWriter.write(ParquetRowDataWriter.java:88)
	at org.apache.paimon.format.parquet.writer.ParquetRowDataBuilder$ParquetWriteSupport.write(ParquetRowDataBuilder.java:83)
	at org.apache.paimon.format.parquet.writer.ParquetRowDataBuilder$ParquetWriteSupport.write(ParquetRowDataBuilder.java:57)
	at org.apache.paimon.shade.org.apache.parquet.hadoop.InternalParquetRecordWriter.write(InternalParquetRecordWriter.java:152)
	at org.apache.paimon.shade.org.apache.parquet.hadoop.ParquetWriter.write(ParquetWriter.java:132)
	at org.apache.paimon.format.parquet.writer.ParquetBulkWriter.addElement(ParquetBulkWriter.java:47)
	at org.apache.paimon.io.SingleFileWriter.writeImpl(SingleFileWriter.java:132)
	at org.apache.paimon.io.StatsCollectingSingleFileWriter.write(StatsCollectingSingleFileWriter.java:61)
	at org.apache.paimon.io.RowDataFileWriter.write(RowDataFileWriter.java:82)
	at org.apache.paimon.io.RowDataFileWriter.write(RowDataFileWriter.java:45)
	at org.apache.paimon.io.RollingFileWriter.write(RollingFileWriter.java:80)
	at org.apache.paimon.utils.SinkWriter$DirectSinkWriter.write(SinkWriter.java:75)
	at org.apache.paimon.append.AppendOnlyWriter.write(AppendOnlyWriter.java:176)
	at org.apache.paimon.append.AppendOnlyWriter.write(AppendOnlyWriter.java:66)
	at org.apache.paimon.operation.AbstractFileStoreWrite.write(AbstractFileStoreWrite.java:155)
	at org.apache.paimon.table.sink.TableWriteImpl.writeAndReturn(TableWriteImpl.java:190)
	at org.apache.paimon.table.sink.TableWriteImpl.writeAndReturn(TableWriteImpl.java:178)
	at org.apache.paimon.table.sink.TableWriteImpl.write(TableWriteImpl.java:156)
	at org.apache.paimon.spark.SparkTableWrite.write(SparkTableWrite.scala:55)
	at org.apache.paimon.spark.commands.PaimonSparkWriter.$anonfun$write$2(PaimonSparkWriter.scala:115)
	at org.apache.paimon.spark.commands.PaimonSparkWriter.$anonfun$write$2$adapted(PaimonSparkWriter.scala:115)
	at scala.collection.Iterator.foreach(Iterator.scala:943)
	at scala.collection.Iterator.foreach$(Iterator.scala:943)
	at scala.collection.AbstractIterator.foreach(Iterator.scala:1431)
	at org.apache.paimon.spark.commands.PaimonSparkWriter.$anonfun$write$1(PaimonSparkWriter.scala:115)
	at org.apache.spark.sql.execution.MapPartitionsExec.$anonfun$doExecute$3(objects.scala:198)
	at org.apache.spark.rdd.RDD.$anonfun$mapPartitionsInternal$2(RDD.scala:893)
	at org.apache.spark.rdd.RDD.$anonfun$mapPartitionsInternal$2$adapted(RDD.scala:893)
	at org.apache.spark.rdd.MapPartitionsRDD.compute(MapPartitionsRDD.scala:52)
	at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:367)
	at org.apache.spark.rdd.RDD.iterator(RDD.scala:331)
	at org.apache.spark.rdd.MapPartitionsRDD.compute(MapPartitionsRDD.scala:52)
	at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:367)
	at org.apache.spark.rdd.RDD.iterator(RDD.scala:331)
	at org.apache.spark.rdd.MapPartitionsRDD.compute(MapPartitionsRDD.scala:52)
	at org.apache.spark.rdd.RDD.computeOrReadCheckpoint(RDD.scala:367)
	at org.apache.spark.rdd.RDD.iterator(RDD.scala:331)
	at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:93)
	at org.apache.spark.TaskContext.runTaskWithListeners(TaskContext.scala:166)
	at org.apache.spark.scheduler.Task.run(Task.scala:141)
	at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$4(Executor.scala:620)
	at org.apache.spark.util.SparkErrorUtils.tryWithSafeFinally(SparkErrorUtils.scala:64)
	at org.apache.spark.util.SparkErrorUtils.tryWithSafeFinally$(SparkErrorUtils.scala:61)
	at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:94)
	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:623)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
	at java.base/java.lang.Thread.run(Unknown Source)
```

> mvn clean install -Dtest=RowTrackingTest -Pspark3 -DfailIfNoTests=false -rf :paimon-spark-3.5_2.12
> mvn clean install -DfailIfNoTests=false -Dtest=RowTrackingTest -Pspark3,flink1,scala-2.13 -rf :paimon-spark-3.3_2.13


