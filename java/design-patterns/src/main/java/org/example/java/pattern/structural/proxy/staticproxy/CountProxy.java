package org.example.java.pattern.structural.proxy.staticproxy;

/**
 * 这是一个代理类（增强CountImpl实现类）
 *
 * @author Administrator
 */
public class CountProxy implements Count {
  private CountImpl countImpl;

  /**
   * 覆盖默认构造器
   *
   * @param countImpl
   */
  public CountProxy(CountImpl countImpl) {
    this.countImpl = countImpl;
  }

  @Override
  public void queryCount() {
    System.out.println("事物开始");
    // 调用委托类的方法;
    countImpl.queryCount();
    System.out.println("事物结束");
  }

  @Override
  public void updateCount() {
    System.out.println("事物开始");
    // 调用委托类的方法;
    countImpl.updateCount();
    System.out.println("事物结束");

  }

}