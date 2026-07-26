package org.example.java.pattern.principle.lsp;

import java.util.HashMap;
import java.util.Map;

/**
 * 里氏替换原则
 * 覆盖或实现父类的方法输入参数可以被放大，反之不行
 */
class Father {
  public void doSomething(HashMap map) {
    System.out.println("father do something.");
  }
}

class Son extends Father {
  public void doSomething(Map map) {
    System.out.println("son do something.");
  }
}

//class Father {
//  public void doSomething(HashMap map) {
//    System.out.println("father do something.");
//  }
//}
//
//class Son extends Father {
//  public void doSomething(HashMap map) {
//    System.out.println("son do something.");
//  }
//}

//class Father {
//  public void doSomething(Map map) {
//    System.out.println("father do something.");
//  }
//}
//
//class Son extends Father {
//  public void doSomething(HashMap map) {
//    System.out.println("son do something.");
//  }
//}

public class Client {
  public static void main(String[] args) {
    Father f = new Father();
    f.doSomething(new HashMap());

    Son s = new Son();
    s.doSomething(new HashMap());
  }
}
