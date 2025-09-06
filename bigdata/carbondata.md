# carbondata

jdk1.8


>spark2-shell 	--executor-memory 20G --executor-cores 6 \
	--jars /home/dataengine/carbondata/lib/carbondata-bloom-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-common-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-core-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-format-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-hadoop-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-lucene-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-processing-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-search-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-spark2-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-spark-common-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-store-sdk-1.4.0.jar,/home/dataengine/carbondata/lib/carbondata-streaming-1.4.0.jar \
	--conf spark.yarn.appMasterEnv.JAVA_HOME="/opt/jdk1.8.0_45" \
	--conf spark.yarn.executorEnv.JAVA_HOME="/opt/jdk1.8.0_45" \
	--conf spark.dynamicAllocation.minExecutors=10 \
	--conf spark.dynamicAllocation.maxExecutors=15 \
	--conf spark.port.maxRetries=100 \
	--conf spark.yarn.executor.memoryOverhead=4069 \
	--conf spark.sql.shuffle.partitions=1000  \
	--conf spark.driver.userClassPathFirst=true     \
	--conf spark.executor.userClassPathFirst=true

spark2.1-cdh
http://apache-carbondata-dev-mailing-list-archive.1130556.n5.nabble.com/Re-Error-while-creating-table-in-carbondata-td25540.html

```
18/07/10 08:27:31 AUDIT table.CarbonCreateTableCommand: [bd04-067][dataengine][Thread-1]Creating Table with Database name [rp_dataengine] and Table name [rp_gapoi_ip_bssid_device_list_carbon3]
18/07/10 08:27:31 ERROR datamap.DataMapStoreManager: main failed to get carbon table from table Path
java.lang.NoSuchMethodError: org.apache.spark.sql.catalyst.catalog.CatalogTable.copy(Lorg/apache/spark/sql/catalyst/TableIdentifier;Lorg/apache/spark/sql/catalyst/catalog/CatalogTableType;Lorg/apache/spark/sql/catalyst/catalog/CatalogStorageFormat;Lorg/apache/spark/sql/types/StructType;Lscala/Option;Lscala/collection/Seq;Lscala/Option;Ljava/lang/String;JJLscala/collection/immutable/Map;Lscala/Option;Lscala/Option;Lscala/Option;Lscala/Option;Lscala/collection/Seq;Z)Lorg/apache/spark/sql/catalyst/catalog/CatalogTable;
  at org.apache.spark.sql.CarbonSource$.updateCatalogTableWithCarbonSchema(CarbonSource.scala:336)
  at org.apache.spark.sql.execution.strategy.DDLStrategy.apply(DDLStrategy.scala:228)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$1.apply(QueryPlanner.scala:62)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$1.apply(QueryPlanner.scala:62)
  at scala.collection.Iterator$$anon$12.nextCur(Iterator.scala:434)
  at scala.collection.Iterator$$anon$12.hasNext(Iterator.scala:440)
  at scala.collection.Iterator$$anon$12.hasNext(Iterator.scala:439)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner.plan(QueryPlanner.scala:92)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$2$$anonfun$apply$2.apply(QueryPlanner.scala:77)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$2$$anonfun$apply$2.apply(QueryPlanner.scala:74)
  at scala.collection.TraversableOnce$$anonfun$foldLeft$1.apply(TraversableOnce.scala:157)
  at scala.collection.TraversableOnce$$anonfun$foldLeft$1.apply(TraversableOnce.scala:157)
  at scala.collection.Iterator$class.foreach(Iterator.scala:893)
  at scala.collection.AbstractIterator.foreach(Iterator.scala:1336)
  at scala.collection.TraversableOnce$class.foldLeft(TraversableOnce.scala:157)
  at scala.collection.AbstractIterator.foldLeft(Iterator.scala:1336)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$2.apply(QueryPlanner.scala:74)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner$$anonfun$2.apply(QueryPlanner.scala:66)
  at scala.collection.Iterator$$anon$12.nextCur(Iterator.scala:434)
  at scala.collection.Iterator$$anon$12.hasNext(Iterator.scala:440)
  at org.apache.spark.sql.catalyst.planning.QueryPlanner.plan(QueryPlanner.scala:92)
  at org.apache.spark.sql.execution.QueryExecution.sparkPlan$lzycompute(QueryExecution.scala:79)
  at org.apache.spark.sql.execution.QueryExecution.sparkPlan(QueryExecution.scala:75)
  at org.apache.spark.sql.execution.QueryExecution.executedPlan$lzycompute(QueryExecution.scala:84)
  at org.apache.spark.sql.execution.QueryExecution.executedPlan(QueryExecution.scala:84)
  at org.apache.spark.sql.execution.QueryExecution.toRdd$lzycompute(QueryExecution.scala:87)
  at org.apache.spark.sql.execution.QueryExecution.toRdd(QueryExecution.scala:87)
  at org.apache.spark.sql.Dataset.<init>(Dataset.scala:185)
  at org.apache.spark.sql.CarbonSession$$anonfun$sql$1.apply(CarbonSession.scala:108)
  at org.apache.spark.sql.CarbonSession$$anonfun$sql$1.apply(CarbonSession.scala:97)
  at org.apache.spark.sql.CarbonSession.withProfiler(CarbonSession.scala:155)
  at org.apache.spark.sql.CarbonSession.sql(CarbonSession.scala:95)
  at org.apache.spark.sql.execution.command.table.CarbonCreateTableCommand.processMetadata(CarbonCreateTableCommand.scala:126)
  at org.apache.spark.sql.execution.command.MetadataCommand.run(package.scala:68)
  at org.apache.spark.sql.execution.command.ExecutedCommandExec.sideEffectResult$lzycompute(commands.scala:58)
  at org.apache.spark.sql.execution.command.ExecutedCommandExec.sideEffectResult(commands.scala:56)
  at org.apache.spark.sql.execution.command.ExecutedCommandExec.doExecute(commands.scala:74)
  at org.apache.spark.sql.execution.SparkPlan$$anonfun$execute$1.apply(SparkPlan.scala:114)
  at org.apache.spark.sql.execution.SparkPlan$$anonfun$execute$1.apply(SparkPlan.scala:114)
  at org.apache.spark.sql.execution.SparkPlan$$anonfun$executeQuery$1.apply(SparkPlan.scala:135)
  at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:151)
  at org.apache.spark.sql.execution.SparkPlan.executeQuery(SparkPlan.scala:132)
  at org.apache.spark.sql.execution.SparkPlan.execute(SparkPlan.scala:113)
  at org.apache.spark.sql.execution.QueryExecution.toRdd$lzycompute(QueryExecution.scala:87)
  at org.apache.spark.sql.execution.QueryExecution.toRdd(QueryExecution.scala:87)
  at org.apache.spark.sql.Dataset.<init>(Dataset.scala:185)
  at org.apache.spark.sql.CarbonSession$$anonfun$sql$1.apply(CarbonSession.scala:108)
  at org.apache.spark.sql.CarbonSession$$anonfun$sql$1.apply(CarbonSession.scala:97)
  at org.apache.spark.sql.CarbonSession.withProfiler(CarbonSession.scala:155)
  at org.apache.spark.sql.CarbonSession.sql(CarbonSession.scala:95)
  ... 68 elided
```


- https://myslide.cn/slides/274
- https://cwiki.apache.org/confluence/display/CARBONDATA/Multi+Level+Indexing

- [Apache顶级项目CarbonData应用实践与2.0新技术规划介绍](http://www.10tiao.com/html/157/201709/2653163167/1.html)
- [Carbondata源码系列（二）文件格式详解](https://cloud.tencent.com/developer/article/1047979)