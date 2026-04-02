package org.example.java.pattern.behavioral.command.tv;

/**
 * 将来自客户端的请求传入一个对象，从而使你可用不同的请求对客户进行参数化。
 * 用于“行为请求者”与“行为实现者”解耦，可实现二者之间的松耦合，以便适应变化。分离变化与不变的因素。
 * <p/>
 * 　　在面向对象的程序设计中，一个对象调用另一个对象，一般情况下的调用过程是：创建目标对象实例；设置调用参数；调用目标对象的方法。
 * <p/>
 * 但在有些情况下有必要使用一个专门的类对这种调用过程加以封装，我们把这种专门的类称作command类。
 * <p/>
 * Command模式可应用于
 * a）整个调用过程比较繁杂，或者存在多处这种调用。这时，使用Command类对该调用加以封装，便于功能的再利用。
 * b）调用前后需要对调用参数进行某些处理。
 * c）调用前后需要进行某些额外处理，比如日志，缓存，记录历史操作等。
 * <p/>
 * Command模式有如下效果：
 * a）将调用操作的对象和知道如何实现该操作的对象解耦。
 * b）Command是头等对象。他们可以像其他对象一样被操作和扩展。
 * c）你可将多个命令装配成一个符合命令。
 * d）增加新的Command很容易，因为这无需改变现有的类。
 */

class TV {
  public int currentChannel = 0;

  public void turnOn() {
    System.out.println("The television is on.");
  }

  public void turnOff() {
    System.out.println("The television is off.");
  }

  public void changeChannel(int channel) {
    this.currentChannel = channel;
    System.out.println("Now TV channel is " + channel);
  }
}

interface Command {
  void execute();
}

class CommandOn implements Command {
  private TV tv;

  public CommandOn(TV tv) {
    this.tv = tv;
  }

  public void execute() {
    tv.turnOn();
  }
}

class CommandOff implements Command {
  private TV tv;

  public CommandOff(TV tv) {
    this.tv = tv;
  }

  public void execute() {
    tv.turnOff();
  }
}

class CommandChange implements Command {
  private TV tv;

  private int channel;

  public CommandChange(TV tv, int channel) {
    this.tv = tv;
    this.channel = channel;
  }

  public void execute() {
    tv.changeChannel(channel);
  }
}

//可以看作是遥控器吧
class Control {
  private Command onCommand, offCommand, changeChannel;

  public Control(Command on, Command off, Command channel) {
    onCommand = on;
    offCommand = off;
    changeChannel = channel;
  }

  public void turnOn() {
    onCommand.execute();
  }

  public void turnOff() {
    offCommand.execute();
  }

  public void changeChannel() {
    changeChannel.execute();
  }
}

public class Client {
  public void assemble() {
    // 命令接收者
    final TV tv = new TV();
//    // 开机命令
//    CommandOn on = new CommandOn(tv);
//    // 关机命令
//    CommandOff off = new CommandOff(tv);
//    // 频道切换命令
//    CommandChange channel = new CommandChange(tv, 2);
//    // 命令控制对象
//    Control control = new Control(on, off, channel);

    //与模板方法结合
    Control control = new Control(
        new Command() {
          @Override
          public void execute() {
            tv.turnOn();
          }
        },
        new Command() {
          @Override
          public void execute() {
            tv.turnOff();
          }
        },
        new Command() {
          @Override
          public void execute() {
            tv.changeChannel(2);
          }
        }
    );
    // 开机
    control.turnOn();
    // 切换频道
    control.changeChannel();
    // 关机
    control.turnOff();
  }

  public static void main(String[] args) {
    new Client().assemble();
  }
}
