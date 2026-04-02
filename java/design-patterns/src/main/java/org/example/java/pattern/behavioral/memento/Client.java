package org.example.java.pattern.behavioral.memento;

//此为备忘录，用来对原发器对象的状态state进行保存
class Memento {
  private String state;

  public Memento(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}

//  发起人:
// 创建一个含有当前的内部状态的备忘录对象。
// 使用备忘录对象存储其内部状态。
class Originator {
  private String state;

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Memento createMemento() {
    return new Memento(state);
  }

  public void useMemento(Memento m) {
    this.state = m.getState();
  }

  public void showState() {
    System.out.println(state);
  }
}

/**
 * 负责人
 * 1)负责保存备忘录对象。
 * 2)不检查备忘录对象的内容。
 */
class CareTaker {
  private Memento memento;

  public Memento getMemento() {
    return memento;
  }

  public void setMemento(Memento memento) {
    this.memento = memento;
  }
}

public class Client {
  public static void main(String[] args) {
    Originator o = new Originator();
    CareTaker ca = new CareTaker();
    o.setState("SLEEP");
    ca.setMemento(o.createMemento());
    o.setState("STUDY");
    o.showState();
    o.useMemento(ca.getMemento());
    o.showState();
  }
}
