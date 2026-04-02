# Flink Paimon Primary Key Table Examples

This document demonstrates how to work with Paimon primary key tables in Flink, including table creation, data operations, and advanced query features.

## Table of Contents
1. **Primary Key Tables**: Both simple and composite primary keys with partitioning
2. **UPSERT Operations**: Using INSERT statements to update existing records
3. **Time Travel**: Query historical data using snapshots and timestamps
4. **Tagging**: Create named snapshots for easy reference
5. **Incremental Queries**: Efficiently query changes between snapshots/tags
6. **Audit Trail**: Track all changes using audit log tables
7. **Performance Optimization(TODO)**: Parallel reading and compaction
8. **Object Storage Integration**: S3/MinIO configuration for cloud storage

## Catalog and Database Setup

First, create a Paimon catalog and use the appropriate database:

```sql
-- Create Paimon catalog with S3 warehouse
CREATE CATALOG paimon_catalog WITH (
    'type' = 'paimon',
    'warehouse' = 's3a://warehouse/paimon'
);

USE CATALOG paimon_catalog;
USE ods;
```

## Primary Key Table Creation

### Order Fact Table (Partitioned Primary Key)

```sql
CREATE TABLE order_fact (
    order_id BIGINT,
    user_id BIGINT,
    product_name STRING,
    quantity INT,
    price DECIMAL(10, 2),
    order_time TIMESTAMP(3),
    status STRING,
    dt STRING,
    PRIMARY KEY (dt, order_id) NOT ENFORCED
) PARTITIONED BY (dt) WITH (
    'bucket' = '2',
    'sink.rolling-policy.file-size' = '1MB',
    'sink.rolling-policy.rollover-interval' = '1 min'
);
```

### User Info Table (Simple Primary Key)

```sql
CREATE TABLE user_info (
    user_id BIGINT,
    user_name STRING,
    email STRING,
    phone STRING,
    address STRING,
    register_time TIMESTAMP(3),
    PRIMARY KEY (user_id) NOT ENFORCED
);
```

## Data Operations

### Configuration Setup

```sql
SET 'execution.runtime-mode' = 'batch';
SET 'sql-client.execution.result-mode' = 'tableau';
```

### Initial Data Loading

#### User Information

```sql
INSERT INTO user_info VALUES
(1001, 'Alice Smith', 'alice@example.com', '1234567890', '123 Main St, City A', CAST('2025-01-15 09:00:00' AS TIMESTAMP(3))),
(1002, 'Bob Johnson', 'bob@example.com', '1234567891', '456 Oak Ave, City B', CAST('2025-02-20 10:30:00' AS TIMESTAMP(3))),
(1003, 'Charlie Brown', 'charlie@example.com', '1234567892', '789 Pine Rd, City C', CAST('2025-03-10 14:15:00' AS TIMESTAMP(3))),
(1004, 'Diana Prince', 'diana@example.com', '1234567893', '321 Elm St, City D', CAST('2025-04-05 11:45:00' AS TIMESTAMP(3))),
(1005, 'Edward Green', 'edward@example.com', '1234567894', '654 Maple Dr, City E', CAST('2025-05-12 16:20:00' AS TIMESTAMP(3))),
(1006, 'Fiona White', 'fiona@example.com', '1234567895', '987 Cedar Ln, City F', CAST('2025-06-18 08:30:00' AS TIMESTAMP(3)));
```

#### Order Data (Snapshot 1)

```sql
INSERT INTO order_fact VALUES
(1, 1001, 'Laptop', 1, 999.99, CAST('2025-08-30 10:00:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(2, 1002, 'Mouse', 2, 25.50, CAST('2025-08-30 10:05:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(3, 1001, 'Keyboard', 1, 75.00, CAST('2025-08-30 10:10:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(4, 1001, 'Gaming Laptop', 1, 1299.99, CAST('2025-08-30 10:15:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(5, 1003, 'Monitor', 1, 299.99, CAST('2025-08-30 11:00:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(6, 1004, 'Headphones', 1, 89.99, CAST('2025-08-30 11:15:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(7, 1005, 'Webcam', 1, 149.99, CAST('2025-08-30 11:30:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30'),
(8, 1003, 'Speakers', 2, 79.99, CAST('2025-08-30 12:00:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30');
```

#### Additional Order (Snapshot 2)

```sql
INSERT INTO order_fact VALUES
(9, 1006, 'Tablet', 1, 399.99, CAST('2025-08-30 12:30:00' AS TIMESTAMP(3)), 'PENDING', '2025-08-30');
```

### Upsert Operations (Status Updates)

#### Update Order Status (Snapshot 3-5)

```sql
-- Confirm order 1 (Snapshot 3)
INSERT INTO order_fact VALUES
(1, 1001, 'Laptop', 1, 999.99, CAST('2025-08-30 10:00:00' AS TIMESTAMP(3)), 'CONFIRMED', '2025-08-30');

-- Ship order 2 (Snapshot 4)
INSERT INTO order_fact VALUES
(2, 1002, 'Mouse', 2, 25.50, CAST('2025-08-30 10:05:00' AS TIMESTAMP(3)), 'SHIPPED', '2025-08-30');

-- Deliver order 3 (Snapshot 5)
INSERT INTO order_fact VALUES
(3, 1001, 'Keyboard', 1, 75.00, CAST('2025-08-30 10:10:00' AS TIMESTAMP(3)), 'DELIVERED', '2025-08-30');
```

### Update and Delete Operations

```sql
-- Update using standard SQL syntax
UPDATE order_fact 
SET order_time = CAST('2025-08-31 09:20:13' AS TIMESTAMP(3)), 
    status = 'SHIPPED' 
WHERE order_id = 7;

-- Delete operation
DELETE FROM order_fact WHERE order_id = 6;

UPDATE order_fact
SET order_time = CAST('2025-08-31 10:00:00' AS TIMESTAMP(3)),
    status = 'SHIPPED'
WHERE order_id = 1;
```

### Audit Log Queries

```sql
-- View audit log for all changes
SELECT * FROM order_fact$audit_log;
```

## Time Travel Queries

### Snapshot-based Queries

#### Query Specific Snapshot

```sql
-- Query snapshot 1 (initial data load)
SELECT * FROM order_fact /*+ OPTIONS('scan.snapshot-id' = '1')*/;
```

**Expected Result:**
```
+----------+---------+---------------+----------+---------+-------------------------+---------+------------+
| order_id | user_id |  product_name | quantity |   price |              order_time |  status |         dt |
+----------+---------+---------------+----------+---------+-------------------------+---------+------------+
|        3 |    1001 |      Keyboard |        1 |   75.00 | 2025-08-30 10:10:00.000 | PENDING | 2025-08-30 |
|        7 |    1005 |        Webcam |        1 |  149.99 | 2025-08-30 11:30:00.000 | PENDING | 2025-08-30 |
|        1 |    1001 |        Laptop |        1 |  999.99 | 2025-08-30 10:00:00.000 | PENDING | 2025-08-30 |
|        2 |    1002 |         Mouse |        2 |   25.50 | 2025-08-30 10:05:00.000 | PENDING | 2025-08-30 |
|        4 |    1001 | Gaming Laptop |        1 | 1299.99 | 2025-08-30 10:15:00.000 | PENDING | 2025-08-30 |
|        5 |    1003 |       Monitor |        1 |  299.99 | 2025-08-30 11:00:00.000 | PENDING | 2025-08-30 |
|        6 |    1004 |    Headphones |        1 |   89.99 | 2025-08-30 11:15:00.000 | PENDING | 2025-08-30 |
|        8 |    1003 |      Speakers |        2 |   79.99 | 2025-08-30 12:00:00.000 | PENDING | 2025-08-30 |
+----------+---------+---------------+----------+---------+-------------------------+---------+------------+
```

> **Note:** Order ID 9 is missing from snapshot 1 as it was added in snapshot 2.

#### Incremental Query Between Snapshots

```sql
-- Query changes between snapshots 1 and 5
SELECT * FROM order_fact /*+ OPTIONS('incremental-between' = '1,5') */;
```

**Result:**
```
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
| order_id | user_id | product_name | quantity |  price |              order_time |    status |         dt |
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
|        1 |    1001 |       Laptop |        1 | 999.99 | 2025-08-30 10:00:00.000 | CONFIRMED | 2025-08-30 |
|        2 |    1002 |        Mouse |        2 |  25.50 | 2025-08-30 10:05:00.000 |   SHIPPED | 2025-08-30 |
|        3 |    1001 |     Keyboard |        1 |  75.00 | 2025-08-30 10:10:00.000 | DELIVERED | 2025-08-30 |
|        9 |    1006 |       Tablet |        1 | 399.99 | 2025-08-30 12:30:00.000 |   PENDING | 2025-08-30 |
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
```

### Timestamp-based Queries

```sql
-- Query data at specific timestamp
SELECT * FROM order_fact /*+ OPTIONS('scan.timestamp-millis' = '1756546237802') */;
```
**Result:**
```
+----------+---------+---------------+----------+---------+-------------------------+-----------+------------+
| order_id | user_id |  product_name | quantity |   price |              order_time |    status |         dt |
+----------+---------+---------------+----------+---------+-------------------------+-----------+------------+
|        3 |    1001 |      Keyboard |        1 |   75.00 | 2025-08-30 10:10:00.000 |   PENDING | 2025-08-30 |
|        7 |    1005 |        Webcam |        1 |  149.99 | 2025-08-30 11:30:00.000 |   PENDING | 2025-08-30 |
|        9 |    1006 |        Tablet |        1 |  399.99 | 2025-08-30 12:30:00.000 |   PENDING | 2025-08-30 |
|        1 |    1001 |        Laptop |        1 |  999.99 | 2025-08-30 10:00:00.000 | CONFIRMED | 2025-08-30 |
|        2 |    1002 |         Mouse |        2 |   25.50 | 2025-08-30 10:05:00.000 |   PENDING | 2025-08-30 |
|        4 |    1001 | Gaming Laptop |        1 | 1299.99 | 2025-08-30 10:15:00.000 |   PENDING | 2025-08-30 |
|        5 |    1003 |       Monitor |        1 |  299.99 | 2025-08-30 11:00:00.000 |   PENDING | 2025-08-30 |
|        6 |    1004 |    Headphones |        1 |   89.99 | 2025-08-30 11:15:00.000 |   PENDING | 2025-08-30 |
|        8 |    1003 |      Speakers |        2 |   79.99 | 2025-08-30 12:00:00.000 |   PENDING | 2025-08-30 |
+----------+---------+---------------+----------+---------+-------------------------+-----------+------------+
```
> **Note:** Each snapshot has a `timeMillis` field in metadata representing the commit time (System.currentTimeMillis() at commit).

## Tagging and Versioning

### Create Tags

#### Create Tag for Snapshot 2

```shell
bin/flink run --target kubernetes-session \
    -Dkubernetes.cluster-id=flink1 \
    -Dkubernetes.service-account=flink-service-account \
    lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    create_tag \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table order_fact \
    --tag_name all \
    --snapshot 2
```

#### Create Tag for Snapshot 5

```shell
bin/flink run --target kubernetes-session \
    -Dkubernetes.cluster-id=flink1 \
    -Dkubernetes.service-account=flink-service-account \
    lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    create_tag \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table order_fact \
    --tag_name delivered_3 \
    --snapshot 5
```

### Query Using Tags

```sql
-- Query specific tag
SELECT * FROM order_fact /*+ OPTIONS('scan.tag-name' = 'all') */;
SELECT * FROM order_fact /*+ OPTIONS('scan.tag-name' = 'delivered_3') */;

-- Incremental query between tags
SELECT * FROM order_fact /*+ OPTIONS('incremental-between' = 'all,delivered_3') */;
```

**Result:**
```
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
| order_id | user_id | product_name | quantity |  price |              order_time |    status |         dt |
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
|        3 |    1001 |     Keyboard |        1 |  75.00 | 2025-08-30 10:10:00.000 | DELIVERED | 2025-08-30 |
|        1 |    1001 |       Laptop |        1 | 999.99 | 2025-08-30 10:00:00.000 | CONFIRMED | 2025-08-30 |
|        2 |    1002 |        Mouse |        2 |  25.50 | 2025-08-30 10:05:00.000 |   SHIPPED | 2025-08-30 |
+----------+---------+--------------+----------+--------+-------------------------+-----------+------------+
```

### Audit Log with Tags

```sql
-- Query audit log between tags
SELECT * FROM order_fact$audit_log /*+ OPTIONS('incremental-between' = 'all,delivered_3') */;
```

## Performance Tuning (TODO)

### Parallel Reading

```sql
-- Set read parallelism 
SELECT * FROM order_fact /*+ OPTIONS('read.parallelism' = '3') */;
```

## Configuration

### Flink Configuration (flink-conf.yaml)

```yaml
# S3/MinIO Configuration
fs.s3a.endpoint: http://127.0.0.1:9000
fs.s3a.path.style.access: true
fs.s3a.connection.ssl.enabled: false
fs.s3a.access.key: minio
fs.s3a.secret.key: minio12345
fs.s3a.aws.credentials.provider: org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider
```

## Maintenance Operations

### Compaction 
```shell
bin/flink run \
    -Drest.address=flink1-rest \
    -Dkubernetes.service-account=flink-service-account \
    -Dexecution.runtime-mode=BATCH \
    lib/paimon-flink-action-1.3-20250828.003001-72.jar \
    compact \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table t \
    --compact_strategy full
```
获取manifest信息
```sql
select * from T$manifests;
```
### troubleshooting

~~什么时候会有index目录：~~
首先，只有在表配置了主键（PRIMARY KEY）
其次，Dynamic Bucket
学习compact，文件结构 https://paimon.apache.org/docs/master/learn-paimon/understand-files/
https://paimon.apache.org/docs/master/concepts/spec/tableindex/
https://paimon.apache.org/docs/master/primary-key-table/data-distribution/#dynamic-bucket


~~为什么compact job 是streaming？~~
需要特别配置`-Dexecution.runtime-mode=BATCH`



