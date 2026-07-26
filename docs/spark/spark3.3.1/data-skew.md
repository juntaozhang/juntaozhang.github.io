
## Join

### AQE 优化
[optimize skewed join](AQE.md)
```
   优化                         作用
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   动态切换 Broadcast Join      运行时实际小 → Shuffle Join 改 Broadcast Join
   倾斜 Join 优化               拆分倾斜分区，避免单 task 处理过多数据
   动态合并分区                 小分区合并，减少 task 数
```


### bucket join

#### SMJ Plan

两种策略：
  1. Map 端不排序（[Tungsten UnsafeShuffleWriter(default)](task_execution.md#UnsafeShuffleWriter)）→ Reduce 端必须加 SortExec
  2. Map 端排序（SortShuffleWriter）→ Reduce 端归并，可能免 Sort
```
  SortMergeJoin [id], [id], Inner
  :- Sort [id ASC NULLS FIRST]              ← 左表排序
  :  +- Exchange hashpartitioning(id, 200)  ← 左表 Shuffle
  :     +- Scan a
  +- Sort [id ASC NULLS FIRST]              ← 右表排序
     +- Exchange hashpartitioning(id, 200)  ← 右表 Shuffle
        +- Scan b
```

#### spark 原生 bucket join
```
  CREATE TABLE bucketed_a (id INT, name STRING) USING PARQUET CLUSTERED BY (id) INTO 4 BUCKETS;
  CREATE TABLE bucketed_b (id INT, value STRING) USING PARQUET CLUSTERED BY (id) INTO 4 BUCKETS;

  SELECT * FROM bucketed_a JOIN bucketed_b ON a.id = b.id;
  SortMergeJoin [id], [id]
  :- Sort [id ASC NULLS FIRST]           ← 必须 Sort！
  :  +- Scan parquet bucketed_a
  :- Sort [id ASC NULLS FIRST]           ← 必须 Sort！
  :  +- Scan parquet bucketed_b
  注意：没有 Exchange，但有 Sort。
```

#### paimon primary key table
PaimonScan 无 Sort！无 Exchange！
```
  Paimon Primary Key Table（有序）
  SortMergeJoin [id], [id]
  :- PaimonScan [id, name]               ←
  :     +- PaimonBatchScan: bucket=4, bucket-key=id, sorted=true
  :- PaimonScan [id, value]              ←
        +- PaimonBatchScan: bucket=4, bucket-key=id, sorted=true
```

### Aggregation
`spark.sql.execution.useObjectHashAggregateExec`

- HashAggregate：依赖内存，利用UnsafeFixedWidthAggregationMap 进行聚合（自动 spill 到磁盘）
- SortAggregate: 不依赖内存，如果 key 基数多，（如果类似 paimon 本身就有序）
    - SMJ 输出有序，SortAggregate 可直接消费），但 Spark 策略里只有 HashAggregate，依赖 spill 机制处理内存压力，没有实现基于有序输入的 SortAggregate 自动切换。



