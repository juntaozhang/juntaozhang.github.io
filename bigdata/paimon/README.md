# Paimon
## Setup
- [local](docs/setup-local.md)
- [flink](docs/setup-flink.md)
- [spark](docs/setup-spark.md)

## Paimon 1.4
- [data distribution: bucket](docs/bucket.md)
- [Compact](docs/compact.md)
  - [files-conflict: dedicated compaction job separate write and compact](https://paimon.apache.org/docs/master/concepts/concurrency-control/#files-conflict), set 'write-only' to true
    - [FlinkDeduplicateExample.java](flink1.20/src/main/java/com/example/paimon/FlinkDeduplicateExample.java)
  - [Deletion Vectors](docs/deletion-vectors.md)
- [Merge Engine](docs/merge.md)
- [Changelog](docs/changelog.md)
- row-tracking
  - [FlinkRowTrackingExample.java](flink1.20/src/main/java/com/example/paimon/FlinkRowTrackingExample.java)

## TODO
- dedup updata/firstrow
- changelog
- lookup compaction
- Chain Table
- example of [RESTCatalog](https://paimon.apache.org/docs/master/concepts/rest/overview/)
- `CALL sys.compact_manifest(table => 'order_fact')`
- `CALL sys.expire_snapshots(table => 'orders', retain_max=>3, retain_min=>2)`

<details>
<summary>data evolution support update and compact</summary>

- [data-evolution.md](docs/data-evolution.md)

TODO, 需要看一下源码，原理是什么，为什么没有支持？\
直觉上Data Evolution 与 支持 compaction 不应该冲突啊？
</details>
<details>
<summary>test</summary>

DynamicBucketTableTest
```
  test(s"mytest") {
    sql(s"""
           |CREATE TABLE T (
           |  pk INT,
           |  v INT,
           |  pt INT
           |) TBLPROPERTIES (
           |  'primary-key' = 'pk',
           |  'bucket' = '1',
           |  'deletion-vectors.enabled' = 'true'
           |)
           |""".stripMargin)

    sql("INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1)")
    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
    sql("DELETE FROM T WHERE pk > 2")
    sql("CALL sys.compact(`table` => 'T')")
    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
//    sql("call sys.compact(`table` => 'T')")
    sql("select * from T").show()
//    sql("INSERT INTO T VALUES (3, 30, 1)")
//    sql("INSERT INTO T VALUES (4, 40, 1)")
//    sql("CALL sys.compact(`table` => 'T', compact_strategy => 'minor')")
//    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
//    sql("INSERT INTO T VALUES (5, 50, 1)")
//    sql("INSERT INTO T VALUES (6, 60, 1)")
//    sql("CALL sys.compact(`table` => 'T', compact_strategy => 'minor')")
//    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
//    sql("DELETE FROM T WHERE pk = 1")
//    sql("CALL sys.compact(`table` => 'T', compact_strategy => 'minor')")
//    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
//    sql("INSERT INTO T VALUES (7, 70, 1)")
//    sql("INSERT INTO T VALUES (8, 80, 1)")
//    sql("INSERT INTO T VALUES (9, 90, 1)")
//    sql("CALL sys.compact(`table` => 'T', compact_strategy => 'minor')")
//    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)

//    val table = loadTable("T")
//    val dvMaintainerFactory =
//      BucketedDvMaintainer.factory(table.store().newIndexFileHandler())
//    getLatestDeletionVectors(table, dvMaintainerFactory, Seq(BinaryRow.EMPTY_ROW))
//
//    val deletionVectors1 = getAllLatestDeletionVectors(table, dvMaintainerFactory)
//    Assertions.assertEquals(1, deletionVectors1.size)
    //    sql("call sys.compact(`table` => 'T', )")
//    sql("SELECT * FROM `T$snapshots`").show(false)

//    sql("SELECT * FROM `T$files` ORDER BY level DESC").show(false)
  }

```

PostponeBucketTableITCase
```text
    @Test
    public void mytest() throws Exception {
        String warehouse = getTempDirPath();
        TableEnvironment tEnv =
                tableEnvironmentBuilder()
                        .batchMode()
                        .setConf(TableConfigOptions.TABLE_DML_SYNC, true)
                        .build();

        tEnv.executeSql(
                "CREATE CATALOG mycat WITH (\n"
                        + "  'type' = 'paimon',\n"
                        + "  'warehouse' = '"
                        + warehouse
                        + "'\n"
                        + ")");
        tEnv.executeSql("USE CATALOG mycat");
        tEnv.executeSql(
                "CREATE TABLE T (\n"
                        + "  k INT,\n"
                        + "  v INT,\n"
                        + "  pt INT,\n"
                        + "  PRIMARY KEY (k) NOT ENFORCED\n"
                        + ") PARTITIONED BY (pt) WITH (\n"
                        + "  'bucket' = '2',\n"
                        + "  'merge-engine' = 'deduplicate'\n"
                        + ")");
        tEnv.executeSql("INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 2), (4, 40, 2)").await();
        tEnv.executeSql("INSERT INTO T VALUES (1, 11, 2)").await();

        assertThat(collect(tEnv.executeSql("SELECT k, v, pt FROM T")))
                .containsExactlyInAnyOrder(
                        "+I[1, 11, 2]", "+I[2, 20, 1]", "+I[3, 30, 2]", "+I[4, 40, 2]");
    }

    @Test
    public void testPostponeBucketPKWithoutPartition() throws Exception {
        String warehouse = getTempDirPath();
        TableEnvironment tEnv =
                tableEnvironmentBuilder()
                        .batchMode()
                        .setConf(TableConfigOptions.TABLE_DML_SYNC, true)
                        .build();

        tEnv.executeSql(
                "CREATE CATALOG mycat WITH (\n"
                        + "  'type' = 'paimon',\n"
                        + "  'warehouse' = '"
                        + warehouse
                        + "'\n"
                        + ")");
        tEnv.executeSql("USE CATALOG mycat");
        tEnv.executeSql(
                "CREATE TABLE T (\n"
                        + "  pk INT,\n"
                        + "  v INT,\n"
                        + "  pt INT,\n"
                        + "  PRIMARY KEY (pk) NOT ENFORCED\n"
                        + ") WITH (\n"
                        + "  'bucket' = '1',\n"
                        + "  'deletion-vectors.enabled' = 'true'\n"
                        + ")");
        tEnv.executeSql("INSERT INTO T VALUES (1, 10, 1), (2, 20, 1), (3, 30, 1), (4, 40, 1)").await();
        tEnv.executeSql("DELETE FROM T WHERE pk = 1").await();
        TableResult result = tEnv.executeSql("SELECT * FROM T$files ORDER BY level DESC");
        System.out.println(String.join(", ", result.getResolvedSchema().getColumnNames()));
        collect(result).forEach(System.out::println);
    }

```
</details>


## Flink 1.17 with Paimon 1.3
- [Kubernetes Setup](flink1.17/docs/k8s-setup.md)
- [Primary Key Table](flink1.17/docs/primary-key-table.md)
- [CDC MySQL/PostgreSQL](flink1.17/docs/cdc-mysql_pg.md)
    - [CDC Source](flink1.17/docs/cdc.md)
- [Changelog](flink1.17/docs/changelog.md)
- [Row Tracking](flink1.17/docs/row-tracking.md)