# Paimon
## Setup
- [local](docs/setup-local.md)
- [flink](docs/setup-flink.md)
- [spark](docs/setup-spark.md)

## Paimon 1.4
- [data distribution: bucket](docs/bucket.md)
- [compact](docs/compact.md)
  - [files-conflict: dedicated compaction job separate write and compact](https://paimon.apache.org/docs/master/concepts/concurrency-control/#files-conflict), set 'write-only' to true
    - [FlinkDeduplicateExample.java](flink1.20/src/main/java/com/example/paimon/FlinkDeduplicateExample.java)
- [changelog](docs/changelog.md)
- row-tracking
  - [FlinkRowTrackingExample.java](flink1.20/src/main/java/com/example/paimon/FlinkRowTrackingExample.java)

## TODO
- example of [RESTCatalog](https://paimon.apache.org/docs/master/concepts/rest/overview/)
- deep dive 
  - test cross-partitions-upsert, primary keys not contain partition fields
    - dedup/partial updata/firstrow
  - `CALL sys.compact_manifest(table => 'order_fact')`
  - `CALL sys.expire_snapshots(table => 'orders', retain_max=>3, retain_min=>2)`
  - [bucketed-append: precommit-compact](https://paimon.apache.org/docs/master/append-table/streaming/#bucketed-append) 
  - [Deletion Vectors](https://paimon.apache.org/docs/master/concepts/spec/tableindex/#deletion-vectors)
  - PK merge engine: partial update
    - testPartialUpdateRemoveRecordOnSequenceGroup

<details>
<summary>data evolution support update and compact</summary>

- [data-evolution.md](docs/data-evolution.md)

TODO, 需要看一下源码，原理是什么，为什么没有支持？\
直觉上Data Evolution 与 支持 compaction 不应该冲突啊？
</details>


## Flink 1.17 with Paimon 1.3
- [Kubernetes Setup](flink1.17/docs/k8s-setup.md)
- [Primary Key Table](flink1.17/docs/primary-key-table.md)
- [CDC MySQL/PostgreSQL](flink1.17/docs/cdc-mysql_pg.md)
    - [CDC Source](flink1.17/docs/cdc.md)
- [Changelog](flink1.17/docs/changelog.md)
- [Row Tracking](flink1.17/docs/row-tracking.md)