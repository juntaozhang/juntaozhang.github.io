package com.example.parquet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.filter2.predicate.FilterApi;
import org.apache.parquet.filter2.predicate.FilterPredicate;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.io.api.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.parquet.filter2.predicate.FilterApi.*;

/**
 * Filtered Parquet Reader Example
 * Demonstrates how to read Parquet files with filtering and projection
 */
public class ParquetReaderFilteredExample {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetReaderFilteredExample.class);

    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : "output.parquet";

        Configuration conf = new Configuration();

        // Enable vectorized reader
        conf.setBoolean("parquet.enable.vectorized.reader", true);
        conf.setBoolean("parquet.enable.dictionary", true);
        conf.setInt("parquet.vectorized.reader.batchsize", 128);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║       Filtered Parquet Reader Example                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n📁 File: " + inputPath);
        System.out.println("⚡ Vectorized reader: enabled");

        // Example 1: Filter by event_type
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("🔍 Example 1: Filter by event_type = 'purchase'");
        System.out.println("═".repeat(70));
        filterByEventType(conf, inputPath, "purchase");

        // Example 2: Filter by country
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("🌍 Example 2: Filter by country = 'CN'");
        System.out.println("═".repeat(70));
        filterByCountry(conf, inputPath, "CN");

        // Example 3: Filter by timestamp range (last 1 hour)
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("⏰ Example 3: Filter by timestamp (recent events)");
        System.out.println("═".repeat(70));
        filterByRecentTimestamp(conf, inputPath);

        // Example 4: Complex filter - country AND event_type
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("🎯 Example 4: Complex filter (country='US' AND event_type='click')");
        System.out.println("═".repeat(70));
        filterByCountryAndEventType(conf, inputPath, "US", "click");

        // Example 5: Filter with IN clause
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("📋 Example 5: Filter with multiple event types");
        System.out.println("═".repeat(70));
        filterByEventTypes(conf, inputPath, new String[]{"purchase", "signup"});

        // Example 6: Filter by event_id range (100 < id < 200)
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("🆔 Example 6: Filter by event_id range (100 < id < 200)");
        System.out.println("═".repeat(70));
        filterByEventIdRange(conf, inputPath, 100L, 200L);

        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("✅ All filtering examples completed!");
        System.out.println("═".repeat(70));
    }

    /**
     * Filter by single event type
     */
    private static void filterByEventType(Configuration conf, String inputPath, String eventType) throws IOException {
        LOG.info("Filtering by event_type: {}", eventType);

        // Create filter: event_type = 'purchase'
        FilterPredicate filter = FilterApi.eq(
                binaryColumn("event_type"),
                Binary.fromString(eventType)
        );

        readWithFilter(conf, inputPath, filter);
    }

    /**
     * Filter by country
     */
    private static void filterByCountry(Configuration conf, String inputPath, String country) throws IOException {
        LOG.info("Filtering by country: {}", country);

        // Create filter: location.country = 'CN'
        FilterPredicate filter = FilterApi.eq(
                binaryColumn("location.country"),
                Binary.fromString(country)
        );

        readWithFilter(conf, inputPath, filter);
    }

    /**
     * Filter by recent timestamp (last 1 hour)
     */
    private static void filterByRecentTimestamp(Configuration conf, String inputPath) throws IOException {
        long oneHourAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        LOG.info("Filtering by timestamp > {}", oneHourAgo);

        // Create filter: timestamp > oneHourAgo
        FilterPredicate filter = FilterApi.gt(
                longColumn("timestamp"),
                oneHourAgo
        );

        readWithFilter(conf, inputPath, filter);
    }

    /**
     * Complex filter: country AND event_type
     */
    private static void filterByCountryAndEventType(Configuration conf, String inputPath,
                                                    String country, String eventType) throws IOException {
        LOG.info("Filtering by country={} AND event_type={}", country, eventType);

        // Create filter: location.country = 'US' AND event_type = 'click'
        FilterPredicate filter = and(
                eq(binaryColumn("location.country"), Binary.fromString(country)),
                eq(binaryColumn("event_type"), Binary.fromString(eventType))
        );

        readWithFilter(conf, inputPath, filter);
    }

    /**
     * Filter with IN clause (multiple event types)
     */
    private static void filterByEventTypes(Configuration conf, String inputPath, String[] eventTypes) throws IOException {
        LOG.info("Filtering by event types: {}", String.join(", ", eventTypes));

        // Create filter: event_type IN ('purchase', 'signup')
        FilterPredicate filter = or(
                eq(binaryColumn("event_type"), Binary.fromString(eventTypes[0])),
                eq(binaryColumn("event_type"), Binary.fromString(eventTypes[1]))
        );

        readWithFilter(conf, inputPath, filter);
    }

    /**
     * Read Parquet file with filter
     */
    private static void readWithFilter(Configuration conf, String inputPath, FilterPredicate filter) throws IOException {
        LOG.debug("Applying filter: {}", filter);

        try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), new Path(inputPath))
                .withConf(conf)
                .withFilter(FilterCompat.get(filter))
                .build()) {

            Group record;
            int count = 0;
            long startTime = System.currentTimeMillis();

            System.out.println("\nReading filtered records...\n");

            while ((record = reader.read()) != null) {
                count++;

                // Print first 5 records
                if (count <= 5) {
                    printRecord(record, count);
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("\n─────────────────────────────────────────────────────────────");
            System.out.println("📊 Filter Statistics:");
            System.out.println("   Matched records: " + count);
            System.out.println("   Time taken: " + (endTime - startTime) + " ms");
            System.out.println("   Average time per record: " +
                    (count > 0 ? String.format("%.3f", (endTime - startTime) / (double) count) : "0") + " ms");

            LOG.info("Filter completed: {} matched records in {} ms", count, (endTime - startTime));
        }
    }

    /**
     * Print record details
     */
    private static void printRecord(Group record, int index) {
        System.out.println("Record #" + index + ":");
        System.out.println("  Event ID:     " + record.getLong("event_id", 0));
        System.out.println("  Event Type:   " + record.getString("event_type", 0));
        System.out.println("  User ID:      " + record.getString("user_id", 0));
        System.out.println("  Session ID:   " + record.getString("session_id", 0));
        System.out.println("  Timestamp:    " + record.getLong("timestamp", 0));

        if (record.getFieldRepetitionCount("location") > 0) {
            Group location = record.getGroup("location", 0);
            System.out.println("  Location:     " + location.getString("country", 0) +
                    ", " + location.getString("city", 0));
        }

        System.out.println();
    }

    /**
     * Filter by event_id range (min < id < max)
     */
    private static void filterByEventIdRange(Configuration conf, String inputPath, long minId, long maxId) throws IOException {
        LOG.info("Filtering by event_id: {} < id < {}", minId, maxId);

        // Create filter: event_id > 100 AND event_id < 200
        FilterPredicate filter = and(
                gt(longColumn("event_id"), minId),
                lt(longColumn("event_id"), maxId)
        );

        readWithFilter(conf, inputPath, filter);
    }
}
