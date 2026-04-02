package test.org.example.java.pattern.visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 增加行的visitor 十分方便（对于原来的类层次增加新的操作，仅仅需要实现一个具体访问者角色就可以了）
 * node结构不能改变 如果改了 代价很大
 * visitor与Structure破坏了node的封装性，
 */
abstract class Node {
  private String id;

  String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }
  abstract void accept(Visitor visitor);
}

class Node1 extends Node {
  private String node1;

  String getNode1() {
    return node1;
  }

  void setNode1(String node1) {
    this.node1 = node1;
  }

  public void accept(Visitor visitor) {
    visitor.node1(this);
  }
}

class Node2 extends Node {
  private String node2;

  String getNode2() {
    return node2;
  }

  void setNode2(String node2) {
    this.node2 = node2;
  }

  public void accept(Visitor visitor) {
    visitor.node2(this);
  }
}

interface Visitor {
  void node1(Node1 node);

  void node2(Node2 node2);
}

class Visitor1 implements Visitor {
  @Override
  public void node1(Node1 node) {
    System.out.println("visitor1 -> node1");
  }

  @Override
  public void node2(Node2 node2) {
    System.out.println("visitor1 -> node2");
  }
}

class Visitor2 implements Visitor {
  @Override
  public void node1(Node1 node) {
    System.out.println("visitor2 -> node1");
  }

  @Override
  public void node2(Node2 node2) {
    System.out.println("visitor2 -> node2");
  }
}

class Visitor3 implements Visitor {
  @Override
  public void node1(Node1 node) {
    System.out.println("visitor3 -> node1");
  }

  @Override
  public void node2(Node2 node2) {
    System.out.println("visitor3 -> node2");
  }
}

class Structure {
  private List<Node> nodeList = new ArrayList<Node>();

  public void action(Visitor visitor) {
    for (Node n : nodeList) {
      n.accept(visitor);
    }
  }

  public void add(Node node) {
    nodeList.add(node);
  }
}

public class Test {
  public static void main(String[] args) {
    Structure structure = new Structure();
    structure.add(new Node1());
    structure.add(new Node2());

    structure.action(new Visitor1());
    structure.action(new Visitor2());
  }
}
