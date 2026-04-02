package com.example.orc;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * ORC Writer Example Demonstrates how to write ORC files with nested schema using vectorized batch
 * writing
 */
public class OrcWriterExample {

    public static void main(String[] args) throws IOException {
        String outputPath = args.length > 0 ? args[0] : "output.orc";

        Configuration conf = new Configuration();

        // Define schema using TypeDescription string
        TypeDescription schema =
                TypeDescription.fromString(
                        "struct<"
                                + "event_id:bigint,"
                                + "event_type:string,"
                                + "timestamp:bigint,"
                                + "user_id:string,"
                                + "session_id:string,"
                                + "location:struct<country:string,city:string,region:string>,"
                                + "tags:array<string>,"
                                + "value:double,"
                                + "count:bigint"
                                + ">");

        System.out.println("Writing ORC file to: " + outputPath);
        System.out.println("Schema: " + schema);

        // Create ORC writer with options and stripe size configuration
        Writer writer =
                OrcFile.createWriter(
                        new Path(outputPath),
                        OrcFile.writerOptions(conf)
                                .setSchema(schema)
                                .stripeSize(4 * 1024 * 1024) // 4MB stripe size (default is 64MB)
                                .bufferSize(262144) // 256KB buffer size
                                .compress(org.apache.orc.CompressionKind.SNAPPY)
                );

        // Create batch for vectorized writing
        VectorizedRowBatch batch = schema.createRowBatch();
        final int BATCH_SIZE = batch.getMaxSize();

        // Get column vectors
        LongColumnVector eventIdVector = (LongColumnVector) batch.cols[0];
        BytesColumnVector eventTypeVector = (BytesColumnVector) batch.cols[1];
        LongColumnVector timestampVector = (LongColumnVector) batch.cols[2];
        BytesColumnVector userIdVector = (BytesColumnVector) batch.cols[3];
        BytesColumnVector sessionIdVector = (BytesColumnVector) batch.cols[4];

        // Nested struct: location
        StructColumnVector locationVector = (StructColumnVector) batch.cols[5];
        BytesColumnVector countryVector = (BytesColumnVector) locationVector.fields[0];
        BytesColumnVector cityVector = (BytesColumnVector) locationVector.fields[1];
        BytesColumnVector regionVector = (BytesColumnVector) locationVector.fields[2];

        // List: tags
        ListColumnVector tagsVector = (ListColumnVector) batch.cols[6];
        BytesColumnVector tagsElementVector = (BytesColumnVector) tagsVector.child;

        // Optional fields
        DoubleColumnVector valueVector = (DoubleColumnVector) batch.cols[7];
        LongColumnVector countVector = (LongColumnVector) batch.cols[8];

        Random random = new Random();
        int numRecords = 100000;

        System.out.println("Writing " + numRecords + " records...");

        int recordCount = 0;
        while (recordCount < numRecords) {
            int rowsInBatch = Math.min(BATCH_SIZE, numRecords - recordCount);
            for (int row = 0; row < rowsInBatch; row++) {
                int batchRow = batch.size++;
                // Basic fields
                eventIdVector.vector[batchRow] = recordCount;
                eventIdVector.isNull[batchRow] = false;

                String eventType = getRandomEventType(random);
                byte[] eventTypeBytes = eventType.getBytes(StandardCharsets.UTF_8);
                eventTypeVector.setVal(batchRow, eventTypeBytes, 0, eventTypeBytes.length);
                eventTypeVector.isNull[batchRow] = false;

                timestampVector.vector[batchRow] = System.currentTimeMillis() - random.nextInt(86400000);
                timestampVector.isNull[batchRow] = false;

                String userId = "user_" + (recordCount / 1000 + random.nextInt(10));
                byte[] userIdBytes = userId.getBytes(StandardCharsets.UTF_8);
                userIdVector.setVal(batchRow, userIdBytes, 0, userIdBytes.length);
                userIdVector.isNull[batchRow] = false;

                String sessionId = generateSessionId(recordCount, random);
                byte[] sessionIdBytes = sessionId.getBytes(StandardCharsets.UTF_8);
                sessionIdVector.setVal(batchRow, sessionIdBytes, 0, sessionIdBytes.length);
                sessionIdVector.isNull[batchRow] = false;

                // Nested location struct
                locationVector.isNull[batchRow] = false;

                String country = getRandomCountry(random);
                byte[] countryBytes = country.getBytes(StandardCharsets.UTF_8);
                countryVector.setVal(batchRow, countryBytes, 0, countryBytes.length);
                countryVector.isNull[batchRow] = false;

                String city = getRandomCity(random);
                byte[] cityBytes = city.getBytes(StandardCharsets.UTF_8);
                cityVector.setVal(batchRow, cityBytes, 0, cityBytes.length);
                cityVector.isNull[batchRow] = false;

                if (random.nextFloat() > 0.3) {
                    String region = "Region-" + recordCount / 10000;
                    byte[] regionBytes = region.getBytes(StandardCharsets.UTF_8);
                    regionVector.setVal(batchRow, regionBytes, 0, regionBytes.length);
                    regionVector.isNull[batchRow] = false;
                } else {
                    regionVector.noNulls = false;
                    regionVector.isNull[batchRow] = true;
                }

                // Array of tags
                int numTags = 5;
                int start = tagsVector.childCount;
                tagsVector.offsets[batchRow] = start;
                tagsVector.lengths[batchRow] = numTags;
                tagsVector.isNull[batchRow] = false;
                tagsVector.childCount += numTags;

                // Ensure tags array is large enough
                tagsElementVector.ensureSize(rowsInBatch * numTags, false);

                for (int tagIdx = 0; tagIdx < numTags; tagIdx++) {
                    int childPos = start + tagIdx;
                    String tag = "tag_" + random.nextInt(100);
                    byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
                    tagsElementVector.setVal(childPos, tagBytes, 0, tagBytes.length);
                    tagsElementVector.isNull[childPos] = false;
                }

                // Optional fields
                if (random.nextFloat() > 0.2) {
                    valueVector.vector[batchRow] = random.nextDouble() * 100;
                    valueVector.isNull[batchRow] = false;
                } else {
                    valueVector.noNulls = false;
                    valueVector.isNull[batchRow] = true;
                }

                if (random.nextFloat() > 0.5) {
                    countVector.vector[batchRow] = 1 + random.nextInt(1000);
                    countVector.isNull[batchRow] = false;
                } else {
                    countVector.noNulls = false;
                    countVector.isNull[batchRow] = true;
                }

                recordCount++;

                // Write batch when full
                if (batch.size == BATCH_SIZE) {
                    writer.addRowBatch(batch);
                    batch.reset();
                    tagsVector.childCount = 0;
                }

                if (recordCount % 1000 == 0) {
                    System.out.println("Written " + recordCount + " records");
                }
            }
        }

        // Write remaining records
        if (batch.size != 0) {
            writer.addRowBatch(batch);
        }

        writer.close();
        System.out.println("Total written: " + numRecords + " records");
        System.out.println("ORC file created successfully!");
    }

    private static String getRandomEventType(Random random) {
        String[] types = {"page_view", "click", "purchase", "signup", "login", "logout"};
        return types[random.nextInt(types.length)];
    }

    private static String getRandomCountry(Random random) {
        String[] countries = {"US", "CN", "UK", "DE", "FR", "JP", "CA", "AU"};
        return countries[random.nextInt(countries.length)];
    }

    private static String getRandomCity(Random random) {
        String[] cities = {
                "New York", "Beijing", "London", "Berlin", "Paris", "Tokyo", "Toronto", "Sydney"
        };
        return cities[random.nextInt(cities.length)];
    }

    /**
     * Generate a unique, high-cardinality session_id Length: 5-10 characters Format: Mix of uppercase
     * letters and numbers to ensure high cardinality
     */
    private static String generateSessionId(int index, Random random) {
        // Generate random length between 5-10
        int length = 5 + random.nextInt(6); // 5, 6, 7, 8, 9, or 10
        return RandomStringUtils.randomAlphanumeric(length).toLowerCase();
    }
}
