package com.example.paimon;

import org.apache.spark.sql.SparkSession;

public class SparkRowTrackingExample {
  public static void main(String[] args) {
    SparkSession spark =
        SparkSession.builder()
            .appName("Spark Paimon Example")
            .master("local[*]")
            .config("spark.default.parallelism", "1")
            .config(
                "spark.sql.extensions",
                "org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions")
            .config("spark.sql.codegen.wholeStage", "false")
            .config("spark.sql.catalog.paimon", "org.apache.paimon.spark.SparkCatalog")
            .config("spark.sql.catalog.paimon.warehouse", "s3://warehouse/paimon")
            .config("spark.sql.catalog.paimon.s3.path.style.access", "true")
            .config("spark.sql.catalog.paimon.s3.access-key", "test")
            .config("spark.sql.catalog.paimon.s3.secret-key", "11111111")
            .config("spark.sql.catalog.paimon.s3.endpoint", "http://localhost:32000")
            .getOrCreate();
    spark.sql("USE paimon.ods");
    //         spark.sql("SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER FROM t order by _ROW_ID
    // asc").show();
    //        spark.sql("SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER, __paimon_file_path,
    // __paimon_partition, __paimon_bucket, __paimon_row_index FROM t WHERE _ROW_ID =
    // 1").show(false);
    //        spark.sql("UPDATE t SET data = 'a4' WHERE _ROW_ID = 1");
    //        spark.sql("UPDATE t SET data = 'a4' WHERE id = 1");
    //        spark.sql("DELETE FROM t WHERE _ROW_ID = 1");
    //        spark.sql("CALL sys.compact(`table` => 'my_order')");
    //    spark.sql("drop table if exists postpone_bucket_table");
    //    spark.sql(
    // """
    // CREATE TABLE postpone_bucket_table (
    //  order_id INT,
    //  customer_id INT,
    //  order_date STRING,
    //  total_amount DECIMAL(10, 2),
    //  status STRING
    // ) PARTITIONED BY (order_date) TBLPROPERTIES (
    //  'primary-key' = 'order_date,order_id',
    //  'bucket' = '-2',
    //  'postpone.batch-write-fixed-bucket' = 'false',
    //  'postpone.default-bucket-num' = '2',
    //  'file.format' = 'parquet'
    // )
    // """);
    //
    //    spark.sql(
    // """
    // INSERT INTO postpone_bucket_table VALUES\s
    //  (201, 1001, '2023-10-03', 100.50, 'COMPLETED'),
    //  (202, 1002, '2023-10-03', 200.75, 'PENDING'),
    //  (203, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (204, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (205, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (206, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (207, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (208, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (209, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (210, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (211, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (212, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (213, 1001, '2023-10-03', 150.20, 'COMPLETED'),
    //  (214, 1001, '2023-10-03', 150.20, 'COMPLETED')
    // """);

//      spark.sql("delete from postpone_bucket_table where order_id = 201");
//      spark.sql("insert into postpone_bucket_table values (215, 1001, '2023-10-03', 100.50, 'COMPLETED')");

//    spark.sql("CALL sys.compact(`table` => 'postpone_bucket_table')");

    spark.stop();
  }
}
