# Apache Paimon

- [module structure and dependencies](module-deps.md)

## Core Concepts

- [basic-concepts](../docs/content/concepts/basic-concepts.md)
- Snapshot: Point-in-time view linked to manifest files
- [SortedRun](../docs/content/primary-key-table/overview.md#sorted-runs)
  - key 不重叠的有序文件组, L0 因为文件间可能重叠，所以每个文件自己成一个 SortedRun
  - L1+ 因为经过 compaction 后天然有序不重叠，所以整层合并成一个 SortedRun
- Table Types
  - [Append Only Table](append-table.md) - `AppendTable`
    - [Row Tracking](row-tracking.md)
      - Copy-on-Write for impacted files
      - Add `_ROW_ID` and `_SEQUENCE_NUMBER` system columns for BUCKET_UNAWARE append tables
    - [Data Evolution Table](data-evolution.md): 通过写入部分列（该列全量数据），实现列级别的更新，读取时自动合并多版本列数据
      - [Data Evolution BTree](btree.md): 用于高效行级查找和过滤（根据 manifests 过滤文件），避免全表扫描
  - [Primary Key Table](primary-key-table.md) -`PrimaryKeyTable`
    - Merge Tree: LSM-like structure for primary key tables
    - [Merge Engine](docs/merge.md)
    - [Chain Table](docs/chain.md)
    - [lookup](lookup.md)
      - [Changelog](docs/changelog.md): 在 compaction 时按 key 查找高层级旧值的机制
      - lookup join: 在 join 时按 key 查找dim table的机制
- Bucket Modes:
  - [append-table bucket mode](append-table.md)
    - bucket = -1: BUCKET_UNAWARE
    - bucket > 0: HASH_FIXED
  - [primary-key-table bucket mode](bucket.md#table-with-pk)
    - HASH_FIXED
    - HASH_DYNAMIC
    - KEY_DYNAMIC
    - POSTPONE_MODE
- cluster
  - [AppendTable: Incremental Clustering](incremental-clustering.md)
  - [PrimaryKeyTable: PK Clustering Override](pk-clustering-override.md)
- [Compact](docs/compact.md)
- Deletion Vectors(TODO)
  - AppendTable clustering.incremental and row-tracking.enabled
  - [PrimaryKeyTable: Deletion Vectors](deletion-vectors.md)
- Format
  - AppendTable
    - [mosaic](mosaic.md)
    - [blob](blob.md)

## Setup

- [local](setup-local.md)
- [flink](setup-flink.md)
- [spark](setup-spark.md)

## TODO

- [btree data-evolution clustering 为什么不能同时开启](btree.md)
- SparkLanceTable
- File Index:bitmap
- example of RESTCatalog
