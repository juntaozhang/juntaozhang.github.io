package com.example.iceberg;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

/**
 * Apache Iceberg Schema 演进示例
 *
 * 演示功能：
 * - 添加新列（无需重写数据）
 * - 修改表属性
 * - 分区演进
 */
public class SchemaEvolutionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Iceberg Schema 演进 ==========");

        // 初始化环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inBatchMode()
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

        // 1. 创建初始表
        System.out.println("\n1. 创建初始表...");
        tEnv.executeSql("""
                DROP TABLE IF EXISTS products
                """);

        tEnv.executeSql("""
                CREATE TABLE products (
                    id BIGINT,
                    name STRING,
                    price DOUBLE,
                    PRIMARY KEY (id) NOT ENFORCED
                )
                WITH (
                    'format-version' = '2',
                    'write.format.default' = 'parquet'
                )
                """);

        // 插入初始数据
        tEnv.executeSql("""
                INSERT INTO products VALUES
                    (1, 'Laptop', 1000.0),
                    (2, 'Mouse', 25.0),
                    (3, 'Keyboard', 75.0)
                """).await();

        System.out.println("初始表结构：");
        tEnv.executeSql("DESCRIBE products").print();

        System.out.println("初始数据：");
        tEnv.executeSql("SELECT * FROM products").print();

        // 2. 添加新列（无需重写数据！）
        System.out.println("\n2. 添加新列 category...");
        tEnv.executeSql("ALTER TABLE products ADD COLUMN category STRING");

        System.out.println("添加新列后的表结构：");
        tEnv.executeSql("DESCRIBE products").print();

        // 填充新列数据
        tEnv.executeSql("UPDATE products SET category = 'Electronics' WHERE id IN (1, 2, 3)").await();

        System.out.println("填充 category 后的数据：");
        tEnv.executeSql("SELECT * FROM products").print();

        // 3. 添加多个新列
        System.out.println("\n3. 添加多个新列...");
        tEnv.executeSql("ALTER TABLE products ADD COLUMN discount DOUBLE");
        tEnv.executeSql("ALTER TABLE products ADD COLUMN description STRING");

        System.out.println("添加多个列后的表结构：");
        tEnv.executeSql("DESCRIBE products").print();

        // 填充新列
        tEnv.executeSql("""
                UPDATE products
                SET discount = 0.1, description = 'High quality product'
                WHERE id = 1
                """).await();

        tEnv.executeSql("""
                UPDATE products
                SET discount = 0.05, description = 'Basic accessory'
                WHERE id IN (2, 3)
                """).await();

        System.out.println("填充所有列后的数据：");
        tEnv.executeSql("SELECT * FROM products").print();

        // 4. 添加分区字段（分区演进）
        System.out.println("\n4. 添加分区字段（分区演进）...");
        tEnv.executeSql("ALTER TABLE products ADD PARTITION FIELD bucket(4, id)");

        System.out.println("添加分区后的表结构：");
        tEnv.executeSql("DESCRIBE products").print();

        // 5. 修改表属性
        System.out.println("\n5. 修改表属性...");
        tEnv.executeSql("""
                ALTER TABLE products SET (
                    'write.target-file-size-bytes' = '536870912'
                )
                """);

        System.out.println("查看修改后的表属性：");
        tEnv.executeSql("SHOW CREATE TABLE products").print();

        // 6. 插入更多数据验证
        System.out.println("\n6. 插入更多数据验证 Schema 演进...");
        tEnv.executeSql("""
                INSERT INTO products VALUES
                    (4, 'Monitor', 300.0, 'Electronics', 0.15, '4K display'),
                    (5, 'Headphones', 150.0, 'Audio', 0.08, 'Noise cancelling')
                """).await();

        System.out.println("最终数据：");
        tEnv.executeSql("SELECT * FROM products ORDER BY id").print();

        System.out.println("\n========== Schema 演进完成 ==========");
        System.out.println("关键特性：所有这些操作都无需重写已有数据文件！");
    }
}
