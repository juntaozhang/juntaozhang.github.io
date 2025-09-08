
https://nightlies.apache.org/flink/flink-cdc-docs-master/docs/connectors/flink-sources/tutorials/build-real-time-data-lake-tutorial/

https://nightlies.apache.org/flink/flink-cdc-docs-master/docs/connectors/flink-sources/postgres-cdc/


### add table in PostgreSQL
[install.md](../../postgres/install.md)


#### Connect to SQL Client
```bash
bin/sql-client.sh embedded -Dexecution.target=remote -Dkubernetes.cluster-id=flink2
```

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


https://nightlies.apache.org/flink/flink-cdc-docs-release-3.1/docs/connectors/flink-sources/overview/
https://paimon.apache.org/docs/master/cdc-ingestion/postgres-cdc/

kubernetes-session
```shell
bin/flink run \
    -Drest.address=flink2-rest \
    -Drest.port=8082 \
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


### troubleshooting:
flink1.17.2 paimon1.3 DECIMAL to BYTES
pg的 -- amount     DECIMAL(10, 2)              NOT NULL,
java.lang.UnsupportedOperationException: Cannot convert field amount from type DECIMAL(10, 2) NOT NULL to BYTES NOT NULL of Paimon table ods.orders.


~~--target kubernetes-session -Dkubernetes.cluster-id=flink1 为什么client 无法修改rest.address 如：~~
2025-09-07 13:15:10,033 INFO  org.apache.flink.kubernetes.KubernetesClusterDescriptor      [] - Retrieve flink cluster flink1 successfully, JobManager Web Interface: http://localhost:8081
使用 `bin/flink run -Drest.address=flink2-rest -Drest.port=8082` instead of `bin/flink run --target kubernetes-session -Dkubernetes.cluster-id=flink1` 


~~--target kubernetes-application  use run-application instead of run~~
org.apache.flink.client.program.ProgramInvocationException: The main method caused an error: No ExecutorFactory found to execute the application.
