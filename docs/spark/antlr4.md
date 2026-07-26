# 语法分析器生成器

功能上与scala解析器相似,但是antlr4是一个独立的工具,可以生成多种语言的解析器,包括java,scala,python等

## Example
- [Calculator.g4](spark-example%2Fsrc%2Fmain%2Fantlr4%2Fcn%2Fjuntaozhang%2Fexample%2Fantlr%2FCalculator.g4)
- [MyCalculatorVisitor.java](spark-example%2Fsrc%2Fmain%2Fjava%2Fcn%2Fjuntaozhang%2Fexample%2Fantlr%2FMyCalculatorVisitor.java)
- [CalculatorParserTest](spark-example/src/main/scala/cn/juntaozhang/example/antlr/CalculatorParserTest.scala)
- [SqlParserTest](spark-example/src/main/scala/cn/juntaozhang/example/antlr/SqlParserTest.scala)

## Antlr4 与 calcite 的关系
Calcite 使用 ANTLR4 来生成SQL解析器。具体来说，Calcite 中定义了SQL语法规则，然后通过ANTLR4生成相应的解析器代码。

ANTLR4：ANTLR4 提供了强大的语法定义和解析工具，能够将SQL文本解析成AST。

# 参考
* http://www.antlr.org/
* https://www.gitbook.com/book/dohkoos/antlr4-short-course/details
* http://codemany.com/tags/antlr/
* [Intellij Plugin](https://github.com/antlr/antlr4/blob/master/doc/java-target.md)
* https://www.cnblogs.com/sld666666/p/6145854.html
* [Spark SQL内部剖析](https://www.dedao.cn/ebook/reader?id=pqvNQ1KRJa7EmgG8MPKrzykNVbDpBWZPed6wQA1xO54nlvZq296YodejLXVJE5eA)