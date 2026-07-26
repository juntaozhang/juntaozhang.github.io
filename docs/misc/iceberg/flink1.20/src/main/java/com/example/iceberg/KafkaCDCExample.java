package com.example.iceberg;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

/**
 * Kafka Debezium CDC -> Iceberg 流式同步示例
 *
 * 演示功能：
 * - 从 Kafka 读取 Debezium JSON 格式的 CDC 数据
 * - 解析和处理 CDC 操作类型（c=create, u=update, d=delete）
 * - 实时同步到 Iceberg
 * - 处理 JSON 格式
 */
public class KafkaCDCExample {

    public static void main(String[] args) {
        System.out.println("========== Kafka Debezium CDC -> Iceberg ==========");

        try {
            // 初始化流式环境
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // 配置检查点
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            checkpointConfig.setCheckpointInterval(30000);
            checkpointConfig.setCheckpointTimeout(600000);
            checkpointConfig.enableExternalizedCheckpoints(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

            env.setParallelism(2);

            EnvironmentSettings settings = EnvironmentSettings.newInstance()
                    .inStreamingMode()
                    .build();

            TableEnvironment tEnv = TableEnvironment.create(env);

            // 配置 Catalog
            tEnv.executeSql("""
                    CREATE CATALOG my_catalog WITH (
                        'type' = 'iceberg',
                        'catalog-type' = 'hadoop',
                        'warehouse' = 's3a://warehouse/iceberg'
                    )
                    """);

            tEnv.executeSql("CREATE DATABASE IF NOT EXISTS my_catalog.db");
            tEnv.executeSql("USE CATALOG my_catalog");
            tEnv.executeSql("USE db");

            // 1. 创建 Kafka CDC 源表
            System.out.println("\n1. 配置 Kafka Debezium 源表...");
            System.out.println("注意：取消下面的注释以使用真实的 Kafka");

            /*
            tEnv.executeSql("""
                    CREATE TABLE kafka_customers_cdc (
                        customer_id BIGINT,
                        customer_name STRING,
                        email STRING,
                        phone STRING,
                        address STRING,
                        city STRING,
                        country STRING,
                        created_at TIMESTAMP(3),
                        updated_at TIMESTAMP(3),
                        -- Debezium 元数据
                        op STRING METADATA FROM 'value.op.type',  -- 操作类型：c=create, u=update, d=delete, r=read
                        ts_ms BIGINT METADATA FROM 'value.timestamp',  -- 事件时间戳
                        source_ts TIMESTAMP(3) METADATA FROM 'value.source.timestamp'  -- 源数据库时间戳
                    )
                    WITH (
                        'connector' = 'kafka',
                        'topic' = 'dbserver1.ecommerce.customers',
                        'properties.bootstrap.servers' = 'localhost:9092',
                        'properties.group.id' = 'iceberg-cdc-customers',
                        'format' = 'debezium-json',
                        'debezium-json.schema.include' = 'false',
                        'scan.startup.mode' = 'earliest-offset',  -- 或 'latest-offset'
                        'properties.auto.offset.reset' = 'earliest'
                    )
                    """);

            System.out.println("Kafka Debezium 源表创建成功！");
            */

            // 2. 创建 Iceberg 目标表
            System.out.println("\n2. 创建 Iceberg 目标表...");
            tEnv.executeSql("""
                    DROP TABLE IF EXISTS customers_from_kafka
                    """);

            tEnv.executeSql("""
                    CREATE TABLE customers_from_kafka (
                        customer_id BIGINT,
                        customer_name STRING,
                        email STRING,
                        phone STRING,
                        address STRING,
                        city STRING,
                        country STRING,
                        created_at TIMESTAMP(3),
                        updated_at TIMESTAMP(3),
                        PRIMARY KEY (customer_id) NOT ENFORCED
                    )
                    PARTITIONED BY (country)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet',
                        'write.metadata.enabled' = 'true',
                        'write.update.mode' = 'merge-on-read',
                        'write.delete.enabled' = 'true'
                    )
                    """);
            System.out.println("Iceberg 目标表创建成功！");

            // 3. 模拟 Kafka CDC 数据
            System.out.println("\n3. 模拟 Kafka CDC 数据...");
            tEnv.executeSql("""
                    INSERT INTO customers_from_kafka VALUES
                        (1, 'Alice Smith', 'alice@example.com', '555-0101', '123 Main St', 'New York', 'USA', TIMESTAMP '2025-02-27 10:00:00', TIMESTAMP '2025-02-27 10:00:00'),
                        (2, 'Bob Johnson', 'bob@example.com', '555-0102', '456 Oak Ave', 'Los Angeles', 'USA', TIMESTAMP '2025-02-27 11:00:00', TIMESTAMP '2025-02-27 11:00:00'),
                        (3, 'Charlie Brown', 'charlie@example.com', '555-0103', '789 Pine Rd', 'Chicago', 'USA', TIMESTAMP '2025-02-27 12:00:00', TIMESTAMP '2025-02-27 12:00:00')
                    """).await();

            System.out.println("初始数据：");
            tEnv.executeSql("SELECT * FROM customers_from_kafka ORDER BY customer_id").print();

            // 4. 模拟不同的 CDC 操作
            System.out.println("\n4. 模拟 CDC 操作...");

            // c = create (INSERT)
            System.out.println("a) CREATE (op='c') - 新客户...");
            tEnv.executeSql("""
                    INSERT INTO customers_from_kafka VALUES
                        (4, 'David Lee', 'david@example.com', '555-0104', '321 Elm St', 'Houston', 'USA', TIMESTAMP '2025-02-27 13:00:00', TIMESTAMP '2025-02-27 13:00:00')
                    """).await();

            // u = update (UPDATE)
            System.out.println("b) UPDATE (op='u') - 更新客户信息...");
            tEnv.executeSql("""
                    UPDATE customers_from_kafka
                    SET email = 'alice.smith@example.com', phone = '555-0199'
                    WHERE customer_id = 1
                    """).await();

            // d = delete (DELETE)
            System.out.println("c) DELETE (op='d') - 删除客户...");
            tEnv.executeSql("""
                    DELETE FROM customers_from_kafka WHERE customer_id = 3
                    """).await();

            System.out.println("\nCDC 操作后的数据：");
            tEnv.executeSql("SELECT * FROM customers_from_kafka ORDER BY customer_id").print();

            // 5. 启动真实的 CDC 同步（取消注释以运行）
            /*
            System.out.println("\n5. 启动 Kafka Debezium -> Iceberg CDC 同步...");
            System.out.println("这将是一个持续运行的流任务...\n");

            // 只处理 create, update, read 操作，忽略 delete（可根据需求调整）
            TableResult cdcResult = tEnv.executeSql("""
                    INSERT INTO customers_from_kafka
                    SELECT
                        customer_id,
                        customer_name,
                        email,
                        phone,
                        address,
                        city,
                        country,
                        created_at,
                        COALESCE(updated_at, created_at) AS updated_at
                    FROM kafka_customers_cdc
                    WHERE op = 'c' OR op = 'u' OR op = 'r'
                    """);

            cdcResult.await();
            */

            // 6. 数据清洗和转换示例
            System.out.println("\n6. 数据清洗和转换示例...");

            tEnv.executeSql("DROP TABLE IF EXISTS customers_cleaned");
            tEnv.executeSql("""
                    CREATE TABLE customers_cleaned (
                        customer_id BIGINT,
                        customer_name STRING,
                        email STRING,
                        phone STRING,
                        full_address STRING,
                        city STRING,
                        country STRING,
                        region STRING,
                        created_at TIMESTAMP(3),
                        updated_at TIMESTAMP(3),
                        PRIMARY KEY (customer_id) NOT ENFORCED
                    )
                    PARTITIONED BY (region)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet'
                    )
                    """);

            // 数据清洗：合并地址字段，添加地区信息
            tEnv.executeSql("""
                    INSERT INTO customers_cleaned
                    SELECT
                        customer_id,
                        UPPER(customer_name) AS customer_name,
                        LOWER(email) AS email,
                        phone,
                        CONCAT(address, ', ', city, ', ', country) AS full_address,
                        city,
                        country,
                        CASE
                            WHEN country IN ('USA', 'Canada') THEN 'North America'
                            WHEN country IN ('UK', 'Germany', 'France') THEN 'Europe'
                            WHEN country IN ('China', 'Japan', 'India') THEN 'Asia'
                            ELSE 'Other'
                        END AS region,
                        created_at,
                        updated_at
                    FROM customers_from_kafka
                    """).await();

            System.out.println("清洗后的数据：");
            tEnv.executeSql("SELECT customer_id, customer_name, email, region FROM customers_cleaned ORDER BY customer_id").print();

            // 7. 统计分析
            System.out.println("\n7. 按地区统计客户数量：");
            tEnv.executeSql("""
                    SELECT
                        region,
                        COUNT(*) AS customer_count
                    FROM customers_cleaned
                    GROUP BY region
                    ORDER BY customer_count DESC
                    """).print();

            System.out.println("\n========== Kafka Debezium CDC 示例完成 ==========");
            System.out.println("\n使用说明：");
            System.out.println("1. 确保 Kafka 已安装并运行");
            System.out.println("2. 配置 Debezium 连接器从 MySQL/PostgreSQL 捕获变更");
            System.out.println("3. 确保 Debezium 将 CDC 数据发送到 Kafka 主题");
            System.out.println("4. 配置 Kafka 连接信息");
            System.out.println("5. 取消注释 Kafka CDC 源表创建语句");
            System.out.println("6. 取消注释 CDC 同步作业启动代码");
            System.out.println("7. 运行此程序");

            System.out.println("\nDebezium 操作类型说明：");
            System.out.println("- c = create (插入)");
            System.out.println("- u = update (更新)");
            System.out.println("- d = delete (删除)");
            System.out.println("- r = read (初始快照读取)");

        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
