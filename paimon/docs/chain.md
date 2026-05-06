# Chain Table

## Example
| case：查询  | main 有数据 | snapshot 有数据 | 找到前置快照    | 读取策略   | snapshot 分区 | delta 分区范围    | 最终数据来源         |
|----------|----------|--------------|-----------|--------|------------|---------------|----------------|
| hour=21  | ✗ 无      | ✗ 无          | ✗ 无       | 【路径 C】只读 delta | -          | [21,22,23,24] | delta(21)      |
| hour=22  | ✓ 有      | ✓ 有          | -         | 【路径 A】直接读 main | [22]       | [21,22,23,24] | main(22)       |
| hour=22  | ✗ 无      | ✓ 有          | -         | 【路径 B】只读 snapshot | [22]     |[21,22,23,24]  | snapshot(22)       |
| hour=23  | ✗ 无      | ✗ 无          | ✓ hour=22 | 【路径 D】Chain Read | [22]       | [21,22,23,24] | snapshot(22) + delta(23) |
| *hour=24 | ✗ 无      | ✗ 无          | ✓ hour=22 | 【路径 D】Chain Read | [22]       | [21,22,23,24] | snapshot(22) + delta(23,24) |

### *hour=24 示例
```text
  查询分区 X (如 *hour=24)
    ↓
  ┌─────────────────────────────────┐
  │  Step 1: 检查 main 分支          │
  └─────────────────────────────────┘
    ↓
    ├─ ✓ main 有分区 X
    │     ↓
    │  【路径 A】直接返回 main 数据
    │     结果：main(X)
    │
    └─ ✗ main 无分区 X
          ↓
    ┌─────────────────────────────────┐
    │  Step 2: 检查 snapshot 分支      │
    └─────────────────────────────────┘
      ↓
      ├─ ✓ snapshot 有分区 X
      │     ↓
      │  【路径 B】直接返回 snapshot 数据
      │     结果：snapshot(X)
      │
      └─ ✗ snapshot 无分区 X
            ↓
      ┌─────────────────────────────────┐
      │  Step 3: 查找前置快照             │
      │  (snapshot 中 < X 的最大分区)     │
      └─────────────────────────────────┘
        ↓
        ├─ ✗ 没有前置快照
        │     ↓
        │  【路径 C】只读 delta
        │     结果：delta(X)
        │
        └─ ✓ 找到前置快照 S (如 hour=22)
              ↓
        【路径 D】Chain Read
               读取范围：snapshot(S) + delta(S+1 ~ X)
               结果：基于 primary-key 合并去重
```

### Delta INSERT OVERWRITE
```text
  INSERT OVERWRITE `t$branch_snapshot` PARTITION (date = '20250810')
  VALUES ('1', '1', '1');
  结果：
  snapshot(date='20250810'):
    t1='1', t2='1', t3='1'

  INSERT OVERWRITE `t$branch_delta` PARTITION (date = '20250811')
  VALUES ('2', '2-2', '2-2'), ('3', '3', '3');
  结果：
  delta(date='20250811'):
    t1='2', t2='2-2', t3='2-2'
    t1='3', t2='3', t3='3'

  snapshot(date='20250810'):
    t1='1', t2='1', t3='1'
  snapshot(date='20250811'):
    t1='2', t2='2', t3='2'
    t1='3', t2='3', t3='3'
```

> SELECT t1, t2, t3 FROM `t` WHERE date = '20250811'
```text
  Chain Read 路径：
  1. main 没有 date='20250811' ✗
  2. snapshot 没有 date='20250811' ✗
  3. 找前置快照：snapshot(date='20250810') ✓
  4. 合并：snapshot('20250810') + delta('20250811')

  结果：
  +---+----+----+
  | t1| t2 | t3 |
  +---+----+----+
  |  1|  1 |  1 |  ← 来自 snapshot('20250810')
  |  2|2-2 |2-2 |  ← 来自 delta('20250811')，主键去重
  |  3|  3 |  3 |  ← 来自 delta('20250811')
  +---+----+----+
```

> SELECT * FROM `t$branch_delta` WHERE date = '20250811'
```text
  结果：只有 delta 的数据 
  +---+----+----+
  |  2|2-2 |2-2 |
  |  3|  3 |  3 |
  +---+----+----+
```

```sql
  INSERT OVERWRITE `t$branch_snapshot` PARTITION (date = '20250811')
  VALUES ('1', '1', '1'), ('2', '2', '2');
  结果：
  snapshot(date='20250811'):
    t1='1', t2='1', t3='1'
    t1='2', t2='2', t3='2'
```

> SELECT * FROM `t` WHERE date = '20250811'
```text
+---+---+---+
|t1 |t2 |t3 |
+---+---+---+
|1  |1  |1  |
|2  |2-1|2-1|
+---+---+---+
```
> SELECT * FROM `t$branch_snapshot` WHERE date = '20250811'
```text
+---+---+---+--------+
|t1 |t2 |t3 |date    |
+---+---+---+--------+
|1  |1  |1  |20250811|
|2  |2-1|2-1|20250811|
+---+---+---+--------+
```


## Deep Dive into Chain Table
> select t1, t2, t3 from `t` where date = '20250811'

```text
spark load table, create FallbackReadFileStoreTable:
loadTable:300, SparkCatalog (org.apache.paimon.spark)
└── loadSparkTable:657, SparkCatalog (org.apache.paimon.spark)
    └── getTable:254, CachingCatalog (org.apache.paimon.catalog)
        └── getTable:484, AbstractCatalog (org.apache.paimon.catalog)
            └── loadTable:291, CatalogUtils (org.apache.paimon.catalog)
                └── create:87, FileStoreTableFactory (org.apache.paimon.table)
                    └── create:103, FileStoreTableFactory (org.apache.paimon.table)
                        └── createChainTable:192, FileStoreTableFactory (org.apache.paimon.table)
```

### FallbackReadFileStoreTable
```text
  FallbackReadFileStoreTable (外层)
  ├── wrapped: table (主分支，如 'main')
  └── fallback: ChainGroupReadTable (内层)
      ├── wrapped: snapshotTable (快照分支，如 'branch_snapshot')
      └── fallback: deltaTable (增量分支，如 'branch_delta')
```

所以 内层的 fallback 就是增量分支表，这个设计实现了三级数据回退：
1. main（主分支，最新全量）
2. branch_snapshot（快照分支，历史全量）
3. branch_delta（增量分支，增量补充）

### PaimonScan

create FallbackReadScan

```text
outputPartitioning:111, PaimonScan (org.apache.paimon.spark)
└── extractBucketTransform:55, PaimonScan (org.apache.paimon.spark)
    └── extractBucketTransform$lzycompute:72, PaimonScan (org.apache.paimon.spark)
        └── extractBucketNumber:91, PaimonScan (org.apache.paimon.spark)
            └── inputSplits:35, PaimonBaseScan (org.apache.paimon.spark)
                    └── inputSplits:43, PaimonSupportsRuntimeFiltering (org.apache.paimon.spark.read)
                        └── getInputSplits:43, PaimonBaseScan (org.apache.paimon.spark)
                            ├── newScan:179, ReadBuilderImpl (org.apache.paimon.table.source)
Create TableScan            │    └── newScan:192, FallbackReadFileStoreTable (org.apache.paimon.table)
Get InputSplits             └── plan()

tableScan = FallbackReadFileStoreTable$FallbackReadScan@26574
├─ mainScan               = DataTableBatchScan@26576                       --- 【main】
└─ fallbackScan           = ChainGroupReadTable$ChainTableBatchScan@26561
   └─ chainGroupReadTable = ChainGroupReadTable@21601
      ├─ mainScan         = DataTableBatchScan@26562                       --- 【snapshot】
      └─ fallbackScan     = DataTableBatchScan@26563                       --- 【delta】
```

tableScan.plan() 逻辑见 ***hour=24 示例** 


## Troubleshooting
<details>
<summary>[fs] Fix AWS SDK miss jar s3-transfer-manager</summary>

https://github.com/apache/paimon/issues/7303

```scala
  val spark = SparkSession.builder
    .appName("Spark Paimon Example")
    .master("local[*]")
    .config("spark.default.parallelism", "1")
    .config("spark.sql.shuffle.partitions", "2")
    .config(
      "spark.sql.extensions",
      "org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions")
    .config("spark.sql.codegen.wholeStage", "false")
    .config("spark.sql.catalog.paimon", "org.apache.paimon.spark.SparkCatalog")
    .config("spark.sql.catalog.paimon.warehouse", "s3://warehouse/paimon")
    .config("spark.sql.catalog.paimon.s3.path.style.access", "true")
    .config("spark.sql.catalog.paimon.s3.access-key", "test")
    .config("spark.sql.catalog.paimon.s3.secret-key", "11111111")
    .config("spark.sql.catalog.paimon.s3.endpoint", "http://localhost:9000")
    .getOrCreate
  spark.sql("USE paimon")
```

```text
spark@spark-client-6989dd9c57-9kjvv:/opt/spark/work-dir$ export DRIVER_HOST=$(ifconfig | awk '/inet / && $2!="127.0.0.1"{print $2; exit}')
spark@spark-client-6989dd9c57-9kjvv:/opt/spark/work-dir$ export MY_S3_ENDPOINT=http://rustfs-svc:9000
spark@spark-client-6989dd9c57-9kjvv:/opt/spark/work-dir$ ../bin/spark-sql \
>     --master k8s://https://kubernetes.docker.internal:6443 \
>     --deploy-mode client \
>     --name spark-sql \
>     --conf spark.driver.host=$DRIVER_HOST \
>     --conf spark.kubernetes.file.upload.path=s3://spark-bucket/spark-deps \
>     --conf spark.sql.catalog.paimon.s3.path.style.access=true \
>     --conf spark.sql.catalog.paimon.s3.endpoint=$MY_S3_ENDPOINT \
>     --conf spark.sql.catalog.paimon.s3.access-key=test \
>     --conf spark.sql.catalog.paimon.s3.secret-key=11111111 \
>     --conf spark.sql.catalog.paimon=org.apache.paimon.spark.SparkCatalog \
>     --conf spark.sql.catalog.paimon.warehouse=s3://warehouse/paimon \
>     --conf spark.sql.extensions=org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions \
>     --conf spark.executor.instances=2 \
>     --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-operator-spark \
>     --conf spark.kubernetes.appKillPodDeletionGracePeriod=300 \
>     --conf spark.kubernetes.container.image=my-spark:3.5.5-paimon-1.4-4
26/02/25 09:54:53 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
26/02/25 09:55:02 WARN HiveConf: HiveConf of name hive.stats.jdbc.timeout does not exist
26/02/25 09:55:02 WARN HiveConf: HiveConf of name hive.stats.retries.wait does not exist
26/02/25 09:55:05 WARN ObjectStore: Version information not found in metastore. hive.metastore.schema.verification is not enabled so recording the schema version 2.3.0
26/02/25 09:55:05 WARN ObjectStore: setMetaStoreSchemaVersion called but recording version is disabled: version = 2.3.0, comment = Set by MetaStore UNKNOWN@10.1.2.184
26/02/25 09:55:05 WARN ObjectStore: Failed to get database default, returning NoSuchObjectException
Spark Web UI available at http://10.1.2.184:4040
Spark master: k8s://https://kubernetes.docker.internal:6443, Application Id: spark-2b199adbbb284e6ca9d66d006a02bc4c
26/02/25 09:55:07 WARN SparkSQLCLIDriver: WARNING: Directory for Hive history file: /home/spark does not exist.   History will not be available during this session.
spark-sql (default)> USE paimon;
26/02/25 09:55:09 WARN HadoopUtils: Could not find Hadoop configuration via any of the supported methods
26/02/25 09:55:09 ERROR SparkSQLDriver: Failed in [USE paimon]
java.lang.NoClassDefFoundError: software/amazon/awssdk/transfer/s3/model/ObjectTransfer
	at org.apache.paimon.s3.S3FileIO.lambda$createFileSystem$0(S3FileIO.java:137)
	at java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(Unknown Source)
	at org.apache.paimon.s3.S3FileIO.createFileSystem(S3FileIO.java:121)
	at org.apache.paimon.s3.HadoopCompliantFileIO.getFileSystem(HadoopCompliantFileIO.java:147)
	at org.apache.paimon.s3.HadoopCompliantFileIO.exists(HadoopCompliantFileIO.java:104)
	at org.apache.paimon.fs.PluginFileIO.lambda$exists$4(PluginFileIO.java:71)
	at org.apache.paimon.fs.PluginFileIO.wrap(PluginFileIO.java:108)
	at org.apache.paimon.fs.PluginFileIO.exists(PluginFileIO.java:71)
	at org.apache.paimon.fs.FileIO.checkOrMkdirs(FileIO.java:303)
	at org.apache.paimon.catalog.CatalogFactory.createUnwrappedCatalog(CatalogFactory.java:98)
	at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:71)
	at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:67)
	at org.apache.paimon.spark.SparkCatalog.initialize(SparkCatalog.java:132)
	at org.apache.spark.sql.connector.catalog.Catalogs$.load(Catalogs.scala:65)
	at org.apache.spark.sql.connector.catalog.CatalogManager.$anonfun$catalog$1(CatalogManager.scala:54)
	at scala.collection.mutable.HashMap.getOrElseUpdate(HashMap.scala:86)
	at org.apache.spark.sql.connector.catalog.CatalogManager.catalog(CatalogManager.scala:54)
	at org.apache.spark.sql.connector.catalog.LookupCatalog$CatalogAndNamespace$.unapply(LookupCatalog.scala:86)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs$$anonfun$apply$1.applyOrElse(ResolveCatalogs.scala:51)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs$$anonfun$apply$1.applyOrElse(ResolveCatalogs.scala:30)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$2(AnalysisHelper.scala:170)
	at org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(origin.scala:76)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$1(AnalysisHelper.scala:170)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.allowInvokingTransformsInAnalyzer(AnalysisHelper.scala:323)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning(AnalysisHelper.scala:168)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning$(AnalysisHelper.scala:164)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsDownWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$4(AnalysisHelper.scala:175)
	at org.apache.spark.sql.catalyst.trees.UnaryLike.mapChildren(TreeNode.scala:1216)
	at org.apache.spark.sql.catalyst.trees.UnaryLike.mapChildren$(TreeNode.scala:1215)
	at org.apache.spark.sql.catalyst.plans.logical.SetCatalogAndNamespace.mapChildren(v2Commands.scala:941)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$1(AnalysisHelper.scala:175)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.allowInvokingTransformsInAnalyzer(AnalysisHelper.scala:323)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning(AnalysisHelper.scala:168)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning$(AnalysisHelper.scala:164)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsDownWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsWithPruning(AnalysisHelper.scala:99)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsWithPruning$(AnalysisHelper.scala:96)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperators(AnalysisHelper.scala:76)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperators$(AnalysisHelper.scala:75)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperators(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs.apply(ResolveCatalogs.scala:30)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs.apply(ResolveCatalogs.scala:27)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$2(RuleExecutor.scala:222)
	at scala.collection.LinearSeqOptimized.foldLeft(LinearSeqOptimized.scala:126)
	at scala.collection.LinearSeqOptimized.foldLeft$(LinearSeqOptimized.scala:122)
	at scala.collection.immutable.List.foldLeft(List.scala:91)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$1(RuleExecutor.scala:219)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$1$adapted(RuleExecutor.scala:211)
	at scala.collection.immutable.List.foreach(List.scala:431)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.execute(RuleExecutor.scala:211)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.org$apache$spark$sql$catalyst$analysis$Analyzer$$executeSameContext(Analyzer.scala:240)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.$anonfun$execute$1(Analyzer.scala:236)
	at org.apache.spark.sql.catalyst.analysis.AnalysisContext$.withNewAnalysisContext(Analyzer.scala:187)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.execute(Analyzer.scala:236)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.execute(Analyzer.scala:202)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$executeAndTrack$1(RuleExecutor.scala:182)
	at org.apache.spark.sql.catalyst.QueryPlanningTracker$.withTracker(QueryPlanningTracker.scala:89)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.executeAndTrack(RuleExecutor.scala:182)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.$anonfun$executeAndCheck$1(Analyzer.scala:223)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.markInAnalyzer(AnalysisHelper.scala:330)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.executeAndCheck(Analyzer.scala:222)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$analyzed$1(QueryExecution.scala:77)
	at org.apache.spark.sql.catalyst.QueryPlanningTracker.measurePhase(QueryPlanningTracker.scala:138)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$executePhase$2(QueryExecution.scala:219)
	at org.apache.spark.sql.execution.QueryExecution$.withInternalError(QueryExecution.scala:546)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$executePhase$1(QueryExecution.scala:219)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.execution.QueryExecution.executePhase(QueryExecution.scala:218)
	at org.apache.spark.sql.execution.QueryExecution.analyzed$lzycompute(QueryExecution.scala:77)
	at org.apache.spark.sql.execution.QueryExecution.analyzed(QueryExecution.scala:74)
	at org.apache.spark.sql.execution.QueryExecution.assertAnalyzed(QueryExecution.scala:66)
	at org.apache.spark.sql.Dataset$.$anonfun$ofRows$2(Dataset.scala:99)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.Dataset$.ofRows(Dataset.scala:97)
	at org.apache.spark.sql.SparkSession.$anonfun$sql$4(SparkSession.scala:691)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:682)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:713)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:744)
	at org.apache.spark.sql.SQLContext.sql(SQLContext.scala:651)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLDriver.run(SparkSQLDriver.scala:68)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processCmd(SparkSQLCLIDriver.scala:501)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1(SparkSQLCLIDriver.scala:619)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1$adapted(SparkSQLCLIDriver.scala:613)
	at scala.collection.Iterator.foreach(Iterator.scala:943)
	at scala.collection.Iterator.foreach$(Iterator.scala:943)
	at scala.collection.AbstractIterator.foreach(Iterator.scala:1431)
	at scala.collection.IterableLike.foreach(IterableLike.scala:74)
	at scala.collection.IterableLike.foreach$(IterableLike.scala:73)
	at scala.collection.AbstractIterable.foreach(Iterable.scala:56)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processLine(SparkSQLCLIDriver.scala:613)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver$.main(SparkSQLCLIDriver.scala:310)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.main(SparkSQLCLIDriver.scala)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
	at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:1034)
	at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:199)
	at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:222)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:91)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1125)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1134)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.lang.ClassNotFoundException: software.amazon.awssdk.transfer.s3.model.ObjectTransfer
	at java.base/java.net.URLClassLoader.findClass(Unknown Source)
	at java.base/java.lang.ClassLoader.loadClass(Unknown Source)
	at org.apache.paimon.plugin.ComponentClassLoader.loadClassFromComponentOnly(ComponentClassLoader.java:126)
	at org.apache.paimon.plugin.ComponentClassLoader.loadClass(ComponentClassLoader.java:105)
	at java.base/java.lang.ClassLoader.loadClass(Unknown Source)
	... 107 more
software/amazon/awssdk/transfer/s3/model/ObjectTransfer
java.lang.NoClassDefFoundError: software/amazon/awssdk/transfer/s3/model/ObjectTransfer
	at org.apache.paimon.s3.S3FileIO.lambda$createFileSystem$0(S3FileIO.java:137)
	at java.base/java.util.concurrent.ConcurrentHashMap.computeIfAbsent(Unknown Source)
	at org.apache.paimon.s3.S3FileIO.createFileSystem(S3FileIO.java:121)
	at org.apache.paimon.s3.HadoopCompliantFileIO.getFileSystem(HadoopCompliantFileIO.java:147)
	at org.apache.paimon.s3.HadoopCompliantFileIO.exists(HadoopCompliantFileIO.java:104)
	at org.apache.paimon.fs.PluginFileIO.lambda$exists$4(PluginFileIO.java:71)
	at org.apache.paimon.fs.PluginFileIO.wrap(PluginFileIO.java:108)
	at org.apache.paimon.fs.PluginFileIO.exists(PluginFileIO.java:71)
	at org.apache.paimon.fs.FileIO.checkOrMkdirs(FileIO.java:303)
	at org.apache.paimon.catalog.CatalogFactory.createUnwrappedCatalog(CatalogFactory.java:98)
	at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:71)
	at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:67)
	at org.apache.paimon.spark.SparkCatalog.initialize(SparkCatalog.java:132)
	at org.apache.spark.sql.connector.catalog.Catalogs$.load(Catalogs.scala:65)
	at org.apache.spark.sql.connector.catalog.CatalogManager.$anonfun$catalog$1(CatalogManager.scala:54)
	at scala.collection.mutable.HashMap.getOrElseUpdate(HashMap.scala:86)
	at org.apache.spark.sql.connector.catalog.CatalogManager.catalog(CatalogManager.scala:54)
	at org.apache.spark.sql.connector.catalog.LookupCatalog$CatalogAndNamespace$.unapply(LookupCatalog.scala:86)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs$$anonfun$apply$1.applyOrElse(ResolveCatalogs.scala:51)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs$$anonfun$apply$1.applyOrElse(ResolveCatalogs.scala:30)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$2(AnalysisHelper.scala:170)
	at org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(origin.scala:76)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$1(AnalysisHelper.scala:170)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.allowInvokingTransformsInAnalyzer(AnalysisHelper.scala:323)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning(AnalysisHelper.scala:168)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning$(AnalysisHelper.scala:164)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsDownWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$4(AnalysisHelper.scala:175)
	at org.apache.spark.sql.catalyst.trees.UnaryLike.mapChildren(TreeNode.scala:1216)
	at org.apache.spark.sql.catalyst.trees.UnaryLike.mapChildren$(TreeNode.scala:1215)
	at org.apache.spark.sql.catalyst.plans.logical.SetCatalogAndNamespace.mapChildren(v2Commands.scala:941)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.$anonfun$resolveOperatorsDownWithPruning$1(AnalysisHelper.scala:175)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.allowInvokingTransformsInAnalyzer(AnalysisHelper.scala:323)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning(AnalysisHelper.scala:168)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsDownWithPruning$(AnalysisHelper.scala:164)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsDownWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsWithPruning(AnalysisHelper.scala:99)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperatorsWithPruning$(AnalysisHelper.scala:96)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperatorsWithPruning(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperators(AnalysisHelper.scala:76)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.resolveOperators$(AnalysisHelper.scala:75)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.resolveOperators(LogicalPlan.scala:32)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs.apply(ResolveCatalogs.scala:30)
	at org.apache.spark.sql.catalyst.analysis.ResolveCatalogs.apply(ResolveCatalogs.scala:27)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$2(RuleExecutor.scala:222)
	at scala.collection.LinearSeqOptimized.foldLeft(LinearSeqOptimized.scala:126)
	at scala.collection.LinearSeqOptimized.foldLeft$(LinearSeqOptimized.scala:122)
	at scala.collection.immutable.List.foldLeft(List.scala:91)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$1(RuleExecutor.scala:219)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$execute$1$adapted(RuleExecutor.scala:211)
	at scala.collection.immutable.List.foreach(List.scala:431)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.execute(RuleExecutor.scala:211)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.org$apache$spark$sql$catalyst$analysis$Analyzer$$executeSameContext(Analyzer.scala:240)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.$anonfun$execute$1(Analyzer.scala:236)
	at org.apache.spark.sql.catalyst.analysis.AnalysisContext$.withNewAnalysisContext(Analyzer.scala:187)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.execute(Analyzer.scala:236)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.execute(Analyzer.scala:202)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.$anonfun$executeAndTrack$1(RuleExecutor.scala:182)
	at org.apache.spark.sql.catalyst.QueryPlanningTracker$.withTracker(QueryPlanningTracker.scala:89)
	at org.apache.spark.sql.catalyst.rules.RuleExecutor.executeAndTrack(RuleExecutor.scala:182)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.$anonfun$executeAndCheck$1(Analyzer.scala:223)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.markInAnalyzer(AnalysisHelper.scala:330)
	at org.apache.spark.sql.catalyst.analysis.Analyzer.executeAndCheck(Analyzer.scala:222)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$analyzed$1(QueryExecution.scala:77)
	at org.apache.spark.sql.catalyst.QueryPlanningTracker.measurePhase(QueryPlanningTracker.scala:138)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$executePhase$2(QueryExecution.scala:219)
	at org.apache.spark.sql.execution.QueryExecution$.withInternalError(QueryExecution.scala:546)
	at org.apache.spark.sql.execution.QueryExecution.$anonfun$executePhase$1(QueryExecution.scala:219)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.execution.QueryExecution.executePhase(QueryExecution.scala:218)
	at org.apache.spark.sql.execution.QueryExecution.analyzed$lzycompute(QueryExecution.scala:77)
	at org.apache.spark.sql.execution.QueryExecution.analyzed(QueryExecution.scala:74)
	at org.apache.spark.sql.execution.QueryExecution.assertAnalyzed(QueryExecution.scala:66)
	at org.apache.spark.sql.Dataset$.$anonfun$ofRows$2(Dataset.scala:99)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.Dataset$.ofRows(Dataset.scala:97)
	at org.apache.spark.sql.SparkSession.$anonfun$sql$4(SparkSession.scala:691)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:682)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:713)
	at org.apache.spark.sql.SparkSession.sql(SparkSession.scala:744)
	at org.apache.spark.sql.SQLContext.sql(SQLContext.scala:651)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLDriver.run(SparkSQLDriver.scala:68)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processCmd(SparkSQLCLIDriver.scala:501)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1(SparkSQLCLIDriver.scala:619)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1$adapted(SparkSQLCLIDriver.scala:613)
	at scala.collection.Iterator.foreach(Iterator.scala:943)
	at scala.collection.Iterator.foreach$(Iterator.scala:943)
	at scala.collection.AbstractIterator.foreach(Iterator.scala:1431)
	at scala.collection.IterableLike.foreach(IterableLike.scala:74)
	at scala.collection.IterableLike.foreach$(IterableLike.scala:73)
	at scala.collection.AbstractIterable.foreach(Iterable.scala:56)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processLine(SparkSQLCLIDriver.scala:613)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver$.main(SparkSQLCLIDriver.scala:310)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.main(SparkSQLCLIDriver.scala)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
	at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:1034)
	at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:199)
	at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:222)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:91)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1125)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1134)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.lang.ClassNotFoundException: software.amazon.awssdk.transfer.s3.model.ObjectTransfer
	at java.base/java.net.URLClassLoader.findClass(Unknown Source)
	at java.base/java.lang.ClassLoader.loadClass(Unknown Source)
	at org.apache.paimon.plugin.ComponentClassLoader.loadClassFromComponentOnly(ComponentClassLoader.java:126)
	at org.apache.paimon.plugin.ComponentClassLoader.loadClass(ComponentClassLoader.java:105)
	at java.base/java.lang.ClassLoader.loadClass(Unknown Source)
	... 107 more
```
</details>

<details>
<summary>Fix ChainSplit NPE after branch table cache invalidation</summary>

https://github.com/apache/paimon/issues/7299

```text
spark-sql (default)> CREATE TABLE default.t (
                   >     `t1` string ,
                   >     `t2` string ,
                   >     `t3` string
                   > ) PARTITIONED BY (`date` string)
                   > TBLPROPERTIES (
                   >   'chain-table.enabled' = 'true',
                   >   -- props about primary key table
                   >   'primary-key' = 'date,t1',
                   >   'sequence.field' = 't2',
                   >   'bucket-key' = 't1',
                   >   'bucket' = '2',
                   >   -- props about partition
                   >   'partition.timestamp-pattern' = '$date',
                   >   'partition.timestamp-formatter' = 'yyyyMMdd'
                   > );
26/02/24 13:05:12 WARN Mimetypes: Unable to find 'mime.types' file in classpath
Time taken: 0.823 seconds
spark-sql (default)> CALL sys.create_branch('default.t', 'snapshot');
true
Time taken: 0.725 seconds, Fetched 1 row(s)
spark-sql (default)>
                   > CALL sys.create_branch('default.t', 'delta');
true
Time taken: 0.441 seconds, Fetched 1 row(s)
spark-sql (default)> ALTER TABLE default.t SET tblproperties
                   >     ('scan.fallback-snapshot-branch' = 'snapshot',
                   >      'scan.fallback-delta-branch' = 'delta');
Time taken: 0.961 seconds
spark-sql (default)>
                   > ALTER TABLE `default`.`t$branch_snapshot` SET tblproperties
                   >     ('scan.fallback-snapshot-branch' = 'snapshot',
                   >      'scan.fallback-delta-branch' = 'delta');
Time taken: 0.667 seconds
spark-sql (default)>
                   > ALTER TABLE `default`.`t$branch_delta` SET tblproperties
                   >     ('scan.fallback-snapshot-branch' = 'snapshot',
                   >      'scan.fallback-delta-branch' = 'delta');
Time taken: 0.954 seconds
spark-sql (default)> insert overwrite `default`.`t$branch_snapshot` partition (date = '20250810')
                   >     values ('1', '1', '1');
Time taken: 24.562 seconds
spark-sql (default)> insert overwrite `default`.`t$branch_delta` partition (date = '20250811')
                   >     values ('2', '1', '1');
Time taken: 21.339 seconds
spark-sql (default)>
                   > select t1, t2, t3 from default.t where date = '20250811'
                   > ;
26/02/24 13:06:21 ERROR TaskSetManager: Failed to serialize task 4, not attempting to retry it.
java.lang.NullPointerException
	at java.base/java.io.DataOutputStream.writeUTF(Unknown Source)
	at java.base/java.io.DataOutputStream.writeUTF(Unknown Source)
	at org.apache.paimon.table.source.ChainSplit.serialize(ChainSplit.java:146)
	at org.apache.paimon.table.source.ChainSplit.writeObject(ChainSplit.java:115)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at java.base/java.io.ObjectStreamClass.invokeWriteObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.defaultWriteFields(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject(Unknown Source)
	at scala.collection.immutable.List$SerializationProxy.writeObject(List.scala:516)
	at jdk.internal.reflect.GeneratedMethodAccessor130.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at java.base/java.io.ObjectStreamClass.invokeWriteObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.defaultWriteFields(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject(Unknown Source)
	at scala.collection.immutable.List$SerializationProxy.writeObject(List.scala:516)
	at jdk.internal.reflect.GeneratedMethodAccessor130.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at java.base/java.io.ObjectStreamClass.invokeWriteObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.defaultWriteFields(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.defaultWriteFields(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeSerialData(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeOrdinaryObject(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject0(Unknown Source)
	at java.base/java.io.ObjectOutputStream.writeObject(Unknown Source)
	at org.apache.spark.serializer.JavaSerializationStream.writeObject(JavaSerializer.scala:46)
	at org.apache.spark.serializer.JavaSerializerInstance.serialize(JavaSerializer.scala:115)
	at org.apache.spark.scheduler.TaskSetManager.prepareLaunchingTask(TaskSetManager.scala:530)
	at org.apache.spark.scheduler.TaskSetManager.$anonfun$resourceOffer$2(TaskSetManager.scala:494)
	at scala.Option.map(Option.scala:230)
	at org.apache.spark.scheduler.TaskSetManager.resourceOffer(TaskSetManager.scala:470)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOfferSingleTaskSet$2(TaskSchedulerImpl.scala:414)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOfferSingleTaskSet$2$adapted(TaskSchedulerImpl.scala:409)
	at scala.Option.foreach(Option.scala:407)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOfferSingleTaskSet$1(TaskSchedulerImpl.scala:409)
	at scala.collection.immutable.Range.foreach$mVc$sp(Range.scala:158)
	at org.apache.spark.scheduler.TaskSchedulerImpl.resourceOfferSingleTaskSet(TaskSchedulerImpl.scala:399)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOffers$20(TaskSchedulerImpl.scala:606)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOffers$20$adapted(TaskSchedulerImpl.scala:601)
	at scala.collection.IndexedSeqOptimized.foreach(IndexedSeqOptimized.scala:36)
	at scala.collection.IndexedSeqOptimized.foreach$(IndexedSeqOptimized.scala:33)
	at scala.collection.mutable.ArrayOps$ofRef.foreach(ArrayOps.scala:198)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOffers$16(TaskSchedulerImpl.scala:601)
	at org.apache.spark.scheduler.TaskSchedulerImpl.$anonfun$resourceOffers$16$adapted(TaskSchedulerImpl.scala:574)
	at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
	at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
	at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
	at org.apache.spark.scheduler.TaskSchedulerImpl.resourceOffers(TaskSchedulerImpl.scala:574)
	at org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend$DriverEndpoint.$anonfun$makeOffers$1(CoarseGrainedSchedulerBackend.scala:366)
	at org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.org$apache$spark$scheduler$cluster$CoarseGrainedSchedulerBackend$$withLock(CoarseGrainedSchedulerBackend.scala:1058)
	at org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend$DriverEndpoint.org$apache$spark$scheduler$cluster$CoarseGrainedSchedulerBackend$DriverEndpoint$$makeOffers(CoarseGrainedSchedulerBackend.scala:360)
	at org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend$DriverEndpoint$$anonfun$receive$1.applyOrElse(CoarseGrainedSchedulerBackend.scala:188)
	at org.apache.spark.rpc.netty.Inbox.$anonfun$process$1(Inbox.scala:115)
	at org.apache.spark.rpc.netty.Inbox.safelyCall(Inbox.scala:213)
	at org.apache.spark.rpc.netty.Inbox.process(Inbox.scala:100)
	at org.apache.spark.rpc.netty.MessageLoop.org$apache$spark$rpc$netty$MessageLoop$$receiveLoop(MessageLoop.scala:75)
	at org.apache.spark.rpc.netty.MessageLoop$$anon$1.run(MessageLoop.scala:41)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
	at java.base/java.lang.Thread.run(Unknown Source)
26/02/24 13:06:21 ERROR TaskSchedulerImpl: Resource offer failed, task set TaskSet_6.0 was not serializable
Job aborted due to stage failure: Failed to serialize task 4, not attempting to retry it. Exception during serialization: java.lang.NullPointerException
org.apache.spark.SparkException: Job aborted due to stage failure: Failed to serialize task 4, not attempting to retry it. Exception during serialization: java.lang.NullPointerException
	at org.apache.spark.scheduler.DAGScheduler.failJobAndIndependentStages(DAGScheduler.scala:2856)
	at org.apache.spark.scheduler.DAGScheduler.$anonfun$abortStage$2(DAGScheduler.scala:2792)
	at org.apache.spark.scheduler.DAGScheduler.$anonfun$abortStage$2$adapted(DAGScheduler.scala:2791)
	at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
	at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
	at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
	at org.apache.spark.scheduler.DAGScheduler.abortStage(DAGScheduler.scala:2791)
	at org.apache.spark.scheduler.DAGScheduler.$anonfun$handleTaskSetFailed$1(DAGScheduler.scala:1247)
	at org.apache.spark.scheduler.DAGScheduler.$anonfun$handleTaskSetFailed$1$adapted(DAGScheduler.scala:1247)
	at scala.Option.foreach(Option.scala:407)
	at org.apache.spark.scheduler.DAGScheduler.handleTaskSetFailed(DAGScheduler.scala:1247)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.doOnReceive(DAGScheduler.scala:3060)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:2994)
	at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:2983)
	at org.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:49)
	at org.apache.spark.scheduler.DAGScheduler.runJob(DAGScheduler.scala:989)
	at org.apache.spark.SparkContext.runJob(SparkContext.scala:2393)
	at org.apache.spark.SparkContext.runJob(SparkContext.scala:2414)
	at org.apache.spark.SparkContext.runJob(SparkContext.scala:2433)
	at org.apache.spark.SparkContext.runJob(SparkContext.scala:2458)
	at org.apache.spark.rdd.RDD.$anonfun$collect$1(RDD.scala:1049)
	at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:151)
	at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:112)
	at org.apache.spark.rdd.RDD.withScope(RDD.scala:410)
	at org.apache.spark.rdd.RDD.collect(RDD.scala:1048)
	at org.apache.spark.sql.execution.SparkPlan.executeCollect(SparkPlan.scala:448)
	at org.apache.spark.sql.execution.SparkPlan.executeCollectPublic(SparkPlan.scala:475)
	at org.apache.spark.sql.execution.HiveResult$.hiveResultString(HiveResult.scala:76)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLDriver.$anonfun$run$2(SparkSQLDriver.scala:76)
	at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$6(SQLExecution.scala:125)
	at org.apache.spark.sql.execution.SQLExecution$.withSQLConfPropagated(SQLExecution.scala:201)
	at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$1(SQLExecution.scala:108)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:900)
	at org.apache.spark.sql.execution.SQLExecution$.withNewExecutionId(SQLExecution.scala:66)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLDriver.run(SparkSQLDriver.scala:76)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processCmd(SparkSQLCLIDriver.scala:501)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1(SparkSQLCLIDriver.scala:619)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.$anonfun$processLine$1$adapted(SparkSQLCLIDriver.scala:613)
	at scala.collection.Iterator.foreach(Iterator.scala:943)
	at scala.collection.Iterator.foreach$(Iterator.scala:943)
	at scala.collection.AbstractIterator.foreach(Iterator.scala:1431)
	at scala.collection.IterableLike.foreach(IterableLike.scala:74)
	at scala.collection.IterableLike.foreach$(IterableLike.scala:73)
	at scala.collection.AbstractIterable.foreach(Iterable.scala:56)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.processLine(SparkSQLCLIDriver.scala:613)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver$.main(SparkSQLCLIDriver.scala:310)
	at org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver.main(SparkSQLCLIDriver.scala)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
	at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:1034)
	at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:199)
	at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:222)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:91)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1125)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1134)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
```
</details>
<details>
<summary>[spark] Support compact_chain_table procedure</summary>


Compact the incremental data of the current cycle with the full data of the previous cycle to generate the full data for the day; for example, the full data for date=20250729 is generated by compacting all incremental partition from 20250723 to 20250729 on t$branch_delta and 20250722 on t$branch_snapshot

- 需要overwritePartition 这个参数吗？
- ChainSplit 中增加 dataSplits 字段，是否必要？这里需要：PaimonUtils.createDataset(DataSplit)
  - `CALL sys.compact_chain_table(table => 't', partition => 'dt=20250810,hour=22')`
- https://github.com/apache/paimon/pull/7313
</details>

## Reference

- https://cwiki.apache.org/confluence/display/PAIMON/PIP-37:+Introduce+Chain+Table