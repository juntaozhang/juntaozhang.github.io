package org.example.java.pattern.behavioral.visitor.reflect;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Created by Juntao.Zhang on 2015/1/12.
 */
public class VisitorReflectivePrint {
  interface Visitable {
    void accept(ReflectiveVisitor visitor);
  }
  interface ReflectiveVisitor {
    void visit(Object o);
  }

  static class VisitableString implements Visitable {
    private String value;
    public VisitableString(String string) {
      value = string;
    }
    public void accept(ReflectiveVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class VisitableFloat implements Visitable {
    private Float value;
    public VisitableFloat(Float f) {
      value = f;
    }
    public void accept(ReflectiveVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class VisitableCollection implements Visitable {
    private Collection value;
    public VisitableCollection(Collection f) {
      value = f;
    }
    public void accept(ReflectiveVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class PrintVisitor implements ReflectiveVisitor {
    public void visitVisitableCollection(VisitableCollection collection) {
      for (Object o : collection.value) {
        if (o instanceof Visitable)
          ((Visitable) o).accept(this);
      }
    }

    public void visitVisitableString(VisitableString string) {
      System.out.println("'" + string.value + "'");
    }

    public void visitVisitableFloat(VisitableFloat f) {
      System.out.println(f.value + "f");
    }

    public void myDefault(Object o) {
      System.out.println(o.toString());
    }

    public void visit(Object o){
      // Class.getName() returns package information as well.
      // This strips off the package information giving us
      // just the class name
      String methodName = o.getClass().getName();
      methodName = "visit" +
          methodName.substring(methodName.lastIndexOf('.') + 1).split("\\$")[1];
      // Now we try to invoke the method visit<methodName>
      try {
        // Get the method visitFoo(Foo foo)
        Method m = getClass().getMethod(methodName, o.getClass());
        // Try to invoke visitFoo(Foo foo)
        m.invoke(this, o);
      } catch (Exception e) {
        // No method, so do the default implementation
        myDefault(o);
      }
    }
  }

  /**
   * 去掉了之前的if else
   * @param args
   */
  public static void main(String[] args) {
    new VisitableFloat(11.2f).accept(new PrintVisitor());
  }
}
