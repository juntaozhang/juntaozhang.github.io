package com.example.fluss;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.concurrent.ExecutionException;

/*
-XX:+IgnoreUnrecognizedVMOptions
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/java.nio=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED
--add-opens=java.base/sun.security.action=ALL-UNNAMED
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED
-Djdk.reflect.useDirectMethodHandle=false
-Dio.netty.tryReflectionSetAccessible=true
 */
public class FlinkReadExample {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set(RestOptions.PORT, 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(1);
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .withConfiguration(conf)
                .inBatchMode()
                .build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

        tEnv.executeSql("CREATE CATALOG fluss_catalog WITH (\n" +
                "                  'type' = 'fluss',\n" +
                "                  'paimon.s3.access-key' = 'test',\n" +
                "                  'paimon.s3.secret-key' = '11111111',\n" +
                "                  'bootstrap.servers' = 'localhost:9123'\n" +
                "                )");
        tEnv.executeSql("use catalog fluss_catalog");
        tEnv.executeSql("CREATE DATABASE IF NOT EXISTS ods");
        tEnv.executeSql("USE ods");

//         createTable(tEnv);
//         insertData(tEnv);
//        tEnv.executeSql("update ods.pk_table set total_amount=113 where user_id=1 and shop_id=1").print();
        tEnv.executeSql("SELECT * FROM pk_table where user_id = 1").print();
//        tEnv.executeSql("SELECT count(1) FROM pk_table$lake").print();
//        tEnv.executeSql("SELECT * FROM pk_table$lake$snapshots").print();

        env.close();
    }

    private static void insertData(StreamTableEnvironment tEnv) throws InterruptedException, ExecutionException {
        for (int i = 0; i < 10; i++) {
            TableResult insertResult = tEnv.executeSql("INSERT INTO pk_table VALUES (" + i % 3 + ", " + i % 3 + ", " + i + ", " + i + ")");
            insertResult.await();
        }
        // TableResult insertResult = tEnv.executeSql("INSERT INTO pk_table VALUES (1, 1, 1, 1),(2, 2, 2, 2),(3, 3, 3, 3)");
        // insertResult.await();
    }

    private static void createTable(StreamTableEnvironment tEnv) {
        tEnv.executeSql("drop table IF EXISTS pk_table");
        tEnv.executeSql("SHOW TABLES").print();
        tEnv.executeSql("CREATE TABLE IF NOT EXISTS pk_table (\n" +
                "              shop_id BIGINT,\n" +
                "              user_id BIGINT,\n" +
                "              num_orders INT,\n" +
                "              total_amount INT,\n" +
                "              PRIMARY KEY (shop_id, user_id) NOT ENFORCED\n" +
                "            ) WITH (\n" +
                "              'table.datalake.enabled' = 'true',\n" +
                "              'table.datalake.format' = 'paimon',\n" +
                "              'table.datalake.freshness' = '30s',\n" +
                "              'bucket.num' = '3'\n" +
                "            )");
        tEnv.executeSql("DESCRIBE pk_table").print();
    }
}
