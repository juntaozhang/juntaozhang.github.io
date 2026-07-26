package com.example.iceberg;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

/**
 * MySQL CDC -> Iceberg 流式同步示例
 *
 * 演示功能：
 * - 从 MySQL 读取 CDC 数据
 * - 实时同步到 Iceberg
 * - 处理 INSERT/UPDATE/DELETE 操作
 * - Schema 映射
 */
public class MySQLCDCExample {

    public static void main(String[] args) {
        System.out.println("========== MySQL CDC -> Iceberg ==========");

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

            // 1. 创建 MySQL CDC 源表
            System.out.println("\n1. 配置 MySQL CDC 源表...");
            System.out.println("注意：取消下面的注释以使用真实的 MySQL");

            /*
            tEnv.executeSql("""
                    CREATE TABLE mysql_orders (
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

            System.out.println("MySQL CDC 源表创建成功！");
            */

            // 2. 创建 Iceberg 目标表
            System.out.println("\n2. 创建 Iceberg 目标表...");
            tEnv.executeSql("""
                    DROP TABLE IF EXISTS orders_from_mysql
                    """);

            tEnv.executeSql("""
                    CREATE TABLE orders_from_mysql (
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
                        'write.delete.enabled' = 'true'
                    )
                    """);
            System.out.println("Iceberg 目标表创建成功！");

            // 3. 模拟数据插入（替代真实的 MySQL CDC）
            System.out.println("\n3. 模拟 MySQL CDC 数据...");
            tEnv.executeSql("""
                    INSERT INTO orders_from_mysql VALUES
                        (1001, 10, 5, 2, 50.00, 0.10, DATE '2025-02-27', TIMESTAMP '2025-02-27 10:00:00', 'Alice', 'Laptop'),
                        (1002, 20, 10, 1, 100.00, 0.05, DATE '2025-02-27', TIMESTAMP '2025-02-27 11:00:00', 'Bob', 'Mouse'),
                        (1003, 30, 15, 3, 75.00, 0.15, DATE '2025-02-27', TIMESTAMP '2025-02-27 12:00:00', 'Charlie', 'Keyboard')
                    """).await();

            System.out.println("初始数据：");
            tEnv.executeSql("SELECT * FROM orders_from_mysql ORDER BY order_id").print();

            // 4. 模拟 CDC 操作
            System.out.println("\n4. 模拟 CDC 操作...");

            // INSERT - 新增订单
            System.out.println("a) INSERT 新订单...");
            tEnv.executeSql("""
                    INSERT INTO orders_from_mysql VALUES
                        (1004, 40, 20, 1, 200.00, 0.08, DATE '2025-02-27', TIMESTAMP '2025-02-27 13:00:00', 'David', 'Monitor')
                    """).await();

            // UPDATE - 更新订单
            System.out.println("b) UPDATE 订单 1002...");
            tEnv.executeSql("""
                    UPDATE orders_from_mysql
                    SET quantity = 2, discount = 0.10
                    WHERE order_id = 1002
                    """).await();

            // DELETE - 删除订单
            System.out.println("c) DELETE 订单 1001...");
            tEnv.executeSql("""
                    DELETE FROM orders_from_mysql WHERE order_id = 1001
                    """).await();

            System.out.println("\nCDC 操作后的数据：");
            tEnv.executeSql("SELECT * FROM orders_from_mysql ORDER BY order_id").print();

            // 5. 启动真实的 CDC 同步（取消注释以运行）
            /*
            System.out.println("\n5. 启动 MySQL -> Iceberg CDC 同步...");
            System.out.println("这将是一个持续运行的流任务...\n");

            TableResult cdcResult = tEnv.executeSql("""
                    INSERT INTO orders_from_mysql
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
                    FROM mysql_orders
                    """);

            cdcResult.await();
            */

            // 6. 查看表文件（验证 CDC 操作）
            System.out.println("\n6. 查看表文件（验证 CDC 操作）：");
            tEnv.executeSql("""
                    SELECT
                        content,
                        CASE
                            WHEN content = 0 THEN 'Data File'
                            WHEN content = 1 THEN 'Position Delete File'
                            WHEN content = 2 THEN 'Equality Delete File'
                            ELSE 'Unknown'
                        END AS file_type,
                        COUNT(*) AS file_count,
                        SUM(length_in_bytes) / 1024 AS size_kb
                    FROM orders_from_mysql.files
                    GROUP BY content
                    ORDER BY content
                    """).print();

            // 7. 查看快照历史
            System.out.println("\n7. 快照历史：");
            tEnv.executeSql("""
                    SELECT
                        snapshot_id,
                        committed_at,
                        summary
                    FROM orders_from_mysql.snapshots
                    ORDER BY committed_at DESC
                    LIMIT 10
                    """).print();

            System.out.println("\n========== MySQL CDC 示例完成 ==========");
            System.out.println("\n使用说明：");
            System.out.println("1. 确保 MySQL 已安装并运行");
            System.out.println("2. 在 MySQL 中创建 ecommerce.orders 表");
            System.out.println("3. 配置 MySQL 连接信息");
            System.out.println("4. 取消注释 MySQL CDC 源表创建语句");
            System.out.println("5. 取消注释 CDC 同步作业启动代码");
            System.out.println("6. 运行此程序");

        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
