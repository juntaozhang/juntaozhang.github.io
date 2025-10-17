# carbondata


[Carbondata使用过程中遇到的几个问题及解决办法](https://www.iteblog.com/archives/1992.html)

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
java 冲突
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

```
spark2-shell --driver-memory 4g --executor-memory 6G  --executor-cores 3     \
	 --conf 'spark.driver.extraJavaOptions=-XX:PermSize=128M -XX:MaxPermSize=256M'    \
	 --conf spark.dynamicAllocation.minExecutors=2     \
	 --conf spark.dynamicAllocation.maxExecutors=15     \
	 --conf spark.port.maxRetries=100     \
	 --conf spark.yarn.executor.memoryOverhead=4069     \
	 --conf spark.sql.shuffle.partitions=1000      \
	 --jars /home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-bloom-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-common-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-core-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-format-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-hadoop-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-lucene-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-processing-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-search-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-spark2-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-spark-common-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-store-sdk-1.4.0.jar,/home/dataengine/carbondata_2.11-1.4.0-dist/carbondata-streaming-1.4.0.jar

import org.apache.spark.sql.CarbonSession._
import org.apache.spark.sql.SparkSession
val carbon = SparkSession.builder().getOrCreateCarbonSession("hdfs://ShareSdkHadoop/user/dataengine/carbondata/")

carbon.sql(
      s"""
         |CREATE TABLE IF NOT EXISTS rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon3 (
         |  `device` string,
         |  `mac` string,
         |  `imei` string,
         |  `clienttime` string,
         |  `clientip` string,
         |  `bssid` string,
         |  `ssid` string,
         |  `flag_mapping` int,
         |  `plat` int)
         |PARTITIONED BY (day STRING)
         |stored by 'carbondata' 
         |tblproperties(
         |'SORT_COLUMNS'='clienttime,clientip,bssid,device,imei,mac,flag_mapping,plat',
         |'DICTIONARY_INCLUDE'='clienttime,flag_mapping,plat',
         |'SORT_SCOPE'='GLOBAL_SORT'
         |)
      """.stripMargin)

carbon.sql(
    s"""
		|CREATE DATAMAP IF NOT EXISTS rp_gapoi_ip_bssid_device_list_carbon_datamap
		|ON TABLE rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon
		|USING 'bloomfilter'
		|DMPROPERTIES ('index_columns'='device, clientip', 'BLOOM_SIZE'='100000000', 'BLOOM_FPP'='0.0001', 'BLOOM_COMPRESS'='true')
	  """.stripMargin)

carbon.sql(
      """
        |INSERT OVERWRITE TABLE rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon2
        |select device,mac,imei,clienttime,clientip,bssid,ssid,flag_mapping,plat,day from test.rp_gapoi_ip_bssid_device_list
      """.stripMargin)

carbon.sql("select * from rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon2 where clientip='111.144.206.112' ").show(false)
carbon.sql("select * from rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon2 where imei='864129035644386'").show(false)
carbon.sql("select * from rp_dataengine.rp_gapoi_ip_bssid_device_list_carbon2 where clienttime='2018-07-07 13:39:19' and bssid='b0:95:8e:36:71:ba'").show(false)
```


# core dump
```
[Stage 7:=====================>                               (117 + 145) / 286]18/08/03 10:12:15 ERROR cluster.YarnScheduler: Lost executor 29 on bd15-161-103: Container marked as failed: container_e41_1531196858550_11452_01_000035 on host: bd15-161-103. Exit status: 134. Diagnostics: Exception from container-launch.
Container id: container_e41_1531196858550_11452_01_000035
Exit code: 134
Exception message: /bin/bash: line 1:  6501 Aborted                 (core dumped) LD_LIBRARY_PATH=/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/../../../CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/../../../GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/impala/lib:/opt/cloudera/parcels/GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native /opt/jdk1.8.0_45/bin/java -server -Xmx10240m '-XX:+UseG1GC' '-XX:MaxNewSize=5g' '-Dcarbon.properties.filepath=/data/jars/carbondata_2.11-1.4.0-dist/carbon.properties' -Djava.io.tmpdir=/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/tmp '-Dspark.port.maxRetries=30' '-Dspark.driver.port=24769' '-Dspark.authenticate=false' '-Dspark.shuffle.service.port=7337' -Dspark.yarn.app.container.log.dir=/var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035 -XX:OnOutOfMemoryError='kill %p' org.apache.spark.executor.CoarseGrainedExecutorBackend --driver-url spark://CoarseGrainedScheduler@10.6.160.215:24769 --executor-id 29 --hostname bd15-161-103 --cores 4 --app-id application_1531196858550_11452 --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/__app__.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/snappy-java-1.1.2.6.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-bloom-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-common-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-core-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-format-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-hadoop-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-lucene-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-processing-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-search-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-spark2-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-spark-common-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-store-sdk-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-streaming-1.4.0.jar > /var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/stdout 2> /var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/stderr

Stack trace: ExitCodeException exitCode=134: /bin/bash: line 1:  6501 Aborted                 (core dumped) LD_LIBRARY_PATH=/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/../../../CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/../../../GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/impala/lib:/opt/cloudera/parcels/GPLEXTRAS-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native:/opt/cloudera/parcels/CDH-5.7.6-1.cdh5.7.6.p0.6/lib/hadoop/lib/native /opt/jdk1.8.0_45/bin/java -server -Xmx10240m '-XX:+UseG1GC' '-XX:MaxNewSize=5g' '-Dcarbon.properties.filepath=/data/jars/carbondata_2.11-1.4.0-dist/carbon.properties' -Djava.io.tmpdir=/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/tmp '-Dspark.port.maxRetries=30' '-Dspark.driver.port=24769' '-Dspark.authenticate=false' '-Dspark.shuffle.service.port=7337' -Dspark.yarn.app.container.log.dir=/var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035 -XX:OnOutOfMemoryError='kill %p' org.apache.spark.executor.CoarseGrainedExecutorBackend --driver-url spark://CoarseGrainedScheduler@10.6.160.215:24769 --executor-id 29 --hostname bd15-161-103 --cores 4 --app-id application_1531196858550_11452 --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/__app__.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/snappy-java-1.1.2.6.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-bloom-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-common-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-core-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-format-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-hadoop-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-lucene-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-processing-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-search-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-spark2-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-spark-common-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-store-sdk-1.4.0.jar --user-class-path file:/data/5/yarn/nm/usercache/dataengine/appcache/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/carbondata-streaming-1.4.0.jar > /var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/stdout 2> /var/log/hadoop-yarn/container/application_1531196858550_11452/container_e41_1531196858550_11452_01_000035/stderr

  at org.apache.hadoop.util.Shell.runCommand(Shell.java:601)
  at org.apache.hadoop.util.Shell.run(Shell.java:504)
  at org.apache.hadoop.util.Shell$ShellCommandExecutor.execute(Shell.java:786)
  at org.apache.hadoop.yarn.server.nodemanager.DefaultContainerExecutor.launchContainer(DefaultContainerExecutor.java:213)
  at org.apache.hadoop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch.call(ContainerLaunch.java:302)
  at org.apache.hadoop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch.call(ContainerLaunch.java:82)
  at java.util.concurrent.FutureTask.run(FutureTask.java:262)
  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
  at java.lang.Thread.run(Thread.java:745)


Container exited with a non-zero exit code 134

```


- https://myslide.cn/slides/274
- https://cwiki.apache.org/confluence/display/CARBONDATA/Multi+Level+Indexing

- [Apache顶级项目CarbonData应用实践与2.0新技术规划介绍](http://www.10tiao.com/html/157/201709/2653163167/1.html)
- [Carbondata源码系列（二）文件格式详解](https://cloud.tencent.com/developer/article/1047979)