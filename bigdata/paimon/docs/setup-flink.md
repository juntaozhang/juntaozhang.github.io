# Setup Flink with Paimon

deploy [rustfs](../../s3/rustfs/README.md) S3-compatible object storage system

[flink s3 filesystem](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/deployment/filesystems/s3/)

## Setup Flink Env
### Build Flink Image

```bash
export FLINK_HOME=../build-target
cp config.yaml $FLINK_HOME/conf/
cp flink-s3-fs-hadoop-*.jar $FLINK_HOME/lib/
cp paimon-flink-*.jar $FLINK_HOME/lib/
cp flink-shaded-hadoop-2-uber-*.jar $FLINK_HOME/lib/

docker build -t my-flink:1.20.3-scala_2.12-java17-paimon -f Dockerfile_flink .
```

### Deploy Flink session cluster with S3
```shell
export cluster_id=flink1
export cluster_port=8082
$FLINK_HOME/bin/kubernetes-session.sh \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=$cluster_port \
    -Drest.bind-port=$cluster_port \
    -Dkubernetes.container.image=my-flink:1.20.3-scala_2.12-java17-paimon \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    -Dkubernetes.containerized.master.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dkubernetes.containerized.taskmanager.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dfs.s3a.endpoint=http://rustfs-svc:9000 \
    -Dfs.s3a.path.style.access=true \
    -Dfs.s3a.connection.ssl.enabled=false \
    -Dfs.s3a.access.key=test \
    -Dfs.s3a.secret.key=11111111 \
    -Dstate.checkpoints.dir=s3a://flink-bucket/$cluster_id/checkpoints \
    -Dstate.savepoints.dir=s3a://flink-bucket/$cluster_id/savepoints
```


### Setup SQL Client

#### Option 1: Using YAML Configuration
```bash
kubectl apply -f flink-sql-client.yaml

# Login to the pod
kubectl exec -it deployment/flink-sql-client -- bash
```

#### Connect to SQL Client
```bash
# Start SQL client
bin/sql-client.sh embedded -Dkubernetes.cluster-id=flink1
```

## Testing with SQL

### Create Catalog and Tables

Setup Paimon catalog and create test tables:

```sql
CREATE CATALOG paimon_catalog WITH (
'type' = 'paimon',
'warehouse' = 's3a://warehouse/paimon'
);
USE CATALOG paimon_catalog;
CREATE DATABASE IF NOT EXISTS ods;
use ods;

SET 'execution.runtime-mode' = 'streaming';
SET 'execution.checkpointing.interval' = '10 s';


CREATE TEMPORARY TABLE src_order (
    order_number BIGINT,
    price DECIMAL(32, 2),
    ts TIMESTAMP(3), WATERMARK FOR ts AS ts - INTERVAL '5' SECOND
) WITH (
    'connector' = 'datagen',
    'rows-per-second' = '1',
    'fields.order_number.kind' = 'sequence',
    'fields.order_number.start' = '1',
    'fields.order_number.end' = '1000000',
    'fields.price.min' = '1',
    'fields.price.max' = '100'
);

CREATE TABLE my_order (
    window_start STRING,
    order_number BIGINT,
    total_amount DECIMAL(32, 2)
) WITH (
    'sink.rolling-policy.file-size' = '1MB',
    'sink.rolling-policy.rollover-interval' = '1 min',
    'sink.rolling-policy.check-interval' = '10 s'
);

INSERT INTO my_order
SELECT
    CAST(TUMBLE_START(ts, INTERVAL '10' SECOND) AS STRING) window_start,
    order_number,
    SUM(price) total_amount
FROM src_order
GROUP BY order_number, TUMBLE(ts, INTERVAL '10' SECOND);
```

### Batch Query

Switch to batch mode and query the data:

```sql
SET 'execution.runtime-mode' = 'batch';
SELECT count(1), sum(total_amount) FROM my_order;

CALL sys.compact('ods.my_order', '', '', '', '');
```
