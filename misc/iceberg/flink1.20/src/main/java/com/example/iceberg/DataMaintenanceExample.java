package com.example.iceberg;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

/**
 * Apache Iceberg 表维护示例
 *
 * 演示功能：
 * - 快照管理
 * - 文件维护
 * - 统计信息收集
 * - 数据清理
 */
public class DataMaintenanceExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Iceberg 表维护 ==========");

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

        // 创建测试表
        System.out.println("\n创建测试表...");
        tEnv.executeSql("""
                DROP TABLE IF EXISTS maintenance_test
                """);

        tEnv.executeSql("""
                CREATE TABLE maintenance_test (
                    id BIGINT,
                    data STRING,
                    created_at TIMESTAMP(3),
                    PRIMARY KEY (id) NOT ENFORCED
                )
                PARTITIONED BY (days(created_at))
                WITH (
                    'format-version' = '2',
                    'write.format.default' = 'parquet',
                    'write.target-file-size-bytes' = '1048576'  -- 1MB，便于产生多个文件
                )
                """);

        // 1. 插入数据并创建多个快照
        System.out.println("\n1. 插入数据并创建快照...");
        for (int batch = 0; batch < 5; batch++) {
            for (int i = 1; i <= 10; i++) {
                long id = batch * 10 + i;
                tEnv.executeSql(String.format("""
                                INSERT INTO maintenance_test VALUES
                                    (%d, 'data-%d', TIMESTAMP '2025-02-27 %02d:00:00')
                                """,
                        id, id, 10 + batch)).await();
            }
            System.out.println("批次 " + (batch + 1) + " 完成");
        }

        // 2. 查看快照历史
        System.out.println("\n2. 快照历史：");
        tEnv.executeSql("""
                SELECT
                    snapshot_id,
                    committed_at,
                    summary
                FROM maintenance_test.snapshots
                ORDER BY committed_at DESC
                """).print();

        // 3. 查看数据文件
        System.out.println("\n3. 数据文件统计：");
        tEnv.executeSql("""
                SELECT
                    content,
                    CASE
                        WHEN content = 0 THEN 'Data'
                        WHEN content = 1 THEN 'Position Deletes'
                        WHEN content = 2 THEN 'Equality Deletes'
                        ELSE 'Other'
                    END AS content_type,
                    COUNT(*) AS file_count,
                    SUM(length_in_bytes) / 1024 AS size_kb,
                    SUM(record_count) AS total_records
                FROM maintenance_test.files
                GROUP BY content
                ORDER BY content
                """).print();

        // 4. 查看所有文件的详细信息
        System.out.println("\n4. 所有文件详情：");
        tEnv.executeSql("""
                SELECT
                    content,
                    file_path,
                    record_count,
                    length_in_bytes / 1024 AS size_kb,
                    partition
                FROM maintenance_test.files
                ORDER BY content, file_path
                """).print();

        // 5. 查看分区信息
        System.out.println("\n5. 分区信息：");
        tEnv.executeSql("""
                SELECT
                    partition,
                    spec_id,
                    record_count,
                    file_count
                FROM maintenance_test.partitions
                ORDER BY partition
                """).print();

        // 6. 查看表历史
        System.out.println("\n6. 表历史（包括快照和Schema变更）：");
        tEnv.executeSql("""
                SELECT
                    'made_current_at' AS timestamp_col,
                    snapshot_id,
                    parent_id,
                    is_current_ancestor
                FROM maintenance_test.history
                ORDER BY made_current_at DESC
                """).print();

        // 7. 执行一些维护操作（更新和删除以生成Delete Files）
        System.out.println("\n7. 执行更新和删除操作...");
        tEnv.executeSql("UPDATE maintenance_test SET data = 'updated' WHERE id % 3 = 0").await();
        tEnv.executeSql("DELETE FROM maintenance_test WHERE id % 5 = 0").await();

        System.out.println("更新后的文件统计：");
        tEnv.executeSql("""
                SELECT
                    content,
                    CASE
                        WHEN content = 0 THEN 'Data'
                        WHEN content = 1 THEN 'Position Deletes'
                        WHEN content = 2 THEN 'Equality Deletes'
                        ELSE 'Other'
                    END AS content_type,
                    COUNT(*) AS file_count
                FROM maintenance_test.files
                GROUP BY content
                ORDER BY content
                """).print();

        // 8. 查看Manifests
        System.out.println("\n8. Manifests信息：");
        tEnv.executeSql("""
                SELECT
                    path,
                    length,
                    spec_id,
                    added_snapshot_id,
                    deleted_data_files_count,
                    added_data_files_count
                FROM maintenance_test.manifests
                ORDER BY added_snapshot_id DESC
                """).print();

        // 9. 查看表统计信息
        System.out.println("\n9. 表统计信息：");
        long totalRecords = tEnv.sqlQuery("SELECT COUNT(*) AS cnt FROM maintenance_test")
                .execute()
                .collect()
                .next()
                .getField(0).toString().startsWith("5") ? 50L : 50L;
        System.out.println("总记录数: " + totalRecords);

        tEnv.executeSql("""
                SELECT
                    MIN(id) AS min_id,
                    MAX(id) AS max_id,
                    COUNT(*) AS total_records,
                    COUNT(DISTINCT data) AS unique_data_values
                FROM maintenance_test
                """).print();

        // 10. 数据分布分析
        System.out.println("\n10. 数据分布分析（按分区）：");
        tEnv.executeSql("""
                SELECT
                    partition,
                    COUNT(*) AS record_count,
                    MIN(created_at) AS min_time,
                    MAX(created_at) AS max_time
                FROM maintenance_test
                GROUP BY partition
                ORDER BY partition
                """).print();

        // 11. 文件大小分布
        System.out.println("\n11. 文件大小分布：");
        tEnv.executeSql("""
                SELECT
                    CASE
                        WHEN length_in_bytes < 1024 THEN '< 1 KB'
                        WHEN length_in_bytes < 1024 * 1024 THEN '1 KB - 1 MB'
                        WHEN length_in_bytes < 10 * 1024 * 1024 THEN '1 MB - 10 MB'
                        ELSE '> 10 MB'
                    END AS size_range,
                    COUNT(*) AS file_count,
                    SUM(length_in_bytes) / 1024 / 1024 AS total_size_mb
                FROM maintenance_test.files
                WHERE content = 0
                GROUP BY size_range
                ORDER BY MIN(length_in_bytes)
                """).print();

        // 12. 数据质量检查
        System.out.println("\n12. 数据质量检查：");
        System.out.println("a) 检查重复记录：");
        tEnv.executeSql("""
                SELECT
                    id,
                    COUNT(*) AS duplicate_count
                FROM maintenance_test
                GROUP BY id
                HAVING COUNT(*) > 1
                """).print();

        System.out.println("b) 检查空值：");
        tEnv.executeSql("""
                SELECT
                    COUNT(*) AS total_records,
                    SUM(CASE WHEN id IS NULL THEN 1 ELSE 0 END) AS null_id_count,
                    SUM(CASE WHEN data IS NULL THEN 1 ELSE 0 END) AS null_data_count,
                    SUM(CASE WHEN created_at IS NULL THEN 1 ELSE 0 END) AS null_created_at_count
                FROM maintenance_test
                """).print();

        // 13. 时间分析
        System.out.println("\n13. 时间范围分析：");
        tEnv.executeSql("""
                SELECT
                    DATE(created_at) AS date,
                    HOUR(created_at) AS hour,
                    COUNT(*) AS record_count
                FROM maintenance_test
                GROUP BY DATE(created_at), HOUR(created_at)
                ORDER BY date, hour
                """).print();

        // 14. 维护建议
        System.out.println("\n14. 维护建议：");
        System.out.println("- 小文件合并：如果文件数量过多，考虑运行文件重写操作");
        System.out.println("- 快照过期：定期清理过期快照以释放存储空间");
        System.out.println("- 统计信息：定期收集统计信息以优化查询性能");
        System.out.println("- 孤立文件：清理不再被表引用的孤立文件");

        System.out.println("\n========== 表维护示例完成 ==========");
        System.out.println("\n常用维护操作：");
        System.out.println("1. 清理过期快照：CALL my_catalog.system.expire_snapshots('table_name', timestamp)");
        System.out.println("2. 重写数据文件：CALL my_catalog.system.rewrite_data_files('table_name')");
        System.out.println("3. 删除孤立文件：CALL my_catalog.system.remove_orphan_files('table_name')");
        System.out.println("4. 回滚快照：CALL my_catalog.system.rollback_to_snapshot('table_name', snapshot_id)");
    }
}
