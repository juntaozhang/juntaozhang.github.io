package org.example.java.pattern.behavioral.strategy;

/**
 * 在什么情况下使用什么算法是由客户端决定的。
 * 策略模式的重心不是如何实现算法，而是如何组织、调用这些算法，从而让程序结构更灵活，具有更好的维护性和扩展性。
 *
 */
class Price {
  //持有一个具体的策略对象
  private MemberStrategy strategy;

  /**
   * 构造函数，传入一个具体的策略对象
   *
   * @param strategy 具体的策略对象
   */
  public Price(MemberStrategy strategy) {
    this.strategy = strategy;
  }

  /**
   * 计算图书的价格
   *
   * @param booksPrice 图书的原价
   * @return 计算出打折后的价格
   */
  public double quote(double booksPrice) {
    return this.strategy.calcPrice(booksPrice);
  }
}

interface MemberStrategy {
  /**
   * 计算图书的价格
   *
   * @param booksPrice 图书的原价
   * @return 计算出打折后的价格
   */
  double calcPrice(double booksPrice);
}

class PrimaryMemberStrategy implements MemberStrategy {
  @Override
  public double calcPrice(double booksPrice) {
    System.out.println("对于初级会员的没有折扣");
    return booksPrice;
  }
}

class IntermediateMemberStrategy implements MemberStrategy {
  @Override
  public double calcPrice(double booksPrice) {
    System.out.println("对于中级会员的折扣为10%");
    return booksPrice * 0.9;
  }
}

class AdvancedMemberStrategy implements MemberStrategy {
  @Override
  public double calcPrice(double booksPrice) {
    System.out.println("对于高级会员的折扣为20%");
    return booksPrice * 0.8;
  }
}

public class Client {
  public static void main(String[] args) {
    //选择并创建需要使用的策略对象
    MemberStrategy strategy = new AdvancedMemberStrategy();
    //创建环境
    Price price = new Price(strategy);
    //计算价格
    double quote = price.quote(300);
    System.out.println("图书的最终价格为：" + quote);
  }
}
