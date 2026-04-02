package com.example.paimon;

import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.nio.file.Paths;

public class FlinkDeduplicateExample {
    public static void main(String[] args) {
        String ckpDir = Paths.get("checkpoints/" + FlinkDeduplicateExample.class.getSimpleName())
                .toUri()
                .toString();
        Configuration conf = new Configuration();
        conf.set(RestOptions.PORT, 8082);
        conf.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, ckpDir);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(2);
        env.enableCheckpointing(30_000);
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .withConfiguration(conf)
                .inStreamingMode().build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);


        // Define the Orders table
        String createSrcTable = """
                CREATE TEMPORARY TABLE src_order (
                order_id BIGINT,
                price DECIMAL(32, 2),
                ts TIMESTAMP(3),
                WATERMARK FOR ts AS ts - INTERVAL '5' SECOND
                ) WITH (
                'connector' = 'datagen',
                'rows-per-second' = '1',
                'fields.order_id.kind' = 'sequence',
                'fields.order_id.start' = '1',
                'fields.order_id.end' = '1000000',
                'fields.price.min' = '1',
                'fields.price.max' = '100'
                )
                """;

        // default bucket='-1'
        String createSinkTable = """
                CREATE TABLE my_order (
                  order_id BIGINT,
                  price DECIMAL(32, 2),
                  ts TIMESTAMP(3),
                  PRIMARY KEY (order_id) NOT ENFORCED
                ) WITH (
                    'write-only' = 'true'
                )
                """;
        String query = "INSERT INTO my_order SELECT * FROM src_order";
        tEnv.executeSql("""
                CREATE CATALOG paimon_catalog WITH (
                'type' = 'paimon',
                'warehouse' = 's3a://warehouse/paimon'
                )
                """);
        tEnv.executeSql("USE CATALOG paimon_catalog");
        tEnv.executeSql("use ods");
        tEnv.executeSql(createSrcTable);
        tEnv.executeSql(createSinkTable);
        tEnv.executeSql(query);
    }
}
