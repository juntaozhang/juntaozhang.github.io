package com.example.replication;

import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * PostgreSQL Logical Replication Example using pgjdbc Replication API
 * <p>
 * This example demonstrates how to consume logical replication stream
 * from PostgreSQL using the built-in wal2json plugin.
 */
public class PostgreSQLReplicationExample {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLReplicationExample.class);

    // Database configuration
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres123";
    private static final String REPLICATION_SLOT_NAME = "pg_java_cdc_slot";

    // Replication configuration
    private static final String OUTPUT_PLUGIN = "wal2json";
    private static final int STATUS_INTERVAL_MS = 10000;

    public static void main(String[] args) throws SQLException {
        logger.info("Starting PostgreSQL Logical Replication Example");

        // Create replication connection
        Connection replicationConnection = createReplicationConnection();
        PGConnection pgConnection = replicationConnection.unwrap(PGConnection.class);

        try {
            // Create replication slot if not exists
            createReplicationSlotIfNotExists(pgConnection);

            // Start logical replication stream
            PGReplicationStream stream = createLogicalReplicationStream(pgConnection);

            // Consume replication stream
            consumeReplicationStream(stream);

        } finally {
            // Clean up
            if (!replicationConnection.isClosed()) {
                replicationConnection.close();
                logger.info("Replication connection closed");
            }
        }
    }

    /**
     * Create a replication connection with proper configuration
     */
    private static Connection createReplicationConnection() throws SQLException {
        logger.info("Creating replication connection to: {}", DB_URL);

        Properties props = new Properties();
        PGProperty.USER.set(props, DB_USER);
        PGProperty.PASSWORD.set(props, DB_PASSWORD);

        // Enable replication mode
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");

        Connection connection = DriverManager.getConnection(DB_URL, props);
        logger.info("Replication connection established successfully");

        return connection;
    }

    /**
     * Create logical replication slot if it doesn't exist
     */
    private static void createReplicationSlotIfNotExists(PGConnection pgConnection) throws SQLException {
        logger.info("Creating replication slot: {}", REPLICATION_SLOT_NAME);

        try {
            pgConnection
                    .getReplicationAPI()
                    .createReplicationSlot()
                    .logical()
                    .withSlotName(REPLICATION_SLOT_NAME)
                    .withOutputPlugin(OUTPUT_PLUGIN)
                    .make();

            logger.info("Replication slot '{}' created successfully", REPLICATION_SLOT_NAME);

        } catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                logger.info("Replication slot '{}' already exists", REPLICATION_SLOT_NAME);
            } else {
                logger.error("Failed to create replication slot", e);
                throw e;
            }
        }
    }

    /**
     * Create logical replication stream
     */
    private static PGReplicationStream createLogicalReplicationStream(PGConnection pgConnection) throws SQLException {
        logger.info("Creating logical replication stream");

        ChainedLogicalStreamBuilder streamBuilder = pgConnection
                .getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(REPLICATION_SLOT_NAME)
                .withSlotOption("include-xids", false)
                .withSlotOption("pretty-print", true)
                .withStatusInterval(STATUS_INTERVAL_MS, TimeUnit.MILLISECONDS);

        PGReplicationStream stream = streamBuilder.start();
        logger.info("Logical replication stream started successfully");

        return stream;
    }

    /**
     * Consume messages from replication stream
     */
    private static void consumeReplicationStream(PGReplicationStream stream) throws SQLException {
        logger.info("Starting to consume replication stream messages");

        try {
            while (true) {
                // Read message with timeout
                ByteBuffer msg = stream.readPending();

                if (msg == null) {
                    // No message available, wait and continue
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.info("Replication stream consumption interrupted");
                        break;
                    }
                    continue;
                }

                // Process the message
                processReplicationMessage(msg, stream);

                // Send feedback to PostgreSQL
                stream.setAppliedLSN(stream.getLastReceiveLSN());
                stream.setFlushedLSN(stream.getLastReceiveLSN());
            }

        } catch (SQLException e) {
            logger.error("Error consuming replication stream", e);
            throw e;
        }
    }

    /**
     * Process individual replication message
     */
    private static void processReplicationMessage(ByteBuffer message, PGReplicationStream stream) {
        // Get LSN information
        LogSequenceNumber lsn = stream.getLastReceiveLSN();

        // Convert message to string
        int offset = message.arrayOffset();
        byte[] source = message.array();
        int length = source.length - offset;
        String messageContent = new String(source, offset, length);

        // Log the message
        logger.info("Received replication message:");
        logger.info("  LSN: {}", lsn);
        logger.info("  Length: {} bytes", length);
        logger.info("  Content: {}", messageContent);
    }
}