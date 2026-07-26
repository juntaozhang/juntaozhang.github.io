package cn.juntaozhang.example.antlr;

public class MyCalculatorVisitor extends CalculatorBaseVisitor<Integer> {
    @Override
    public Integer visitAddOrSub(CalculatorParser.AddOrSubContext ctx) {
        Integer o1 = ctx.expr(0).accept(this);
        Integer o2 = ctx.expr(1).accept(this);
        if ("+".equals(ctx.getChild(1).getText())) {
            return o1 + o2;
        } else {
            return o1 - o2;
        }
    }

    @Override
    public Integer visitMulOrDiv(CalculatorParser.MulOrDivContext ctx) {
        Integer o1 = ctx.expr(0).accept(this);
        Integer o2 = ctx.expr(1).accept(this);
        if ("/".equals(ctx.getChild(1).getText())) {
            return o1 / o2;
        } else {
            return o1 * o2;
        }
    }

    @Override
    public Integer visitParenExpr(CalculatorParser.ParenExprContext ctx) {
        return ctx.getChild(1).accept(this);
    }

    @Override
    public Integer visitInt(CalculatorParser.IntContext ctx) {
        return Integer.parseInt(ctx.getText());
    }
}
