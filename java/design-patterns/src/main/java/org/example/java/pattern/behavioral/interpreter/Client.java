package org.example.java.pattern.behavioral.interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: ZhangJuntao
 * Date: 13-2-14
 * Time: 上午12:26
 */
abstract class Expression {
  //解析公式和数值,其中var中的key值是是公式中的参数，value值是具体的数字
  public abstract int interpreter(HashMap<String, Integer> var);
}

class VarExpression extends Expression {
  private String key;

  public VarExpression(String _key) {
    this.key = _key;
  }

  //从map中取之
  public int interpreter(HashMap<String, Integer> var) {
    return var.get(this.key);
  }
}

abstract class SymbolExpression extends Expression {
  protected Expression left;
  protected Expression right;

  //所有的解析公式都应只关心自己左右两个表达式的结果
  public SymbolExpression(Expression _left, Expression _right) {
    this.left = _left;
    this.right = _right;
  }
}

class AddExpression extends SymbolExpression {
  public AddExpression(Expression _left, Expression _right) {
    super(_left, _right);
  }

  //把左右两个表达式运算的结果加起来
  public int interpreter(HashMap<String, Integer> var) {
    return super.left.interpreter(var) + super.right.interpreter(var);
  }
}

class SubExpression extends SymbolExpression {
  public SubExpression(Expression _left, Expression _right) {
    super(_left, _right);
  }

  //左右两个表达式相减
  public int interpreter(HashMap<String, Integer> var) {
    return super.left.interpreter(var) - super.right.interpreter(var);
  }
}

class Calculator {
  //定义的表达式
  private Expression expression;

  //构造函数传参,并解析
  public Calculator(String expStr) {
    //定义一个堆栈，安排运算的先后顺序
    Stack<Expression> stack = new Stack<Expression>();
    //表达式拆分为字符数组
    char[] charArray = expStr.toCharArray();
    //运算
    Expression left = null;
    Expression right = null;
    for (int i = 0; i < charArray.length; i++) {
      switch (charArray[i]) {
        case '+'://加法
          //加法结果放到堆栈中
          left = stack.pop();
          right = new VarExpression(String.valueOf(charArray[++i]));
          stack.push(new AddExpression(left, right));
          break;
        case '-':
          left = stack.pop();
          right = new VarExpression(String.valueOf(charArray[++i]));
          stack.push(new SubExpression(left, right));
          break;
        default://公式中的变量
          stack.push(new VarExpression(String.valueOf(charArray[i])));
      }
    }
    //把运算结果抛出来
    this.expression = stack.pop();
  }

  //开始运算
  public int run(HashMap<String, Integer> var) {
    return this.expression.interpreter(var);
  }
}

public class Client {
  public static void main(String[] args) throws IOException {
    String expStr = getExpStr();
    //赋值
    HashMap<String, Integer> var = getValue(expStr);
    Calculator cal = new Calculator(expStr);
    System.out.println("运算结果为：" + expStr + "=" + cal.run(var));
  }

  //获得表达式
  public static String getExpStr() throws IOException {
    System.out.print("请输入表达式：");
    return (new BufferedReader(new InputStreamReader(System.in))).readLine();
  }

  //获得值映射
  public static HashMap<String, Integer> getValue(String exprStr) throws IOException, IOException {
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    //解析有几个参数要传递
    for (char ch : exprStr.toCharArray()) {
      if (ch != '+' && ch != '-') {
        //解决重复参数的问题
        if (!map.containsKey(String.valueOf(ch))) {
          System.out.print("请输入" + ch + "的值:");
          String in = (new BufferedReader(new InputStreamReader(System.in))).readLine();
          map.put(String.valueOf(ch), Integer.valueOf(in));
        }
      }
    }
    return map;
  }
}
