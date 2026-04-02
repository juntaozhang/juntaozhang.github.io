package com.example.fluss;

import org.apache.fluss.client.Connection;
import org.apache.fluss.client.ConnectionFactory;
import org.apache.fluss.client.admin.Admin;
import org.apache.fluss.config.ConfigOptions;
import org.apache.fluss.config.Configuration;
import org.apache.fluss.metadata.TableInfo;
import org.apache.fluss.metadata.TablePath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AdminClientExample {
    public static void main(String[] args) throws Exception {
        Map<String, String> flussConfigs = new HashMap<>();
        flussConfigs.put(ConfigOptions.BOOTSTRAP_SERVERS.key(), "localhost:9123");
        Admin admin;
        try (Connection connection = ConnectionFactory.createConnection(Configuration.fromMap(flussConfigs))) {
            admin = connection.getAdmin();
            TablePath tablePath = TablePath.of("ods", "pk_table");
            CompletableFuture<TableInfo> future = admin.getTableInfo(tablePath);
            TableInfo tableInfo = future.get();
            System.out.println("Table ID: " + tableInfo.getTableId());
            System.out.println("Schema: " + tableInfo.getSchema());
        }
    }
}
