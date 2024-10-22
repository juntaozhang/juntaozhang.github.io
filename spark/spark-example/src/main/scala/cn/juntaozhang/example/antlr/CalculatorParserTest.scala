package cn.juntaozhang.example.antlr

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

object CalculatorParserTest {
  def main(args: Array[String]): Unit = {
    val lexer = new CalculatorLexer(CharStreams.fromString("(6 + 3 * 3) / 3"))
    val tokens = new CommonTokenStream(lexer)
    val parser = new CalculatorParser(tokens)
    println(new MyCalculatorVisitor().visit(parser.expr()))
  }
}
