# Data Evolution
## compaction not support for data evolution

```text
SET spark.sql.cli.print.header=true;

CREATE TABLE s (id INT, b INT);
INSERT INTO s VALUES (1, 11), (2, 22);

CREATE TABLE t (id INT, b INT, c INT) TBLPROPERTIES ('row-tracking.enabled' = 'true', 'data-evolution.enabled' = 'true');
INSERT INTO t VALUES (2, 2, 2), (3, 3, 3);
MERGE INTO t
USING s
ON t.id = s.id
WHEN MATCHED THEN UPDATE SET t.b = s.b
WHEN NOT MATCHED THEN INSERT (id, b, c) VALUES (id, b, 0);


CALL sys.compact(table => 't');

select *, _ROW_ID, _SEQUENCE_NUMBER from t order by _ROW_ID asc;
+--------+----+--------+---------+------------------+
|     id |  b |      c | _ROW_ID | _SEQUENCE_NUMBER |
+--------+----+--------+---------+------------------+
| <NULL> | 22 | <NULL> |       0 |                2 |
| <NULL> |  3 | <NULL> |       1 |                2 |
|      2 |  2 |      2 |       0 |                1 |
|      3 |  3 |      3 |       1 |                1 |
|      1 | 11 |      0 |       2 |                2 |
+--------+----+--------+---------+------------------+
```

code pipeline
```text
rule:
PaimonProcedureResolver 
    PaimonCallStatement(sys.compact) -> PaimonCallCommand(CompactProcedure)
PaimonStrategy
    PaimonCallCommand -> PaimonCallExec
    
PaimonCallExec
    run -> CompactProcedure.call
    
SparkProcedures register all call
```

## Troubleshooting

<details>
<summary>Disable compaction for data evolution table</summary>

Spark and Flink inconsistent: https://github.com/apache/paimon/pull/6342
</details>