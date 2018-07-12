## Window.rangeBetween

```
scala> val df = Seq((1, "a"), (1, "a"), (2, "a"), (1, "b"), (2, "b"), (3, "b"),(2,"a")).toDF("id", "category")
df: org.apache.spark.sql.DataFrame = [id: int, category: string]

scala> df.withColumn("sum",sum('id) over Window.partitionBy('category).orderBy('id).rangeBetween(0,1)).show()
+---+--------+---+
| id|category|sum|
+---+--------+---+
|  1|       b|  3|
|  2|       b|  5|
|  3|       b|  3|
|  1|       a|  6|
|  1|       a|  6|
|  2|       a|  4|
|  2|       a|  4|
+---+--------+---+


scala> Seq((1, "a"), (1, "a"), (2, "a"), (1, "b"), (2, "b"), (3, "b"),(3, "a")).toDF("id", "category").withColumn("sum",sum('id) over Window.partitionBy('category).orderBy('id).rangeBetween(0,1)).show()
+---+--------+---+
| id|category|sum|
+---+--------+---+
|  1|       b|  3|
|  2|       b|  5|
|  3|       b|  3|
|  1|       a|  4|
|  1|       a|  4|
|  2|       a|  5|
|  3|       a|  3|
+---+--------+---+
```


>Window.partitionBy("country").orderBy("date").rowsBetween(Window.unboundedPreceding, Window.currentRow)

- [Introducing Window Functions in Spark SQL](https://databricks.com/blog/2015/07/15/introducing-window-functions-in-spark-sql.html)




// memory info
val mb = 1024*1024
val runtime = Runtime.getRuntime
logger.log("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
logger.log("** Free Memory:  " + runtime.freeMemory / mb)
logger.log("** Total Memory: " + runtime.totalMemory / mb)
logger.log("** Max Memory:   " + runtime.maxMemory / mb)


sql("select device,pkg from dm_sdk_mapping.device_applist where day=20180516").map {
      case Row(device: String, pkg: String) => (device,pkg, a5.value.getOrElse(pkg, -1))
    }.filter(_._2 == -1).count()


# coalesce

```
import spark.implicits._
val n1 = Seq((1, "1_1"), (2, "2_1"), (3, "3_1")).toDF("id", "n1")
val n2 = Seq((1, "1_2"), (2, "2_2"), (4, "4_2")).toDF("id", "n2")
val n3 = Seq((5, "5_3"), (2, "2_3"), (4, "4_3")).toDF("id", "n3")
n1.join(n2,Seq("id"),"full").show
```

+---+----+----+
| id|  n1|  n2|
+---+----+----+
|  1| 1_1| 1_2|
|  3| 3_1|null|
|  4|null| 4_2|
|  2| 2_1| 2_2|
+---+----+----+

```
n1.join(n2,Seq("id"),"full").join(n2,Seq("id"),"full").show()
```
+---+----+----+----+
| id|  n1|  n2|  n2|
+---+----+----+----+
|  1| 1_1| 1_2| 1_2|
|  3| 3_1|null|null|
|  4|null| 4_2| 4_2|
|  2| 2_1| 2_2| 2_2|
+---+----+----+----+


