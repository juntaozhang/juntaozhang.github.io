package com.example.fluss;

import org.apache.flink.client.cli.CheckpointOptions;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.apache.flink.core.execution.RecoveryClaimMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.config.ExecutionConfigOptions;
import org.apache.flink.table.api.config.OptimizerConfigOptions;

import java.nio.file.Paths;
import java.time.Duration;

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
public class DeltaJoinExample {

    public static void main(String[] args) throws Exception {
        String ckpDir = Paths.get("checkpoints/" + DeltaJoinExample.class.getSimpleName())
                .toUri()
                .toString();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);
        tEnv.getConfig().set(ExecutionConfigOptions.TABLE_EXEC_RESOURCE_DEFAULT_PARALLELISM, 1);
        tEnv.getConfig().set(RestOptions.PORT, 28081);

        // force delta join strategy
        tEnv.getConfig().set(OptimizerConfigOptions.TABLE_OPTIMIZER_DELTA_JOIN_STRATEGY, OptimizerConfigOptions.DeltaJoinStrategy.NONE);
        tEnv.getConfig().set(CheckpointingOptions.CHECKPOINTING_INTERVAL, Duration.ofSeconds(10));
        tEnv.getConfig().set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, ckpDir);

        // restore from checkpoint
//        tEnv.getConfig().set(
//                StateRecoveryOptions.SAVEPOINT_PATH,
//                "file:///Users/juntao/src/github.com/juntaozhang/juntaozhang.github.io/checkpoints/DeltaJoinExample/52c6b5e88dbda91f7a7ca77eaac6b60c/chk-1013");
//        tEnv.getConfig().set(StateRecoveryOptions.RESTORE_MODE, RecoveryClaimMode.NO_CLAIM);

        useDB(tEnv);
        // createTables(tEnv);

        tEnv.executeSql(
                "INSERT INTO snk \n"
                        + "SELECT T1.city_id, T1.order_id, T1.content, T2.city_name \n"
                        + "FROM left_src T1 \n"
                        + "JOIN right_src T2 \n"
                        + "ON T1.city_id = T2.city_id");
    }

    public static void useDB(StreamTableEnvironment tEnv) {
        tEnv.executeSql(
                "CREATE CATALOG fluss_catalog WITH ("
                        + "'type' = 'fluss', "
                        + "'paimon.s3.access-key' = 'test', "
                        + "'paimon.s3.secret-key' = '11111111', "
                        + "'bootstrap.servers' = 'localhost:9123'"
                        + ")"
        );

        tEnv.executeSql("USE CATALOG fluss_catalog");
        tEnv.executeSql("CREATE DATABASE IF NOT EXISTS ods");
        tEnv.executeSql("USE ods");
    }

    public static void createTables(StreamTableEnvironment tEnv) {

        tEnv.executeSql("drop table left_src");
        tEnv.executeSql("drop table right_src");
        tEnv.executeSql("drop table snk");
        tEnv.executeSql(
                "CREATE TABLE left_src (\n"
                        + "  city_id INT NOT NULL,\n"
                        + "  order_id INT NOT NULL,\n"
                        + "  content VARCHAR NOT NULL,\n"
                        + "  PRIMARY KEY (city_id, order_id) NOT ENFORCED\n"
                        + ") WITH (\n"
                        + "  '-- table.delete.behavior' = 'IGNORE',\n"
                        + "  'connector' = 'fluss',\n"
                        + "  'bucket.key' = 'city_id'\n"
                        + ")");

        System.out.println("✓ Created table: left_src");
        tEnv.executeSql(
                "CREATE TABLE right_src (\n"
                        + "  city_id INT NOT NULL,\n"
                        + "  city_name VARCHAR NOT NULL,\n"
                        + "  PRIMARY KEY (city_id) NOT ENFORCED\n"
                        + ") WITH (\n"
                        + "  '-- table.delete.behavior' = 'IGNORE',\n"
                        + "  'connector' = 'fluss',\n"
                        + "  'bucket.key' = 'city_id'\n"
                        + ")");

        System.out.println("✓ Created table: right_src");

        tEnv.executeSql(
                "CREATE TABLE snk (\n"
                        + "  city_id INT NOT NULL,\n"
                        + "  order_id INT NOT NULL,\n"
                        + "  content VARCHAR NOT NULL,\n"
                        + "  city_name VARCHAR NOT NULL,\n"
                        + "  PRIMARY KEY (city_id, order_id) NOT ENFORCED\n"
                        + ") WITH (\n"
                        + "  'connector' = 'fluss'\n"
                        + ")");
        System.out.println("✓ Created table: snk");
    }
}
