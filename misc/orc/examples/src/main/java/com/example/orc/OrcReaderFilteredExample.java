package com.example.orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.hive.ql.io.sarg.PredicateLeaf;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgument;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgumentFactory;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.example.orc.OrcReaderExample.printRecord;

/**
 * Filtered ORC Reader Example
 * Demonstrates how to read ORC files with filtering
 */
public class OrcReaderFilteredExample {

    private static final Logger LOG = LoggerFactory.getLogger(OrcReaderFilteredExample.class);

    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : "output.orc";

        Configuration conf = new Configuration();

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║       Filtered ORC Reader Example                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n📁 File: " + inputPath);

        // Example 1: Filter by event_id range
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("🔢 Example 1: Filter by event_id range");
        System.out.println("═".repeat(70));
        filterByEventIdRange(conf, inputPath);

        // Example 2: Filter by user_id
        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("👤 Example 2: Filter by user_id");
        System.out.println("═".repeat(70));
        filterByUserId(conf, inputPath);

        System.out.println("\n\n" + "═".repeat(70));
        System.out.println("✅ All filtering examples completed!");
        System.out.println("═".repeat(70));
    }

    /**
     * Filter by event_id range using ORC predicate push-down (SearchArgument)
     * <p>
     * Predicate push-down allows ORC to skip entire stripes/row groups that don't match
     * the filter, significantly improving read performance.
     */
    private static void filterByEventIdRange(Configuration conf, String inputPath) throws IOException {
        // Define event_id range filter
        long minId = 200;
        long maxId = 203;
        LOG.info("Filtering by event_id: {} <= event_id <= {}", minId, maxId);

        // Build SearchArgument for predicate push-down
        SearchArgument argument = SearchArgumentFactory.newBuilder()
                .between("event_id", PredicateLeaf.Type.LONG, minId, maxId)
                .build();

        // Create ORC reader
        Reader reader = OrcFile.createReader(
                new Path(inputPath),
                OrcFile.readerOptions(conf));

        System.out.println("\nTotal rows in file: " + reader.getNumberOfRows());
        System.out.println("Filtering with SearchArgument (predicate push-down): " + minId + " <= event_id <= " + maxId);

        // Create row batch
        TypeDescription schema = reader.getSchema();
        VectorizedRowBatch batch = schema.createRowBatch();

        // Create record reader WITH SearchArgument for predicate push-down
        // This tells ORC to only read rows that match the predicate
        RecordReader rows = reader.rows(
                reader.options()
                        .searchArgument(argument, new String[]{"event_id"})
                        .useSelected(true)
                        .allowSARGToFilter(true));

        int totalRecords = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("\nReading filtered records (ORC handles filtering)...\n");

        try {
            while (rows.nextBatch(batch)) {
                for (int row = 0; batch.selectedInUse && row < batch.size; ++row) {
                    totalRecords++;

                    // Print first 5 records
                    if (totalRecords <= 5) {
                        printRecord(batch, batch.selected[row]);
                    }

                }
            }
        } finally {
            rows.close();
            reader.close();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n─────────────────────────────────────────────────────────────");
        System.out.println("📊 Filter Statistics (with Predicate Push-Down):");
        System.out.println("   Matched records: " + totalRecords);
        System.out.println("   Time taken: " + (endTime - startTime) + " ms");
        System.out.println("   Average time per record: " +
                (totalRecords > 0 ? String.format("%.3f", (endTime - startTime) / (double) totalRecords) : "0") + " ms");
        System.out.println("   Note: ORC skipped stripes/row groups that don't match predicate");

        LOG.info("Filter completed: {} matched records in {} ms", totalRecords, (endTime - startTime));
    }

    /**
     * Filter by specific user_id using ORC predicate push-down (SearchArgument)
     * <p>
     * Predicate push-down allows ORC to skip entire stripes/row groups that don't match
     * the filter, significantly improving read performance.
     */
    private static void filterByUserId(Configuration conf, String inputPath) throws IOException {
        // Define user_id to filter
        String targetUserId = "user_0";
        LOG.info("Filtering by user_id: {}", targetUserId);

        // Build SearchArgument for predicate push-down with string equality
        SearchArgument argument = SearchArgumentFactory.newBuilder()
                .equals("user_id", PredicateLeaf.Type.STRING, targetUserId)
                .build();

        // Create ORC reader
        Reader reader = OrcFile.createReader(
                new Path(inputPath),
                OrcFile.readerOptions(conf)
        );

        System.out.println("\nTotal rows in file: " + reader.getNumberOfRows());
        System.out.println("Filtering with SearchArgument (predicate push-down): user_id = " + targetUserId);

        // Create row batch
        TypeDescription schema = reader.getSchema();
        VectorizedRowBatch batch = schema.createRowBatch();

        // Create record reader WITH SearchArgument for predicate push-down
        // This tells ORC to only read rows that match the predicate
        RecordReader rows = reader.rows(
                reader.options()
                        .searchArgument(argument, new String[]{"user_id"})
                        .useSelected(true)
                        .allowSARGToFilter(true)
        );

        int totalRecords = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("\nReading filtered records (ORC handles filtering)...\n");

        try {
            while (rows.nextBatch(batch)) {
                for (int row = 0; batch.selectedInUse && row < batch.size; ++row) {
                    totalRecords++;

                    // Print first 5 records
                    if (totalRecords <= 5) {
                        printRecord(batch, batch.selected[row]);
                    }
                }
            }
        } finally {
            rows.close();
            reader.close();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n─────────────────────────────────────────────────────────────");
        System.out.println("📊 Filter Statistics (with Predicate Push-Down):");
        System.out.println("   Matched records: " + totalRecords);
        System.out.println("   Time taken: " + (endTime - startTime) + " ms");
        System.out.println("   Average time per record: " +
                (totalRecords > 0 ? String.format("%.3f", (endTime - startTime) / (double) totalRecords) : "0") + " ms");
        System.out.println("   Note: ORC skipped stripes/row groups that don't match predicate");

        LOG.info("Filter completed: {} matched records in {} ms", totalRecords, (endTime - startTime));
    }
}