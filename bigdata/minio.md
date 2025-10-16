# MinIO
## build minio in k8s
```shell
helm install minio minio/minio \
--set mode=standalone \
--set rootUser=minio,rootPassword=minio12345 \
--set service.type=LoadBalancer \
--set service.port=9000
```

## build client in local
```shell
curl --progress-bar -L https://dl.min.io/aistor/mc/release/darwin-arm64/mc \
    --create-dirs \
    -o ~/minio-binaries/mc
    
alias mc='~/minio-binaries/mc'

mc alias set localminio http://127.0.0.1:9000 minio minio12345
```


## 在 K8s 集群内部直接用 mc
```shell
k run mc \
    --image=bitnami/minio-client:latest --restart=Never \
    --command -- bash -lc 'sleep infinity'
```
>k exec -it mc -- bash
```
mc alias set minio http://minio.default.svc.cluster.local:9000 minio minio12345
mc ls minio
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
