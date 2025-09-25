package com.example.paimon;

import org.apache.spark.sql.SparkSession;

public class SparkRowTrackingExample {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Spark Paimon Example")
                .master("local[*]")
                .config("spark.sql.extensions", "org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions")
                .config("spark.sql.codegen.wholeStage", "false")
                .config("spark.sql.catalog.paimon", "org.apache.paimon.spark.SparkCatalog")
                .config("spark.sql.catalog.paimon.warehouse", "s3://warehouse/paimon")
                .config("spark.sql.catalog.paimon.s3.path.style.access", "true")
                .config("spark.sql.catalog.paimon.s3.access-key", "minio")
                .config("spark.sql.catalog.paimon.s3.secret-key", "minio12345")
                .config("spark.sql.catalog.paimon.s3.endpoint", "http://localhost:9000")
                .getOrCreate();
        spark.sql("USE paimon.ods");
//         spark.sql("SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER FROM t order by _ROW_ID asc").show();
//        spark.sql("SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER, __paimon_file_path, __paimon_partition, __paimon_bucket, __paimon_row_index FROM t WHERE _ROW_ID = 1").show(false);
//        spark.sql("UPDATE t SET data = 'a4' WHERE _ROW_ID = 1");
//        spark.sql("UPDATE t SET data = 'a4' WHERE id = 1");
        spark.sql("DELETE FROM t WHERE _ROW_ID = 1");
        spark.stop();
    }
}