package org.example.java.pattern.structural.adapter;

//目标接口
interface Target {
  public void request();
}

class Adaptee {
  public void specificRequest() {
    System.out.println("Adaptee specificRequest()");
  }
}

class Adapter extends Adaptee implements Target {
  public void request() {
    this.specificRequest();
  }
}

public class Client {
  public static void main(String[] args) {
    Target target = new Adapter();
    target.request();
  }
}
