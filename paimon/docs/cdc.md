

VM:
```text
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
```

Args:
```text
mysql_sync_table
--warehouse s3a://warehouse/paimon
--database ods
--table orders
--primary_keys id
--partition_keys user_id
--mysql_conf hostname=localhost
--mysql_conf username=root
--mysql_conf port=3307
--mysql_conf password=root123
--mysql_conf database-name=test
--mysql_conf table-name=orders
--mysql_conf server-time-zone=UTC
--table_conf bucket=1
--table_conf merge-engine=deduplicate
--table_conf changelog-producer=input
```