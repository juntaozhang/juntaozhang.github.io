# memoryOverhead

## repartitionAndSortWithinPartitions memoryOverhead

> 609.4 G 1100 avg:569.2 M

```
Job aborted due to stage failure: Task 135 in stage 5.0 failed 4 times, most recent failure: Lost task 135.3 in stage 5.0 (TID 5749, bd15-21-33-113, executor 272): ExecutorLostFailure (executor 272 exited caused by one of the running tasks) Reason: Container killed by YARN for exceeding memory limits. 18.2 GB of 18 GB physical memory used. Consider boosting spark.yarn.executor.memoryOverhead.
```

```
    val df = 大表 20个字段
    def toDF(dataset: DataFrame): DataFrame = {
      spark.createDataFrame(
        dataset.rdd.map(r => (r.getAs[String]("id"), r)).filter(
          pair => pair._1 != null && pair._1.nonEmpty && pair._1 != "-1"
        ).repartitionAndSortWithinPartitions(partitioner).map(_._2),
        dataset.schema
      )
    }
    val deviceProfileFullDF = toDF(df)

    deviceProfileFullDF.persist(StorageLevel.DISK_ONLY)

    println(s"基础画像 count = ${deviceProfileFullDF.count()}")
```

job params
```
spark2-submit --master yarn --deploy-mode cluster \
    --class com.mob.dmp.portrait.DmpPortrait2HbaseFull --queue dataengine \
    --driver-memory 5g --executor-memory 16g --executor-cores 8 \
    --name "dmp-portrait-full->hbase[$d]" \
    --conf spark.shuffle.service.enabled=true \
    --conf spark.speculation=true \
    --conf spark.speculation.quantile=0.9 \
    --conf spark.yarn.executor.memoryOverhead=2g \
    --conf spark.files.overwrite=true \
    --conf spark.sql.shuffle.partitions=1000 \
    --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=35 -XX:ConcGCThreads=12 -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution" \
    --files ${DATA_MIGRATION_CONF_DIR}/dmp-portrait.properties,${DATA_MIGRATION_CONF_DIR}/log4j.properties \
    --jars ${DATA_MIGRATION_LIB}/udf-manager-0.0.4-SNAPSHOT-jar-with-dependencies.jar \
    ${DATA_MIGRATION_LIB}/dmp-portrait-0.0.1-jar-with-dependencies.jar
```


 spark2-shell --master yarn --queue dataengine --executor-memory 5g --driver-memory 10g --executor-cores 5 --conf spark.speculation=true \
    --conf spark.speculation.quantile=0.9 \
    --conf spark.shuffle.service.enabled=true \
    --jars udf-manager-0.0.4-SNAPSHOT-jar-with-dependencies.jar