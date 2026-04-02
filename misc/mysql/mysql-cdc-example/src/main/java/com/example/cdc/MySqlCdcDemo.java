package com.example.cdc;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class MySqlCdcDemo {
    private static final Logger logger = LoggerFactory.getLogger(MySqlCdcDemo.class);

    private String host;
    private int port;
    private String username;
    private String password;

    public MySqlCdcDemo() {
        loadConfiguration();
    }

    public static void main(String[] args) {
        MySqlCdcDemo demo = new MySqlCdcDemo();
        try {
            demo.startCdcListener();
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error running MySQL CDC demo", e);
            System.exit(1);
        }
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.warn("Failed to load application.properties, using default values", e);
        }

        this.host = props.getProperty("mysql.host", "localhost");
        this.port = Integer.parseInt(props.getProperty("mysql.port", "3307"));
        this.username = props.getProperty("mysql.username", "test");
        this.password = props.getProperty("mysql.password", "test12345");
    }

    public void startCdcListener() throws IOException {
        logger.info("Starting MySQL CDC listener...");
        logger.info("Connecting to MySQL: {}:{}", host, port);
        BinaryLogClient client = new BinaryLogClient(host, port, username, password);
        client.setServerId(1);
        client.registerEventListener(event -> {
            EventData data = event.getData();
            EventHeader header = event.getHeader();

            if (data instanceof TableMapEventData tableMapData) {
                logger.info("Table Map Event: database={}, table={}, tableId={}",
                        tableMapData.getDatabase(), tableMapData.getTable(), tableMapData.getTableId());
            } else if (data instanceof WriteRowsEventData) {
                handleInsertEvent((WriteRowsEventData) data, header);
            } else if (data instanceof UpdateRowsEventData) {
                handleUpdateEvent((UpdateRowsEventData) data, header);
            } else if (data instanceof DeleteRowsEventData) {
                handleDeleteEvent((DeleteRowsEventData) data, header);
            } else if (data instanceof QueryEventData) {
                logger.info("{}", data);
            }
        });

        client.connect();
        logger.info("MySQL CDC listener started successfully!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Shutting down MySQL CDC listener...");
                client.disconnect();
            } catch (IOException e) {
                logger.error("Error during shutdown", e);
            }
        }));
    }

    private void handleInsertEvent(WriteRowsEventData data, EventHeader header) {
        logger.info("=== INSERT Event ===");
        logger.info("Timestamp: {}", header.getTimestamp());
        logger.info("Table ID: {}", data.getTableId());
        logger.info("Included Columns: {}", data.getIncludedColumns());

        for (Serializable[] row : data.getRows()) {
            logger.info("Inserted Row: {}", formatRow(row));
        }
    }

    private void handleUpdateEvent(UpdateRowsEventData data, EventHeader header) {
        logger.info("=== UPDATE Event ===");
        logger.info("Timestamp: {}", header.getTimestamp());
        logger.info("Table ID: {}", data.getTableId());
        logger.info("Included Columns (Before): {}", data.getIncludedColumnsBeforeUpdate());
        logger.info("Included Columns (After): {}", data.getIncludedColumns());

        for (int i = 0; i < data.getRows().size(); i++) {
            logger.info("Before: {}", formatRow(data.getRows().get(i).getKey()));
            logger.info("After:  {}", formatRow(data.getRows().get(i).getValue()));
        }
    }

    private void handleDeleteEvent(DeleteRowsEventData data, EventHeader header) {
        logger.info("=== DELETE Event ===");
        logger.info("Timestamp: {}", header.getTimestamp());
        logger.info("Table ID: {}", data.getTableId());
        logger.info("Included Columns: {}", data.getIncludedColumns());

        for (Serializable[] row : data.getRows()) {
            logger.info("Deleted Row: {}", formatRow(row));
        }
    }

    private String formatRow(Serializable[] row) {
        if (row == null) return "null";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(row[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}