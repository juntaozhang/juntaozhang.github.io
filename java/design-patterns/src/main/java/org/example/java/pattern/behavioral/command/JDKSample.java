package org.example.java.pattern.behavioral.command;

/**
 * User: ZhangJuntao
 * Date: 13-10-23
 * Time: 上午7:29
 */
public class JDKSample {
  static class ConcreteCommandA implements Runnable {
    @Override
    public void run() {
      System.out.println("do something.");
    }
  }

  public static void main(String[] args) {
    ConcreteCommandA commandA = new ConcreteCommandA();
    new Thread(commandA).start();
  }

}
