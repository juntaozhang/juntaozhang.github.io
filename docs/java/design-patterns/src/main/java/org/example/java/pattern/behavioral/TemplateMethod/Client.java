package org.example.java.pattern.behavioral.TemplateMethod;

/**
 * User: ZhangJuntao
 * Date: 13-10-20
 * Time: 下午7:09
 */
abstract class TemplatePattern {

  //模板方法
  public final void templateMethod() {
    privateMethod();
    hookMethod();//勾子方法
    abstractMethod();//抽象方法
  }

  private void privateMethod() {
    System.out.println("父类实现业务逻辑");
  }

  public void hookMethod() {
    System.out.println("父类默认实现，子类可覆盖");
  }

  protected abstract void abstractMethod();//子类负责实现业务逻辑
}

class TemplatePatternImpl extends TemplatePattern {
  @Override
  protected void abstractMethod() {
    System.out.println("method3()在子类TemplatePatternImpl中实现了！！");
  }
}

public class Client {
  public static void main(String[] args) {
    TemplatePattern t1 = new TemplatePatternImpl();
    t1.templateMethod();
  }
}


