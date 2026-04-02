package org.example.java.pattern.structural.flyweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//抽象享元角色类
interface Flyweight {
  // 一个示意性方法，参数state是外蕴状态
  public void operation(String state);
}

class ConcreteFlyweight implements Flyweight {
  private Character intrinsicState = null;

  public ConcreteFlyweight(Character state) {
    this.intrinsicState = state;
  }

  @Override
  public void operation(String state) {
    System.out.println("Intrinsic State = " + this.intrinsicState);
    System.out.println("Extrinsic State = " + state);
  }
}

class ConcreteCompositeFlyweight implements Flyweight {
  private Map<Character, Flyweight> files = new HashMap<Character, Flyweight>();

  public void add(Character key, Flyweight fly) {
    files.put(key, fly);
  }

  @Override
  public void operation(String state) {
    for (Character c : files.keySet()) {
      files.get(c).operation(state);
    }
  }
}

class FlyweightFactory {
  private Map<Character, Flyweight> files = new HashMap<Character, Flyweight>();

  public Flyweight getFlyweight(Character state) {
    Flyweight fly = files.get(state);
    if (fly == null) {
      fly = new ConcreteFlyweight(state);
      files.put(state, fly);
    }
    return fly;
  }

  public Flyweight getFlyweight(List<Character> compositeState) {
    ConcreteCompositeFlyweight compositeFly = new ConcreteCompositeFlyweight();
    for (Character state : compositeState) {
      compositeFly.add(state, this.getFlyweight(state));
    }
    return compositeFly;
  }
}

public class Client {
  public static void main(String[] args) {
/*    FlyweightFactory factory = new FlyweightFactory();
    Flyweight fly = factory.getFlyweight('a');
    fly.operation("First Call");

    fly = factory.getFlyweight('b');
    fly.operation("Second Call");

    fly = factory.getFlyweight('a');
    fly.operation("Third Call");*/

    List<Character> compositeState = new ArrayList<Character>();
    compositeState.add('a');
    compositeState.add('b');
    compositeState.add('c');
    compositeState.add('a');
    compositeState.add('b');

    FlyweightFactory flyFactory = new FlyweightFactory();
    Flyweight compositeFly1 = flyFactory.getFlyweight(compositeState);
    Flyweight compositeFly2 = flyFactory.getFlyweight(compositeState);
    compositeFly1.operation("Composite Call");

    System.out.println("---------------------------------");
    System.out.println("复合享元模式是否可以共享对象：" + (compositeFly1 == compositeFly2));

    Character state = 'a';
    Flyweight fly1 = flyFactory.getFlyweight(state);
    Flyweight fly2 = flyFactory.getFlyweight(state);
    System.out.println("单纯享元模式是否可以共享对象：" + (fly1 == fly2));
    fly1.operation("Simple Call");
  }
}
