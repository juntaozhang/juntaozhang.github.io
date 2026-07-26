# happens-before规则
意义:内存模型通过hb原则并发操作过程中的有序性问题

actionA先行发生actionB，就是说A对B可见（A的结果在B执行时可见）

as-if-serial:不管怎么排序,单线程结果不变

这个规则实际上成为了是否符合线程安全要求的最基本依据，也是在重排序中的一个默认保证

* Program order rule. 线程内的代码能够保证执行的先后顺序
* Monitor lock rule. 对于同一个锁，一个解锁操作一定要发生在时间上后发生的另一个锁定操作之前
* Volatile variable rule. 保证前一个对volatile的写操作在后一个volatile的读操作之前
* Thread start rule. 一个线程内的任何操作必需在这个线程的start()调用之后
* Thread termination rule. 一个线程的所有操作都会在线程终止之前
```java
final List<String> list = new ArrayList<>();
list.add("0");// (0 happens-before 1) Thread start rule
Thread t = new Thread(new Runnable() {
  @Override
  public void run() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("in thread");//1
    list.add("1");
  }
});
t.start();
t.join();
list.add("2");  //2 (1 happens before 2) Thread termination rule
System.out.println(list);
```


Interruption rule. 要保证interrupt()的调用在中断检查之前发生
Finalizer rule. 一个对象的终结操作的开始必需在这个对象构造完成之后

Transitivity. 可传递性