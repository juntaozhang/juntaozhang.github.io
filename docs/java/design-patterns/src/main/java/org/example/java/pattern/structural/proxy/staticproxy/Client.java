package org.example.java.pattern.structural.proxy.staticproxy;

/**
 * 测试Count类
 *
 * @author Administrator
 */
public class Client {
  public static void main(String[] args) {
    CountProxy countProxy = new CountProxy(new CountImpl());
    BookProxy bookProxy = new BookProxy(new BookImpl());
    countProxy.updateCount();
    System.out.println();
    countProxy.queryCount();
    System.out.println("=====");
    bookProxy.addBook();
    System.out.println();
    bookProxy.queryBook();

  }
}

