package com.example.paimon;

import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.nio.file.Paths;

public class ProceduresExample {
    public static void main(String[] args) {
        String ckpDir = Paths.get("checkpoints/" + ProceduresExample.class.getSimpleName())
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


        tEnv.executeSql("""
                CREATE CATALOG paimon_catalog WITH (
                'type' = 'paimon',
                'warehouse' = 's3a://warehouse/paimon'
                )
                """);
        tEnv.executeSql("USE CATALOG paimon_catalog");
        // tEnv.executeSql("CALL sys.compact_database(including_databases => 'ods', mode => 'combined')");
        tEnv.executeSql("CALL sys.compact_database(including_databases => 'ods', mode => 'divided')");
    }
}
