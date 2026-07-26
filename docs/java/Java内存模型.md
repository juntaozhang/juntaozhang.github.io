# Java 内存模型（JMM）

- 主内存：物理内存RAM（所有CPU共享） + L3（CPU各个核共享）
- 工作内存：CPU 核心私有 L1 缓存 + L2 缓存 + CPU 寄存器
- volatile：立即刷新到RAM，并使其他线程工作内存失效，禁止排序
    - 不保证原子性

## volatile

### 保证内存可见性

可见性是指线程之间的可见性，一个线程修改的状态对另一个线程是可见的。也就是一个线程修改的结果，另一个线程马上就能看到。

CPU 会根据缓存一致性协议，强制线程重新从主内存加载最新的值到自己的工作内存中，而不是直接用 CPU 缓存中的值。

### 禁止指令重排序

CPU 是和缓存做交互的，但是由于 CPU 运行效率太高，所以会不等待当前命令返回结果从而继续执行下一个命令，就会有乱序执行的情况发生。

## `sleep` / IO 与主内存同步

`sleep` / IO 有可能让出 CPU 重新读取主内存，只是偶然同步了主内存，不可靠。

## 内存模型是什么？

### JMM 的读写流程（抽象模型）

1. 线程从主内存读取变量 → 放入自己的工作内存
2. 线程在工作内存中对变量进行读写
3. 变量修改后，需要同步回主内存
4. 线程能否看到共享变量的最新值，取决于是否使用了正确的同步机制（如 `volatile` 或 `synchronized`）

JMM 就是 **Java 内存模型（Java Memory Model）**。因为在不同的硬件生产商和不同的操作系统下，内存的访问有一定的差异，所以会造成相同的代码运行在不同的系统上会出现各种问题。所以 Java 内存模型（JMM）屏蔽掉各种硬件和操作系统的内存访问差异，以实现让 Java 程序在各种平台下都能达到一致的并发效果。

Java 内存模型规定所有的变量都存储在主内存中，包括实例变量、静态变量，但是不包括局部变量和方法参数。每个线程都有自己的工作内存，线程的工作内存保存了该线程用到的变量和主内存的副本拷贝，线程对变量的操作都在工作内存中进行。线程不能直接读写主内存中的变量。

每个线程的工作内存都是独立的，线程操作数据只能在工作内存中进行，然后刷回到主存。这是 Java 内存模型定义的线程基本工作方式。

## happens-before 规则

### A/B 相同或不同线程的操作

A happens-before B，就是"在多线程环境下，A 的执行结果对 B 是可见的，且 A 的执行顺序排在 B 前"。

### 顺序规则

```java
int a = 1;
int b = a + 1;
```

### 锁规则

```java
// Thread A:
synchronized(lock) {
   shared = 1;
}

// Thread B:
synchronized(lock) {
   int x = shared; // 一定能看到 1
}
```

A 的 unlock happens-before B 的 lock，所以 B 能看到 A 的操作结果。

### `volatile` 规则

```java
volatile boolean flag = false;

// Thread A:
flag = true;

// Thread B:
while (flag) {
   // 一定能看到 true
}
```

`flag = true` happens-before `if (flag)`，所以读线程一定能看到写的 `true`。

### 线程启动规则

```java
int data = 0;

// Thread A:
data = 42;
Thread t = new Thread(() -> {
   System.out.println(data); // 一定能看到 42
});
t.start();
```

`data = 42` happens-before `t.run()`。

### 线程终止规则

```java
Thread t = new Thread(() -> {
    result = 100;
});
t.start();
t.join();
System.out.println(result); // 一定能看到 100
```

t 线程的所有操作都 happens-before `join()` 返回。

### 传递性

A happens-before B，B happens-before C ⇒ A happens-before C

## as-if-serial（看起来像是串行执行）

as-if-serial 是一种编译优化原则，允许编译器/JVM 重排序代码，只要程序在单线程环境下的执行效果"看起来就像是按照源码顺序执行的"即可。

| 优化行为 | 说明 |
|----------|------|
| 指令重排序 | 编译器/JIT 会重排指令顺序 |
| 临时变量合并/消除 | 不影响外部行为的变量可能被优化掉 |
| 表达式合并 | 可将多个无副作用表达式合并 |
| 内联展开 | 函数调用可能被内联，改变执行路径但结果相同 |
