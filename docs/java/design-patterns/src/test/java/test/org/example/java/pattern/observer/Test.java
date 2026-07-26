package test.org.example.java.pattern.observer;

import java.util.Observable;
import java.util.Observer;

/**
 * User: ZhangJuntao
 * Date: 13-10-16
 * Time: 下午9:35
 */

class MyObservable extends Observable {
  private String state;

  String getState() {
    return state;
  }

  void setState(String state) {
    if (!state.equals(this.state)) {
      this.state = state;
      setChanged();
      notifyObservers();
    }
  }
}

class Observer1 implements Observer {
  @Override
  public void update(Observable o, Object arg) {
    System.out.println("notify Observer1");
  }
}

class Observer2 implements Observer {
  @Override
  public void update(Observable o, Object arg) {
    System.out.println("notify Observer2");
  }
}


public class Test {
  public static void main(String[] args) {
    MyObservable myObservable = new MyObservable();
    myObservable.addObserver(new Observer1());
    myObservable.addObserver(new Observer2());

    myObservable.setState("1");
    myObservable.setState("2");
  }
}
