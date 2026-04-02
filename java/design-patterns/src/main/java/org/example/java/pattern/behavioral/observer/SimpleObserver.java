package org.example.java.pattern.behavioral.observer;

import java.util.Enumeration;
import java.util.Vector;

/**
 * 观察者模式定义了一种一对多的依赖关系，让多个观察者对象同时监听某一个主题对象。
 * 这个主题对象在状态上发生变化时，会通知所有观察者对象，使他们能够自动更新自己。
 */
interface Subject {
  public void attach(Observer observer);

  public void detach(Observer observer);

  void notifyObservers();
}

class ConcreteSubject implements Subject {
  private Vector<Observer> observersVector = new Vector<Observer>();

  public void attach(Observer observer) {
    observersVector.addElement(observer);
  }

  public void detach(Observer observer) {
    observersVector.removeElement(observer);
  }

  public void notifyObservers() {
    Enumeration enumeration = observers();
    while (enumeration.hasMoreElements()) {
      ((Observer) enumeration.nextElement()).update();
    }
  }

  public Enumeration observers() {
    return ((Vector) observersVector.clone()).elements();
  }
}

interface Observer {
  void update();
}

class ConcreteObserver1 implements Observer {
  public void update() {
    // Write your code here
    System.out.println("ConcreteObserver1 update.");
  }
}

class ConcreteObserver2 implements Observer {
  public void update() {
    // Write your code here
    System.out.println("ConcreteObserver2 update.");
  }
}

public class SimpleObserver {
  public static void main(String args[]) {
    Subject subject = new ConcreteSubject();
    subject.attach(new ConcreteObserver1());
    subject.attach(new ConcreteObserver2());
    subject.notifyObservers();
  }
}
