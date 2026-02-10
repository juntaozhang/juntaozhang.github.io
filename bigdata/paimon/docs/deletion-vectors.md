# Table Mode

## MOR (Merge On Read)
```flinksql
CREATE TABLE T (
  k INT,
  v INT,
  pt INT,
  PRIMARY KEY (k) NOT ENFORCED
) WITH (
  'bucket' = '2'
);
INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1);
INSERT INTO T VALUES (1, 11, 1);
call sys.compact(`table` => 'T')
INSERT INTO T VALUES (5, 50, 1);
INSERT INTO T VALUES (6, 60, 1);
SELECT * FROM T$files ORDER BY level DESC;
```
```text
+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
|partition|bucket|file_path                                                                                                                                                                 |file_format|schema_id|level|record_count|file_size_in_bytes|min_key|max_key|null_value_counts|min_value_stats   |max_value_stats   |min_sequence_number|max_sequence_number|creation_time          |deleteRowCount|file_source|first_row_id|write_cols|
+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
|{}       |0     |/private/var/folders/8w/3j0ns1bd6xjf_9n5_h2dyjt80000gn/T/spark-185ff4a9-54e7-4750-9173-2da5b8f91282/test.db/T/bucket-0/data-cd30fd21-1e18-444d-8c08-a187e2c5e84e-0.parquet|parquet    |0        |5    |4           |1360              |[1]    |[4]    |{pk=0, pt=0, v=0}|{pk=1, pt=1, v=11}|{pk=4, pt=1, v=40}|1                  |4                  |2026-02-04 23:34:27.957|0             |COMPACT    |NULL        |NULL      |
|{}       |0     |/private/var/folders/8w/3j0ns1bd6xjf_9n5_h2dyjt80000gn/T/spark-185ff4a9-54e7-4750-9173-2da5b8f91282/test.db/T/bucket-0/data-c6e1dcac-1d42-4c8a-8b43-9b351696a229-0.parquet|parquet    |0        |0    |1           |1223              |[5]    |[5]    |{pk=0, pt=0, v=0}|{pk=5, pt=1, v=50}|{pk=5, pt=1, v=50}|5                  |5                  |2026-02-04 23:34:28.245|0             |APPEND     |NULL        |NULL      |
|{}       |0     |/private/var/folders/8w/3j0ns1bd6xjf_9n5_h2dyjt80000gn/T/spark-185ff4a9-54e7-4750-9173-2da5b8f91282/test.db/T/bucket-0/data-2bfecd53-0da9-417b-99b2-8c568bfbb6e1-0.parquet|parquet    |0        |0    |1           |1223              |[6]    |[6]    |{pk=0, pt=0, v=0}|{pk=6, pt=1, v=60}|{pk=6, pt=1, v=60}|6                  |6                  |2026-02-04 23:34:28.512|0             |APPEND     |NULL        |NULL      |
+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
```

- 更新后会生成 L0 文件（小文件）
- 读取时会合并 L0 和 L1 文件
- 写入性能好，读取性能相对较差

### MOR Read Optimized
定时触发compaction，合并L0文件

## MOW (Merge On Write)
'full-compaction.delta-commits' = '1'
每次commit都会触发一次compaction
写性能差


## MOW (Merge On Write)
'deletion-vectors.enabled' = 'true'

```sparksql
drop table T;
CREATE TABLE T (
  pk INT,
  v INT,
  pt INT
) TBLPROPERTIES (
  'primary-key' = 'pk',
  'bucket' = '1',
  'deletion-vectors.enabled' = 'true'
);
INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1);
update T set v = 11 where pk = 1;
SELECT * FROM `T$snapshots` ORDER BY level DESC;
SELECT * FROM `T$files` ORDER BY level DESC;
```
```text
+-----------+---------+------------------------------------+-------------------+-----------+-----------------------+----------------------------------------------------+----------------------------------------------------+-----------------------+------------------+------------------+----------------------+---------+-----------+
|snapshot_id|schema_id|commit_user                         |commit_identifier  |commit_kind|commit_time            |base_manifest_list                                  |delta_manifest_list                                 |changelog_manifest_list|total_record_count|delta_record_count|changelog_record_count|watermark|next_row_id|
+-----------+---------+------------------------------------+-------------------+-----------+-----------------------+----------------------------------------------------+----------------------------------------------------+-----------------------+------------------+------------------+----------------------+---------+-----------+
|1          |0        |bf9054ab-0144-47dd-9406-185cc62bb836|9223372036854775807|APPEND     |2026-02-05 00:18:02.112|manifest-list-564c6027-1c47-4841-9cb9-94e1217de620-0|manifest-list-564c6027-1c47-4841-9cb9-94e1217de620-1|NULL                   |4                 |4                 |NULL                  |NULL     |0          |
|2          |0        |bf9054ab-0144-47dd-9406-185cc62bb836|9223372036854775807|COMPACT    |2026-02-05 00:18:02.296|manifest-list-564c6027-1c47-4841-9cb9-94e1217de620-2|manifest-list-564c6027-1c47-4841-9cb9-94e1217de620-3|NULL                   |4                 |0                 |NULL                  |NULL     |0          |
|3          |0        |4a82fdcc-2b83-4d3b-a768-4439b4e38353|9223372036854775807|APPEND     |2026-02-05 00:18:03.298|manifest-list-e24a9787-da8b-4836-ac9b-939ad874b671-0|manifest-list-e24a9787-da8b-4836-ac9b-939ad874b671-1|NULL                   |5                 |1                 |NULL                  |NULL     |0          |
|4          |0        |4a82fdcc-2b83-4d3b-a768-4439b4e38353|9223372036854775807|COMPACT    |2026-02-05 00:18:03.33 |manifest-list-e24a9787-da8b-4836-ac9b-939ad874b671-2|manifest-list-e24a9787-da8b-4836-ac9b-939ad874b671-3|NULL                   |4                 |-1                |NULL                  |NULL     |0          |
+-----------+---------+------------------------------------+-------------------+-----------+-----------------------+----------------------------------------------------+----------------------------------------------------+-----------------------+------------------+------------------+----------------------+---------+-----------+

+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
|partition|bucket|file_path                                                                                                                                                                 |file_format|schema_id|level|record_count|file_size_in_bytes|min_key|max_key|null_value_counts|min_value_stats   |max_value_stats   |min_sequence_number|max_sequence_number|creation_time          |deleteRowCount|file_source|first_row_id|write_cols|
+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
|{}       |0     |/private/var/folders/8w/3j0ns1bd6xjf_9n5_h2dyjt80000gn/T/spark-b183e34c-72c7-4b25-a733-74d5679bcd6f/test.db/T/bucket-0/data-2d0ce06b-637d-4ee6-b45b-92090f223169-0.parquet|parquet    |0        |5    |4           |1359              |[1]    |[4]    |{pk=0, pt=0, v=0}|{pk=1, pt=1, v=10}|{pk=4, pt=1, v=40}|0                  |3                  |2026-02-05 00:18:39.254|0             |APPEND     |NULL        |NULL      |
+---------+------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------+---------+-----+------------+------------------+-------+-------+-----------------+------------------+------------------+-------------------+-------------------+-----------------------+--------------+-----------+------------+----------+
```

compaction 会 [forcePickL0](compact.md#forceuplevel0compaction)

compact vs prepareCommit:\
- compact
  - flushWriteBuffer(forcedFullCompaction=true) : default true
- MergeTreeWriter.prepareCommit：
  - flushWriteBuffer(forcedFullCompaction=false): minor compact
- trigger MergeTreeCompactTask in MergeTreeCompactManager
  - KeyValueFileStoreWrite.createWriter中 createCompactManager 可以看到 CompactManager 创建过程

为什么 table.newScan().plan().splits() 看不到 deletion-vectors.enabled 的数据？
- table.newSnapshotReader().onlyReadRealBuckets().read().splits() 可以看到 deletion-vectors.enabled 的数据
- `table.newScan()` 读取数据时会跳过 level0
    ```text
    CoreOptions.batchScanSkipLevel0 中 DELETION_VECTORS_ENABLED || FIRST_ROW 会启动： 
    DataTableBatchScan 中会跳过 level0
    if (!schema.primaryKeys().isEmpty() && options.batchScanSkipLevel0()) {
        if (options.toConfiguration()
                .get(CoreOptions.BATCH_SCAN_MODE)
                .equals(CoreOptions.BatchScanMode.NONE)) {
            snapshotReader.withLevelFilter(level -> level > 0).enableValueFilter();
        }
    }
    ```
### append files
`INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1);`:\
会产生两个 sortedRuns，一个是 append，一个是 compaction
```text
partition: BinaryRow{pos=1}
bucket: 0
DataIncrement {
  newFiles = [data-2d0ce06b-637d-4ee6-b45b-92090f223169-0.parquet]
  deletionFiles = []
  changelogFiles = []
  newIndexFiles = []
  deletedIndexFiles = []
}
CompactIncrement {
  compactBefore = [data-2d0ce06b-637d-4ee6-b45b-92090f223169-0.parquet]
  compactAfter = [data-2d0ce06b-637d-4ee6-b45b-92090f223169-0.parquet]
  changelogFiles = []
  newIndexFiles = []
  deletedIndexFiles = []
}
```
commitMessages 会有 newFilesIncrement 和 compactIncrement， 这两种类型的commits 会导致两个sortedRuns,\
外面看就是一次action会有两个snapshots。

与 Postpone compaction 的区别是，Postpone 把 postpose bucket 的 文件写到 fixed bucket中，\
这个过程是就是 compaction的一部分，所以 BucketFiles 会从 compactIncrement 文件中去掉 newFilesIncrement 文件,\
从最终来看就只有一个snapshot。


### write deleted index file 
- 当 deletion-vectors.enabled = 'false'（默认）：使用传统 Delete Record
- 当 deletion-vectors.enabled = 'true'：使用 Deletion Vector Index，不再使用 Delete Record
    - `delete from T where id > 2`, 会生成以下 commits：
      ```text
      - partition: BinaryRow{pos=1}
      - bucket: 0
      - newFilesIncrement:
        - newFiles: [data-17787bc0-0d46-4dda-a016-4c2ed4b2d3fd.parquet]
        - deletionFiles: [] 
        - newIndexFiles: []
      - compactIncrement:
        - compactBefore: [data-17787bc0-0d46-4dda-a016-4c2ed4b2d3fd.parquet]
        - compactAfter: []
        - changedLogFiles: []
        - newIndexFiles: [index-848a8794-3da4-4d68-8ba0-2683da383853] 
      ```
    - 会生成两个 sortedRuns，一个是 append，实际上是删除的record，一个是 compaction
      - compaction 可以看成删除之前 delete record文件，使用 deleted index file
- 源码中：`MergeTreeCompactTask`->`ChangelogMergeTreeRewriter.rewriteOrProduceChangelog`中由于`dropDelete`(标识是deletion-vectors，所以Compact After 为空)

delete index files generation, when compact will:
```text
`MergeTreeCompactTask.doCompact` -> `ChangelogMergeTreeRewriter.upgrade` -> `SortMergeIterator.merge` 
    -> `LookupChangelogMergeFunctionWrapper.getResult`: which will get fileName and position
    -> `BucketedDvMaintainer.notifyNewDeletion` will add a position in roaringBitmap
```

write deleted index file:
```text
`AbstractFileStoreWrite.prepareCommit`
    -> `LazyCompactDeletionFile.getOrCompute`
    -> `BucketedDvMaintainer.writeDeletionVectorsIndex`
    -> `DeletionVectorIndexFileWriter.writeSingleFile` 
```

### read deleted index file
`select * from T`, 会从deleted index 中过滤掉删除的record

spark 流程：\
ReadBuilderImpl -> PrimaryKeyFileStoreTable

SparkTable -> PaimonScanBuilder -> PaimonScan -> PaimonBatch 
-> PaimonPartitionReaderFactory -> PaimonPartitionReader.readSplit

KeyValueTableRead ->  RawFileSplitRead-> ApplyDeletionVectorReader -> ApplyDeletionFileRecordIterator -> 过滤被删除的record

### MOW manual compaction
```sparksql
drop table T;
CREATE TABLE T (
  pk INT,
  v INT,
  pt INT
) TBLPROPERTIES (
  'primary-key' = 'pk',
  'bucket' = '1',
  'write-only'='true',
  'deletion-vectors.enabled' = 'true'
);
INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1); -- can't be seen before compaction
call sys.compact(`table` => 'T'); -- full compaction
update T set v = 11 where pk = 1;
SELECT * FROM `T$snapshots` ORDER BY level DESC;
call sys.compact(`table` => 'T');
SELECT * FROM `T$files` ORDER BY level DESC;
-- ALTER TABLE T SET TBLPROPERTIES ('deletion-vectors.enabled' = 'false');
```

[full compaction](compact.md#full-compaction) 会枚举 partitions buckets `write.compact` 对应的 files

源码中：\
KeyValueFileStoreWrite.compact -> createWriterContainer create BucketedDvMaintainer -> DeletionVectorsIndexFile(read bitmap)\

full compaction -> MergeTreeWriter.compact -> FileRewriteCompactTask -> MergeTreeCompactRewriter -> DropDeleteReader->ApplyDeletionFileRecordIterator

after full compaction, 会生成新的 sortedRun, 旧的数据会被删除，形成新的文件
