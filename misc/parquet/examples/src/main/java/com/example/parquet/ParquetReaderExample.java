package com.example.parquet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;

import java.io.IOException;

/**
 * Vectorized Parquet Reader Example
 * Demonstrates how to read Parquet files efficiently
 */
public class ParquetReaderExample {

    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : "output.parquet";

        Configuration conf = new Configuration();

        // Enable vectorized reader
        conf.setBoolean("parquet.enable.vectorized.reader", true);
        conf.setBoolean("parquet.enable.dictionary", true);

        // Set batch size for vectorized reading
        conf.setInt("parquet.vectorized.reader.batchsize", 128);

        System.out.println("Reading Parquet file: " + inputPath);
        System.out.println("Vectorized reader enabled: " +
                conf.getBoolean("parquet.enable.vectorized.reader", false));

        // Create Parquet reader
        try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), new Path(inputPath))
                .withConf(conf)
                .build()) {

            Group record;
            int totalRecords = 0;
            long startTime = System.currentTimeMillis();

            System.out.println("\nReading records...");
            System.out.println("=========================================");

            while ((record = reader.read()) != null) {
                totalRecords++;

                // Print first 3 records for demonstration
                if (totalRecords <= 3) {
                    System.out.println("\nRecord " + totalRecords + ":");
                    System.out.println("  Event ID: " + record.getLong("event_id", 0));
                    System.out.println("  Event Type: " + record.getString("event_type", 0));
                    System.out.println("  User ID: " + record.getString("user_id", 0));
                    System.out.println("  Session ID: " + record.getString("session_id", 0));

                    if (record.getFieldRepetitionCount("location") > 0) {
                        Group location = record.getGroup("location", 0);
                        System.out.println("  Location: " + location.getString("country", 0) +
                                ", " + location.getString("city", 0));
                    }
                }

                // Print progress every 32 records (8 times for 256 records)
                if (totalRecords % 32 == 0) {
                    System.out.println("Progress: Read " + totalRecords + " records");
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("\n=========================================");
            System.out.println("Reading completed in " + (endTime - startTime) + " ms");
            System.out.println("Total records read: " + totalRecords);
            System.out.println("Average time per record: " +
                    (totalRecords > 0 ? (endTime - startTime) / (double) totalRecords : 0) + " ms");
            System.out.println("Vectorized reading completed successfully!");
        }
    }
}