package org.example.java.pattern.behavioral.visitor.customer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 访问者模式实现的基本思路：
 * 定义一个就接口来代表要新加入的功能，为了通用，也就是定义一个通用的功能方法来代表新加入的功能。
 * 在对象结构上添加一个方法，作为通用的方法，也就是可以代表要添加的功能，在这个方法中传入具体的实现新功能的对象。
 * 在对象实现的具体实现对象中实现这个方法，回调传入具体的实现新功能的对象，就相当于调用到新功能上了。
 * <p/>
 * 访问者的功能：能给一系列对象透明第添加新功能，从而避免在维护期间对一系列对象进行修改，而且还能变相实现复用访问者所具有的功能。
 * <p/>
 * 访问者模式的本质：预留通路，实现回调。
 * <p/>
 * 访问者模式的优缺点：
 * 好的扩展性：能在不修改对象结构的前提下，为对象结构中的元素添加新功能。
 * 好的复用性：可以通过访问者来定义整个对象结构通用的功能，从而提高复用的程度。
 * 分离无关行为：把相关的行为封装在一起，构成一个访问者，这样每个访问者都比较单一。
 * 对象结构很难变化：不适用于对象结构中类经常变化的情况，因为对象结构发生了改变，访问者的接口和访问者得实现都要发生相应的改变，代价太高。
 * 破坏封装：通常需要对象结构开放内部数据给访问者和ObjectStructure，破坏了对象的封装性。
 */
abstract class Customer {
  private String customerId;
  private String name;

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * 接受访问者的访问
   *
   * @param visitor
   */
  public abstract void accept(Visitor visitor);
}

class PersonalCustomer extends Customer {
  private int age;

  int getAge() {
    return age;
  }

  void setAge(int age) {
    this.age = age;
  }

  @Override
  public void accept(Visitor visitor) {
    //回调访问者对象的方法
    visitor.visitPersonalCustomer(this);
  }
}

class EnterpriseCustomer extends Customer {
  private int size;

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void accept(Visitor visitor) {
    //回调访问者对象的方法
    visitor.visitEnterpriseCustomer(this);
  }
}

interface Visitor {
  void visitPersonalCustomer(PersonalCustomer entity);

  void visitEnterpriseCustomer(EnterpriseCustomer entity);
}

/**
 * 具体的访问者，实现对客户的偏好分析
 */
class PredilectionAnalyzeVisitor implements Visitor {
  @Override
  public void visitEnterpriseCustomer(EnterpriseCustomer ec) {
    // 根据以往的购买历史、潜在购买意向，以及客户所在行业的发展趋势、客户的发展趋势等的分析
    System.out.println("现在对企业客户" + ec.getName() + "进行产品偏好分析");
  }

  @Override
  public void visitPersonalCustomer(PersonalCustomer pc) {
    System.out.println("现在对个人客户" + pc.getName() + "进行产品偏好分析");
  }
}

/**
 * 具体的访问者，实现客户提出服务请求的功能
 */
class ServiceRequestVisitor implements Visitor {

  @Override
  public void visitEnterpriseCustomer(EnterpriseCustomer ec) {
    // 企业客户提出的具体服务请求
    System.out.println(ec.getName() + "企业提出服务请求");
  }

  @Override
  public void visitPersonalCustomer(PersonalCustomer pc) {
    // 个人客户提出的具体服务请求
    System.out.println("客户" + pc.getName() + "提出服务请求");
  }
}

class ObjectStructure {

  /**
   * 要操作的客户集合
   */
  private Collection<Customer> col = new ArrayList<Customer>();

  /**
   * 提供客户端操作的高层接口，具体的功能由客户端传入的访问者决定
   *
   * @param visitor 客户端需要的访问者
   */
  public void handleRequest(Visitor visitor) {
    for (Customer cm : col) {
      cm.accept(visitor);
    }
  }

  /**
   * 组建对象结构，想对象中添加元素
   * 不同的对象结构有不同的构建方式
   *
   * @param ele 加入到对象的结构元素
   */
  public void addElement(Customer ele) {
    this.col.add(ele);
  }
}

public class Client {
  public static void main(String[] args) {
    ObjectStructure os = new ObjectStructure();
    //实体 不常变化
    Customer cml = new EnterpriseCustomer();
    cml.setName("ABC集团");
    os.addElement(cml);

    Customer cm2 = new EnterpriseCustomer();
    cm2.setName("CDE公司");
    os.addElement(cm2);

    Customer cm3 = new PersonalCustomer();
    cm3.setName("张三");
    os.addElement(cm3);

    //visitor 常变
    ServiceRequestVisitor srVisitor = new ServiceRequestVisitor();
    os.handleRequest(srVisitor);

    PredilectionAnalyzeVisitor paVisitor = new PredilectionAnalyzeVisitor();
    os.handleRequest(paVisitor);

  }
}
