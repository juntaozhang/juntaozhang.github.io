package com.example.iceberg;

import org.apache.spark.sql.SparkSession;

/**
 * jvm args:
 * --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED
 * <p>
 * Apache Iceberg 核心特性示例 - Spark + S3（纯 SQL 版本）
 * <p>
 * 主要特性：
 * 1. 基本 CRUD 操作
 * 2. Schema 演进（Schema Evolution）
 * 3. 分区演进（Partition Evolution）
 * 4. 时间旅行（Time Travel）
 * 5. 删除文件（Delete Files - Position/Equality Deletes）
 * 6. 增量查询（Incremental Query）
 * 7. 快照管理（Snapshot Management）
 */
public class IcebergS3Example {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Iceberg S3 Example")
                .master("local[*]")
                .config("spark.sql.shuffle.partitions", "2")
                .config("spark.hadoop.fs.s3a.access.key", "minio")
                .config("spark.hadoop.fs.s3a.secret.key", "minio12345")
                .config("spark.hadoop.fs.s3a.endpoint", "http://localhost:9000")
                .config("spark.hadoop.fs.s3a.path.style.access", "true")
                .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
                .config("spark.sql.catalog.my_catalog", "org.apache.iceberg.spark.SparkCatalog")
                .config("spark.sql.catalog.my_catalog.type", "hadoop")
                .config("spark.sql.catalog.my_catalog.warehouse", "s3a://warehouse/iceberg")
//                .config("spark.sql.catalog.my_catalog.type", "rest")
//                .config("spark.sql.catalog.my_catalog.uri", "http://localhost:8181")
//                .config("spark.sql.catalog.my_catalog.warehouse", "s3a://warehouse/iceberg")
                .getOrCreate();

        // ========================================
        // 特性 1: 基本 CRUD 操作
        // ========================================
        System.out.println("========== 1. 基本 CRUD 操作 ==========");

        spark.sql("CREATE DATABASE IF NOT EXISTS my_catalog.db");
        spark.sql("USE my_catalog.db");

        spark.sql("""
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT,
                    customer_id BIGINT,
                    product_id BIGINT,
                    quantity INT,
                    price DOUBLE,
                    order_date DATE,
                    order_ts TIMESTAMP
                )
                USING iceberg
                PARTITIONED BY (days(order_date))
                TBLPROPERTIES (
                    'format-version' = '2'
                )
                """);

        // 使用 INSERT INTO .. VALUES 语句插入数据
        spark.sql("""
                INSERT INTO orders
                VALUES
                    (1, 10, 5, 2, 50.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 10:00:00'),
                    (2, 20, 10, 1, 100.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 11:00:00'),
                    (3, 10, 15, 3, 150.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 12:00:00'),
                    (4, 30, 20, 1, 200.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 13:00:00'),
                    (5, 40, 25, 2, 250.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 14:00:00')
                """);

        // 批量插入更多数据
        for (int i = 6; i <= 10; i++) {
            spark.sql(String.format("""
                            INSERT INTO orders
                            VALUES (%d, %d, %d, %d, %.1f, DATE '2025-02-27', TIMESTAMP '2025-02-27 10:00:00')
                            """,
                    i, (i % 50) + 1, (i % 30) + 1, 1, (i % 30) * 10.0));
        }

        System.out.println("查询数据（前 10 条）：");
        spark.sql("SELECT * FROM orders LIMIT 10").show();

        // 更新数据
        spark.sql("SELECT * FROM orders WHERE product_id = 10").show();
        spark.sql("UPDATE orders SET price = price * 1.1 WHERE product_id = 10");
        System.out.println("更新后的数据（product_id = 10）：");
        spark.sql("SELECT * FROM orders WHERE product_id = 10").show();

        // ========================================
        // 特性 2: Schema 演进（Schema Evolution）
        // ========================================
        System.out.println("\n========== 2. Schema 演进 ==========");

        // 添加新列（无需重写数据！）
        spark.sql("ALTER TABLE my_catalog.db.orders ADD COLUMN discount DOUBLE");
        spark.sql("ALTER TABLE my_catalog.db.orders ADD COLUMN comment STRING");

        // 填充新列数据
        spark.sql("UPDATE my_catalog.db.orders SET discount = 0.1 WHERE product_id > 15");
        spark.sql("UPDATE my_catalog.db.orders SET comment = 'VIP customer' WHERE customer_id < 10");

        System.out.println("Schema 演进后的数据：");
        spark.sql("SELECT * FROM my_catalog.db.orders LIMIT 10").show();

        // 查看表结构
        System.out.println("当前表结构：");
        spark.sql("DESCRIBE orders").show();

        // ========================================
        // 特性 3: 分区演进（Partition Evolution）
        // ========================================
        System.out.println("\n========== 3. 分区演进 ==========");

        // 添加新的分区字段（旧数据不变，新数据按新分区存储）
        spark.sql("ALTER TABLE orders ADD PARTITION FIELD bucket(4,customer_id)");

        System.out.println("添加分区后的表结构：");
        spark.sql("DESCRIBE orders").show();

        // ========================================
        // 特性 4: 时间旅行（Time Travel）
        // ========================================
        System.out.println("\n========== 4. 时间旅行 ==========");

        // 查看所有快照
        System.out.println("所有快照：");
        spark.sql("SELECT snapshot_id, committed_at, summary FROM my_catalog.db.orders.snapshots").show(false);

        // 插入更多数据以创建新快照
        spark.sql("""
                INSERT INTO orders
                VALUES (101, 5, 35, 1, 350.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 15:00:00', 0.2, 'test')
                """);

        spark.sql("""
                INSERT INTO orders
                VALUES (102, 5, 40, 2, 400.0, DATE '2025-02-27', TIMESTAMP '2025-02-27 16:00:00', 0.2, 'test')
                """);

        System.out.println("查询当前版本的数据：");
        spark.sql("SELECT * FROM my_catalog.db.orders LIMIT 5").show();

        // 使用 snapshot_id 进行时间旅行
        String snapshotIdQuery = "SELECT snapshot_id FROM my_catalog.db.orders.snapshots ORDER BY committed_at ASC LIMIT 1";
        long firstSnapshotId = spark.sql(snapshotIdQuery).first().getLong(0);

        System.out.println("使用 snapshot_id:" + firstSnapshotId + " 查询第一个快照：");
        spark.sql("SELECT * FROM my_catalog.db.orders VERSION AS OF " + firstSnapshotId + " LIMIT 5").show();

        // ========================================
        // 特性 5: 删除文件（Delete Files）
        // ========================================
        System.out.println("\n========== 5. 删除文件 ==========");

        // 删除特定行（生成 Delete Files）
        spark.sql("DELETE FROM my_catalog.db.orders WHERE customer_id = 10");
        System.out.println("删除 customer_id = 10 后的数据：");
        spark.sql("SELECT * FROM my_catalog.db.orders WHERE customer_id = 10").show();

        // 查看表中的数据文件（包括 Delete Files）
        System.out.println("表中的文件：");
        spark.sql("SELECT * FROM my_catalog.db.orders.files").show(false);

        // ========================================
        // 特性 6: 增量查询（Incremental Query）
        // ========================================
        System.out.println("\n========== 6. 增量查询 ==========");

        // 插入新数据
        spark.sql("""
                INSERT INTO orders
                VALUES (103, 25, 45, 1, 450.0, DATE '2025-02-28', TIMESTAMP '2025-02-28 10:00:00', 0.4, 'test2')
                """);

        spark.sql("""
                INSERT INTO orders
                VALUES (104, 25, 50, 2, 500.0, DATE '2025-02-28', TIMESTAMP '2025-02-28 11:00:00', 0.4, 'test2')
                """);

        // 增量查询：只读取 append 操作的新数据
        System.out.println("增量查询（读取变更数据）：");
        spark.sql("""
                  CALL system.create_changelog_view(
                    table => 'my_catalog.db.orders',
                    options => map(
                      'start-snapshot-id', '%s'
                    ),
                    changelog_view => 'orders_changelog_view'
                  )
                """.formatted(firstSnapshotId));
        spark.sql("SELECT * FROM orders_changelog_view").show();

        // ========================================
        // 特性 7: 快照管理（Snapshot Management）
        // ========================================
        System.out.println("\n========== 7. 快照管理 ==========");

        // 查看表历史
        System.out.println("表历史：");
        spark.sql("SELECT * FROM my_catalog.db.orders.history").show();

        // 回滚到之前的快照
        System.out.println("回滚到第一个快照...");
        spark.sql("CALL my_catalog.system.rollback_to_snapshot('db.orders', " + firstSnapshotId + ")");

        System.out.println("回滚后的数据行数：");
        spark.sql("SELECT count(*) AS row_count FROM my_catalog.db.orders").show();

        // 恢复到最新快照
        System.out.println("恢复到最新快照...");
        String latestSnapshotQuery = "SELECT snapshot_id FROM my_catalog.db.orders.snapshots ORDER BY committed_at DESC LIMIT 1";
        long latestSnapshotId = spark.sql(latestSnapshotQuery).first().getLong(0);
        spark.sql("CALL my_catalog.system.set_current_snapshot('db.orders', " + latestSnapshotId + ")");

        System.out.println("恢复后的数据行数：");
        spark.sql("SELECT count(*) AS row_count FROM my_catalog.db.orders").show();

        // ========================================
        // 特性 8: 分区裁剪（Partition Pruning）
        // ========================================
        System.out.println("\n========== 8. 分区裁剪 ==========");

        // Iceberg 自动进行分区裁剪（即使使用隐藏分区）
        System.out.println("查询特定日期的数据（自动分区裁剪）：");
        spark.sql("SELECT * FROM my_catalog.db.orders WHERE order_date = DATE '2025-02-27' LIMIT 10").show();

        // 查看查询计划（可以看到分区裁剪）
        System.out.println("查询计划（查看分区裁剪）：");
        spark.sql("EXPLAIN SELECT * FROM my_catalog.db.orders WHERE order_date = DATE '2025-02-27'").show(false);

        // ========================================
        // 特性 9: 统计信息与查询优化
        // ========================================
        System.out.println("\n========== 9. 统计信息 ==========");

        // 收集统计信息
        spark.sql("CALL my_catalog.system.compute_table_stats('db.orders')");

        // 查看统计信息
        System.out.println("Manifests 信息：");
        spark.sql("SELECT * FROM my_catalog.db.orders.manifests").show();

        System.out.println("分区信息：");
        spark.sql("SELECT * FROM my_catalog.db.orders.partitions").show();

        // ========================================
        // 特性 10: Merge-On-Read（通过 DELETE 文件实现）
        // ========================================
        System.out.println("\n========== 10. Merge-On-Read 示例 ==========");

        long countBefore = spark.sql("SELECT count(*) FROM my_catalog.db.orders").first().getLong(0);
        spark.sql("DELETE FROM my_catalog.db.orders WHERE customer_id = 20");
        long countAfter = spark.sql("SELECT count(*) FROM my_catalog.db.orders").first().getLong(0);

        System.out.println("删除前行数: " + countBefore);
        System.out.println("删除后行数: " + countAfter);
        System.out.println("删除的行数: " + (countBefore - countAfter));

        // 查看生成的 Delete Files
        System.out.println("Delete Files（content=2 表示 Delete Files）：");
        spark.sql("SELECT * FROM my_catalog.db.orders.files WHERE content != 0").show(false);

        spark.stop();
    }
}
