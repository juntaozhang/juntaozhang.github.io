# Fluss

## Installing the chart on a cluster
`helm install zk bitnami/zookeeper -f zk.yaml`

`git clone https://github.com/apache/fluss.git`

`helm install fluss ./helm`

`kubectl apply -f fluss-svc.yaml`


## Clean Up
```text
# Uninstall Fluss
helm uninstall fluss

# Uninstall ZooKeeper
helm uninstall zk

# Delete PVCs
kubectl delete pvc -l app.kubernetes.io/name=fluss
```

## Start Fluss Local Cluster
edit server.yaml
```text
datalake.format: paimon
datalake.paimon.metastore: filesystem
datalake.paimon.warehouse: s3://warehouse/paimon
datalake.paimon.s3.endpoint: http://localhost:32000
datalake.paimon.s3.access-key: test
datalake.paimon.s3.secret-key: 11111111
datalake.paimon.s3.path.style.access: true
```

- Run in Java 11
- Custom build paimon package
- Add paimon-bundle-1.4-SNAPSHOT.jar, paimon-s3-1.4-SNAPSHOT.jar to plugins/paimon
- Start app: `bin/local-cluster.sh start`
- Run flink1.20 in IntelliJ:
  - [FlussFlinkReadExample.java](flink1.20/src/main/java/com/example/fluss/FlussFlinkReadExample.java) read from Fluss
  - [FlussFlinkSinkExample.java](flink1.20/src/main/java/com/example/fluss/FlussFlinkSinkExample.java) write to Fluss
  - [FlussLakeTieringEntrypoint.java](flink1.20/src/main/java/com/example/fluss/FlussLakeTieringEntrypoint.java) 
- Run Delta Join in flink2.2
  - [DeltaJoinExample.java](../flink2.2/src/main/java/com/example/fluss/DeltaJoinExample.java)


## Troubleshooting
<details>
<summary>java.lang.NullPointerException: Cannot invoke "org.apache.fluss.server.zk.data.lease.KvSnapshotTableLease.getBucketSnapshots()" because "tableLease"</summary>

`releaseBucket` 方法不支持幂等性导致的：\
In extreme scenarios of Flink Delta Join jobs, after checkpoint completes, `notifyCheckpointComplete` is triggered. Despite successful request delivery and `releaseBucket` execution in Fluss, response failure may occur for certain reasons. This causes Flink to repeatedly resend the request (and fail consistently), leading to state inconsistency between Flink and Fluss.

```text
java.util.concurrent.ExecutionException: org.apache.fluss.exception.UnknownServerException: org.apache.fluss.exception.UnknownServerException: Failed to release kv snapshot lease for8af2a797-779d-4690-8e2b-14f889791412
Caused by: java.lang.NullPointerException: Cannot invoke "org.apache.fluss.server.zk.data.lease.KvSnapshotTableLease.getBucketSnapshots()" because "tableLease" is null
	at org.apache.fluss.server.coordinator.lease.KvSnapshotLeaseHandler.releaseBucket(KvSnapshotLeaseHandler.java:141)
	at org.apache.fluss.server.coordinator.lease.KvSnapshotLeaseManager.lambda$release$3(KvSnapshotLeaseManager.java:267)
	at org.apache.fluss.utils.concurrent.LockUtils.inLock(LockUtils.java:32)
```
</details>

<details>
<summary>use MinIO will cause SecurityTokenException</summary>

```text
remote.data.dir: s3://fluss-bucket/data
s3.endpoint: http://localhost:32000
s3.access-key: test
s3.secret-key: 11111111
s3.region: us-east-1
s3.path.style.access: true
```
在读 `remote.data.dir` 需要访问s3 这时候去 aws 做认证时 异常了。
```text
Caused by: org.apache.fluss.exception.SecurityTokenException: Failed to get file access security token: The security token included in the request is invalid. (Service: AWSSecurityTokenService; Status Code: 403; Error Code: InvalidClientTokenId; Request ID: 9008b1f3-8273-46a9-8b65-303583aa6cc6; Proxy: null)
```
</details>

<details>
<summary>Read Data from LakeHouse: No FileSystem for scheme "s3"</summary>

`MetadataManager.removeSensitiveTableOptions` 在返回 TableInfo tableLakeOptions 时 删除了 ak/sk
注意添加ak/sk:
```text
CREATE CATALOG fluss_catalog WITH (
  'type' = 'fluss',
  'paimon.s3.access-key' = 'test',
  'paimon.s3.secret-key' = '11111111',
  'bootstrap.servers' = 'localhost:9123'
)
```
details log:

local debug add `JVM_ARGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:61126"` in fluss-daemon.sh

```text
--add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED 
-XX:+IgnoreUnrecognizedVMOptions 
Missing required options are:

s3.access-key
s3.secret-key
		at org.apache.paimon.fs.FileIO.get(FileIO.java:515) ~[paimon-bundle-1.3.1.jar:1.3.1]
		at org.apache.paimon.catalog.CatalogFactory.createUnwrappedCatalog(CatalogFactory.java:97) ~[paimon-bundle-1.3.1.jar:1.3.1]
		at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:71) ~[paimon-bundle-1.3.1.jar:1.3.1]
		at org.apache.paimon.catalog.CatalogFactory.createCatalog(CatalogFactory.java:67) ~[paimon-bundle-1.3.1.jar:1.3.1]
		at org.apache.fluss.lake.paimon.source.PaimonSplitPlanner.getCatalog(PaimonSplitPlanner.java:88) ~[fluss-lake-paimon-0.10-SNAPSHOT.jar:0.10-SNAPSHOT]
		at org.apache.fluss.lake.paimon.source.PaimonSplitPlanner.plan(PaimonSplitPlanner.java:68) ~[fluss-lake-paimon-0.10-SNAPSHOT.jar:0.10-SNAPSHOT]
		at org.apache.fluss.flink.lake.LakeSplitGenerator.generateHybridLakeFlussSplits(LakeSplitGenerator.java:107) ~[fluss-flink-1.20-0.10-SNAPSHOT.jar:0.10-SNAPSHOT]
		at org.apache.fluss.flink.source.enumerator.FlinkSourceEnumerator.generateHybridLakeFlussSplits(FlinkSourceEnumerator.java:753) ~[fluss-flink-1.20-0.10-SNAPSHOT.jar:0.10-SNAPSHOT]
		at org.apache.fluss.flink.source.enumerator.FlinkSourceEnumerator.lambda$startInBatchMode$0(FlinkSourceEnumerator.java:359) ~[fluss-flink-1.20-0.10-SNAPSHOT.jar:0.10-SNAPSHOT]
		at org.apache.flink.runtime.source.coordinator.ExecutorNotifier.lambda$notifyReadyAsync$2(ExecutorNotifier.java:80) ~[flink-runtime-1.20.3.jar:1.20.3]
		at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515) [?:?]
		at java.util.concurrent.FutureTask.run$$$capture(FutureTask.java:264) [?:?]
		at java.util.concurrent.FutureTask.run(FutureTask.java) [?:?]
		at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304) [?:?]
		at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128) [?:?]
		at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628) [?:?]
		at java.lang.Thread.run(Thread.java:829) [?:?]
	Suppressed: org.apache.hadoop.fs.UnsupportedFileSystemException: No FileSystem for scheme "s3"
```
</details>

