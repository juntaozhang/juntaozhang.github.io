package com.example.iceberg;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

/**
 * Apache Iceberg 流式写入示例
 *
 * 演示功能：
 * - 从 Kafka 流式读取数据
 * - 流式写入 Iceberg 表
 * - 检查点配置
 * - Exactly-Once 语义
 */
public class StreamingWriteExample {

    public static void main(String[] args) {
        System.out.println("========== Iceberg 流式写入 ==========");

        try {
            // 初始化流式环境
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // 配置检查点（流式任务必须）
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            checkpointConfig.setCheckpointInterval(30000); // 30秒
            checkpointConfig.setCheckpointTimeout(600000); // 10分钟
            checkpointConfig.setMinPauseBetweenCheckpoints(10000); // 最小间隔10秒
            checkpointConfig.setMaxConcurrentCheckpoints(1);
            checkpointConfig.enableExternalizedCheckpoints(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

            env.setParallelism(2);

            // 创建 TableEnvironment
            EnvironmentSettings settings = EnvironmentSettings.newInstance()
                    .inStreamingMode()  // 流式模式
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
                    CREATE TABLE IF NOT EXISTS orders_stream (
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
                        'write.format.default' = 'parquet',
                        'write.metadata.enabled' = 'true'
                    )
                    """);
            System.out.println("目标表创建成功！");

            // 2. 创建流式源表（使用 DataGen 模拟数据）
            System.out.println("\n2. 创建流式源表...");
            tEnv.executeSql("""
                    CREATE TABLE orders_source (
                        id BIGINT,
                        customer_id BIGINT,
                        product_id BIGINT,
                        quantity INT,
                        price DOUBLE
                    )
                    WITH (
                        'connector' = 'datagen',
                        'rows-per-second' = '1',
                        'fields.id.kind' = 'sequence',
                        'fields.id.start' = '100',
                        'fields.id.end' = '1000',
                        'fields.customer_id.min' = '1',
                        'fields.customer_id.max' = '50',
                        'fields.product_id.min' = '1',
                        'fields.product_id.max' = '30',
                        'fields.quantity.min' = '1',
                        'fields.quantity.max' = '5',
                        'fields.price.min' = '10.0',
                        'fields.price.max' = '500.0',
                        'number-of-rows' = '20'
                    )
                    """);
            System.out.println("流式源表创建成功！");

            // 3. 启动流式写入
            System.out.println("\n3. 启动流式写入任务...");
            System.out.println("注意：这是一个持续运行的流任务");
            System.out.println("运行 10 秒后自动停止（演示）\n");

            // 流式写入到 Iceberg
            TableResult result = tEnv.executeSql("""
                    INSERT INTO orders_stream
                    SELECT
                        id,
                        customer_id,
                        product_id,
                        quantity,
                        price,
                        DATE '2025-02-27',
                        LOCALTIMESTAMP
                    FROM orders_source
                    """);

            // 等待一段时间让数据写入
            Thread.sleep(10000);

            // 取消作业
            System.out.println("\n停止流式写入任务...");
            result.getJobClient().ifPresent(client -> {
                try {
                    client.cancel();
                } catch (Exception e) {
                    System.err.println("取消任务失败: " + e.getMessage());
                }
            });

            // 4. 查询写入的数据
            System.out.println("\n4. 查询写入的数据...");
            Thread.sleep(2000); // 等待数据提交
            tEnv.executeSql("SELECT * FROM orders_stream ORDER BY id DESC LIMIT 10").print();

            // 5. 查看表统计信息
            System.out.println("\n5. 表统计信息：");
            tEnv.executeSql("SELECT COUNT(*) AS total_records FROM orders_stream").print();

            // 6. 查看快照历史
            System.out.println("\n6. 快照历史（流式写入会产生多个快照）：");
            tEnv.executeSql("""
                    SELECT
                        snapshot_id,
                        committed_at,
                        summary
                    FROM orders_stream.snapshots
                    ORDER BY committed_at DESC
                    LIMIT 10
                    """).print();

            System.out.println("\n========== 流式写入完成 ==========");
            System.out.println("关键特性：");
            System.out.println("- 使用检查点保证 Exactly-Once 语义");
            System.out.println("- 自动管理小文件");
            System.out.println("- 支持Schema演进");
            System.out.println("- 分区自动管理");

        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
