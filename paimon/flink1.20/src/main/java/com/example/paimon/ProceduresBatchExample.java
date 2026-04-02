package com.example.paimon;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class ProceduresBatchExample {
  public static void main(String[] args) {
    Configuration conf = new Configuration();
    conf.set(RestOptions.PORT, 8083);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
    env.setParallelism(1);
    EnvironmentSettings settings =
        EnvironmentSettings.newInstance().withConfiguration(conf).inBatchMode().build();
    StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

    tEnv.executeSql(
        """
                CREATE CATALOG paimon_catalog WITH (
                'type' = 'paimon',
                'warehouse' = 's3a://warehouse/paimon'
                )
                """);
    tEnv.executeSql("USE CATALOG paimon_catalog");
    tEnv.executeSql("USE ods");
    //        tEnv.executeSql("delete from ods.postpone_bucket_table where order_id = 9");
    //    tEnv.executeSql(
    // """
    // INSERT INTO postpone_bucket_table VALUES\s
    //  (201, 1001, '2023-10-04', 100.50, 'COMPLETED'),
    //  (202, 1002, '2023-10-04', 200.75, 'PENDING'),
    //  (203, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (204, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (205, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (206, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (207, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (208, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (209, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (210, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (211, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (212, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (213, 1001, '2023-10-04', 150.20, 'COMPLETED'),
    //  (214, 1001, '2023-10-04', 150.20, 'COMPLETED')
    // """);
    //        tEnv.executeSql("CALL sys.compact(`table` => 'ods.postpone_bucket_table',
    // compact_strategy => 'full')");

    //    tEnv.executeSql(
    // """
    // CREATE TABLE postpone_bucket_table2 (
    //  order_id INT,
    //  customer_id INT,
    //  order_date STRING,
    //  total_amount DECIMAL(10, 2),
    //  status STRING,
    //  PRIMARY KEY (order_date, order_id) NOT ENFORCED
    // ) WITH (
    //  'bucket' = '-2',
    //  'postpone.batch-write-fixed-bucket' = 'false',
    //  'postpone.default-bucket-num' = '2',
    //  'file.format' = 'parquet'
    // );
    // """);
//    tEnv.executeSql(
//        "insert into postpone_bucket_table2 values (202, 1001, '2023-10-04', 100.50, 'COMPLETED')");
    tEnv.executeSql(
        "CALL sys.compact(`table` => 'ods.postpone_bucket_table2', compact_strategy => 'full')");
  }
}
