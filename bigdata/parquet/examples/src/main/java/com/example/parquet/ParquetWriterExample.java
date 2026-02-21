package com.example.parquet;

import java.io.IOException;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.*;

/** Parquet Writer Example Demonstrates how to write Parquet files with nested schema */
public class ParquetWriterExample {

  public static void main(String[] args) throws IOException {
    String outputPath = args.length > 0 ? args[0] : "output.parquet";

    // Define schema: user events with nested data
    MessageType schema = parseSchema();

    Configuration conf = new Configuration();

    // Set schema in configuration for GroupWriteSupport
    GroupWriteSupport.setSchema(schema, conf);

    // Configure and create Parquet writer using try-with-resources
    // Note: ParquetWriter builder is not available in 1.16.0, using constructor
    // Page size must be >= 64 bytes for proper operation
    try (ParquetWriter<Group> writer =
        new ParquetWriter<>(
            new Path(outputPath),
            new GroupWriteSupport(),
            CompressionCodecName.SNAPPY,
            1024 * 1024,  // row group size (256 KB)
            64 * 1024,   // page size (64 KB) - minimum is 64 bytes
            64 * 1024,   // dictionary page size (64 KB)
            true,        // enable dictionary
            false,       // enable validation
            ParquetProperties.WriterVersion.PARQUET_2_0,
            conf)) {

      System.out.println("Writing Parquet file to: " + outputPath);

      // Write sample records
      Random random = new Random();
      int numRecords = 100000;

      for (int i = 0; i < numRecords; i++) {
        Group record = createRecord(schema, i, random);
        writer.write(record);

        if ((i + 1) % 1000 == 0) {
          System.out.println("Written " + (i + 1) + " records");
        }
      }

      System.out.println("Total written: " + numRecords + " records");
    }

    System.out.println("Parquet file created successfully!");
  }

  /** Define Parquet schema with nested structure */
  private static MessageType parseSchema() {
    return Types.buildMessage()
        .required(PrimitiveType.PrimitiveTypeName.INT64).as(OriginalType.INT_64).named("event_id")
        .required(PrimitiveType.PrimitiveTypeName.BINARY).named("event_type")
        .required(PrimitiveType.PrimitiveTypeName.INT64).as(OriginalType.TIMESTAMP_MILLIS).named("timestamp")
        .required(PrimitiveType.PrimitiveTypeName.BINARY).named("user_id")
        .required(PrimitiveType.PrimitiveTypeName.BINARY).named("session_id")
        .requiredGroup()
            .required(PrimitiveType.PrimitiveTypeName.BINARY).named("country")
            .required(PrimitiveType.PrimitiveTypeName.BINARY).named("city")
            .optional(PrimitiveType.PrimitiveTypeName.BINARY).named("region")
        .named("location")
        .requiredGroup()
            .repeated(PrimitiveType.PrimitiveTypeName.BINARY).named("tag")
        .named("tags")
        .optional(PrimitiveType.PrimitiveTypeName.DOUBLE).named("value")
        .optional(PrimitiveType.PrimitiveTypeName.INT32).named("count")
        .named("UserEvent");
  }

  /** Create a sample record */
  private static Group createRecord(MessageType schema, int index, Random random) {
    Group group = new SimpleGroup(schema);

    // Basic fields
    group.append("event_id", (long) index);
    group.append("event_type", getRandomEventType(random));
    group.append(
        "timestamp",
        System.currentTimeMillis() - random.nextInt(86400000)); // Random time in last 24h
    group.append("user_id", "user_" + (1 + random.nextInt(1000)));
    group.append("session_id", generateSessionId(index, random));

    // Nested location
    Group location = group.addGroup("location");
    location.append("country", getRandomCountry(random));
    location.append("city", getRandomCity(random));
    if (random.nextFloat() > 0.3) {
      location.append("region", "Region-" + random.nextInt(10));
    }

    // Array of tags
    Group tags = group.addGroup("tags");
    int numTags = 1 + random.nextInt(4);
    for (int i = 0; i < numTags; i++) {
      tags.append("tag", "tag_" + random.nextInt(100));
    }

    // Optional fields
    if (random.nextFloat() > 0.5) {
      group.append("value", random.nextDouble() * 100);
    }
    if (random.nextFloat() > 0.5) {
      group.append("count", random.nextInt(1000));
    }

    return group;
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

    return RandomStringUtils.secure().nextAlphanumeric(length).toLowerCase();
  }
}
