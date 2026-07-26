package org.example.java.pattern.structural.proxy.staticproxy;


/**
 * 委托类(包含业务逻辑)
 *
 * @author Administrator
 */
public class BookImpl implements Book {

  @Override
  public void queryBook() {
    System.out.println("查看书方法...");

  }

  @Override
  public void addBook() {
    System.out.println("增加书方法...");

  }

}

