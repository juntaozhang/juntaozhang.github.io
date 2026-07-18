## RBO

谓词下推、列裁剪、常量折叠、投影合并、无效聚合 / 过滤删除、简单 Join 顺序交换

### FilterProjectRemoveRule

删掉没用的列、删掉恒真 / 恒假条件:

```sql
SELECT id, name FROM (
    SELECT * FROM user
) t 
WHERE 1=1 and age > 18

==>

SELECT id, name FROM user WHERE age > 18
```

### ConstantFoldingRule 常量折叠

> SELECT * FROM t WHERE age > 10+20;

### ProjectMergeRule

> SELECT name FROM (SELECT id,name,age FROM t) tmp where age>18;

可以去掉子查询，直接从 t 中查询：

> SELECT name FROM t WHERE t.age>18;

### 谓词下推

age 过滤直接下推到数据源 scan plan：

> SELECT * FROM t1 JOIN t2 ON t1.id=t2.id WHERE t1.age>18;

## CBO

### Join 物理算子择优

* Broadcast/Map Join
* HashJoin
* SortMergeJoin
* NestedLoopJoin

多表 Join 代价重排、聚合两阶段拆分、过滤选择率代价估算、物化视图改写、Union 代价择优。

### Join Reorder

[spark CostBasedJoinReorder](../../spark/spark3.3.1/CBO.md):

### 基于 runtime stat谓词下推

TODO 是否是估计值？

bloom filter 下推到数据源 scan plan，[spark sql bloom filter](../../spark/spark3.3.1/bloom_filter.md)：

```sql
SELECT t.key, t.value1, t2.value2
FROM(
  SELECT key, value1
  FROM t1
  WHERE might_contain(
    (SELECT bloom_filter_agg(key)
     FROM t2
     WHERE t2.value2 > 0.01),
    t1.key)) t
JOIN(
  SELECT key, value2
  FROM t2
  WHERE t2.value2 > 0.01
) t2 ON t.key = t2.key
```

###
