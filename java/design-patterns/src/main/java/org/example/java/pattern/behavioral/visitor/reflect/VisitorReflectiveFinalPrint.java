package org.example.java.pattern.behavioral.visitor.reflect;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Created by Juntao.Zhang on 2015/1/12.
 */
public class VisitorReflectiveFinalPrint {
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
      try {
        getMethod(o.getClass()).invoke(this, o);
      } catch (Exception e) {
        // No method, so do the default implementation
        myDefault(o);
      }
    }

    protected Method getMethod(Class c) {
      Class newC = c;
      Method m = null;
      // Try the superclasses
      while (m == null && newC != Object.class) {
        String method = newC.getName();
        method = "visit" + method.substring(method.lastIndexOf('.') + 1).split("\\$")[1];
        try {
          m = getClass().getMethod(method, newC);
        } catch (NoSuchMethodException e) {
          newC = newC.getSuperclass();
        }
      }
      // Try the interfaces.  If necessary, you
      // can sort them first to define 'visitable' interface wins
      // in case an object implements more than one.
      if (newC == Object.class) {
        Class[] interfaces = c.getInterfaces();
        for (Class anInterface : interfaces) {
          String method = anInterface.getName();
          method = "visit" + method.substring(method.lastIndexOf('.') + 1).split("\\$")[1];
          try {
            m = getClass().getMethod(method, anInterface);
          } catch (NoSuchMethodException ignore) {
          }
        }
      }
      if (m == null) {
        try {
          m = getClass().getMethod("myDefault", Object.class);
        } catch (Exception ignore) {
        }
      }
      return m;
    }
  }

  public static void main(String[] args) {
    new VisitableFloat(11.2f).accept(new PrintVisitor());
  }
}
