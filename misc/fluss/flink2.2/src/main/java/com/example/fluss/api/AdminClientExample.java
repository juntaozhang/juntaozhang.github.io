package com.example.fluss.api;

import org.apache.fluss.client.Connection;
import org.apache.fluss.client.ConnectionFactory;
import org.apache.fluss.client.admin.Admin;
import org.apache.fluss.client.admin.KvSnapshotLease;
import org.apache.fluss.client.metadata.AcquireKvSnapshotLeaseResult;
import org.apache.fluss.config.ConfigOptions;
import org.apache.fluss.config.Configuration;
import org.apache.fluss.metadata.TableBucket;
import org.apache.fluss.metadata.TableInfo;
import org.apache.fluss.metadata.TablePath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AdminClientExample {
    public static void main(String[] args) throws Exception {
        Map<String, String> flussConfigs = new HashMap<>();
        flussConfigs.put(ConfigOptions.BOOTSTRAP_SERVERS.key(), "localhost:9123");
        Admin admin;
        try (Connection connection = ConnectionFactory.createConnection(Configuration.fromMap(flussConfigs))) {
            admin = connection.getAdmin();
            TablePath tablePath = TablePath.of("ods", "left_src");
            CompletableFuture<TableInfo> future = admin.getTableInfo(tablePath);
            TableInfo tableInfo = future.get();
            System.out.println("Table ID: " + tableInfo.getTableId());
            System.out.println("Bucket Count: " + tableInfo.getNumBuckets());
            System.out.println("Schema: " + tableInfo.getSchema());

            String leaseId = "MyJavaTest1";
            long leaseDurationMs = 1_000; // 10S

            KvSnapshotLease lease = admin.createKvSnapshotLease(leaseId, leaseDurationMs);
            System.out.println("Created lease: " + leaseId);
            Map<TableBucket, Long> snapshotIds = new HashMap<>();
            for (int bucketId = 0; bucketId < tableInfo.getNumBuckets(); bucketId++) {
                TableBucket bucket = new TableBucket(tableInfo.getTableId(), bucketId);
                snapshotIds.put(bucket, 0L);  // 0 表示获取最新快照
            }

            System.out.println("Acquiring snapshots...");
            CompletableFuture<AcquireKvSnapshotLeaseResult> acquireFuture = lease.acquireSnapshots(snapshotIds);
            AcquireKvSnapshotLeaseResult result = acquireFuture.get();

            if (result.getUnavailableSnapshots().isEmpty()) {
                System.out.println("Successfully acquired all snapshots");
            } else {
                System.out.println("Some snapshots are unavailable:");
                result.getUnavailableSnapshots().forEach((k, v) -> {
                    System.out.printf("Bucket [%s=>%d] is unavailable%n", k, v);
                });
                // TODO
            }

            System.out.println("Processing data using acquired snapshots...");
            // TODO: 实际的数据处理逻辑
            Thread.sleep(10_000);

            System.out.println("Releasing some snapshots...");
            Set<TableBucket> bucketsToRelease = new HashSet<>();
            bucketsToRelease.add(new TableBucket(tableInfo.getTableId(), 0));
            lease.releaseSnapshots(bucketsToRelease).get();
            System.out.println("Released bucket 0");

            System.out.println("Continuing with remaining snapshots...");
            // TODO
            Thread.sleep(8000);

            System.out.println("Dropping the lease...");
            lease.dropLease().get();
            System.out.println("Lease dropped successfully");

        }
    }
}
