package com.example.paimon;

import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Decimal;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;

import java.math.BigDecimal;

public class DecimalExample {
    public static void main(String[] args) {
        int scale = 2;
        Schema decSchema = Decimal.builder(scale).build();
        System.out.println(decSchema);
        BigDecimal v = new BigDecimal("123.45");

        // 底层字节：unscaled = 12345 -> 0x30 0x39
        byte[] raw = Decimal.fromLogical(decSchema, v);

        // 还原为 BigDecimal（按 scale=2）
        BigDecimal roundtrip = Decimal.toLogical(decSchema, raw);

        // roundtrip == 123.45
        System.out.println(roundtrip);
    }
}
