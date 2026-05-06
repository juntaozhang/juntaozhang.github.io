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

## Flink 1.17 with Paimon 1.3
- [Kubernetes Setup](flink1.17/docs/k8s-setup.md)
- [Primary Key Table](flink1.17/docs/primary-key-table.md)
- [CDC MySQL/PostgreSQL](flink1.17/docs/cdc-mysql_pg.md)
    - [CDC Source](flink1.17/docs/cdc.md)
- [Changelog](flink1.17/docs/changelog.md)
- [Row Tracking](flink1.17/docs/row-tracking.md)

## Reference
- [Apache Paimon V0.9 Meetup](https://www.bilibili.com/video/BV1YdYNe2Enh)