# dataframe

## udf and change col
```
import org.apache.spark.sql.functions.udf
val t = spark.createDataFrame(Seq(
  ("a", "1"),
  ("b", "2"),
  ("c", "3")
)).toDF("id", "content")
def code(arg: String) = arg + "=>"
val addCol = udf(code _)
val t2 = t.withColumn("id_orignal", t("id")).withColumn("id", addCol(t("id")))
t2.show()


val t3 = spark.createDataFrame(Seq(
  ("e", "4"),
  ("b", "5"),
  ("c", "6")
  )).toDF("id", "content1")
val t4 = t3.withColumn("id_orignal", t3("id")).withColumn("id", addCol(t3("id")))
t4.show()

val t5 =t4.join(
  t2, Seq("id"), "full"
)
t5.show()
```


```
    import org.apache.spark.sql.functions.udf
    def code(arg: String) = arg + "=>"
    val addCol = udf(code _)
    import org.apache.spark.sql.DataFrame

    def toDF(dataset: DataFrame): DataFrame = {
      val d = dataset.withColumn(
        "id_original", dataset("id")
      )
      spark.createDataFrame(
        d.filter(
          d("id").isNotNull && d("id").notEqual("-1")
        ).withColumn(
          "id", addCol(d("id"))
        ).rdd.map(r => (r.getAs[String]("id"), r)).map(
          p => (p._1, p._2)
        ).map(_._2),
        d.schema
      )
    }


    val t = spark.createDataFrame(Seq(
      ("a", "1"),
      ("b", "2"),
      ("c", "3")
    )).toDF("id", "content")


    val t2 = toDF(t)

    t2.show()


    val t3 = spark.createDataFrame(Seq(
      ("e", "4"),
      ("b", "5"),
      ("c", "6")
    )).toDF("id", "content1")
    val t4 = toDF(t3)
    t4.show()

    val t5 =t4.join(
      t2, Seq("id","id_original"), "full"
    )
    t5.show()
```