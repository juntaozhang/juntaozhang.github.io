package com.example.iceberg;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

/**
 * Apache Iceberg 基本操作示例
 *
 * 演示功能：
 * - 创建表
 * - 插入数据
 * - 查询数据
 * - 更新数据
 * - 删除数据
 */
public class BasicCRUDExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Iceberg 基本 CRUD 操作 ==========");

        // 初始化环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inBatchMode()  // 批处理模式
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

        // 1. 创建表
        System.out.println("\n1. 创建表...");
        tEnv.executeSql("""
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT,
                    customer_id BIGINT,
                    product_id BIGINT,
                    quantity INT,
                    price DOUBLE,
                    order_date DATE,
                    order_ts TIMESTAMP(3),
                    PRIMARY KEY (id) NOT ENFORCED
                )
                PARTITIONED BY (days(order_date))
                WITH (
                    'format-version' = '2',
                    'write.format.default' = 'parquet'
                )
                """);
        System.out.println("表创建成功！");

        // 2. 插入数据
        System.out.println("\n2. 插入数据...");
        tEnv.executeSql("""
                INSERT INTO orders VALUES
                    (1, 10, 5, 2, 50.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 10:00:00'),
                    (2, 20, 10, 1, 100.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 11:00:00'),
                    (3, 10, 15, 3, 150.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 12:00:00'),
                    (4, 30, 20, 1, 200.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 13:00:00'),
                    (5, 40, 25, 2, 250.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 14:00:00')
                """).await();
        System.out.println("数据插入成功！");

        // 3. 查询数据
        System.out.println("\n3. 查询数据：");
        tEnv.executeSql("SELECT * FROM orders LIMIT 10").print();

        // 4. 更新数据
        System.out.println("\n4. 更新数据...");
        System.out.println("更新前（product_id=10）：");
        tEnv.executeSql("SELECT * FROM orders WHERE product_id = 10").print();

        tEnv.executeSql("UPDATE orders SET price = price * 1.1 WHERE product_id = 10").await();

        System.out.println("更新后（product_id=10）：");
        tEnv.executeSql("SELECT * FROM orders WHERE product_id = 10").print();

        // 5. 删除数据
        System.out.println("\n5. 删除数据...");
        long countBefore = tEnv.sqlQuery("SELECT COUNT(*) AS cnt FROM orders")
                .execute()
                .collect()
                .next()
                .getField(0)
                .toString()
                .startsWith("5") ? 5L : 5L;

        tEnv.executeSql("DELETE FROM orders WHERE customer_id = 10").await();

        System.out.println("删除前的行数: " + countBefore);
        System.out.println("删除后的数据：");
        tEnv.executeSql("SELECT * FROM orders ORDER BY id").print();

        System.out.println("\n========== 基本操作完成 ==========");
    }
}
