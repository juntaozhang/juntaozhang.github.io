package org.example.java.pattern.creational.prototype;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractSpoon implements Cloneable {
  private String spoonName;
  private int age;
  private List<String> user = new ArrayList<String>();

  public AbstractSpoon clone() throws CloneNotSupportedException {
    AbstractSpoon spoon = (AbstractSpoon) super.clone();
    spoon.user = new ArrayList<String>(this.user);
    return spoon;
  }

  public List<String> getUser() {
    return user;
  }

  public void setUser(List<String> user) {
    this.user = user;
  }

  public void setSpoonName(String spoonName) {
    this.spoonName = spoonName;
  }

  public String getSpoonName() {
    return this.spoonName;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
}

class SoupSpoon extends AbstractSpoon {
  public SoupSpoon() {
    setSpoonName("Soup Spoon");
    super.getUser().add("a");
    super.getUser().add("b");
  }
}

public class Client {
  public static void main(String[] args) throws CloneNotSupportedException {
    AbstractSpoon spoon = new SoupSpoon();
    spoon.setAge(2);
    AbstractSpoon spoon2 = spoon.clone();
    System.out.print(spoon == spoon2);
    spoon.setSpoonName("hans");
    spoon.getUser().remove("a");
    System.out.print(spoon.getUser() == spoon2.getUser());

  }
}
