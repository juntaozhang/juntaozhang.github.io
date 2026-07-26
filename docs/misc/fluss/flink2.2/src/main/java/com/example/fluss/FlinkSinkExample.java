package com.example.fluss;

import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.config.ExecutionConfigOptions;
import org.apache.flink.table.api.config.OptimizerConfigOptions;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class FlinkSinkExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.inBatchMode();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

        DeltaJoinExample.useDB(tEnv);
//        tEnv.getConfig().set(RestOptions.PORT, 28083);
//        tEnv.executeSql(
//                        "INSERT INTO left_src VALUES\n"
//                                + "(1, 100, 'Order 1 from City 1'),\n"
//                                + "(1, 101, 'Order 2 from City 1'),\n"
//                                + "(2, 200, 'Order 1 from City 2'),\n"
//                                + "(2, 201, 'Order 2 from City 2'),\n"
//                                + "(3, 300, 'Order 1 from City 3')").await();
//
//        tEnv.executeSql(
//                        "INSERT INTO right_src VALUES\n"
//                                + "(1, 'New York'),\n"
//                                + "(2, 'Los Angeles'),\n"
//                                + "(3, 'Chicago'),\n"
//                                + "(4, 'Houston'),\n"
//                                + "(5, 'Phoenix')").await();

//        tEnv.executeSql("INSERT INTO left_src VALUES (1, 100, '" + new Date() + "')");
//        tEnv.executeSql("INSERT INTO right_src VALUES (1, 'New York2')");
        tEnv.executeSql("delete FROM left_src WHERE city_id = 1 AND order_id = 100");
//        tEnv.executeSql("delete FROM right_src WHERE city_id = 1");
//        tEnv.executeSql("delete FROM snk WHERE city_id =  1 AND order_id = 101");
//        tEnv.executeSql("INSERT INTO left_src VALUES (1, 102, 'test2')");
//        tEnv.executeSql("INSERT INTO right_s  rc VALUES (2, 'Los Angeles')");
    }
}
