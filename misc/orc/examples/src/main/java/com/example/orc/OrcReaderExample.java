package com.example.orc;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.*;

/**
 * Vectorized ORC Reader Example
 * Demonstrates how to read ORC files efficiently using vectorized batch reading
 */
public class OrcReaderExample {

    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : "output.orc";

        Configuration conf = new Configuration();

        System.out.println("Reading ORC file: " + inputPath);

        // Create ORC reader
        Reader reader = OrcFile.createReader(
                new Path(inputPath),
                OrcFile.readerOptions(conf)
        );

        // Get file metadata
        System.out.println("\nFile Metadata:");
        System.out.println("=========================================");
        System.out.println("Schema: " + reader.getSchema());
        System.out.println("Number of rows: " + reader.getNumberOfRows());
        System.out.println("Compression: " + reader.getCompressionKind());
        System.out.println("Number of stripes: " + reader.getStripes().size());
        System.out.println("=========================================");

        // Create row batch for vectorized reading using the file's schema
        TypeDescription schema = reader.getSchema();
        VectorizedRowBatch batch = schema.createRowBatch();

        // Create record reader
        RecordReader rows = reader.rows(reader.options());

        int totalRecords = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("\nReading records...");
        System.out.println("=========================================");

        try {
            while (rows.nextBatch(batch)) {
                for (int row = 0; row < batch.size; ++row) {
                    totalRecords++;

                    // Print first 3 records for demonstration
                    if (totalRecords <= 3) {
                        System.out.println("\nRecord " + totalRecords + ":");
                        printRecord(batch, row);
                    }

                    // Print progress every 1000 records
                    if (totalRecords % 1000 == 0) {
                        System.out.println("Progress: Read " + totalRecords + " records");
                    }
                }
            }
        } finally {
            rows.close();
            reader.close();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n=========================================");
        System.out.println("Reading completed in " + (endTime - startTime) + " ms");
        System.out.println("Total records read: " + totalRecords);
        System.out.println("Average time per record: " +
                (totalRecords > 0 ? (endTime - startTime) / (double) totalRecords : 0) + " ms");
        System.out.println("Vectorized ORC reading completed successfully!");
    }

    /**
     * Print record details from vectorized batch
     */
    public static void printRecord(VectorizedRowBatch batch, int row) {
        // Access columns by index
        // 0: event_id (long)
        // 1: event_type (string)
        // 2: timestamp (long)
        // 3: user_id (string)
        // 4: session_id (string)
        // 5: location (struct)
        // 6: tags (list)
        // 7: value (double, optional)
        // 8: count (long, optional)

        // Basic columns
        LongColumnVector eventIdCol = (LongColumnVector) batch.cols[0];
        BytesColumnVector eventTypeCol = (BytesColumnVector) batch.cols[1];
        LongColumnVector timestampCol = (LongColumnVector) batch.cols[2];
        BytesColumnVector userIdCol = (BytesColumnVector) batch.cols[3];
        BytesColumnVector sessionIdCol = (BytesColumnVector) batch.cols[4];

        // Handle repeating values for basic columns
        int eventRow = eventIdCol.isRepeating ? 0 : row;
        int eventTypeRow = eventTypeCol.isRepeating ? 0 : row;
        int timestampRow = timestampCol.isRepeating ? 0 : row;
        int userIdRow = userIdCol.isRepeating ? 0 : row;
        int sessionIdRow = sessionIdCol.isRepeating ? 0 : row;

        long eventId = eventIdCol.vector[eventRow];
        String eventType = eventTypeCol.toString(eventTypeRow);
        long timestamp = timestampCol.vector[timestampRow];
        String userId = userIdCol.toString(userIdRow);
        String sessionId = sessionIdCol.toString(sessionIdRow);

        System.out.println("  Event ID: " + eventId);
        System.out.println("  Event Type: " + eventType);
        System.out.println("  Timestamp: " + timestamp);
        System.out.println("  User ID: " + userId);
        System.out.println("  Session ID: " + sessionId);

        // Location struct (column 5)
        if (batch.cols[5] != null) {
            ColumnVector locationCol = batch.cols[5];
            if (!locationCol.isNull[row]) {
                printLocation(locationCol, row);
            } else {
                System.out.println("  Location: null");
            }
        }

        // Tags list (column 6)
        if (batch.cols[6] != null) {
            ListColumnVector tagsCol = (ListColumnVector) batch.cols[6];
            if (!tagsCol.isNull[row]) {
                printTags(tagsCol, row);
            } else {
                System.out.println("  Tags: null");
            }
        }

        // Value (column 7, optional)
        if (batch.cols[7] != null) {
            DoubleColumnVector valueCol = (DoubleColumnVector) batch.cols[7];
            if (!valueCol.isNull[row]) {
                int valueRow = valueCol.isRepeating ? 0 : row;
                System.out.println("  Value: " + valueCol.vector[valueRow]);
            } else {
                System.out.println("  Value: null");
            }
        }

        // Count (column 8, optional)
        if (batch.cols[8] != null) {
            LongColumnVector countCol = (LongColumnVector) batch.cols[8];
            if (!countCol.isNull[row]) {
                int countRow = countCol.isRepeating ? 0 : row;
                System.out.println("  Count: " + countCol.vector[countRow]);
            } else {
                System.out.println("  Count: null");
            }
        }

        System.out.println();
    }

    /**
     * Print location struct fields
     */
    private static void printLocation(ColumnVector locationCol, int row) {
        // Location struct contains: country (string), city (string)
        StructColumnVector locationStruct = (StructColumnVector) locationCol;
        BytesColumnVector countryCol = (BytesColumnVector) locationStruct.fields[0];
        BytesColumnVector cityCol = (BytesColumnVector) locationStruct.fields[1];

        int locationRow = locationStruct.isRepeating ? 0 : row;
        int countryRow = countryCol.isRepeating ? 0 : locationRow;
        int cityRow = cityCol.isRepeating ? 0 : locationRow;

        String country = countryCol.toString(countryRow);
        String city = cityCol.toString(cityRow);

        System.out.println("  Location:");
        System.out.println("    Country: " + country);
        System.out.println("    City: " + city);
    }

    /**
     * Print tags list
     */
    private static void printTags(ListColumnVector tagsCol, int row) {
        int tagsRow = tagsCol.isRepeating ? 0 : row;
        int offset = (int) tagsCol.offsets[tagsRow];
        int length = (int) tagsCol.lengths[tagsRow];

        System.out.print("  Tags: [");
        BytesColumnVector tagsVector = (BytesColumnVector) tagsCol.child;

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            int tagIndex = offset + i;
            String tag = tagsVector.toString(tagIndex);
            System.out.print(tag);
        }
        System.out.println("]");
    }
}