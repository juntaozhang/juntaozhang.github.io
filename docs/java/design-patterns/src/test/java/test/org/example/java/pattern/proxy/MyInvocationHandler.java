package test.org.example.java.pattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("正在执行的方法：" + method);
    if (args != null) {
      System.out.println("下面是执行该方法时传入的实参：");
      for (Object o : args) {
        System.out.println(o);
      }
    } else {
      System.out.println("该方法没有实参");
    }
    return null;
  }

}
