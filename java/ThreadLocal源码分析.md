#Java ThreadLocal源码分析

## 1.举例
```java
public static class MyExample {

    private static ThreadLocal<Integer> age = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<String> name = new ThreadLocal<String>() {
      public String initialValue() {
        return "default";
      }
    };

    public static void main(String[] args) {
      new Thread(() -> {
        age.set(30);
        name.set("zhang tongxue");
        System.out.println(Thread.currentThread() + " " + age.get());
        System.out.println(Thread.currentThread() + " " + name.get());
      }).start();
      new Thread(() -> {
        try {
          Thread.sleep(200);
        } catch (InterruptedException ignore) {
        }
        System.out.println(Thread.currentThread() + " " + age.get());
        System.out.println(Thread.currentThread() + " " + name.get());
      }).start();
    }
  }
```

执行结果:
```log
Thread[Thread-0,5,main] 30
Thread[Thread-0,5,main] zhang tongxue
Thread[Thread-1,5,main] 0
Thread[Thread-1,5,main] default
```

## 2.分析
#### 类结构
![这里写图片描述](http://img.blog.csdn.net/20180204163056220?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2VpeGluXzQxNzA1Nzgw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

#### 实际存储结构
![这里写图片描述](http://img.blog.csdn.net/20180204163202933?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2VpeGluXzQxNzA1Nzgw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

#### 源码
set方法
```java
    public void set(T value) {
        Thread t = Thread.currentThread();//获取当前线程
        ThreadLocalMap map = getMap(t);//t.threadLocals 获取线程中的Map
        if (map != null)
            map.set(this, value);//Map常规散列表
        else
            createMap(t, value);
    }
```

get方法
```java
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);//散列表,先求hashcode
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

```

## 总结
- 每个线程私有一份拷贝的变量，这里实现非常巧妙，也容易会被面试官问你如何设计Threadlocal（假如你不知道实现的话）
- ThreadLocal只是对需要存储的对象的管理，而存储实际是由当前Thread负责。
- 使用ThreadLocal可以使对象达到线程隔离的目的。同一个ThreadLocal操作不同的Thread，实质是各个Thread对自己的变量操作。

#### 使用场景
- 为什么要使用ThreadLocal，个人感觉有两个原因:
	- 1.是与其它线程的隔离
	- 2.是可以在一个线程的生命周期中使用同一个对象
	
在一些业务处理中，组件之间处理同一个业务，需要处理很多上下文信息（输入，调用参数，中间结果），往往通过threadlocal 方式比较优雅。