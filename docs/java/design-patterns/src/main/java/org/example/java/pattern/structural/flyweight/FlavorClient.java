package org.example.java.pattern.structural.flyweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Order {
  abstract void sell();
}

class FlavorOrder extends Order {
  public String flavor;
  private int number = 100;

  // 获取咖啡口味
  public FlavorOrder(String flavor) {
    this.flavor = flavor;
  }

  @Override
  public void sell() {
    number--;
    System.out.println("卖出一份" + flavor + "的咖啡。");
  }
}

class FlavorFactory {
  private Map<String, Order> flavorPool = new HashMap<String, Order>();
  // 静态工厂,负责生成订单对象
  private static FlavorFactory flavorFactory = new FlavorFactory();

  private FlavorFactory() {
  }

  public static FlavorFactory getInstance() {
    return flavorFactory;
  }

  public Order getOrder(String flavor) {
    Order order = null;
    if (flavorPool.containsKey(flavor)) {// 如果此映射包含指定键的映射关系，则返回 true
      order = flavorPool.get(flavor);
    } else {
      order = new FlavorOrder(flavor);
      flavorPool.put(flavor, order);
    }
    return order;
  }

  public int getTotalFlavorsMade() {
    return flavorPool.size();
  }
}

public class FlavorClient {
  private static List<Order> orders = new ArrayList<Order>();

  // 增加订单
  private static void takeOrders(String flavor) {
    orders.add(FlavorFactory.getInstance().getOrder(flavor));
  }

  public static void main(String[] args) {
    // 增加订单
    takeOrders("摩卡");
    takeOrders("卡布奇诺");
    takeOrders("香草星冰乐");
    takeOrders("香草星冰乐");
    takeOrders("拿铁");
    takeOrders("卡布奇诺");
    takeOrders("拿铁");
    takeOrders("卡布奇诺");
    takeOrders("摩卡");
    takeOrders("香草星冰乐");
    takeOrders("卡布奇诺");
    takeOrders("摩卡");
    takeOrders("香草星冰乐");
    takeOrders("拿铁");
    takeOrders("拿铁");
    // 卖咖啡
    for (Order order : orders) {
      order.sell();
    }
    // 打印生成的订单java对象数量
    System.out.println("\n客户一共买了 " + orders.size() + " 杯咖啡! ");
    // 打印生成的订单java对象数量
    System.out.println("共生成了 " + FlavorFactory.getInstance().getTotalFlavorsMade() + " 个 FlavorOrder java对象! ");
  }
}
