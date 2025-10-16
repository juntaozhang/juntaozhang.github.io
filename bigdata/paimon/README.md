# paimon

[Flink1.17 Paimon on Kubernetes](flink1.17-paimon.md)

[Paimon Changelog and Audit Log](flink1.17-paimon-changelog-audit-demo.md)

[Flink1.17 Paimon Primary Key Table](flink1.17-paimon-primary-key-guide.md)

[Flink1.17 Paimon CDC](flink1.17_paimon_cdc.md)

[flink1.17-paimon1.3-example](flink1.17-paimon1.3-example)

[flink Action debug](pr-6239/postgres-decimal-type-not-support.md)

TODO:

- how does [Dedicated Compaction Job](https://paimon.apache.org/docs/master/maintenance/dedicated-compaction/#dedicated-compaction-job) work, separate write and compact
- deepdive in `CALL sys.compact_manifest(table => 'order_fact')`
-  lightweight implementation example of [RESTCatalog](https://paimon.apache.org/docs/master/concepts/rest/overview/)
- `CALL sys.expire_snapshots(table => 'orders', retain_max=>3, retain_min=>2)`
- compact return false when compact not needed


https://paimon.apache.org/docs/master/append-table/streaming/#bucketed-append 中的
'precommit-compact' = 'false'


'sink.rolling-policy.file-size' = '1MB',
'sink.rolling-policy.rollover-interval' = '1 min',
'sink.rolling-policy.check-interval' = '10 s'

Deletion Vectors index 怎么生效
https://paimon.apache.org/docs/master/concepts/spec/tableindex/#deletion-vectors


测试KEY_DYNAMIC 模式，order_id index 是否可以存在多个partition
https://paimon.apache.org/docs/master/primary-key-table/data-distribution/#cross-partitions-upsert


- testPartialUpdateRemoveRecordOnSequenceGroup