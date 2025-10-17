# Flink Paimon on Kubernetes

This guide demonstrates how to set up and run Apache Paimon with Apache Flink on Kubernetes, including integration with MinIO for S3-compatible storage.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Download Dependencies](#download-dependencies)
- [Environment Setup](#environment-setup)
- [Testing with SQL](#testing-with-sql)
- [Data Inspection](#data-inspection)

## Prerequisites

- Kubernetes cluster with sufficient resources
- kubectl configured to access the cluster
- Docker for building custom images
- MinIO deployed in the cluster for S3-compatible storage
- Flink service account configured in Kubernetes

## Download Dependencies

### Flink Distribution
```bash
# Download Flink 1.17.2
wget https://archive.apache.org/dist/flink/flink-1.17.2/flink-1.17.2-bin-scala_2.12.tgz
tar -zxf flink-1.17.2-bin-scala_2.12.tgz
mv flink-1.17.2 ../
```

### Paimon and Hadoop Dependencies
[minio.md](../../minio.md)
```bash
# Download Paimon connector and Hadoop shaded dependencies from https://repository.apache.org/snapshots/org/apache/paimon/paimon-flink-1.17/
wget https://repository.apache.org/content/groups/snapshots/org/apache/paimon/paimon-flink-1.17/1.3-SNAPSHOT/paimon-flink-1.17-1.3-20250906.002721-80.jar
wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.8.3-10.0/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar
```

### Setup Flink Libraries
```bash
# Copy required JARs to Flink lib directory
export FLINK_HOME=../flink-1.17.2
cp flink-conf.yaml $FLINK_HOME/conf/
cp $FLINK_HOME/lib/flink-s3-fs-hadoop-*.jar ./
cp paimon-flink-*.jar $FLINK_HOME/lib/
cp flink-shaded-hadoop-2-uber-*.jar $FLINK_HOME/lib/
```

### Reference Documentation
- [Paimon Flink Quick Start](https://paimon.apache.org/docs/master/flink/quick-start/)
- [Flink S3 FileSystem](https://nightlies.apache.org/flink/flink-docs-release-2.1/docs/deployment/filesystems/s3/)

## Environment Setup

### Build Docker Image

Build a custom Flink image with Paimon dependencies:

```bash
docker build -t flink:1.17.2-scala_2.12-paimon -f Dockerfile_flink1.17 ./
```

### Deploy Flink Session Cluster on Kubernetes

Deploy Flink session cluster with S3 configuration for MinIO:

```shell
export cluster_id=flink1
$FLINK_HOME/bin/kubernetes-session.sh \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=8081 \
    -Drest.bind-port=8081 \
    -Dkubernetes.container.image=flink:1.17.2-scala_2.12-paimon \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    -Dkubernetes.containerized.master.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dkubernetes.containerized.taskmanager.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dfs.s3a.endpoint=http://minio.default.svc.cluster.local:9000 \
    -Dfs.s3a.path.style.access=true \
    -Dfs.s3a.connection.ssl.enabled=false \
    -Dfs.s3a.access.key=minio \
    -Dfs.s3a.secret.key=minio12345 \
    -Dstate.checkpoints.dir=s3a://flink-bucket/$cluster_id/checkpoints \
    -Dstate.savepoints.dir=s3a://flink-bucket/$cluster_id/savepoints
```

### Setup SQL Client

#### Option 1: Using YAML Configuration
```bash
kubectl apply -f flink1.17-paimon-sql-client.yaml

# Login to the pod
kubectl exec -it deployment/flink-sql-client -- bash
```

#### Option 2: Using kubectl run
```bash
kubectl run flink-client \
  --image=flink:1.17.2-scala_2.12-paimon \
  --restart=Never \
  --command -- bash -lc 'sleep infinity'
kubectl exec -it pod/flink-client -- bash
```

#### Connect to SQL Client
```bash
# Start SQL client
bin/sql-client.sh embedded -Dexecution.target=remote -Dkubernetes.cluster-id=flink1
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
```

## Data Inspection

### MinIO Data Verification

Verify data stored in MinIO using mc (MinIO Client):

**Reference:** [Paimon Data File Specification](https://paimon.apache.org/docs/master/concepts/spec/datafile/)

```bash
# Setup MinIO alias
mc alias set myminio http://minio.default.svc.cluster.local:9000 minio minio12345

# List Paimon table files
mc ls localminio/warehouse/paimon/ods.db/my_order1/

# View snapshot metadata
mc cat localminio/warehouse/paimon/ods.db/my_order1/snapshot/snapshot-9
```

### Paimon Metadata Analysis

Analyze Paimon manifest files using Avro tools:

```bash
# Convert manifest list to JSON format
java -jar ~/.m2/repository/org/apache/avro/avro-tools/1.11.3/avro-tools-1.11.3.jar \
  tojson <(mc cat localminio/warehouse/paimon/ods.db/my_order1/manifest/manifest-list-c1897398-a6fe-45db-9086-7eec0f04a816-14)
```

#### Manifest List Output:

```bash
# Convert delta manifest list to JSON
java -jar ~/.m2/repository/org/apache/avro/avro-tools/1.11.3/avro-tools-1.11.3.jar \
  tojson <(mc cat localminio/warehouse/paimon/ods.db/my_order1/manifest/manifest-list-c1897398-a6fe-45db-9086-7eec0f04a816-15)
```

#### Delta Manifest List Output:
```bash
# Convert specific manifest to JSON
java -jar ~/.m2/repository/org/apache/avro/avro-tools/1.11.3/avro-tools-1.11.3.jar \
  tojson <(mc cat localminio/warehouse/paimon/ods.db/my_order1/manifest/manifest-766f9495-6622-4a85-9225-278d321a238b-7)
```

reference:
https://paimon.apache.org/docs/master/learn-paimon/understand-files/

### Parquet Data Files

Inspect Parquet data files using parquet-tools:

```bash
# Set AWS credentials for S3 access
export AWS_ACCESS_KEY_ID=minio
export AWS_SECRET_ACCESS_KEY=minio12345
export AWS_REGION=us-east-1
export AWS_ENDPOINT_URL=http://localhost:9000

# Show Parquet file content
parquet-tools show s3://warehouse/paimon/ods.db/my_order1/bucket-0/data-1315b321-1e18-465e-8d9b-cd9cb3f685d5-0.parquet
```
