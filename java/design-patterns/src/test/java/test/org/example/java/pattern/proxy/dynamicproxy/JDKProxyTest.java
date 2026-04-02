package test.org.example.java.pattern.proxy.dynamicproxy;

import org.example.java.pattern.structural.proxy.dynamicproxy.Book;
import org.example.java.pattern.structural.proxy.dynamicproxy.BookImpl;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Juntao.Zhang on 2015/1/6.
 */
public class JDKProxyTest {
  @Test
  public void testProxy() throws IOException {
    Book proxy = (Book) Proxy.newProxyInstance(Book.class.getClassLoader(), new Class[]{Book.class}, new InvocationHandler() {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //执行方法
        System.out.println("before");
        Object o = method.invoke(new BookImpl(), args);
        System.out.println("after");
        return o;
      }
    });
    proxy.addBook();
  }
}
