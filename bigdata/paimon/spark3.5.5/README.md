

# Spark 3.5.5 with Paimon Integration

## Prerequisites

### Required JAR files
Download the following Paimon JAR files:
- `paimon-s3-xxx.jar`
- `paimon-spark-xxx.jar`

## Setup

### 1. Build Docker Image
```bash
docker build -t spark:3.5.5-paimon-1.4 -f Dockerfile_spark3.5.5 ./
```

### 2. Environment Configuration
```bash
export DRIVER_HOST=$(ifconfig | awk '/inet / && $2!="127.0.0.1"{print $2; exit}')
export MY_S3_ENDPOINT=http://minio:9000
```

### 3. Launch Spark SQL with Paimon
```bash
./bin/spark-sql \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode client \
    --name spark-sql \
    --conf spark.driver.host=$DRIVER_HOST \
    --conf spark.kubernetes.file.upload.path=s3://spark-bucket/spark-deps \
    --conf spark.sql.catalog.paimon.s3.path.style.access=true \
    --conf spark.sql.catalog.paimon.s3.endpoint=$MY_S3_ENDPOINT \
    --conf spark.sql.catalog.paimon.s3.access-key=minio \
    --conf spark.sql.catalog.paimon.s3.secret-key=minio12345 \
    --conf spark.sql.catalog.paimon=org.apache.paimon.spark.SparkCatalog \
    --conf spark.sql.catalog.paimon.warehouse=s3://warehouse/paimon \
    --conf spark.sql.extensions=org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions \
    --conf spark.executor.instances=2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-operator-spark \
    --conf spark.kubernetes.appKillPodDeletionGracePeriod=300 \
    --conf spark.kubernetes.container.image=spark:3.5.5-paimon-1.4
```

### 4. Basic Operations
```sql
USE paimon;
USE ods;
SHOW TABLES;
```

### paimon row tracking
```sparksql
SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER FROM t order by _ROW_ID asc;
UPDATE t SET data = 'a1' WHERE id = 1;
DELETE FROM t WHERE id = 2;
```

## src deepdive
```text
== Physical Plan ==
Execute UpdatePaimonTableCommand
   +- UpdatePaimonTableCommand RelationV2[id#0, data#1, __paimon_file_path#2, __paimon_row_index#3L, __paimon_partition#4, __paimon_bucket#5, _ROW_ID#6L, _SEQUENCE_NUMBER#7L] paimon.ods.t ods.t, org.apache.paimon.table.AppendOnlyFileStoreTable@51c7d472, (_ROW_ID#6L = 1), [assignment(data#1, a4)]

inject custom Catalyst rules
PaimonSparkSessionExtensions->PaimonUpdateTable(Rule)
    ->UpdatePaimonTableCommand(LogicalPlan)

ExecutedCommandExec(cmd=UpdatePaimonTableCommand)
    ->cmd.run->performUpdateForNonPkTable->writeUpdatedAndUnchangedData
        ->PaimonSparkWriter.write


Add metadata columns:
DataSourceV2Relation.metadataOutput:
    SparkTable.metadataColumns
    

Create Dataset of updateColumns:
UpdatePaimonTableCommand.writeUpdatedAndUnchangedData
SparkTableWrite.write->toPaimonRow
```




## Troubleshooting

### Issue: `java.lang.NoClassDefFoundError: com/amazonaws/AmazonClientException`
**Solution:**
- Add `paimon-s3-1.3-SNAPSHOT.jar` to the classpath
- Use `s3` protocol instead of `s3a` like `spark.sql.catalog.paimon.warehouse=s3://warehouse/paimon` not `spark.sql.catalog.paimon.warehouse=s3a://warehouse/paimon`

### delete by ROW_ID, java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Long
> DELETE FROM t WHERE _ROW_ID = 2;
[issue desc](../pr-6335/pr-desc.md)

## References
- [Spark 3.5.5 on k8s](../../../../spark/spark3.5.5/spark3.5.5-k8s.md)
- [Paimon Spark Quick Start](https://paimon.apache.org/docs/master/spark/quick-start/)
- [Paimon Filesystems Documentation](https://paimon.apache.org/docs/master/maintenance/filesystems/)
- [Support row lineage PR](https://github.com/apache/paimon/issues/6022)
