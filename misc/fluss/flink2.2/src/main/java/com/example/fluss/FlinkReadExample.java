package com.example.fluss;

import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.config.ExecutionConfigOptions;

import static com.example.fluss.DeltaJoinExample.createTables;
import static com.example.fluss.DeltaJoinExample.useDB;

public class FlinkReadExample {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);
        tEnv.getConfig().set(ExecutionConfigOptions.TABLE_EXEC_RESOURCE_DEFAULT_PARALLELISM, 1);
        tEnv.getConfig().set(RestOptions.PORT, 28082);

        useDB(tEnv);
//        createTables(tEnv);

//        TableResult result = tEnv.executeSql("SELECT * FROM left_src");
//        TableResult result = tEnv.executeSql("SELECT * FROM right_src");
        TableResult result = tEnv.executeSql("SELECT * FROM snk");
        result.print();
    }
}
