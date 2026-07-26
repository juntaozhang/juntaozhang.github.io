package cn.juntaozhang.example.antlr;

import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.parser.SqlBaseParserBaseVisitor;

public class MySqlBaseVisitor extends SqlBaseParserBaseVisitor<String> {
    @Override
    public String visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        System.out.println("visitSingleStatement: " + ctx.getText());
        return super.visitSingleStatement(ctx);
    }

    @Override
    public String visitSingleExpression(SqlBaseParser.SingleExpressionContext ctx) {
        System.out.println("visitSingleExpression: " + ctx.getText());
        return super.visitSingleExpression(ctx);
    }

    @Override
    public String visitWhereClause(SqlBaseParser.WhereClauseContext ctx) {
        System.out.println("visitWhereClause: " + ctx.getText());
        return super.visitWhereClause(ctx);
    }


}
