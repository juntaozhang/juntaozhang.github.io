// scalastyle:off
package cn.juntaozhang.example.spark

import org.apache.spark.sql.{SparkSession, functions}

object AddExpressionExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("AddExpressionExample")
      .master("local[*]")
      .config("spark.sql.codegen.wholeStage", "true")
      .config("spark.sql.autoBroadcastJoinThreshold", "-1")
      .getOrCreate()

    import spark.implicits._

    // 创建一个示例DataFrame
    val df = spark.range(-5, 5).toDF("col1")
    df.createOrReplaceTempView("t")

    // 使用abs函数
    val result = spark.sql("select abs(col1) from t")

    // 查看物理计划
    result.explain(true)

    // 显示结果
    result.show()

    // 打印生成的代码
    result.queryExecution.debug.codegen()
  }
}
// scalastyle:on
