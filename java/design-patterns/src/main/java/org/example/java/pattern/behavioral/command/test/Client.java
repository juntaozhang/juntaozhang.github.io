package org.example.java.pattern.behavioral.command.test;

/**
 * User: ZhangJuntao
 * Date: 13-10-22
 * Time: 下午10:37
 */


interface Receiver {
  void doSomething();
}

class ReceiverA implements Receiver {

  @Override
  public void doSomething() {
    System.out.println("a do something.");
  }
}

class ReceiverB implements Receiver {

  @Override
  public void doSomething() {
    System.out.println("b do something.");
  }
}

abstract class Command {
  Receiver receiver;

  protected Command(Receiver receiver) {
    this.receiver = receiver;
  }

  public abstract void execute();
}

class CommandA extends Command {

  protected CommandA(Receiver receiver) {
    super(receiver);
  }

  @Override
  public void execute() {
    receiver.doSomething();
  }
}

class CommandB extends Command {
  protected CommandB(Receiver receiver) {
    super(receiver);
  }

  @Override
  public void execute() {
    receiver.doSomething();
  }
}

class Invoker {
  private Command command;

  Invoker(Command command) {
    this.command = command;
  }

  public void action() {
    command.execute();
  }
}

public class Client {
  public static void main(String[] args) {
//    Command test = new CommandA(new ReceiverB());
//    Invoker invoker = new Invoker(test);
//    invoker.action();

    //与模板方法结合
    Invoker invoker = new Invoker(new Command(new ReceiverB()) {
      @Override
      public void execute() {
        receiver.doSomething();
      }
    });
    invoker.action();
  }
}
