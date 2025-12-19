# Cost-Based Optimizer(CBO)

## CostBasedJoinReorder
* 目前spark (N-way Join, N=2), 即只支持二叉树型的两两 Join 执行。  
* 究其原因：Spark 的物理 Join 算子（BroadcastHashJoin、ShuffleHashJoin、SortMergeJoin）均为「二元算子」  
* Left Deep Tree：每个Inner Join的右节点永远只是一个单表，这种树我们叫左深树
* Bushy Tree：结构最为自由，表可以按照任意顺序Join
* 当前只支持 inner join，所以 A join B == B join A

```scala
sql("drop table if exists order_all")
sql("drop table if exists order")
sql("drop table if exists user")
sql("drop table if exists region")

sql("SET spark.sql.autoBroadcastJoinThreshold = -1")
sql("SET spark.sql.shuffle.partitions = 50")
sql("SET spark.sql.adaptive.coalescePartitions.enabled = false")
sql("SET spark.sql.adaptive.advisoryPartitionSizeInBytes = 20k")
sql("SET spark.sql.adaptive.coalescePartitions.minPartitionSize = 20k")

sql("SET spark.sql.cbo.enabled = true")
sql("SET spark.sql.cbo.joinReorder.dp.star.filter = true")
sql("SET spark.sql.cbo.joinReorder.enabled = true")

spark.range(0, 10000, 1, 100).selectExpr("id AS order_id", "id % 10000 AS user_id", "id % 100 AS region_id", "CAST(id AS STRING) AS order_num").createOrReplaceTempView("order")
spark.range(0, 1000, 1, 10).selectExpr("id AS user_id", "id % 100 AS region_id", "CAST(id AS STRING) AS user_name").createOrReplaceTempView("user")
spark.range(0, 100, 1, 1).selectExpr("id AS region_id", "CAST(id AS STRING) AS region_name").createOrReplaceTempView("region")

sql(
  """
    |SELECT o.order_id, o.user_id, o.region_id, order_num, user_name, region_name
    |FROM order o
    |JOIN user u ON o.user_id = u.user_id AND o.region_id = u.region_id
    |JOIN region r ON o.region_id = r.region_id
    |""".stripMargin).write.mode(SaveMode.Overwrite).saveAsTable("order_all")
```
[join reorder log](asset/join_reorder.log)

| No. | Conditions                      | Purpose                  |
|-----|---------------------------------|--------------------------|
| 1   | `items.size > 2`                | 仅对有多表 Join（≥3表）触发重排      |
| 2   | `size ≤ joinReorderDPThreshold` | 限制 DP 算法计算量，避免优化阶段耗时过高   |
| 3   | `conditions.nonEmpty`           | 仅对有有效关联条件的 Join 重排 |
| 4   | `rowCount.isDefined`            | 保证 CBO 能准确评估代价，避免重排后得到更差的执行计划 |


## JoinReorderDP
A join B == B join A, 即 dp[{A,B}] = dp[{B,A}]

假设两两之间都可以关联上，DP 执行流程简化：
```text
Level 0: 单表集合
  +-----------+-----------+-----------+-----------+
  | dp[{A}]   | dp[{B}]   | dp[{C}]   | dp[{D}]   |
  |-----------|-----------|-----------|-----------|
  |  a        |  b        |  c        |  d        |--- cost
  +-----------+-----------+-----------+-----------+
  
Level 1: 两表组合 C(4,2) = 6
  +-----------------+-----------------+-----------------+-----------------+-----------------+-----------------+
  | dp[{A,B}]       | dp[{A,C}]       | dp[{A,D}]       | dp[{B,C}]       | dp[{B,D}]       | dp[{C,D}]       |
  |-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|
  | dp[{A}]+dp[{B}] | dp[{A}]+dp[{C}] | dp[{A}]+dp[{D}] | dp[{B}]+dp[{C}] | dp[{B}]+dp[{D}] | dp[{C}]+dp[{D}] |--- cost
  +-----------------+-----------------+-----------------+-----------------+-----------------+-----------------+

Level 2: 三表组合 4 x 3 = 12
  +-------------------+-------------------+-------------------+-------------------+    
  | dp[{A,B,C}]       | dp[{A,B,D}]       | dp[{A,C,D}]       | dp[{B,C,D}]       |    
  +-------------------+-------------------+-------------------+-------------------+    
  | dp[{A}]+dp[{B,C}] | dp[{A}]+dp[{B,D}] | dp[{A}]+dp[{C,D}] | dp[{B}]+dp[{C,D}] |┐   
  | dp[{B}]+dp[{A,C}] | dp[{B}]+dp[{A,D}] | dp[{C}]+dp[{A,D}] | dp[{C}]+dp[{B,D}] |┤--- min(cost)
  | dp[{C}]+dp[{A,B}] | dp[{D}]+dp[{A,B}] | dp[{D}]+dp[{A,C}] | dp[{D}]+dp[{B,C}] |┘   
  +-------------------+-------------------+-------------------+-------------------+    

Level 3: 四表组合 12(3个表) + 3(2个表) = 15
  +---------------------+     
  | dp[{A,B,C,D}]       |     
  +---------------------+     
  | dp[{A}]+dp[{B,C,D}] | ┐       
  | dp[{B}]+dp[{A,C,D}] | ┤    
  | dp[{C}]+dp[{A,B,D}] | ┤      
  | dp[{D}]+dp[{A,B,C}] | ┤--- min(cost)   
  | dp[{A,B}]+dp[{C,D}] | ┤       
  | dp[{A,C}]+dp[{B,D}] | ┤    
  | dp[{A,D}]+dp[{B,C}] | ┘   
  +---------------------+     
```

对于表集合 S，遍历其所有可能的非空真子集 S1，将 S 分割成 S1 和 S2 两部分，然后取所有分割方式中代价最小的一种,  
状态方程：`dp[S] = min(dp[S1] + dp[S2] for all S1 where ∅ ⊂ S1 ⊂ S)`

Spark 中 `joinReorderDPThreshold` 默认 12（最多处理 12 表），以下是常见 n 值的分割数：

递推式为 $f(n) = \sum_{\substack{1 \leq i \leq n/2}} \frac{\binom{n}{i} \cdot f(i) \cdot f(n-i)}{1 + \mathbf{1}\{i = n-i\}}$

完全的二叉连接树的数量：

| $n$ | 计算过程                                                                                     | 结果 $f(n)$      |
|-----|------------------------------------------------------------------------------------------|----------------|
| 1   | -                                                                                        | 1              |
| 2   | -                                                                                        | 1              |
| 3   | $\mathrm{C}_3^1 f(2)f(1)$                                                                | 3              |
| 4   | $\mathrm{C}_4^1 f(3)f(2) + \frac{1}{2}\mathrm{C}_4^2 f(2)f(2)$                           | 15             |
| 5   | $\mathrm{C}_5^1 f(4)f(1) + \mathrm{C}_5^2 f(3)f(2)$                                      | 105            |
| 6   | $\mathrm{C}_6^1 f(5)f(1) + \mathrm{C}_6^2 f(4)f(2) + \frac{1}{2}\mathrm{C}_6^3 f(3)f(3)$ | 945            |
| 7   |                                                                                          | 10,395         |
| 8   |                                                                                          | 135,135        |
| 9   |                                                                                          | 2,027,025      |
| 10  |                                                                                          | 34,459,425     |
| 11  |                                                                                          | 654,729,075    |
| 12  |                                                                                          | 13,749,310,575 |


## JoinPlan Cost 
spark.sql.cbo.joinReorder.card.weight 默认是 0.7

基数（Cardinality）比率：a card / b card
字节大小（Size）比率：a size / b size

综合比率 = (基数比率 ^ weight) * (大小比率 ^ (1 - weight))

* weight = 1： 只关注基数，即只关注窄表的基数大小，
* weight = 0： 只关注大小，即只关注宽表，宽表一般每行的size 很大

## issue
DP 执行流程中如果两个表没有关联键，就会少掉一部分组合，
比如 dp[A,C]不存在，Level2如下图：
```text
  +-------------------+-------------------+-------------------+-------------------+    
  | dp[{A,B,C}]       | dp[{A,B,D}]       | dp[{A,C,D}]       | dp[{B,C,D}]       |    
  +-------------------+-------------------+-------------------+-------------------+    
  | dp[{A}]+dp[{B,C}] | dp[{A}]+dp[{B,D}] | dp[{A}]+dp[{C,D}] | dp[{B}]+dp[{C,D}] |┐   
  |      -            | dp[{B}]+dp[{A,D}] | dp[{C}]+dp[{A,D}] | dp[{C}]+dp[{B,D}] |┤--- min(cost)
  | dp[{C}]+dp[{A,B}] | dp[{D}]+dp[{A,B}] |      -            | dp[{D}]+dp[{B,C}] |┘   
  +-------------------+-------------------+-------------------+-------------------+   
```

如果存在 A join B join C where a_k=b_k and b_k=c_k 是不是可以推断出 a_k=c_k?  
见：[SPARK-54725: Add inferring transitive join conditions](../pr/SPARK-54725_reorder.md)


# Reference
- [浅析MySQL Join Reorder算法](https://developer.aliyun.com/article/1606637)
- [TiDB Join Reorder 算法简介](https://docs.pingcap.com/zh/tidb/stable/join-reorder/)

