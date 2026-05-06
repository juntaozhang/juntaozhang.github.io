# Java基础

## 重写 vs 重载

| 特性 | 重写（Override） | 重载（Overload） |
|------|----------------|----------------|
| 发生位置 | 子类与父类之间 | 同一个类中 |
| 方法签名 | 相同 | 不同（参数列表不同） |
| 返回类型 | 相同或协变 | 可以不同 |
| 访问修饰符 | 不能更严格 | 可以不同 |
| 异常 | 不能抛出更宽泛的受检异常 | 可以不同 |

```java
// 重载示例
int add(int a, int b) {
    return a + b;
}

double add(double a, double b) {
    return a + b;
}
```

## JVM、JRE、JDK 的区别

| 组件 | 说明 |
|------|------|
| **JVM** | Java 虚拟机，负责执行 `.class` 字节码，是 Java 程序运行的核心 |
| **JRE** | Java 运行环境，包含 JVM + 核心类库 + 支持文件，用来运行 Java 程序 |
| **JDK** | Java 开发工具包，包含 JRE + 编译器（javac）+ 调试工具 + 开发工具等 |

> **注意**：Java 9 起，JRE 已被模块化合并进 JDK，官方不再单独发布 JRE。下载的 JDK 中自带运行所需的模块和 JVM。
>
> 模块（module）是 Java 9+ 的基础构建单元，`jlink` 利用模块系统构建一个仅包含所需功能的 mini-JRE，可用于发布、部署、微服务等场景，兼顾性能、安全和体积。

[simple run after Java 9](../module-info-example/build.sh)

## 值传递和引用传递

- `long` 是基本类型，传参时是**值传递**（复制值）
- 引用类型也一样是**值传递**，只不过复制的是引用的"值"而已

`String` 不是基本类型，而是一个**引用类型**（Reference Type），且是一个**不可变的对象**。

### Integer 缓存机制

Java 中关于 `Integer`（或 `Byte`、`Short`、`Long` 等包装类）在自动装箱/拆箱和对象缓存时的特殊行为——尤其是 `Integer` 值在 `-128` 到 `127` 之间的对象缓存机制，所以在这个范围内使用 `==` 返回 `true`，但超出这个范围时就不再复用，结果可能是 `false`。

这是一个同一种类型，行为不一致的典型"坑点"：

```text
Integer a = 1;
Integer b = 1;
System.out.println(a == b); // true

a = 128;
b = 128;
System.out.println(a == b); // false

int c = 128, d = 128;
System.out.println(c == d); // true
```

## `==` 和 `equals` 的区别

- 若比较的是**基本类型**（如 `int`、`long`），`==` 比较的是**值**
- 若比较的是**引用类型**（如 `String`、`Integer`、`Object`），`==` 比较的是**对象引用是否相同**，即是否指向同一内存地址

## `hashCode()` 与 `equals()` 的关系

- 如果两个对象 `equals()` 相等，那么它们的 `hashCode()` 必须相等
- 如果两个对象 `hashCode()` 相等，它们不一定 `equals()` 相等（哈希冲突）
- 重写 `equals()` 时必须同时重写 `hashCode()`，以维护上述契约

## String 的特性

1. **不变性**：`String` 是只读字符串，是一个典型的 immutable 对象，对它进行任何操作，其实都是创建一个新的对象，再把引用指向该对象。不变模式的主要作用在于当一个对象需要被多线程共享并频繁访问时，可以保证数据的一致性
2. **常量池优化**：`String` 对象创建之后，会在字符串常量池中进行缓存，如果下次创建同样的对象时，会直接返回缓存的引用
3. **final**：使用 `final` 来定义 `String` 类，表示 `String` 类不能被继承，提高了系统的安全性

### HashMap 用 String 做 key 有什么好处？

`HashMap` 通过 key 的 `hashCode` 来确定 value 的存储位置，因为字符串是不可变的，所以当创建字符串后，它的 `hashCode` 被缓存下来，不需要再次计算，所以相比于其他对象更快。

## String、StringBuffer、StringBuilder 的区别

| 特性 | String | StringBuffer | StringBuilder |
|------|--------|--------------|---------------|
| 可变性 | 不可变 | 可变 | 可变 |
| 线程安全 | 安全（不可变） | 安全（synchronized） | 不安全 |
| 性能 | 低（每次操作创建新对象） | 中 | 高 |
| 适用场景 | 字符串常量 | 多线程字符串操作 | 单线程字符串操作 |

## Error 和 Exception 的区别

JAVA 标准库内建了一些通用的异常，这些类以 `Throwable` 为顶层父类。`Throwable` 又派生出 `Error` 类和 `Exception` 类。

### Error

`Error` 属于程序无法处理的错误，我们没办法通过 `catch` 来进行捕获。例如，系统崩溃，内存不足，堆栈溢出等，编译器不会对这类错误进行检测，一旦这类错误发生，通常应用程序会被终止，仅靠应用程序本身无法恢复。

### Exception

`Exception` 以及它的子类，代表程序运行时发生的各种不期望发生的事件。可以被 Java 异常处理机制使用，是异常处理的核心。`Exception` 又可以分为：

- **运行时异常**（`RuntimeException`，又叫非受检异常）
- **非运行时异常**（又叫受检异常）

### 非受检异常和受检异常的区别

是否强制要求调用者必须处理此异常，如果强制要求调用者必须进行处理，那么就使用受检异常，否则就选择非受检异常。

## 常见异常

| 异常 | 说明 |
|------|------|
| `java.lang.IllegalAccessError` | 违法访问错误。当一个应用试图访问、修改某个类的域（Field）或者调用其方法，但是又违反域或方法的可见性声明，则抛出该异常 |
| `java.lang.InstantiationError` | 实例化错误。当一个应用试图通过 Java 的 `new` 操作符构造一个抽象类或者接口时抛出该异常 |
| `java.lang.OutOfMemoryError` | 内存不足错误。当可用内存不足以让 Java 虚拟机分配给一个对象时抛出该错误 |
| `java.lang.StackOverflowError` | 堆栈溢出错误。当一个应用递归调用的层次太深而导致堆栈溢出或者陷入死循环时抛出该错误 |
| `java.lang.ClassCastException` | 类造型异常。假设有类 A 和 B（A 不是 B 的父类或子类），O 是 A 的实例，那么当强制将 O 构造为类 B 的实例时抛出该异常 |
| `java.lang.ClassNotFoundException` | 找不到类异常。当应用试图根据字符串形式的类名构造类，而在遍历 CLASSPATH 之后找不到对应名称的 class 文件时，抛出该异常 |
| `java.lang.ArithmeticException` | 算术条件异常。譬如：整数除零等 |
| `java.lang.ArrayIndexOutOfBoundsException` | 数组索引越界异常。当对数组的索引值为负数或大于等于数组大小时抛出 |
| `java.lang.IndexOutOfBoundsException` | 索引越界异常。当访问某个序列的索引值小于 0 或大于等于序列大小时，抛出该异常 |
| `java.lang.InstantiationException` | 实例化异常。当试图通过 `newInstance()` 方法创建某个类的实例，而该类是一个抽象类或接口时，抛出该异常 |
| `java.lang.NoSuchFieldException` | 属性不存在异常。当访问某个类的不存在的属性时抛出该异常 |
| `java.lang.NoSuchMethodException` | 方法不存在异常。当访问某个类的不存在的方法时抛出该异常 |
| `java.lang.NullPointerException` | 空指针异常。当应用试图在要求使用对象的地方使用了 `null` 时，抛出该异常 |
| `java.lang.NumberFormatException` | 数字格式异常。当试图将一个 `String` 转换为指定的数字类型，而该字符串确不满足数字类型要求的格式时，抛出该异常 |
| `java.lang.StringIndexOutOfBoundsException` | 字符串索引越界异常。当使用索引值访问某个字符串中的字符，而该索引值小于 0 或大于等于序列大小时，抛出该异常 |

## 反射

### --add-opens

```bash
--add-opens java.base/java.lang=ALL-UNNAMED
```
开放给所有非模块化的代码

它的作用是：将 `java.base` 模块中的 `java.lang` 包的"反射访问权限"开放给 unnamed modules（即没有 `module-info` 的传统类或库）。

- 一个包含 `module-info.java` 并指定模块名的项目就是 **named module**。你需要使用 `--module-path` 和 `--module` 来编译/运行它。这种模块在 Java 9+ 模块系统中具有强封装、安全性高、依赖明确的优点
- Java 模块系统（JPMS）设计是为了从根上解决"类路径地狱"和 JAR 隐私问题，它在语言设计上是先进的，但现实中由于兼容性、学习曲线、框架适配等原因，还远未成为主流，更多用于高封装需求、运行时裁剪、平台开发场景

### JAR 冲突使用 Java 9 模块解决
```
├── lib-a
│         ├── pom.xml
│         └── src
│             └── main
│                 └── java
│                     ├── module-info.java
│                     └── org
│                         └── example
│                             └── Util.java
├── lib-b
│         ├── pom.xml
│         └── src
│             └── main
│                 └── java
│                     ├── module-info.java
│                     └── org
│                         ├── b
│                         │   └── Person.java
│                         └── example
│                             └── Util.java
└── main
    ├── pom.xml
    └── src
        └── main
            └── java
                ├── module-info.java
                └── org
                    └── example
                        └── main
                            └── App.java
```
[App.java](../module-info-example/main/src/main/java/org/example/main/App.java)

> java --add-opens lib.b/org.b=main -p lib-a/target/classes:lib-b/target/classes:main/target/classes -m main/org.example.main.App

or
> java -cp lib-a/target/classes:lib-b/target/classes:main/target/classes org.example.main.App


## 泛型

泛型是一种**语法糖**。泛型只存在于编译阶段，而不存在于运行阶段。在编译后的 class 文件中，是没有泛型这个概念的。

### 泛型的优点

泛型的主要目标是**提高 Java 程序的类型安全**：

- 编译时期就可以检查出因 Java 类型不正确导致的 `ClassCastException` 异常
- 符合**越早出错代价越小**原则

### Java 逆变
[ContravarianceDemo.java](../java-example/src/main/java/com/example/ContravarianceDemo.java)

逆变的意思就是：方法变量定义比较宽泛，如果传进来子类，可以满足条件
 
`(cat) -> ...` 表达式是 `(animal) -> ...` 的父类
```text
Function<Animal, String> animalFeeder = (animal) -> "喂了动物";
Function<Cat, String> catFeeder = (cat) -> "喂了猫";
catFeeder = animalFeeder;
```

## 序列化与反序列化

### `serialVersionUID`

当实现 `Serializable` 的类发生影响结构变化时，必须手动更新 `serialVersionUID`，以防反序列化失败。阿里巴巴手册强制要求显式声明它，避免使用 JVM 自动生成的 UID。

怎么兼容旧版本？
- [SerialVersionUIDTest.java](../java-example/src/main/java/com/example/SerialVersionUIDTest.java)
  - 只能在 `readObject` 兼容，有很大局限性。
- KryoSerializer 序列化不依赖 serialVersionUID
- 如果数据结构经常变，引入 Avro、Protobuf 等格式
- 数据湖做法：底层结构还是Parquet 或 orc
  - 有一层 manifest 描述这个表的变化

## 深拷贝与浅拷贝

| 类型 | 说明 |
|------|------|
| **浅拷贝** | 并不是真的拷贝，只是复制指向某个对象的指针，而不复制对象本身，新旧对象还是共享同一块内存 |
| **深拷贝** | 会另外创造一个一模一样的对象，新对象跟原对象不共享内存，修改新对象不会影响到原对象 |


## 基本数据类型
### [byte[], int, long三者之间的相互转换代码](https://gitee.com/zjt_hans/hello-java/tree/master/JavaSE/src/test/java/test/org/example/java/LongIntegerBytesConvertorTest.java)

|类型|字节数|位数|取值范围|备注|
| ---- | ---- | ---- | ---- | ---- |
|bit（位）|-|1位|0 / 1|二进制最小单位|
|byte（字节）|1|8位|-128 ~ 127<br>$-2^7 ～ 2^7-1$|存储基本单位<br>1字节=8位<br>1英文字母=1字节<br>1汉字=2字节|
|short|2|16位|-32768 ~ 32767<br>$-2^{15} ～ 2^{15}-1$|短整型|
|int|4|32位|-2147483648 ~ 2147483647<br>$-2^{31} ～ 2^{31}-1$|整型，默认字长|
|long|8|64位|-9223372036854774808 ~ 9223372036854774807<br>$-2^{63} ～ 2^{63}-1$|长整型|
|float|4|32位|$1.401298×10^{-45} ～ 3.402823×10^{38}$|单精度浮点数|
|double|8|64位|$4.9×10^{-324} ～ 1.797693×10^{308}$|双精度浮点数|


### java >>>(补零) >>(补符号位) <<
[ShiftingTest](https://gitee.com/zjt_hans/hello-java/tree/master/JavaSE/src/test/java/test/org/example/java/ShiftingTest.java)

### & |
[AndOrTest](https://gitee.com/zjt_hans/hello-java/tree/master/JavaSE/src/main/java/test/test/org/example/java/AndOrTest.java)


### 成员访问控制权限
- private 私：只有自己类能用
- public 公开：到处都能用
- default 默认：同包
- protected 保护：同包、跨包子类
