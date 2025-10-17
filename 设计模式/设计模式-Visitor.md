## 引子
1.方法重载
```java
class FirstTest {
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
    // 观察有什么区别
    test.run(person);//①
    test.run((Man) person);//②
  }
}
```

执行结果是:
```
this is person fun
this is man fun
```
方法重载:根据对象引用来调用对应方法,①引用是Person②引用是Man

2.方法重写
再看一个例子
```java
class SecondTest {
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
    Man man = new Man();
    Person person = man;
    person.run();//①
    man.run();//②
  }
}
```

执行结果:
```
this is man fun
this is man fun
```
不管对象引用是Man还是Person,都执行的是实际对象的方法

### 纯理论解释分派问题

`Person person = new Man();`
- 静态类型:变量被声明时的类型,如上例中person静态类型是`Person`,上文说的引用
- 实际类型:变量所引用的对象的真实类型,如上例中person实际类型是`Man`,对应上文说的实际对象

分派:根据对象类型(方法所属对象,参数对象)而对方法进行的选择
  - 静态分派发生在编译时期，分派根据静态类型信息发生
    如FirstTest重载(重载方法的分派是根据静态类型进行的。这个分派过程在编译时期就完成了),
  - 动态分派发生在运行时期，动态地置换掉某个方法
    如SecondTest重写,这就是所谓的多态性,编译期间编译器并不知道`person`引用指向的实际类型是什么,运行期间发现是`Man`类型
    于是替换成`Man.run`
  - 单分派,只根据一种对象类型确定方法
  - 多分派,根据多种对象类型确定方法
  
java/scala是静态多分派(FirstTest),动态单分派(SecondTest)语言
  - 静态多分派:编译期间,根据方法所属对象实际类型和参数对象静态类型来确定方法
  - 动态单分派:运行期间,根据方法所属对象实际类型动态地置换掉该方法

### 虚函数机制分析

方法重写体现了虚函数机制:

1. **编译期**: 编译器看到 `person.run()` 时，通过静态类型 `Person` 确定方法签名
2. **运行期**: JVM 通过虚函数表(vtable)根据实际对象类型 `Man` 找到真正执行的方法

**虚函数表机制**:
- 每个类都有一个虚函数表，存储该类所有虚方法的地址
- 对象创建时，对象头部包含指向其类虚函数表的指针
- 方法调用时，通过对象→vtable→具体方法实现动态分派

#### Code Generation (CodeGen) 优化

虚函数调用的性能优化技术:

1. **编译时优化**: 将多态调用转换为直接调用，避免虚函数表查找
2. **代码生成**: 根据具体类型生成特化代码，消除运行时分派开销
3. **JIT配合**: 与JVM即时编译器协作，实现方法内联和去虚拟化

**应用场景**: Spark等大数据框架通过CodeGen将表达式树编译为Java字节码，显著提升执行性能


## visitor模式应用
实际项目中我们需要用到动态多分派怎么办?
即我们需要更加方法所属对象和参数所属对象的实际类型来确定方法,也就是说我们希望`FirstTest`中①②都输出`this is man fun`
于是我们对`FirstTest`做了修改如下:
```java
  static class ThirdTest {
    static class Person{
    }
    static class Man extends Person{
    }

    static class VisitorTest {
      void visit(Person person) {
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
      test.visit(person);
      test.visit((Man) person);
    }
  }
```

我们发现又是利用`if else`来流程判断,谈不上优雅的设计,不符合'开闭原则(对扩展开,对修改闭)',于是我们提炼出来一种设计模式
访问者模式,我们把需要扩展的开放出来如下:
```java
  static class FourthTest {
    static class Person{
      void accept(Visitable myVisitor) {
        myVisitor.visit(this);
      }
    }

    static class Man extends Person{
      @Override
      void accept(Visitable myVisitor) {
        myVisitor.visit(this);
      }
    }

    interface Visitable {

      void visit(Person person);

      void visit(Man person);
    }

    static class Visitor1 implements Visitable {
      public void visit(Person person) {
        System.out.println("1.this is person fun");
      }
      public void visit(Man man) {
        System.out.println("1.this is man fun");
      }
    }

    static class Visitor2 implements Visitable {
      public void visit(Person person) {
        System.out.println("2.this is person fun");
      }
      public void visit(Man man) {
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
```
对于visitor使用场景,`Visitable`是对外开放的可扩展的,Person/Man访问对象是稳定的状态,一旦要增加一种访问对象比如Woman,
会导致所有Visitable实现全部要修改,Visitable方法也需要稳定,所以访问者模式要注意使用场景

## JDK中使用案例
`Files`中`walkFileTree`需要传入实现类`SimpleFileVisitor`,对于文件来说访问对象是稳定的,如目录/文件等,对访问者
是可扩展的,不同的场景对访问对象处理是不一样,如*.tmp文件删除,时间超过1分钟的文件修改名字...

```java
       Path start = ...
       Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
           @Override
           public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
               throws IOException
           {
               Files.delete(file);
               return FileVisitResult.CONTINUE;
           }
           @Override
           public FileVisitResult postVisitDirectory(Path dir, IOException e)
               throws IOException
           {
               if (e == null) {
                   Files.delete(dir);
                   return FileVisitResult.CONTINUE;
               } else {
                   // directory iteration failed
                   throw e;
               }
           }
       });
```

### Paimon 中的JdbcToPaimonTypeVisitor
```java
private static class MySqlToPaimonTypeVisitor implements JdbcToPaimonTypeVisitor {
    private static final MySqlToPaimonTypeVisitor INSTANCE = new MySqlToPaimonTypeVisitor();

    public DataType visit(String type, @Nullable Integer length, @Nullable Integer scale, TypeMapping typeMapping) {
        return MySqlTypeUtils.toDataType(type, length, scale, typeMapping);
    }
}

private static class PostgresToPaimonTypeVisitor implements JdbcToPaimonTypeVisitor {
    private static final PostgresToPaimonTypeVisitor INSTANCE = new PostgresToPaimonTypeVisitor();

    public DataType visit(String type, @Nullable Integer length, @Nullable Integer scale, TypeMapping typeMapping) {
        return PostgresTypeUtils.toDataType(type, length, scale, typeMapping);
    }
}
```
不是经典的 Visitor，更接近 Strategy + 简单 Factory 组合。命名存在误导，同时有点过度设计，
`JdbcToPaimonTypeVisitor` -> `DataTypeConverter`, 感觉直接删除这层visitor层直接在`JdbcSchemaUtils.buildSchema`传入`XXXTypeUtils.toDataType`。
如果把各种DataType统一抽象到visitor中，则visitor才有意义，目前所有类型都放到了`XXXTypeUtils`，在`toDataType` 枚举各种情况。

## 参考
- http://www.cnblogs.com/youxin/archive/2013/05/25/3099016.html
- JDK src


