# JVM编译优化
----------
## JIT
just in time(即时编译)

javac把我们写的代码转换成java字节码
JVM通过解释字节码翻译成对应的机器指令

如果不需要的JVM解释,机器直接执行,那么速度会非常快,但是编译速度会很慢,于是引入JIT

### 解释器
>省去编译时间，立即执行

### 编译器
#### how？3阶段
1.高级中间代码
>方法内联，常量替换

2.低级中间代码
>控制检查消除，范围检查消除

3.机器代码
>逃逸分析

#### client vs erver
> C1获取更高的编译速度
> C2获取更好的编译质量

### 触发条件（热点代码）
- 被多次调用方法
- 循环体
- 基于计数器的热点探针方法

基本工作原理:
![JIT](https://juntaozhang.github.io/java/assets/imgs/JIT.png)

----------

## 编译优化技术
- 数组边界检查消除
```
int a[] = new int[10000];
for(int i=0;i<10000;i++){
	a[i]=i;//系统会自动进行上下界检查,但是从上下文可以判断不需要检查
}
```

#### 表达式优化
#### 方法内联

看上去虽然是把调用方法替换成目标方法,但是因为java的多态性,Java默认方法都是虚方法,对象只有运行阶段才知道其实际类型,所以需要采用激进优化,如果发生于接受者不一致的情况,取消内联

* final修饰, 非虚方法
* CHA, class hierarchy analysis(类型继承分析), 接口是否有多于一种实现,类是否存在子类等等
	
#### 逃逸分析

分析对象的动态作用域,是否被外部方法引用,是否被外部线程访问到

* **同步消除**: 方法内部StringBuffer被StringBuilder替换
* **栈上分配**: 对象占用随着栈帧出栈而销毁,既减轻gc又加快执行速度
* **标量替换**: 
```
  static class Person {
    int age;
  }
  public int test() {
    Person person = new Person();
    person.age = 10;
    //...
    return person.age;
  }
  //-----被优化后-----
  public int test() {
    int age = 10;
    //...
    return age;
  }
```