package com.example.fluss;

/*
--fluss.bootstrap.servers
localhost:9123
--datalake.format
paimon
--datalake.paimon.metastore
filesystem
--datalake.paimon.warehouse
s3://warehouse/paimon
--datalake.paimon.s3.endpoint
localhost:32000
--datalake.paimon.s3.access-key
test
--datalake.paimon.s3.secret-key
11111111
--datalake.paimon.s3.connection.ssl.enabled
false
--datalake.paimon.s3.path.style.access
true
 */
public class FlussLakeTieringEntrypoint {
    public static void main(String[] args) throws Exception {
        org.apache.fluss.flink.tiering.FlussLakeTieringEntrypoint.main(args);
    }

}
