package test.org.example.java.pattern.proxy.dynamicproxy;

import org.example.java.pattern.structural.proxy.dynamicproxy.*;
import org.example.java.pattern.structural.proxy.staticproxy.CountImpl;
import org.junit.Test;

/**
 * Created by Juntao.Zhang on 2014/8/5.
 */
public class DynamicProxyTest {
  @Test
  public void testCglibProxy() {
    Count count = (Count) new CglibProxy().getInstance(new CountImpl());
    Book book = (Book) new CglibProxy().getInstance(new BookImpl());
//    System.out.printf("count proxy is %s.%n", count);
//    System.out.printf("book proxy is %s.%n", book);
    count.updateCount();
    System.out.println();
    count.queryCount();
    System.out.println("=====");
    book.addBook();
    System.out.println();
    book.queryBook();
  }

  @Test
  public void testJdkProxy() {
    Count count = (Count) new JDKProxy().bind(new CountImpl());
    Book book = (Book) new JDKProxy().bind(new BookImpl());
//    System.out.printf("count proxy is %s.%n", count);
//    System.out.printf("book proxy is %s.%n", book);
    count.updateCount();
    System.out.println();
    count.queryCount();
    System.out.println("=====");
    book.addBook();
    System.out.println();
    book.queryBook();
  }
}
