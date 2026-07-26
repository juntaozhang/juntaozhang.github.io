package org.example.java.pattern.structural.proxy.staticproxy;

/**
 * 这是一个代理类（增强BookImpl实现类）
 *
 * @author Administrator
 */
public class BookProxy implements Book {
  private BookImpl bookImpl;

  /**
   * 覆盖默认构造器
   */
  public BookProxy(BookImpl bookImpl) {
    this.bookImpl = bookImpl;
  }

  @Override
  public void queryBook() {
    System.out.println("事物开始");
    // 调用委托类的方法;
    bookImpl.queryBook();
    System.out.println("事物结束");
  }

  @Override
  public void addBook() {
    System.out.println("事物开始");
    // 调用委托类的方法;
    bookImpl.addBook();
    System.out.println("事物结束");

  }

}