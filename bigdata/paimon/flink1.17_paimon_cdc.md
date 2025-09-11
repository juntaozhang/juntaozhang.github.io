
https://nightlies.apache.org/flink/flink-cdc-docs-master/docs/connectors/flink-sources/tutorials/build-real-time-data-lake-tutorial/

https://nightlies.apache.org/flink/flink-cdc-docs-master/docs/connectors/flink-sources/postgres-cdc/


### add table in PostgreSQL
[install.md](../../postgres/install.md)


### PG SQL
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

-- 更新一条
UPDATE public.orders SET status = 'PAID', amount = 109.99 WHERE id = 1;

-- 删除一条
DELETE FROM public.orders WHERE id = 2;
```

#### Connect to SQL Client
```bash
bin/sql-client.sh embedded -Dexecution.target=remote -Dkubernetes.cluster-id=flink1
```


```SQL
CREATE CATALOG paimon_catalog WITH (
'type' = 'paimon',
'warehouse' = 's3a://warehouse/paimon'
);
USE CATALOG paimon_catalog;
use ods;

CREATE TABLE default_catalog.default_database.pg_orders_src (
id BIGINT,
user_id BIGINT,
amount DECIMAL(10,2),
status STRING,
created_at TIMESTAMP(3),
PRIMARY KEY (id) NOT ENFORCED
) WITH (
'connector' = 'postgres-cdc',
'hostname' = 'postgresql.default.svc.cluster.local',
'port' = '5432',
'username' = 'postgres',
'password' = 'postgres123',
'database-name' = 'test',
'schema-name' = 'public',
'table-name' = 'orders',         -- 可支持正则
'slot.name' = 'pg_cdc_slot',     -- 逻辑复制槽
'decoding.plugin.name' = 'pgoutput',   -- Postgres 10+ 默认
'scan.incremental.snapshot.enabled' = 'true'  -- 先快照后增量
);


DROP TABLE IF EXISTS paimon_catalog.ods.orders;
CREATE TABLE paimon_catalog.ods.orders (
id         BIGINT                      NOT NULL,
user_id    BIGINT                      NOT NULL,
-- amount     DECIMAL(10, 2)              NOT NULL,
amount     float8                      NOT NULL,
status     VARCHAR(20),                -- 与 PG 一致（而不是 STRING）
created_at TIMESTAMP(6),               -- 精度对齐 PG 的 (6)
PRIMARY KEY (id) NOT ENFORCED
) WITH (
'bucket' = '1',
'merge-engine' = 'deduplicate',
'changelog-producer' = 'input'
);

SET 'execution.runtime-mode' = 'STREAMING';
SET 'execution.checkpointing.interval' = '10 s';
SET 'execution.checkpointing.mode' = 'EXACTLY_ONCE';
INSERT INTO paimon_catalog.ods.orders SELECT * FROM default_catalog.default_database.pg_orders_src;


SET 'execution.runtime-mode' = 'STREAMING';
SET 'sql-client.execution.result-mode' = 'tableau';
select * from paimon_catalog.ods.`orders$audit_log` /*+ OPTIONS('scan.mode'='latest') */;
```

https://nightlies.apache.org/flink/flink-cdc-docs-release-3.1/docs/connectors/flink-sources/overview/
https://paimon.apache.org/docs/master/cdc-ingestion/postgres-cdc/

kubernetes-session
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
    --postgres_conf hostname=postgresql.default.svc.cluster.local \
    --postgres_conf port=5432 \
    --postgres_conf username=postgres \
    --postgres_conf password=postgres123 \
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

kubernetes-application
```shell
export cluster_id=flink-postgres-sync-table
bin/flink run-application \
    --target kubernetes-application \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=8084 \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.container.image=flink:1.17.2-scala_2.12-paimon \
    -Dexecution.checkpointing.interval=10s \
    -Dexecution.checkpointing.mode=EXACTLY_ONCE \
    -Dkubernetes.containerized.master.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dkubernetes.containerized.taskmanager.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dfs.s3a.endpoint=http://minio.default.svc.cluster.local:9000 \
    -Dfs.s3a.path.style.access=true \
    -Dfs.s3a.connection.ssl.enabled=false \
    -Dfs.s3a.access.key=minio \
    -Dfs.s3a.secret.key=minio12345 \
    -Dstate.checkpoints.dir=s3a://flink-bucket/$cluster_id/checkpoints \
    -Dstate.savepoints.dir=s3a://flink-bucket/$cluster_id/savepoints \
    local:///opt/flink/lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    postgres_sync_table \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table orders \
    --primary_keys id \
    --postgres_conf hostname=postgresql.default.svc.cluster.local \
    --postgres_conf port=5432 \
    --postgres_conf username=postgres \
    --postgres_conf password=postgres123 \
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


## MySQL to Paimon
mysql-connector-j-9.4.0.jar
flink-sql-connector-mysql-cdc-3.1.1.jar
```shell
bin/flink run \
    -Drest.address=flink1-rest \
    -Drest.port=8081 \
    -Dexecution.checkpointing.interval=10s \
    -Dexecution.checkpointing.mode=EXACTLY_ONCE \
    lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    mysql_sync_table \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table orders \
    --primary_keys id \
    --mysql_conf hostname=mysql \
    --mysql_conf username=root \
    --mysql_conf port=3307 \
    --mysql_conf password=root123 \
    --mysql_conf database-name='test' \
    --mysql_conf table-name='orders' \
    --table_conf bucket=1 \
    --table_conf merge-engine=deduplicate \
    --table_conf changelog-producer=input
```

## troubleshooting:
#### flink1.17.2 paimon1.3 postgres_sync_table DECIMAL to BYTES
[bug.md](flink1.17-paimon1.3-example/bug.md)

#### target kubernetes-session -Dkubernetes.cluster-id=flink2 为什么client 无法修改rest.address 如:
```text
2025-09-07 13:15:10,033 INFO  org.apache.flink.kubernetes.KubernetesClusterDescriptor      [] - Retrieve flink cluster flink1 successfully, JobManager Web Interface: http://localhost:8081
```
使用 `bin/flink run -Drest.address=flink2-rest -Drest.port=8082` instead of `bin/flink run --target kubernetes-session -Dkubernetes.cluster-id=flink1` 

#### No ExecutorFactory found to execute the application
org.apache.flink.client.program.ProgramInvocationException: The main method caused an error: No ExecutorFactory found to execute the application.
`-- target kubernetes-application`  use `run-application` instead of `run`

#### mysql > 8.2.3 `show MASTER STATUS` replaced by `SHOW REPLICA STATUS` cause exception, so need downgrade to 8.2.3
```text
Caused by: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'MASTER STATUS' at line 1
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:112)
	at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:114)
	at com.mysql.cj.jdbc.StatementImpl.executeQuery(StatementImpl.java:1311)
	at io.debezium.jdbc.JdbcConnection.queryAndMap(JdbcConnection.java:641)
	at io.debezium.jdbc.JdbcConnection.queryAndMap(JdbcConnection.java:510)
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.currentBinlogOffset(DebeziumUtils.java:123)
	... 7 more
```

#### add java args`--mysql_conf server-time-zone=UTC`
```text
22:30:05,338 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
org.apache.flink.table.api.ValidationException: The MySQL server has a timezone offset (0 seconds ahead of UTC) which does not match the configured timezone Asia/Shanghai. Specify the right server-time-zone to avoid inconsistencies for time-related fields.
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.checkTimeZone(MySqlValidator.java:215) ~[flink-sql-connector-mysql-cdc-3.1.1.jar:3.1.1]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:76) ~[flink-sql-connector-mysql-cdc-3.1.1.jar:3.1.1]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.1.1.jar:3.1.1]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:217) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.applyCall(RecreateOnResetOperatorCoordinator.java:320) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.start(RecreateOnResetOperatorCoordinator.java:70) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.operators.coordination.OperatorCoordinatorHolder.start(OperatorCoordinatorHolder.java:181) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:165) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startAllOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:82) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.scheduler.SchedulerBase.startScheduling(SchedulerBase.java:615) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.jobmaster.JobMaster.startScheduling(JobMaster.java:1044) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.jobmaster.JobMaster.startJobExecution(JobMaster.java:961) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.jobmaster.JobMaster.onStart(JobMaster.java:424) ~[flink-runtime-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.rpc.RpcEndpoint.internalCallOnStart(RpcEndpoint.java:198) ~[flink-rpc-core-1.17.2.jar:1.17.2]
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor$StoppedState.lambda$start$0(AkkaRpcActor.java:622) ~[flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at org.apache.flink.runtime.concurrent.akka.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68) ~[flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor$StoppedState.start(AkkaRpcActor.java:621) ~[flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleControlMessage(AkkaRpcActor.java:190) ~[flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:24) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:20) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:127) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:126) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:20) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:175) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.Actor.aroundReceive(Actor.scala:537) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.Actor.aroundReceive$(Actor.scala:535) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.AbstractActor.aroundReceive(AbstractActor.scala:220) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.ActorCell.receiveMessage$$$capture(ActorCell.scala:579) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.actor.ActorCell.invoke(ActorCell.scala:547) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:270) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.dispatch.Mailbox.run(Mailbox.scala:231) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at akka.dispatch.Mailbox.exec(Mailbox.scala:243) [flink-rpc-akka_5fd6d196-9244-4a29-baf4-6243206d46ce.jar:1.17.2]
	at java.util.concurrent.ForkJoinTask.doExec$$$capture(ForkJoinTask.java:373) [?:?]
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java) [?:?]
	at java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182) [?:?]
	at java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655) [?:?]
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622) [?:?]
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165) [?:?]
```

#### UpdatedDataFieldsProcessFunction 作用是什么？
`CdcSinkBuilder.build` registers `SCHEMA_CHANGE_OUTPUT_TAG` (via `getSideOutput`) and attaches `UpdatedDataFieldsProcessFunction` to handle schema change events (updated field metadata).

#### why mysql cdc support `amount decimal(10,2) NOT NULL`
`MySqlRecordParser.flatMap` not use `extractSchema`:
`MySqlSnapshotSplitReadTask.createDataEventsForTable` create `row` by `ResultSet`, and `CdcSourceRecord` amount is already decimal
EventDispatcher.BufferingSnapshotChangeRecordReceiver.changeRecord