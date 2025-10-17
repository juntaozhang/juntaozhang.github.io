# [cdc] Fix PostgreSQL DECIMAL type conversion issue

## Problem

When using `postgres_sync_table` action with Paimon CDC, PostgreSQL DECIMAL fields are incorrectly parsed as BYTES type instead of DECIMAL, causing schema conversion failures.

**Error**: `UnsupportedOperationException: Cannot convert field amount from type DECIMAL(10, 2) NOT NULL to BYTES NOT NULL`

## Root Cause
The issue occurs in the PostgreSQL CDC record parsing pipeline:
- PostgreSQL DECIMAL(10,2) fields are handled by Debezium with `decimal.handling.mode=precise` (default)
- `DebeziumSchemaUtils.decimalLogicalName()` field is `org.apache.kafka.connect.data.Decimal`, but field name and className is shaded to `org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Decimal`
    > ![1.png](1.png)
- `PostgresRecordParser.extractFieldType()` incorrectly parses shaded Debezium decimal schema as BYTES

## Reproduce

- **Flink**: 1.17
- **Paimon**: 1.3  
- **Action**: postgres_sync_table

### PostgreSQL table

```sql
CREATE TABLE public.orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO public.orders (id, user_id, amount, status)
VALUES
    (1, 101, 99.99, 'CREATED'),
    (2, 102, 49.50, 'CREATED'),
    (3, 101, 10.00, 'PAID');
```

### Flink Command
```shell
bin/flink run \
    -Drest.address=flink1-rest \
    -Drest.port=8081 \
    -Dexecution.checkpointing.interval=10s \
    -Dexecution.checkpointing.mode=EXACTLY_ONCE \
    lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    postgres_sync_table \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table orders \
    --primary_keys id \
    --postgres_conf hostname=xxx \
    --postgres_conf port=5432 \
    --postgres_conf username=postgres \
    --postgres_conf password=xxx \
    --postgres_conf database-name=test \
    --postgres_conf schema-name=public \
    --postgres_conf table-name=orders \
    --postgres_conf slot.name=pg_cdc_slot \
    --postgres_conf decoding.plugin.name=pgoutput \
    --postgres_conf scan.incremental.snapshot.enabled=true \
    --table_conf bucket=1 \
    --table_conf merge-engine=deduplicate \
    --table_conf changelog-producer=input
```

### Exception log:
```text
2025-09-10 16:30:25
java.lang.UnsupportedOperationException: Cannot convert field amount from type DECIMAL(10, 2) NOT NULL to BYTES NOT NULL of Paimon table ods.orders.
	at org.apache.paimon.flink.sink.cdc.UpdatedDataFieldsProcessFunctionBase.applySchemaChange(UpdatedDataFieldsProcessFunctionBase.java:144)
	at org.apache.paimon.flink.sink.cdc.UpdatedDataFieldsProcessFunction.processElement(UpdatedDataFieldsProcessFunction.java:77)
	at org.apache.paimon.flink.sink.cdc.UpdatedDataFieldsProcessFunction.processElement(UpdatedDataFieldsProcessFunction.java:43)
	at org.apache.flink.streaming.api.operators.ProcessOperator.processElement(ProcessOperator.java:66)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.pushToOperator(ChainingOutput.java:94)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.collect(ChainingOutput.java:81)
	at org.apache.flink.streaming.runtime.tasks.CopyingBroadcastingOutputCollector.collect(CopyingBroadcastingOutputCollector.java:60)
	at org.apache.flink.streaming.api.operators.ProcessOperator$ContextImpl.output(ProcessOperator.java:103)
	at org.apache.paimon.flink.sink.cdc.CdcParsingProcessFunction.processElement(CdcParsingProcessFunction.java:70)
	at org.apache.flink.streaming.api.operators.ProcessOperator.processElement(ProcessOperator.java:66)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.pushToOperator(ChainingOutput.java:94)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.collect(ChainingOutput.java:75)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.collect(ChainingOutput.java:39)
	at org.apache.flink.streaming.api.operators.TimestampedCollector.collect(TimestampedCollector.java:51)
	at java.base/java.util.ArrayList.forEach(Unknown Source)
	at org.apache.paimon.flink.action.cdc.postgres.PostgresRecordParser.flatMap(PostgresRecordParser.java:121)
	at org.apache.paimon.flink.action.cdc.postgres.PostgresRecordParser.flatMap(PostgresRecordParser.java:78)
	at org.apache.flink.streaming.api.operators.StreamFlatMap.processElement(StreamFlatMap.java:47)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.pushToOperator(ChainingOutput.java:94)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.collect(ChainingOutput.java:75)
	at org.apache.flink.streaming.runtime.tasks.ChainingOutput.collect(ChainingOutput.java:39)
	at org.apache.flink.streaming.runtime.tasks.SourceOperatorStreamTask$AsyncDataOutputToOutput.emitRecord(SourceOperatorStreamTask.java:309)
	at org.apache.flink.streaming.api.operators.source.SourceOutputWithWatermarks.collect(SourceOutputWithWatermarks.java:110)
	at org.apache.flink.streaming.api.operators.source.SourceOutputWithWatermarks.collect(SourceOutputWithWatermarks.java:101)
	at org.apache.flink.cdc.connectors.base.source.reader.IncrementalSourceRecordEmitter$OutputCollector.collect(IncrementalSourceRecordEmitter.java:176)
	at org.apache.paimon.flink.action.cdc.serialization.CdcDebeziumDeserializationSchema.deserialize(CdcDebeziumDeserializationSchema.java:78)
	at org.apache.flink.cdc.connectors.base.source.reader.IncrementalSourceRecordEmitter.emitElement(IncrementalSourceRecordEmitter.java:156)
	at org.apache.flink.cdc.connectors.base.source.reader.IncrementalSourceRecordEmitter.processElement(IncrementalSourceRecordEmitter.java:118)
	at org.apache.flink.cdc.connectors.base.source.reader.IncrementalSourceRecordEmitter.emitRecord(IncrementalSourceRecordEmitter.java:88)
	at org.apache.flink.cdc.connectors.base.source.reader.IncrementalSourceRecordEmitter.emitRecord(IncrementalSourceRecordEmitter.java:57)
	at org.apache.flink.connector.base.source.reader.SourceReaderBase.pollNext(SourceReaderBase.java:144)
	at org.apache.flink.streaming.api.operators.SourceOperator.emitNext(SourceOperator.java:419)
	at org.apache.flink.streaming.runtime.io.StreamTaskSourceInput.emitNext(StreamTaskSourceInput.java:68)
	at org.apache.flink.streaming.runtime.io.StreamOneInputProcessor.processInput(StreamOneInputProcessor.java:65)
	at org.apache.flink.streaming.runtime.tasks.StreamTask.processInput(StreamTask.java:550)
	at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.runMailboxLoop(MailboxProcessor.java:231)
	at org.apache.flink.streaming.runtime.tasks.StreamTask.runMailboxLoop(StreamTask.java:839)
	at org.apache.flink.streaming.runtime.tasks.StreamTask.invoke(StreamTask.java:788)
	at org.apache.flink.runtime.taskmanager.Task.runWithSystemExitMonitoring(Task.java:952)
	at org.apache.flink.runtime.taskmanager.Task.restoreAndInvoke(Task.java:931)
	at org.apache.flink.runtime.taskmanager.Task.doRun(Task.java:745)
	at org.apache.flink.runtime.taskmanager.Task.run(Task.java:562)
	at java.base/java.lang.Thread.run(Unknown Source)
```
