package org.example.java.pattern.structural.facade;

class DrawerOne {
  public void open() {
    System.out.println("第一个抽屉被打开了");
    getKey();
  }

  public void getKey() {
    System.out.println("得到第二个抽屉的钥匙");
  }

}

class DrawerTwo {
  public void open() {
    System.out.println("第二个抽屉被打开了");
    getFile();
  }

  public void getFile() {
    System.out.println("得到这个重要文件");
  }
}

class DrawerFacade {
  DrawerOne one = new DrawerOne();
  DrawerTwo two = new DrawerTwo();

  public void open() {
    one.open();
    two.open();
  }
}

public class DrawerTest {
  //目的：获得重要文件
  public static void main(String[] args) {
    /*DrawerOne one = new DrawerOne();
    DrawerTwo two = new DrawerTwo();
    one.open();
    two.open();*/

    DrawerFacade drawer=new DrawerFacade();
    drawer.open();
  }
}
