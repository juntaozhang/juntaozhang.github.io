package cn.juntaozhang.example.antlr

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.apache.spark.sql.catalyst.parser.{SqlBaseLexer, SqlBaseParser}

object SqlParserTest {
  def main(args: Array[String]): Unit = {
    val lexer = new SqlBaseLexer(CharStreams.fromString("select * from student where age > 37"))
    val tokens = new CommonTokenStream(lexer)
    val parser = new SqlBaseParser(tokens)
    new MySqlBaseVisitor().visit(parser.singleStatement())
    new MySqlBaseVisitor().visit(parser.singleExpression())
    new MySqlBaseVisitor().visit(parser.whereClause())
  }
}
