/**
 * @author juntao zhang
 */
public class Visitor {

  static class FirstTest {
    static class Person{
    }

    static class Man extends Person{
    }

    static class Test {
      void run(Person person) {
        System.out.println("this is person fun");
      }
      void run(Man man) {
        System.out.println("this is man fun");
      }
    }

    public static void main(String[] args) {
      Person person = new Man();
      Test test = new Test();
      test.run(person);
      test.run((Man) person);
    }
  }

  static class SecondTest {
    static class Person{
      void run() {
        System.out.println("this is person fun");
      }
    }

    static class Man extends Person{
      @Override
      void run() {
        System.out.println("this is man fun");
      }
    }

    public static void main(String[] args) {
      Person person = new Man();
      person.run();
      ((Man) person).run();
    }
  }

  static class ThirdTest {
    static class Person{
    }

    static class Man extends Person{
    }

    static class VisitorTest {
      void run(Person person) {
        if(person instanceof Man){
          System.out.println("this is man fun");
        }else{
          System.out.println("this is person fun");
        }
      }
    }

    public static void main(String[] args) {
      Person person = new Man();
      VisitorTest test = new VisitorTest();
      test.run(person);
      test.run((Man) person);
    }
  }

  static class FourthTest {
    static class Person{
      void accept(Visitable myVisitor) {
        myVisitor.run(this);
      }
    }

    static class Man extends Person{
      @Override
      void accept(Visitable myVisitor) {
        myVisitor.run(this);
      }
    }

    interface Visitable {

      void run(Person person);

      void run(Man person);
    }

    static class Visitor1 implements Visitable {
      public void run(Person person) {
        System.out.println("1.this is person fun");
      }
      public void run(Man man) {
        System.out.println("1.this is man fun");
      }
    }

    static class Visitor2 implements Visitable {
      public void run(Person person) {
        System.out.println("2.this is person fun");
      }
      public void run(Man man) {
        System.out.println("2.this is man fun");
      }
    }

    public static void main(String[] args) {
      Person person = new Man();
      Visitable visitor = new Visitor1();
      Visitable visitor2 = new Visitor2();
      person.accept(visitor);
      ((Man) person).accept(visitor2);
    }
  }
}
