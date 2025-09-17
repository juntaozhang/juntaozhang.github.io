# Flink 1.17 + Paimon CDC 源码深入剖析

## 主流程概览

### 1. Source 阶段 (数据读取)
```
SourceOperatorStreamTask (Flink)
└── SourceOperator.initReader()
    └── PostgresSourceReader (Flink CDC)
        └── IncrementalSourceRecordEmitter.processElement()
            ├── emit DataChangeRecord 
            ├── emit SchemaChangeEvent
            └── emit Watermark
```

**关键步骤**:
- `processElement`: `DataChangeRecord` → `CdcSourceRecord`
- `SourceOutputWithWatermarks` will enrich the record with watermark and timestamp

### 2. Parse 阶段 (数据解析) in SourceOperatorStreamTask
```
StreamFlatMap.processElement() (Flink)
└── PostgresRecordParser.flatMap() → extractRecords() (Paimon)
    └── TimestampedCollector (Flink)
        ├── CdcParsingProcessFunction 
        |     → output SchemaChange by tag
        |     → output CdcRecord
        └── UpdatedDataFieldsProcessFunction → applySchemaChange Schema Evolution
```

### 3. Write 阶段 (数据写入)
```
OneInputStreamTask (Flink)
└── CdcRecordStoreWriteOperator (Paimon)
    └── write(StoreSinkWriteImpl)
        └── 数据写入 Paimon 存储
```

### 4. Commit 阶段 (事务提交)
```
SubtaskCheckpointCoordinator (Flink)
└── checkpointState()
    └── CommitterOperator
        └── SinkWriterOperator
            └── 提交事务和元数据
```

### 流程图

```mermaid
graph TD
    A[PostgreSQL] --> B[Flink CDC Source]
    B --> C[PostgresRecordParser]
    C --> D[Schema Evolution]
    C --> E[Data Processing]
    E --> F[Paimon Writer]
    S[checkpoint] --> J[Paimon Commit]
    F --> H[Paimon Storage]
    J --> H
```

https://paimon.apache.org/docs/master/learn-paimon/understand-files/#flink-stream-write


TODO:

https://paimon.apache.org/docs/master/append-table/streaming/#bucketed-append 中的
'precommit-compact' = 'false'


'sink.rolling-policy.file-size' = '1MB',
'sink.rolling-policy.rollover-interval' = '1 min',
'sink.rolling-policy.check-interval' = '10 s'

CREATE TABLE t (id INT, data STRING) WITH ('row-tracking.enabled' = 'true');
INSERT INTO t VALUES (11, 'a'), (22, 'b');
SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER FROM t;


Changelog Producer:
None: 不查找旧值，不写changelog，适用于批作业，
Input: 不查找旧值, binlog CDC