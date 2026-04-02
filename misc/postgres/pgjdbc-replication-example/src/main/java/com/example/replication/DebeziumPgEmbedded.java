package com.example.replication;

import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebeziumPgEmbedded {

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();

        // 引擎基础
        p.setProperty("name", "pg-cdc-embedded");
        p.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");

        // Offset 存储到本地文件（生产一般用 Kafka Connect 或别的外部存储）
        p.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        p.setProperty("offset.storage.file.filename", "postgres/data/offsets.dat");
        p.setProperty("offset.flush.interval.ms", "1000");

        // PostgreSQL 连接
        p.setProperty("database.hostname", "localhost");
        p.setProperty("database.port", "5432");
        p.setProperty("database.user", "postgres");
        p.setProperty("database.password", "postgres123");
        p.setProperty("database.dbname", "test");

        // 逻辑复制插件/发布与复制槽
        p.setProperty("plugin.name", "pgoutput");
        p.setProperty("slot.name", "debezium_slot_demo");
        p.setProperty("publication.name", "debezium_pub_demo");
        p.setProperty("publication.autocreate.mode", "filtered"); // filtered|all_tables|disabled


        // 监控范围与快照
        p.setProperty("table.include.list", "public.orders"); // 逗号分隔多个表
        p.setProperty("snapshot.mode", "initial");           // initial|initial_only|never|always

        // 其他可选优化
        p.setProperty("include.schema.changes", "false");
        p.setProperty("tombstones.on.delete", "false");
        p.setProperty("time.precision.mode", "adaptive");
        /*
        precise（默认）：用 Kafka Connect 的逻辑类型 org.apache.kafka.connect.data.Decimal 承载（底层是 BYTES + scale），完全无损。适合 Avro/Protobuf 等能识别 Decimal 逻辑类型的消费端；用 JSON 转换器时常会看到 Base64 字节表现。
        string：把小数当字符串输出，便于不支持 Decimal 逻辑类型或只会吃文本 JSON 的系统；缺点是失去数值语义（需要下游再显式转换）。PostgreSQL 的 NUMERIC 特殊值 NaN 只有在 string/double 模式下才可表示。
        double：转为 IEEE-754 双精度浮点，易于计算和展示，但有精度丢失风险；同样可表示 PostgreSQL 的 NaN
         */
        p.setProperty("decimal.handling.mode", "precise");

        p.setProperty("database.server.name", "pg"); // 1.x 必填，用作事件/主题前缀
        // p.setProperty("topic.prefix", "pg"); // 2.x topic 前缀（即使不用 Kafka，这个是必填用于命名变更来源）

        Configuration config = Configuration.from(p);

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine
                .create(Json.class)
                .using(config.asProperties())
                .notifying(record -> {
                    System.out.println("==[ " + record.destination() + " ]====================");
                    System.out.println(record.value());
                    System.out.println();
                })
                .build();

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.submit(engine);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                engine.close();
            } catch (Exception ignored) {
            }
            ex.shutdown();
        }));
        Thread.currentThread().join();
    }
}
