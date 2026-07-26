// scalastyle:off
package cn.juntaozhang.example.spark

import org.apache.spark.sql.catalyst.expressions.{Add, Alias, AttributeReference, Literal}
import org.apache.spark.sql.catalyst.plans.logical.{LocalRelation, Project}
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class LogicalPlanSpec extends AnyFunSuite with BeforeAndAfterAll {

  val spark: SparkSession = SparkSession.builder()
    .appName("LogicalPlanTest")
    .master("local")
    .getOrCreate()

  import spark._
  import spark.implicits._

  override def beforeAll(): Unit = {
    super.beforeAll()
    sql(
      """
        |CREATE OR REPLACE TEMP VIEW sales_table AS
        |    SELECT * FROM VALUES
        |    ("A", 20210101, 100),
        |    ("A", 20210102, 200),
        |    ("B", 20210101, 300),
        |    ("A", 20210103, 150),
        |    ("B", 20210102, 400),
        |    ("A", 20210105, 50),
        |    ("B", 20210104, 50)
        |    AS sales_table(category, dt, sales)
        |""".stripMargin)


    Seq(("Alice", 29, "New York"))
      .toDF("name", "age", "city")
      .createOrReplaceTempView("people")
  }

  override def afterAll(): Unit = {
    if (spark != null) {
      spark.stop()
    }
    super.afterAll()
  }

  test("logical plan") {
    val query = "SELECT name, age FROM people WHERE salary > 50000"

    // 解析查询
    val logicalPlan = spark.sessionState.sqlParser.parsePlan(query)
    println(logicalPlan)

    // 分析查询
    val analyzedPlan = spark.sessionState.analyzer.execute(logicalPlan)
    println(analyzedPlan)
  }

  test("logical plan2") {
    val query = "SELECT name, age FROM people WHERE age > 30"

    // 解析查询
    val logicalPlan = spark.sessionState.sqlParser.parsePlan(query)
    println(logicalPlan)

    // 分析查询
    val analyzedPlan = spark.sessionState.analyzer.execute(logicalPlan)
    println(analyzedPlan)
  }

  test("canonicalize 标准化") {
    // 创建一个简单的LogicalPlan
    val plan1 = Project(Seq(Alias(Add(Literal(1), AttributeReference("a", IntegerType)()), "result")()), LocalRelation(AttributeReference("a", IntegerType)()))

    val plan2 = Project(Seq(Alias(Add(AttributeReference("a", IntegerType)(), Literal(1)), "result")()), LocalRelation(AttributeReference("a", IntegerType)()))

    // 打印原始逻辑计划
    println(s"Original Plan 1: ${plan1}")
    println(s"Original Plan 2: ${plan2}")

    // 规范化逻辑计划
    val canonicalizedPlan1 = plan1.canonicalized
    val canonicalizedPlan2 = plan2.canonicalized

    // 打印规范化后的逻辑计划
    println(s"Canonicalized Plan 1: ${canonicalizedPlan1}")
    println(s"Canonicalized Plan 2: ${canonicalizedPlan2}")

    // 比较规范化后的逻辑计划
    println(s"Plans are equal: ${canonicalizedPlan1 == canonicalizedPlan2}")
    println(s"Plans are equal: ${canonicalizedPlan1 canEqual canonicalizedPlan2}")
  }

  // TODO: not working
  test("eliminate subquery => EliminateSubqueryAliases") {
    val query = (
      """
        |SELECT *
        |FROM (
        |  SELECT category, sales
        |  FROM sales_table
        |) AS subquery
        |WHERE subquery.category = 'A';
        |""".stripMargin)
    sql(query).explain(true)
  }

  test("Window") {
    sql(
      """
        |select * from sales_table
        |""".stripMargin).show()


    sql(
      """
        |SELECT
        |  category,
        |  sales,
        |  SUM(sales) OVER (
        |      PARTITION BY category ORDER BY sales DESC
        |      ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) as cumulative_sales
        |FROM
        |  sales_table order by sales DESC
        |""".stripMargin).show()


    sql(
      """
        |SELECT
        |  category,
        |  sales,
        |  SUM(sales) OVER (
        |      PARTITION BY category ORDER BY sales DESC
        |      ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
        |  ) as cumulative_sales
        |FROM
        |  sales_table order by sales DESC
        |""".stripMargin).show()
  }

  test("ROWS BETWEEN vs RANGE BETWEEN") {
    sql(
      """
        |SELECT
        |  category,
        |  dt,
        |  sales,
        |  sum(sales) OVER (
        |      PARTITION BY category ORDER BY dt asc
        |      ROWS BETWEEN 0 PRECEDING AND 1 FOLLOWING
        |  ) as rank
        |FROM
        |  sales_table
        |""".stripMargin).show()

    sql(
      """
        |SELECT
        |  category,
        |  dt,
        |  sales,
        |  sum(sales) OVER (
        |      PARTITION BY category ORDER BY dt asc
        |      RANGE BETWEEN 0 PRECEDING AND 1 FOLLOWING
        |  ) as rank
        |FROM
        |  sales_table
        |""".stripMargin).show()
  }

  test("PullOutNondeterministic") {
    // PullOutNondeterministic规则的主要目的是为了确保聚合操作的一致性和正确性。
    // 在聚合查询中，如果聚合键（即在GROUP BY子句中指定的列）包含非确定性表达式，
    // 那么同一个键可能会在每次查询执行时产生不同的值，从而导致聚合结果不一致。

    // 所以会先放到project中使其确定，然后再进行聚合操作，见PullOutNondeterministic.scala
    val df = sql(
      """
        |SELECT category, SUM(sales)
        |FROM sales_table
        |GROUP BY category, rand()
        |""".stripMargin)
    df.explain(true)
    df.show()
  }

  test("udf HandleNullInputsForUDF null check") {
    val data = Seq(
      Row("zhangsan", 20),
      Row("lisi", null),
      Row("wangwu", 30)
    )

    val schema = StructType(Array(
      StructField("name", StringType, nullable = false),
      StructField("age", IntegerType, nullable = true)
    ))

    val df = spark.createDataFrame(
      spark.sparkContext.parallelize(data),
      schema
    )

    spark.udf.register("add", (age: Int) => age + 1)
    val result = df.withColumn("age2", expr("add(age)"))
    result.show()
    // add('age) AS age2 -> if (isnull(age#22)) null else add(knownnotnull(age#22)) AS age2
    result.explain(true)
  }


}
// scalastyle:on