package com.example.paimon;

import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.nio.file.Paths;

public class LookupExample {
    public static void main(String[] args) {
        String ckpDir = Paths.get("checkpoints/" + LookupExample.class.getSimpleName())
                .toUri()
                .toString();
        Configuration conf = new Configuration();
        conf.set(RestOptions.PORT, 8083);
        conf.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, ckpDir);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(3);
        env.enableCheckpointing(5_000);
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .withConfiguration(conf)
                .inStreamingMode().build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);
        String warehouse = "s3a://warehouse/paimon";
        tEnv.executeSql("CREATE CATALOG paimon_catalog WITH (\n" +
                "                'type' = 'paimon',\n" +
                "                'warehouse' = '" + warehouse + "'\n" +
                "                )");
        tEnv.executeSql("USE CATALOG paimon_catalog");
        tEnv.executeSql("CREATE TEMPORARY TABLE orders (\n" +
                "                order_id BIGINT,\n" +
                "                price DECIMAL(32, 2),\n" +
                "                ts TIMESTAMP(3),\n" +
                "                customer_id INT,\n" +
                "                proc_time AS PROCTIME() \n" +
                "                ) WITH (\n" +
                "                'connector' = 'datagen',\n" +
                "                'rows-per-second' = '1',\n" +
                "                'fields.order_id.kind' = 'sequence',\n" +
                "                'fields.order_id.start' = '1',\n" +
                "                'fields.order_id.end' = '1000000',\n" +
                "                'fields.price.min' = '1',\n" +
                "                'fields.price.max' = '100',\n" +
                "                'fields.customer_id.min' = '1',\n" +
                "                'fields.customer_id.max' = '2'\n" +
                "                )");
        tEnv.executeSql("drop table if exists lkp_order_customer");
        tEnv.executeSql("CREATE TABLE lkp_order_customer (\n" +
                "    order_id BIGINT,\n" +
                "    price DECIMAL(32, 2),\n" +
                "    ts TIMESTAMP(3),\n" +
                "    customer_id INT,\n" +
                "    customer_name STRING,\n" +
                "    PRIMARY KEY (order_id) NOT ENFORCED\n" +
                "  ) WITH ( \n" +
                "    'bucket' = '3',\n" +
                "    'bucket-key' = 'order_id',\n" +
                "    'write-only' = 'false'\n" +
                ")");
        tEnv.executeSql("INSERT INTO lkp_order_customer SELECT /*+ LOOKUP('table'='c', 'shuffle'='true') */ \n" +
                "    o.order_id,\n" +
                "    o.price,\n" +
                "    o.ts,\n" +
                "    c.id,\n" +
                "    c.name\n" +
                "FROM orders o\n" +
                "LEFT JOIN customers FOR SYSTEM_TIME AS OF o.proc_time AS c\n" +
                "ON o.customer_id = c.id");
    }
}
