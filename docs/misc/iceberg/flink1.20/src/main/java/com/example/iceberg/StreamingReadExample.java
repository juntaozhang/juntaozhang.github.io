package com.example.iceberg;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

/**
 * Apache Iceberg 流式读取示例
 *
 * 演示功能：
 * - 从 Iceberg 表流式读取数据
 * - 监控表的增量变更
 * - 消费新写入的数据
 */
public class StreamingReadExample {

    public static void main(String[] args) {
        System.out.println("========== Iceberg 流式读取 ==========");

        try {
            // 初始化流式环境
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
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

            // 1. 创建目标表
            System.out.println("\n1. 创建目标表...");
            tEnv.executeSql("""
                    DROP TABLE IF EXISTS events_stream
                    """);

            tEnv.executeSql("""
                    CREATE TABLE events_stream (
                        event_id BIGINT,
                        event_type STRING,
                        user_id BIGINT,
                        event_data STRING,
                        event_ts TIMESTAMP(3),
                        PRIMARY KEY (event_id) NOT ENFORCED
                    )
                    PARTITIONED BY (days(event_ts))
                    WITH (
                        'format-version' = '2',
                        'write.format.default' = 'parquet'
                    )
                    """);
            System.out.println("目标表创建成功！");

            // 2. 批量写入初始数据
            System.out.println("\n2. 写入初始数据...");
            tEnv.executeSql("""
                    INSERT INTO events_stream VALUES
                        (1, 'page_view', 100, '{\"page\": \"/home\"}', TIMESTAMP '2025-02-27 10:00:00'),
                        (2, 'click', 100, '{\"button\": \"submit\"}', TIMESTAMP '2025-02-27 10:01:00'),
                        (3, 'page_view', 101, '{\"page\": \"/products\"}', TIMESTAMP '2025-02-27 10:02:00'),
                        (4, 'search', 102, '{\"query\": \"laptop\"}', TIMESTAMP '2025-02-27 10:03:00'),
                        (5, 'page_view', 103, '{\"page\": \"/cart\"}', TIMESTAMP '2025-02-27 10:04:00')
                    """).await();

            System.out.println("初始数据：");
            tEnv.executeSql("SELECT * FROM events_stream ORDER BY event_id").print();

            // 3. 流式读取配置
            System.out.println("\n3. 配置流式读取...");

            /*
            // 方式 1: 从当前快照开始流式读取
            tEnv.executeSql("""
                    CREATE TABLE events_streaming_read WITH (
                        'connector' = 'iceberg',
                        'catalog-name' = 'my_catalog',
                        'database' = 'db',
                        'table' = 'events_stream',
                        'scan.mode' = 'latest'  -- 从最新快照开始，只读新增数据
                    )
                    """);
            */

            /*
            // 方式 2: 从最早快照开始流式读取
            tEnv.executeSql("""
                    CREATE TABLE events_streaming_read WITH (
                        'connector' = 'iceberg',
                        'catalog-name' = 'my_catalog',
                        'database' = 'db',
                        'table' = 'events_stream',
                        'scan.mode' = 'earliest'  -- 从最早的快照开始
                    )
                    """);
            */

            /*
            // 方式 3: 从特定快照开始流式读取
            tEnv.executeSql("""
                    CREATE TABLE events_streaming_read WITH (
                        'connector' = 'iceberg',
                        'catalog-name' = 'my_catalog',
                        'database' = 'db',
                        'table' = 'events_stream',
                        'scan.start-snapshot-id' = '1234567890123456789'  -- 指定快照ID
                    )
                    """);
            */

            /*
            // 方式 4: 从特定时间戳开始流式读取
            tEnv.executeSql("""
                    CREATE TABLE events_streaming_read WITH (
                        'connector' = 'iceberg',
                        'catalog-name' = 'my_catalog',
                        'database' = 'db',
                        'table' = 'events_stream',
                        'scan.start-timestamp' = '1740633600000'  -- 毫秒时间戳
                    )
                    """);
            */

            System.out.println("注意：取消注释上面的配置以创建流式读取表");

            // 4. 启动流式读取（取消注释以运行）
            /*
            System.out.println("\n4. 启动流式读取任务...");
            System.out.println("这将持续监控表的新增数据...\n");

            TableResult streamResult = tEnv.executeSql("""
                    SELECT * FROM events_streaming_read
                    """);

            streamResult.print();
            */

            // 5. 模拟流式写入和读取
            System.out.println("\n5. 模拟流式场景...");

            // 写入更多数据
            System.out.println("写入更多事件...");
            for (int i = 6; i <= 10; i++) {
                long finalI = i;
                tEnv.executeSql(String.format("""
                                INSERT INTO events_stream VALUES
                                    (%d, 'page_view', %d, '{\"page\": \"/product-%d\"}', TIMESTAMP '2025-02-27 10:%02d:00')
                                """,
                        finalI, 100 + finalI, finalI, 5 + finalI)).await();
            }

            System.out.println("所有数据：");
            tEnv.executeSql("SELECT * FROM events_stream ORDER BY event_id").print();

            // 6. 按时间窗口聚合
            System.out.println("\n6. 实时统计（每小时事件数量）：");
            tEnv.executeSql("""
                    SELECT
                        HOUR(event_ts) AS hour,
                        event_type,
                        COUNT(*) AS event_count
                    FROM events_stream
                    GROUP BY HOUR(event_ts), event_type
                    ORDER BY hour, event_type
                    """).print();

            // 7. 按用户统计
            System.out.println("\n7. 用户活动统计：");
            tEnv.executeSql("""
                    SELECT
                        user_id,
                        COUNT(*) AS total_events,
                        COUNT(DISTINCT event_type) AS unique_event_types,
                        MIN(event_ts) AS first_event,
                        MAX(event_ts) AS last_event
                    FROM events_stream
                    GROUP BY user_id
                    ORDER BY total_events DESC
                    """).print();

            // 8. 查看表的快照
            System.out.println("\n8. 表的快照历史：");
            tEnv.executeSql("""
                    SELECT
                        snapshot_id,
                        committed_at,
                        summary
                    FROM events_stream.snapshots
                    ORDER BY committed_at DESC
                    """).print();

            System.out.println("\n========== 流式读取示例完成 ==========");
            System.out.println("\n关键特性：");
            System.out.println("- 流式读取可以监控表的变更");
            System.out.println("- 支持从不同位置开始读取（earliest/latest/snapshot-id/timestamp）");
            System.out.println("- 自动检测新增的快照和数据");
            System.out.println("- 适合实时监控和分析场景");

            System.out.println("\n使用场景：");
            System.out.println("- 实时数据仓库");
            System.out.println("- CDC 数据同步");
            System.out.println("- 实时报表和仪表板");
            System.out.println("- 事件驱动的应用");

        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
