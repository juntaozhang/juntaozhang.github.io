package org.example.java.pattern.structural.bridge;

abstract class AbstractMessage {
  //持有一个实现部分的对象
  MessageImplementor impl;

  public AbstractMessage(MessageImplementor impl) {
    this.impl = impl;
  }

  public void sendMessage(String message, String toUser) {
    this.impl.send(message, toUser);
  }
}

class CommonMessage extends AbstractMessage {
  public CommonMessage(MessageImplementor impl) {
    super(impl);
  }

  @Override
  public void sendMessage(String message, String toUser) {
    // 对于普通消息，直接调用父类方法，发送消息即可
    super.sendMessage(message, toUser);
  }
}

class UrgencyMessage extends AbstractMessage {
  public UrgencyMessage(MessageImplementor impl) {
    super(impl);
  }

  @Override
  public void sendMessage(String message, String toUser) {
    message = "加急：" + message;
    super.sendMessage(message, toUser);
    this.watch(message, toUser);
  }

  //扩展自己的新功能，监控某消息的处理状态
  public void watch(String message, String toUser) {
    System.out.println("监控【发送消息“" + message + "”给" + toUser + "】");
  }
}

interface MessageImplementor {
  public void send(String message, String toUser);
}

class MessageSMS implements MessageImplementor {
  @Override
  public void send(String message, String toUser) {
    //SMS实现
    System.out.println("短信，发送消息“" + message + "”给" + toUser);
  }
}

class MessageEmail implements MessageImplementor {
  @Override
  public void send(String message, String toUser) {
    // Email实现
    System.out.println("邮件，发送消息“" + message + "”给" + toUser);
  }
}

public class MessageTest {
  public static void main(String[] args) throws Exception {
    //创建具体的实现对象
    MessageImplementor impl = new MessageSMS();
    //创建普通消息对象
    AbstractMessage message = new UrgencyMessage(impl);
    message.sendMessage("申请", "李总");

    //将实现方式切换成邮件，再次发送
    impl = new MessageEmail();
    //创建加急消息对象
    message = new CommonMessage(impl);
    message.sendMessage("申请", "张总");
  }
}

