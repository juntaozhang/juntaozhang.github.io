package org.example.java.pattern.creational.builder;

class Contact {
  private int age;
  private int safeID;
  private String name;
  private String address;

  private Contact(Builder b) {
    age = b.age;
    safeID = b.safeID;
    name = b.name;
    address = b.address;
  }

  public int getAge() {
    return age;
  }

  public int getSafeID() {
    return safeID;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public static class Builder {
    private int age = 0;
    private int safeID = 0;
    private String name = null;
    private String address = null;

    // 构建的步骤
    public Builder(String name) {
      this.name = name;
    }

    public Builder age(int val) {
      age = val;
      return this;
    }

    public Builder safeID(int val) {
      safeID = val;
      return this;
    }

    public Builder address(String val) {
      address = val;
      return this;
    }

    public Contact build() { // 构建，返回一个新对象
      return new Contact(this);
    }
  }
}


public class Client {
  public static void main(String[] args) {
    Contact contact = new Contact.Builder("Ace").age(10).address("beijing").build();
    System.out.println("name=" + contact.getName() + "age =" + contact.getAge() + "address" + contact.getAddress());
  }
}
