package com.example.fluss;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.fluss.flink.row.OperationType;
import org.apache.fluss.flink.row.RowWithOp;
import org.apache.fluss.flink.sink.FlussSink;
import org.apache.fluss.flink.sink.serializer.FlussSerializationSchema;
import org.apache.fluss.row.GenericRow;
import org.apache.fluss.types.RowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Random;

public class FlinkSinkExample {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkSinkExample.class);

    // Configuration - modify these values to match your environment
    private static final String FLUSS_BOOTSTRAP_SERVERS = "localhost:9123";
    private static final String DATABASE_NAME = "ods";
    private static final String TABLE_NAME = "pk_table";

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set(RestOptions.PORT, 8083);
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.enableCheckpointing(30000);
        env.setParallelism(2);

        // Create Table Environment
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

        tEnv.executeSql("CREATE CATALOG fluss_catalog WITH (\n" +
                "                  'type' = 'fluss',\n" +
                "                  'bootstrap.servers' = '" + FLUSS_BOOTSTRAP_SERVERS + "'\n" +
                "                )");
        tEnv.executeSql("USE CATALOG fluss_catalog");
        tEnv.executeSql("USE " + DATABASE_NAME);

        // Create a data stream with sample data matching pk_table structure
        DataStream<PkTableRecord> recordStream = env.addSource(new PkTableRecordSource())
                .name("PkTable Record Generator")
                .uid("pktable-generator");

        // Create the Fluss sink
        FlussSink<PkTableRecord> flussSink = FlussSink.<PkTableRecord>builder()
                .setBootstrapServers(FLUSS_BOOTSTRAP_SERVERS)
                .setDatabase(DATABASE_NAME)
                .setTable(TABLE_NAME)
                .setSerializationSchema(new PkTableRecordSerializationSchema())
                .build();

        // Add sink to the data stream
        recordStream.sinkTo(flussSink)
                .name("Fluss Sink")
                .uid("fluss-sink");

        // Execute the job
        LOG.info("Executing Flink job...");
        env.execute("Flink 1.20 - Fluss PkTable Sink Example");
    }

    private static class PkTableRecordSource implements SourceFunction<PkTableRecord> {
        private volatile boolean isRunning = true;
        private final Random random = new Random();

        @Override
        public void run(SourceContext<PkTableRecord> ctx) throws Exception {
            int counter = 0;
            while (isRunning) {
                long userId = 10L + counter;
                long shopId = counter % 10;
                int numOrders = random.nextInt(100) + 1;
                int totalAmount = random.nextInt(1000) + 1;

                PkTableRecord record = new PkTableRecord(shopId, userId, numOrders, totalAmount);

                // Emit the record
                ctx.collect(record);

                LOG.info("Generated record: shopId={}, userId={}, numOrders={}, totalAmount={}, date={}",
                        shopId, userId, numOrders, totalAmount, new Date());

                Thread.sleep(5000);

                // Stop after generating 100 records for demo
                counter++;
                if (counter >= 100) {
                    LOG.info("Generated {} records, stopping.", counter);
                    break;
                }
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    static class PkTableRecord {
        private Long shopId;
        private Long userId;
        private Integer numOrders;
        private Integer totalAmount;

        public PkTableRecord() {
        }

        public PkTableRecord(Long shopId, Long userId, Integer numOrders, Integer totalAmount) {
            this.shopId = shopId;
            this.userId = userId;
            this.numOrders = numOrders;
            this.totalAmount = totalAmount;
        }

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getNumOrders() {
            return numOrders;
        }

        public void setNumOrders(Integer numOrders) {
            this.numOrders = numOrders;
        }

        public Integer getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Integer totalAmount) {
            this.totalAmount = totalAmount;
        }

        @Override
        public String toString() {
            return "PkTableRecord{" +
                    "shopId=" + shopId +
                    ", userId=" + userId +
                    ", numOrders=" + numOrders +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }

    static class PkTableRecordSerializationSchema implements FlussSerializationSchema<PkTableRecord> {

        private static final long serialVersionUID = 1L;

        private RowType rowType;

        @Override
        public void open(InitializationContext context) throws Exception {
            // Get the target table schema
            this.rowType = context.getRowSchema();

            // Validate schema compatibility with PkTableRecord class
            if (rowType.getFieldCount() < 4) {
                throw new IllegalStateException(
                        "Schema must have at least 4 fields to serialize PkTableRecord objects: " +
                                "shopId (BIGINT), userId (BIGINT), numOrders (INT), totalAmount (INT)");
            }
        }

        @Override
        public RowWithOp serialize(PkTableRecord record) throws Exception {
            // Create a GenericRow with the data
            GenericRow row = new GenericRow(rowType.getFieldCount());

            // Field 0: shop_id (BIGINT) - Primary Key
            row.setField(0, record.getShopId());

            // Field 1: user_id (BIGINT) - Primary Key
            row.setField(1, record.getUserId());

            // Field 2: num_orders (INT)
            row.setField(2, record.getNumOrders());

            // Field 3: total_amount (INT)
            row.setField(3, record.getTotalAmount());

            // Set any additional fields to null
            for (int i = 4; i < rowType.getFieldCount(); i++) {
                row.setField(i, null);
            }

            // Use UPSERT operation for primary key table
            return new RowWithOp(row, OperationType.UPSERT);
        }
    }
}
