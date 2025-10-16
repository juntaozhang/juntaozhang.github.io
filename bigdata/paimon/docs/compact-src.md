

## Flink CompactAction TODO

## Spark sql
### Paimon Procedure: compact aware bucket pk table with many small files
`org.apache.paimon.spark.procedure.CompactProcedureTest`

#### compact high level pipeline
```mermaid
sequenceDiagram
    PaimonSqlExtensionsParser.CallContext->>PaimonCallStatement: visitCall(parse call statement)
    PaimonCallStatement->>PaimonCallCommand: PaimonProcedureResolver.apply
    PaimonCallCommand->>PaimonCallExec: PaimonStrategy.apply
    PaimonCallExec->>+CompactProcedure: run
    CompactProcedure->>CompactProcedure: call
    CompactProcedure->>CompactProcedure: execute
    CompactProcedure->>-CompactProcedure: compactAwareBucketTable
```

#### compactAwareBucketTable, spark mapPartitions task
- get `readParallelism` from partitions and buckets, 每个task处理一个bucket compact具体流程如下：
```mermaid
graph LR
    CompactProcedure.compactAwareBucketTable --> TableWriteImpl.compact --> KeyValueFileStoreWrite.compact --> MergeTreeWriter.compact --> |prepare SortedRun files meta| MergeTreeCompactManager.triggerCompaction
    triggerCompaction -->|异步提交| MergeTreeCompactTask.call --> MergeTreeCompactRewriter.rewrite --> |按策略合并旧文件输出新文件| RollingFileWriter.write
```

- 真实合并文件在`MergeTreeCompactRewriter`, 基于策略挑选需要压缩的文件集合，判断是否可丢弃删除标记，然后提交对应的压缩任务, 
create reader pipeline:
```mermaid
graph LR
    MergeTreeCompactRewriter.readerForMergeTree-->MergeTreeReaders.readerForMergeTree-->KeyValueFileReaderFactory.createRecordReader-->KeyValueDataFileRecordReader-->|mergeSort|SortMergeReaderWithLoserTree--> |create|ConcatRecordReader
```

- 然后`readBatch` stack:
![compact-reader-stack.png](imgs/compact-reader-stack.png)



  

#### commit metadata
`compactAwareBucketTable` commit manifest
```mermaid
graph LR
    compactAwareBucketTable-->TableCommitImpl.commit --> |create manifest|createManifestCommittable
    TableCommitImpl.commit --> TableCommitImpl.commitMultiple --> FileStoreCommitImpl.commit --> |appendTableFiles|tryCommit --> tryCommitOnce --> RenamingSnapshotCommit.commitLatestHint
```

- [Concurrency Control](https://paimon.apache.org/docs/master/concepts/concurrency-control/)
`tryCommit` will wait for a while if `tryCommitOnce` failed, then retry again, until success or exceed max retry times.
`tryCommitOnce` will base on the lastest snapshot as base `baseManifestList`, then create new `deltaManifestList`, then generates a new snapshot based on the current snapshot,
if commit failed due to other client already committed, try again by lastest snapshot.
