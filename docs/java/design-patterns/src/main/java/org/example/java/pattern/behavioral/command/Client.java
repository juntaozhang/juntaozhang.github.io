package org.example.java.pattern.behavioral.command;

/**
 * 缺点
 * 使用命令模式可能会导致某些系统有过多的具体命令类。
 * 因为针对每一个命令都需要设计一个具体命令类，因此某些系统可能需要大量具体命令类，这将影响命令模式的使用。
 * <p/>
 * 适用环境
 * 1.系统需要将请求调用者和请求接收者解耦，使得调用者和接收者不直接交互。
 * 2.系统需要在不同的时间指定请求、将请求排队和执行请求。
 * 3.系统需要支持命令的撤销(Undo)操作和恢复(Redo)操作。
 * 4.系统需要将一组操作组合在一起，即支持宏命令。
 */
interface Command {
  void execute();
}

class ConcreteCommand implements Command {
  private Receiver receiver = null;

  ConcreteCommand(Receiver receiver) {
    this.receiver = receiver;
  }

  public void execute() {
    receiver.action();
  }
}


class Receiver {
  //真正执行命令操作的功能代码
  public void action() {
    System.out.println("receiver action.");
  }
}


class Invoker {
  private Command command = null;

  public void setCommand(Command command) {
    this.command = command;
  }

  public void runCommand() {
    command.execute();
  }
}

public class Client {
  public void assemble() {
    //创建接收者
    Receiver receiver = new Receiver();
    //创建命令对象，设定它的接收者
    Command command = new ConcreteCommand(receiver);
    //创建Invoker，把命令对象设置进去
    Invoker invoker = new Invoker();
    invoker.setCommand(command);
    invoker.runCommand();
  }

  public static void main(String[] args) {
    new Client().assemble();
  }
}
