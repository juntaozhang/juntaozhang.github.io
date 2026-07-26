# PostgreSQL JDBC Replication API Example

This is a Java example project based on the PostgreSQL JDBC Replication API, demonstrating 
how to use pgjdbc to directly connect to a PostgreSQL logical replication stream.


[DebeziumPgEmbedded.java](src/main/java/com/example/replication/DebeziumPgEmbedded.java) use debezium which flink cdc use it too.
test decimal.handling.mode, use precise default, config in `org.apache.flink.cdc.debezium.table.RowDataDebeziumDeserializeSchema`