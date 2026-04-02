# Row Tracking

## Basic Operations
```sparksql
SELECT id, data, _ROW_ID, _SEQUENCE_NUMBER FROM t order by _ROW_ID asc;
UPDATE t SET data = 'a1' WHERE id = 1;
DELETE FROM t WHERE id = 2;
```

## Troubleshooting

<details>
<summary>delete by ROW_ID, java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Long</summary>

> DELETE FROM t WHERE _ROW_ID = 2;

[Support UPDATE/DELETE by _ROW_ID for row tracking](../pr/pr-rowwriter-fields-inconsistent.md)
</details>