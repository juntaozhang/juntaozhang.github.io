# fix bug: postgres decimal type not support
## env 
flink1.17
paimon1.3
postgres_sync_table

create table in postgres:
```postgres.sql
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

```mysql.sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (id, user_id, amount, status)
VALUES
  (1, 101, 99.99, 'CREATED'),
  (2, 102, 49.50, 'CREATED'),
  (3, 101, 10.00, 'PAID');
```

exception log:
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

### debug in local

change src code and rebuild paimon-flink-1.17
> mvn install -DskipTests -Drat.skip=true -P flink1 -pl :paimon-flink-1.17 -am

> cp ~/.m2/repository/org/apache/paimon/paimon-flink-1.17/1.3-SNAPSHOT/paimon-flink-1.17-1.3-SNAPSHOT.jar  ./


vm.args:
```text
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
-Drest.port=8083
-Dexecution.checkpointing.interval=10s
```

java args:
```text
postgres_sync_table
--warehouse s3a://warehouse/paimon
--database ods
--table orders
--primary_keys id
--postgres_conf hostname=localhost
--postgres_conf port=5432
--postgres_conf username=postgres
--postgres_conf password=postgres123
--postgres_conf database-name=test
--postgres_conf schema-name=public
--postgres_conf table-name=orders
--postgres_conf slot.name=pg_cdc_slot
--postgres_conf decoding.plugin.name=pgoutput
--postgres_conf scan.incremental.snapshot.enabled=true
--table_conf bucket=1
--table_conf merge-engine=deduplicate
--table_conf changelog-producer=input
```

mysql java args:
```text
mysql_sync_table
--warehouse
s3a://warehouse/paimon
--database
ods
--table
orders
--primary_keys
id
--mysql_conf
hostname=localhost
--mysql_conf
username=root
--mysql_conf
port=3307
--mysql_conf
password=root123
--mysql_conf
database-name=test
--mysql_conf
table-name=orders
--mysql_conf
server-time-zone=UTC
--table_conf
bucket=1
--table_conf
merge-engine=deduplicate
--table_conf
changelog-producer=input
```



## RCA

pg: `amount DECIMAL(10, 2) NOT NULL`
java.lang.UnsupportedOperationException: Cannot convert field amount from type DECIMAL(10, 2) NOT NULL to BYTES NOT NULL of Paimon table ods.orders.

PostgresSyncTableAction.retrieveSchema -> JdbcSchemaUtils create table schema(PostgresToPaimonTypeVisitor.toDataType) is right

build TableSchema for flink-connector-postgres-cdc
io.debezium.connector.postgresql.PostgresValueConverter.schemaBuilder -> numericSchema -> SpecialValueDecimal.builder(PRECISE)
    io.debezium.relational.TableSchemaBuilder.create -> Decimal.builder(org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Decimal)

RowDataDebeziumDeserializeSchema.createDecimalConverter deal with Decimal
    use `debezium.decimal.handling.mode=precise` by default
    https://debezium.io/documentation/reference/stable/connectors/postgresql.html#postgres
CdcDebeziumDeserializationSchema from paimon read data
    JsonConverter 类加载时执行执行 -> LOGICAL_CONVERTERS.put("org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Decimal"
        jsonConverter.fromConnectData 执行成功 CdcSourceRecord 没有问题

CdcDebeziumDeserializationSchema 
    -> StreamFlatMap#userFunction(PostgresRecordParser)
        -> PostgresRecordParser.extractSchema->extractFieldType 这里在解析schema的时候解析成BYTES，具体原因如下：
        DebeziumSchemaUtils.decimalLogicalName() field is shaded to org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Decimal





