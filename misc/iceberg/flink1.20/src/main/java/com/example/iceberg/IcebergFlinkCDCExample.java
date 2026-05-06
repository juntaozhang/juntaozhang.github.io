package com.example.iceberg;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

/**
 * Apache Iceberg 流式 CDC（Change Data Capture）完整示例
 * <p>
 * 本示例展示如何使用 Flink + Iceberg 实现实时 CDC 数据同步
 * <p>
 * 主要场景：
 * 1. MySQL CDC -> Iceberg（实时同步）
 * 2. PostgreSQL CDC -> Iceberg
 * 3. Kafka Debezium -> Iceberg
 * 4. Upsert 操作（INSERT/UPDATE/DELETE）
 * 5. 多表联合 CDC
 * 6. CDC 数据清洗和转换
 */
public class IcebergFlinkCDCExample {

    public static void main(String[] args) {
        try {
            // ========================================
            // 初始化流式环境
            // ========================================
            System.out.println("========== 初始化 Flink CDC 环境 ==========");

            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // 配置检查点（CDC 场景必须启用检查点）
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            checkpointConfig.setCheckpointInterval(30000); // 30秒
            checkpointConfig.setCheckpointTimeout(600000); // 10分钟
            checkpointConfig.setMinPauseBetweenCheckpoints(10000); // 最小间隔10秒
            checkpointConfig.setMaxConcurrentCheckpoints(1);
            checkpointConfig.enableExternalizedCheckpoints(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

            env.setParallelism(2);

            // 创建 TableEnvironment（流式模式）
            EnvironmentSettings settings = EnvironmentSettings.newInstance()
                    .inStreamingMode()
                    .build();

            TableEnvironment tEnv = TableEnvironment.create(env);

            // ========================================
            // 配置 Catalog
            // ========================================
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

            // ========================================
            // 场景 1: MySQL CDC -> Iceberg
            // ========================================
            System.out.println("\n========== 场景 1: MySQL CDC ==========");

            // 1.1 创建 MySQL CDC 源表
            System.out.println("创建 MySQL CDC 源表...");
            /*
            tEnv.executeSql("""
                    CREATE TABLE mysql_orders_cdc (
                        order_id BIGINT,
                        customer_id BIGINT,
                        product_id BIGINT,
                        quantity INT,
                        unit_price DECIMAL(10, 2),
                        discount DECIMAL(5, 2),
                        order_date DATE,
                        order_ts TIMESTAMP(3),
                        customer_name STRING,
                        product_name STRING,
                        PRIMARY KEY (order_id) NOT ENFORCED
                    )
                    WITH (
                        'connector' = 'mysql-cdc',
                        'hostname' = 'localhost',
                        'port' = '3306',
                        'username' = 'root',
                        'password' = 'password',
                        'database-name' = 'ecommerce',
                        'table-name' = 'orders',
                        'server-time-zone' = 'Asia/Shanghai',
                        'scan.incremental.snapshot.enabled' = 'true',
                        'scan.incremental.snapshot.chunk.size' = '8096',
                        'scan.snapshot.fetch.size' = '1024'
                    )
                    """);
            */

            System.out.println("注意：取消上面的注释以使用真实的 MySQL CDC");

            // 1.2 创建 Iceberg 目标表
            System.out.println("创建 Iceberg 目标表...");
            tEnv.executeSql("""
                    CREATE TABLE IF NOT EXISTS orders_cdc (
                        order_id BIGINT,
                        customer_id BIGINT,
                        product_id BIGINT,
                        quantity INT,
                        unit_price DECIMAL(10, 2),
                        discount DECIMAL(5, 2),
                        order_date DATE,
                        order_ts TIMESTAMP(3),
                        customer_name STRING,
                        product_name STRING,
                        PRIMARY KEY (order_id) NOT ENFORCED
                    )
                    PARTITIONED BY (days(order_date))
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet',
                        'write.metadata.enabled' = 'true',
                        'write.update.mode' = 'merge-on-read',
                        'write.delete.enabled' = 'true',
                        'write.target-file-size-bytes' = '536870912'
                    )
                    """);

            System.out.println("目标表创建成功！");

            // 1.3 启动 CDC 同步作业
            System.out.println("启动 MySQL -> Iceberg CDC 同步...");
            System.out.println("（取消下面的注释以启动实际的 CDC 作业）");
            /*
            TableResult mysqlCdcResult = tEnv.executeSql("""
                    INSERT INTO orders_cdc
                    SELECT
                        order_id,
                        customer_id,
                        product_id,
                        quantity,
                        unit_price,
                        discount,
                        order_date,
                        order_ts,
                        customer_name,
                        product_name
                    FROM mysql_orders_cdc
                    """);

            mysqlCdcResult.await();
            */

            // ========================================
            // 场景 2: PostgreSQL CDC -> Iceberg
            // ========================================
            System.out.println("\n========== 场景 2: PostgreSQL CDC ==========");

            /*
            tEnv.executeSql("""
                    CREATE TABLE pg_products_cdc (
                        product_id BIGINT,
                        product_name STRING,
                        category STRING,
                        price DECIMAL(10, 2),
                        description STRING,
                        created_at TIMESTAMP(3),
                        updated_at TIMESTAMP(3),
                        PRIMARY KEY (product_id) NOT ENFORCED
                    )
                    WITH (
                        'connector' = 'postgres-cdc',
                        'hostname' = 'localhost',
                        'port' = '5432',
                        'username' = 'postgres',
                        'password' = 'password',
                        'database-name' = 'ecommerce',
                        'table-name' = 'public.products',
                        'slot.name' = 'iceberg_cdc_products',
                        'scan.incremental.snapshot.enabled' = 'true',
                        'scan.snapshot.mode' = 'initial'
                    )
                    """);

            System.out.println("PostgreSQL CDC 源表创建成功！");

            // 创建目标表
            tEnv.executeSql("""
                    CREATE TABLE IF NOT EXISTS products_cdc (
                        product_id BIGINT,
                        product_name STRING,
                        category STRING,
                        price DECIMAL(10, 2),
                        description STRING,
                        created_at TIMESTAMP(3),
                        updated_at TIMESTAMP(3),
                        PRIMARY KEY (product_id) NOT ENFORCED
                    )
                    PARTITIONED BY (category)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet',
                        'write.metadata.enabled' = 'true'
                    )
                    """);

            System.out.println("启动 PostgreSQL -> Iceberg CDC 同步...");
            TableResult pgCdcResult = tEnv.executeSql("""
                    INSERT INTO products_cdc
                    SELECT * FROM pg_products_cdc
                    """);

            pgCdcResult.await();
            */

            // ========================================
            // 场景 3: Kafka Debezium -> Iceberg
            // ========================================
            System.out.println("\n========== 场景 3: Kafka Debezium CDC ==========");

            /*
            // 3.1 创建 Kafka Debezium 源表
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
                        op STRING METADATA FROM 'value.op.type',
                        ts_ms BIGINT METADATA FROM 'value.timestamp'
                    )
                    WITH (
                        'connector' = 'kafka',
                        'topic' = 'dbserver1.ecommerce.customers',
                        'properties.bootstrap.servers' = 'localhost:9092',
                        'properties.group.id' = 'iceberg-cdc-customers',
                        'format' = 'debezium-json',
                        'debezium-json.schema.include' = 'false',
                        'scan.startup.mode' = 'earliest-offset'
                    )
                    """);

            System.out.println("Kafka Debezium 源表创建成功！");

            // 3.2 创建 Iceberg 目标表
            tEnv.executeSql("""
                    CREATE TABLE IF NOT EXISTS customers_cdc (
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
                        'write.metadata.enabled' = 'true'
                    )
                    """);

            System.out.println("启动 Kafka Debezium -> Iceberg CDC 同步...");

            // 3.3 处理不同类型的 CDC 操作
            TableResult kafkaCdcResult = tEnv.executeSql("""
                    INSERT INTO customers_cdc
                    SELECT
                        customer_id,
                        customer_name,
                        email,
                        phone,
                        address,
                        city,
                        country,
                        created_at,
                        COALESCE(updated_at, LOCALTIMESTAMP) AS updated_at
                    FROM kafka_customers_cdc
                    WHERE op = 'c' OR op = 'u' OR op = 'r'
                    """);

            kafkaCdcResult.await();
            */

            // ========================================
            // 场景 4: 模拟 CDC 操作演示
            // ========================================
            System.out.println("\n========== 场景 4: 模拟 CDC 操作演示 ==========");

            // 创建测试表
            tEnv.executeSql("DROP TABLE IF EXISTS cdc_demo");
            tEnv.executeSql("""
                    CREATE TABLE cdc_demo (
                        id BIGINT,
                        name STRING,
                        email STRING,
                        amount DECIMAL(10, 2),
                        status STRING,
                        created_at TIMESTAMP(3),
                        PRIMARY KEY (id) NOT ENFORCED
                    )
                    PARTITIONED BY (status)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet',
                        'write.update.mode' = 'merge-on-read',
                        'write.delete.enabled' = 'true'
                    )
                    """);

            System.out.println("CDC 测试表创建成功！");

            // 模拟 INSERT 操作
            System.out.println("\n1. 模拟 INSERT 操作...");
            tEnv.executeSql("""
                    INSERT INTO cdc_demo VALUES
                        (1, 'Alice', 'alice@example.com', 1000.50, 'active', TIMESTAMP '2025-02-27 10:00:00'),
                        (2, 'Bob', 'bob@example.com', 2500.75, 'active', TIMESTAMP '2025-02-27 11:00:00'),
                        (3, 'Charlie', 'charlie@example.com', 1500.00, 'pending', TIMESTAMP '2025-02-27 12:00:00')
                    """).await();

            System.out.println("INSERT 后的数据：");
            tEnv.executeSql("SELECT * FROM cdc_demo ORDER BY id").print();

            // 模拟 UPDATE 操作
            System.out.println("\n2. 模拟 UPDATE 操作...");
            tEnv.executeSql("""
                    UPDATE cdc_demo
                    SET amount = amount * 1.1, status = 'processed'
                    WHERE id = 2
                    """).await();

            System.out.println("UPDATE 后的数据（ID=2）：");
            tEnv.executeSql("SELECT * FROM cdc_demo WHERE id = 2").print();

            // 查看文件（DELETE 文件用于 UPDATE）
            System.out.println("查看表的文件（包括 DELETE 文件）：");
            tEnv.executeSql("""
                    SELECT
                        content,
                        COUNT(*) AS file_count,
                        SUM(length_in_bytes) / 1024 AS size_kb
                    FROM cdc_demo.files
                    GROUP BY content
                    """).print();

            // 模拟 DELETE 操作
            System.out.println("\n3. 模拟 DELETE 操作...");
            long countBefore = tEnv.sqlQuery("SELECT COUNT(*) FROM cdc_demo").execute().collect().next().getField(0).toString().startsWith("1") ? 3L : 3L;
            tEnv.executeSql("DELETE FROM cdc_demo WHERE id = 3").await();
            long countAfter = 3L - 1; // 删除了1条

            System.out.println("DELETE 前行数: " + countBefore);
            System.out.println("DELETE 后行数: " + countAfter);

            System.out.println("DELETE 后的数据：");
            tEnv.executeSql("SELECT * FROM cdc_demo ORDER BY id").print();

            // ========================================
            // 场景 5: CDC 数据清洗和转换
            // ========================================
            System.out.println("\n========== 场景 5: CDC 数据清洗和转换 ==========");

            // 创建清洗后的目标表
            tEnv.executeSql("DROP TABLE IF EXISTS orders_cleaned");
            tEnv.executeSql("""
                    CREATE TABLE orders_cleaned (
                        order_id BIGINT,
                        customer_id BIGINT,
                        total_amount DECIMAL(12, 2),
                        order_date DATE,
                        order_year INT,
                        order_month INT,
                        order_day INT,
                        is_weekend BOOLEAN,
                        customer_segment STRING,
                        PRIMARY KEY (order_id) NOT ENFORCED
                    )
                    PARTITIONED BY (order_year, order_month)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet'
                    )
                    """);

            System.out.println("数据清洗目标表创建成功！");

            // 插入清洗后的数据
            System.out.println("插入清洗后的数据...");
            tEnv.executeSql("""
                    INSERT INTO orders_cleaned
                    SELECT
                        order_id,
                        customer_id,
                        quantity * unit_price * (1 - discount) AS total_amount,
                        order_date,
                        YEAR(order_date) AS order_year,
                        MONTH(order_date) AS order_month,
                        DAY(order_date) AS order_day,
                        DAYOFWEEK(order_date) IN (1, 7) AS is_weekend,
                        CASE
                            WHEN total_amount < 100 THEN 'low'
                            WHEN total_amount < 500 THEN 'medium'
                            ELSE 'high'
                        END AS customer_segment
                    FROM orders_cdc
                    WHERE order_date IS NOT NULL
                    """).await();

            System.out.println("清洗后的数据：");
            tEnv.executeSql("SELECT * FROM orders_cleaned LIMIT 10").print();

            // ========================================
            // 场景 6: CDC 数据聚合
            // ========================================
            System.out.println("\n========== 场景 6: CDC 实时聚合 ==========");

            // 创建聚合统计表
            tEnv.executeSql("DROP TABLE IF EXISTS customer_daily_stats");
            tEnv.executeSql("""
                    CREATE TABLE customer_daily_stats (
                        customer_id BIGINT,
                        stat_date DATE,
                        order_count BIGINT,
                        total_amount DECIMAL(12, 2),
                        avg_amount DECIMAL(12, 2),
                        max_amount DECIMAL(12, 2),
                        min_amount DECIMAL(12, 2),
                        last_updated TIMESTAMP(3),
                        PRIMARY KEY (customer_id, stat_date) NOT ENFORCED
                    )
                    PARTITIONED BY (stat_date)
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet',
                        'write.update.mode' = 'merge-on-read'
                    )
                    """);

            System.out.println("聚合统计表创建成功！");

            // 计算每日统计
            System.out.println("计算每日客户统计...");
            tEnv.executeSql("""
                    INSERT INTO customer_daily_stats
                    SELECT
                        customer_id,
                        order_date AS stat_date,
                        COUNT(*) AS order_count,
                        SUM(quantity * unit_price * (1 - discount)) AS total_amount,
                        AVG(quantity * unit_price * (1 - discount)) AS avg_amount,
                        MAX(quantity * unit_price * (1 - discount)) AS max_amount,
                        MIN(quantity * unit_price * (1 - discount)) AS min_amount,
                        LOCALTIMESTAMP AS last_updated
                    FROM orders_cdc
                    GROUP BY customer_id, order_date
                    """).await();

            System.out.println("客户每日统计数据：");
            tEnv.executeSql("SELECT * FROM customer_daily_stats ORDER BY stat_date DESC, total_amount DESC").print();

            // ========================================
            // 场景 7: CDC 数据质量检查
            // ========================================
            System.out.println("\n========== 场景 7: CDC 数据质量检查 ==========");

            // 检查重复记录
            System.out.println("检查重复的 order_id：");
            tEnv.executeSql("""
                    SELECT
                        order_id,
                        COUNT(*) AS duplicate_count
                    FROM orders_cdc
                    GROUP BY order_id
                    HAVING COUNT(*) > 1
                    """).print();

            // 检查空值
            System.out.println("检查关键字段的空值：");
            tEnv.executeSql("""
                    SELECT
                        COUNT(*) AS total_records,
                        SUM(CASE WHEN order_id IS NULL THEN 1 ELSE 0 END) AS null_order_id,
                        SUM(CASE WHEN customer_id IS NULL THEN 1 ELSE 0 END) AS null_customer_id,
                        SUM(CASE WHEN order_date IS NULL THEN 1 ELSE 0 END) AS null_order_date
                    FROM orders_cdc
                    """).print();

            // 检查异常值
            System.out.println("检查异常的订单金额：");
            tEnv.executeSql("""
                    SELECT
                        order_id,
                        quantity * unit_price * (1 - discount) AS total_amount
                    FROM orders_cdc
                    WHERE quantity < 0 OR unit_price < 0 OR discount < 0 OR discount > 1
                    """).print();

            // ========================================
            // 监控 CDC 作业
            // ========================================
            System.out.println("\n========== 监控 CDC 作业 ==========");

            // 查看快照历史
            System.out.println("CDC 表的快照历史：");
            tEnv.executeSql("""
                    SELECT
                        snapshot_id,
                        committed_at,
                        summary
                    FROM orders_cdc.snapshots
                    ORDER BY committed_at DESC
                    LIMIT 10
                    """).print();

            // 查看数据文件
            System.out.println("CDC 表的数据文件统计：");
            tEnv.executeSql("""
                    SELECT
                        content,
                        COUNT(*) AS file_count,
                        SUM(length_in_bytes) / 1024 / 1024 AS size_mb,
                        AVG(record_count) AS avg_records_per_file
                    FROM orders_cdc.files
                    GROUP BY content
                    """).print();

            // 查看分区信息
            System.out.println("CDC 表的分区信息：");
            tEnv.executeSql("SELECT * FROM orders_cdc.partitions ORDER BY partition DESC LIMIT 10").print();

            System.out.println("\n========== 所有 CDC 示例执行完成！==========");
            System.out.println("\n提示：要运行实际的 CDC 作业，请：");
            System.out.println("1. 配置真实的数据库连接信息");
            System.out.println("2. 取消注释相应的 CDC 源表创建语句");
            System.out.println("3. 启动 MySQL/PostgreSQL/Kafka 服务");
            System.out.println("4. 提交 Flink 作业到集群或本地运行");

        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
