# Paimon
## Setup
- [local](docs/setup-local.md)
- [flink](docs/setup-flink.md)
- [spark](docs/setup-spark.md)

## Paimon 1.4
- [data distribution: bucket](docs/bucket.md)
- [Compact](docs/compact.md)
  - [files-conflict: dedicated compaction job separate write and compact](https://paimon.apache.org/docs/master/concepts/concurrency-control/#files-conflict), set 'write-only' to true
    - [FlinkDeduplicateExample.java](flink1.20/src/main/java/com/example/paimon/FlinkDeduplicateExample.java)
  - [Deletion Vectors](docs/deletion-vectors.md)
- [Merge Engine](docs/merge.md)
- [Changelog](docs/changelog.md)
- row-tracking
  - [FlinkRowTrackingExample.java](flink1.20/src/main/java/com/example/paimon/FlinkRowTrackingExample.java)
- [Chain Table](docs/chain.md)

## TODO
- Incremental Clustering
- File Index:bitmap 
- changelog
- example of [RESTCatalog](https://paimon.apache.org/docs/master/concepts/rest/overview/)
- `CALL sys.compact_manifest(table => 'order_fact')`
- `CALL sys.expire_snapshots(table => 'orders', retain_max=>3, retain_min=>2)`
- TableCommitImpl/FileStoreCommitImpl commit manifest
  - flink: CommitterOperatorFactory/CommitterOperator
  - spark: PaimonSparkWriter => commit
- spark read
  - BatchScanExec => PaimonScan => getInputSplits
    - DataTableScan
      - DataEvolutionSplitRead.createFileReader => 可以根据 file.firstRowId() 
        - DataFileRecordReader.readBatchInternal => assignRowTracking (修改 SequenceNumber)

<details>
<summary>data evolution support update and compact</summary>

- [data-evolution.md](docs/data-evolution.md)

TODO, 需要看一下源码，原理是什么，为什么没有支持？\
直觉上Data Evolution 与 支持 compaction 不应该冲突啊？
</details>
<details>
<summary>test</summary>
</details>


## Flink 1.17 with Paimon 1.3
- [Kubernetes Setup](flink1.17/docs/k8s-setup.md)
- [Primary Key Table](flink1.17/docs/primary-key-table.md)
- [CDC MySQL/PostgreSQL](flink1.17/docs/cdc-mysql_pg.md)
    - [CDC Source](flink1.17/docs/cdc.md)
- [Changelog](flink1.17/docs/changelog.md)
- [Row Tracking](flink1.17/docs/row-tracking.md)