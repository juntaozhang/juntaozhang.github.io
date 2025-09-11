package com.example.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebeziumMySQLExample {

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();

        // 引擎基础
        p.setProperty("name", "mysql-cdc-embedded");
        p.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");

        // Offset 存储到本地文件（生产一般用 Kafka Connect 或别的外部存储）
        p.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        p.setProperty("offset.storage.file.filename", "mysql/data/offsets.dat");
        p.setProperty("offset.flush.interval.ms", "1000");

        p.setProperty("database.hostname", "localhost");
        p.setProperty("database.port", "3307");
        p.setProperty("database.user", "root");
        p.setProperty("database.password", "root123");
        p.setProperty("database.server.id", "1");
        p.setProperty("database.server.name", "mysql");

        // 监控范围与快照
        p.setProperty("table.include.list", "test.orders"); // 逗号分隔多个表
        p.setProperty("snapshot.mode", "initial");           // initial|initial_only|never|always

        /*
        precise（默认）：用 Kafka Connect 的逻辑类型 org.apache.kafka.connect.data.Decimal 承载（底层是 BYTES + scale），完全无损。适合 Avro/Protobuf 等能识别 Decimal 逻辑类型的消费端；用 JSON 转换器时常会看到 Base64 字节表现。
        string：把小数当字符串输出，便于不支持 Decimal 逻辑类型或只会吃文本 JSON 的系统；缺点是失去数值语义（需要下游再显式转换）。PostgreSQL 的 NUMERIC 特殊值 NaN 只有在 string/double 模式下才可表示。
        double：转为 IEEE-754 双精度浮点，易于计算和展示，但有精度丢失风险；同样可表示 PostgreSQL 的 NaN
         */
        p.setProperty("decimal.handling.mode", "precise");

        // 1.x
        p.setProperty("database.history", "io.debezium.relational.history.MemoryDatabaseHistory");
        p.setProperty("database.server.name", "mysql");

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine
                .create(Json.class)
                .using(p)
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
