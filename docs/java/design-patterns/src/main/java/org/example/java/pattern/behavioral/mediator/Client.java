package org.example.java.pattern.behavioral.mediator;

import java.util.ArrayList;
import java.util.List;

abstract class Colleague {
  private Mediator mediator;
  private String data;

  public Colleague(Mediator m) {
    mediator = m;
  }

  public Mediator getMediator() {
    return mediator;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  // 发送消息
  public abstract void sendData();

  // 收到消息
  public abstract void getData(String data);

  // 发送消息
  public void sendData(String data) {
    this.data = data;
    mediator.action(this);
  }
}

class ConcreteColleagueView extends Colleague {
  public ConcreteColleagueView(Mediator m) {
    super(m);
  }

  public void getData(String data) {
    System.out.println("view receive data " + data);
  }

  public void sendData() {
    System.out.println("view send data " + getData());
  }
}

class ConcreteColleagueModel extends Colleague {
  public ConcreteColleagueModel(Mediator m) {
    super(m);
  }

  public void getData(String data) {
    System.out.println("model receive data " + data);
  }

  public void sendData() {
    System.out.println("model send data " + getData());
  }
}

// 中介者
interface Mediator {
  // Mediator针对Colleague的一个交互行为
  void action(Colleague sender);

  // 加入Colleague对象
  void addColleague(Colleague colleague);
}

class ConcreteMediatorController implements Mediator {
  private List<Colleague> colleagues = new ArrayList<Colleague>(0);

  public void addColleague(Colleague colleague) {
    colleagues.add(colleague);
  }

  public void action(Colleague actor) {
    String data = actor.getData();
    for (Colleague colleague : colleagues) {
      if (colleague.equals(actor)) {
        colleague.sendData();
      }
    }
    for (Colleague colleague : colleagues) {
      if (!colleague.equals(actor)) {
        colleague.getData(data);
      }
    }
  }
}

// 测试类
public class Client {
  public static void main(String[] args) {
    // 生成中介者 并注入到各个Colleague对象中
    Mediator mediator = new ConcreteMediatorController();
    Colleague view = new ConcreteColleagueView(mediator);
    Colleague model = new ConcreteColleagueModel(mediator);

    // 注册对象到中介
    mediator.addColleague(view);
    mediator.addColleague(model);

    // view 触发行为
    view.sendData("[DTO1] .");

    // model 触发行为
    model.sendData("[DTO2].");
  }
}
