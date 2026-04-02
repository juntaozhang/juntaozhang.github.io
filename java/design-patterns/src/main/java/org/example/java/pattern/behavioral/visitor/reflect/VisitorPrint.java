package org.example.java.pattern.behavioral.visitor.reflect;

import java.util.Collection;

/**
 * Created by Juntao.Zhang on 2015/1/12.
 */
public class VisitorPrint {
  interface Visitor {
    void visitCollection(VisitableCollection collection);
    void visitString(VisitableString string);
    void visitFloat(VisitableFloat f);
  }

  interface Visitable {
    void accept(Visitor visitor);
  }

  static class VisitableString implements Visitable {
    private String value;
    public VisitableString(String string) {
      value = string;
    }
    public void accept(Visitor visitor) {
      visitor.visitString(this);
    }
  }

  static class VisitableFloat implements Visitable {
    private Float value;
    public VisitableFloat(Float f) {
      value = f;
    }
    public void accept(Visitor visitor) {
      visitor.visitFloat(this);
    }
  }

  static class VisitableCollection implements Visitable {
    private Collection value;
    public VisitableCollection(Collection f) {
      value = f;
    }
    public void accept(Visitor visitor) {
      visitor.visitCollection(this);
    }
  }

  static class PrintVisitor implements Visitor {
    public void visitCollection(VisitableCollection collection) {
      for (Object o : collection.value) {
        if (o instanceof Visitable)
          ((Visitable) o).accept(this);
      }
    }

    public void visitString(VisitableString string) {
      System.out.println("'" + string.value + "'");
    }

    public void visitFloat(VisitableFloat f) {
      System.out.println(f.value + "f");
    }
  }

  /**
   * 去掉了之前的if else
   * @param args
   */
  public static void main(String[] args) {
    //这是visitor模式，一般参数为常变化的，下面的抵用相反
    new VisitableFloat(11.2f).accept(new PrintVisitor());
    //这是普通的接口调用，看似差不多其实思想大不同
    new PrintVisitor().visitFloat(new VisitableFloat(12.3f));
  }
}
