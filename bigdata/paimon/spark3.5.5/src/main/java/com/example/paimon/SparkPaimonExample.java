//package com.example.paimon;
//
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.types.DataTypes;
//import org.apache.spark.sql.types.StructField;
//import org.apache.spark.sql.types.StructType;
//
//import java.util.Arrays;
//
//import static org.apache.spark.sql.functions.*;
//
//public class SparkPaimonExample {
//
//    public static void main(String[] args) {
//        // Create Spark session with Paimon support
//        SparkSession spark = SparkSession.builder()
//                .appName("Spark Paimon Example")
//                .master("local[*]")
//                .config("spark.sql.extensions", "org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions")
//                .config("spark.sql.catalog.paimon", "org.apache.paimon.spark.SparkCatalog")
//                .config("spark.sql.catalog.paimon.warehouse", "file:///tmp/paimon-warehouse")
//                .getOrCreate();
//
//        try {
//            System.out.println("=== Spark Paimon Example Started ===");
//
//            // Create a Paimon database
//            spark.sql("CREATE DATABASE IF NOT EXISTS paimon.test_db");
//            spark.sql("USE paimon.test_db");
//
//            // Create a Paimon table
//            String createTableSQL = """
//                CREATE TABLE IF NOT EXISTS user_behavior (
//                    user_id BIGINT,
//                    item_id BIGINT,
//                    category_id BIGINT,
//                    behavior STRING,
//                    ts TIMESTAMP,
//                    dt STRING
//                ) USING PAIMON
//                TBLPROPERTIES (
//                    'primary-key' = 'user_id,item_id,dt',
//                    'bucket' = '2'
//                )
//                PARTITIONED BY (dt)
//                """;
//
//            spark.sql(createTableSQL);
//            System.out.println("✓ Created Paimon table: user_behavior");
//
//            // Insert some sample data
//            String insertSQL = """
//                INSERT INTO user_behavior VALUES
//                (1001, 2001, 301, 'view', TIMESTAMP '2024-01-01 10:00:00', '2024-01-01'),
//                (1001, 2002, 302, 'cart', TIMESTAMP '2024-01-01 10:05:00', '2024-01-01'),
//                (1002, 2001, 301, 'view', TIMESTAMP '2024-01-01 10:10:00', '2024-01-01'),
//                (1002, 2003, 303, 'purchase', TIMESTAMP '2024-01-01 10:15:00', '2024-01-01'),
//                (1003, 2002, 302, 'view', TIMESTAMP '2024-01-01 10:20:00', '2024-01-01')
//                """;
//
//            spark.sql(insertSQL);
//            System.out.println("✓ Inserted sample data");
//
//            // Query the data
//            System.out.println("\n=== Querying Paimon table ===");
//            Dataset<Row> result = spark.sql("SELECT * FROM user_behavior ORDER BY user_id, ts");
//            result.show();
//
//            // Perform some analytics
//            System.out.println("\n=== User behavior summary ===");
//            Dataset<Row> summary = spark.sql("""
//                SELECT
//                    behavior,
//                    COUNT(*) as count,
//                    COUNT(DISTINCT user_id) as unique_users
//                FROM user_behavior
//                GROUP BY behavior
//                ORDER BY count DESC
//                """);
//            summary.show();
//
//            // Update some data (Paimon supports UPSERT)
//            System.out.println("\n=== Updating data ===");
//            spark.sql("""
//                INSERT INTO user_behavior VALUES
//                (1001, 2001, 301, 'purchase', TIMESTAMP '2024-01-01 11:00:00', '2024-01-01')
//                """);
//
//            System.out.println("✓ Updated user behavior (view -> purchase)");
//
//            // Query again to see the update
//            System.out.println("\n=== Data after update ===");
//            Dataset<Row> updatedResult = spark.sql("""
//                SELECT * FROM user_behavior
//                WHERE user_id = 1001 AND item_id = 2001
//                ORDER BY ts
//                """);
//            updatedResult.show();
//
//            // Show table metadata
//            System.out.println("\n=== Table metadata ===");
//            spark.sql("DESCRIBE EXTENDED user_behavior").show(50, false);
//
//            System.out.println("\n=== Spark Paimon Example Completed Successfully ===");
//
//        } catch (Exception e) {
//            System.err.println("Error running Spark Paimon example: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            spark.stop();
//        }
//    }
//}