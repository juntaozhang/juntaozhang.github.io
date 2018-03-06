#WeakReference(弱引用)与WeakHashMap

## 是什么?

### Java中4种引用,强度依次是(S > S >W > P)
- 强引用
	>java中创建对象默认是强引用,只要引用被持有,不会被GC
	
- 软引用(SoftReference)
	>只有内存不足的时候才会被GC
	
- 弱引用(WeakReference)
	>只要发生GC,对象就会被销毁
	
- 虚引用(PhantomReference)
	>形同虚设,永远get不到该对象

### 举例

- 软引用
```java
  /**
   * -verbose:gc -Xmx150M 软引用使用 get() 方法取得对象的强引用从而访问目标对象。 所指向的对象按照JVM的使用情况（Heap 内存是否临近阈值）来决定是否回收。
   * 可以避免 Heap 内存不足所导致的异常。
   */
  static class Soft {

    public static void main(String[] args) throws Exception {
      SoftReference<byte[]> reference = new SoftReference<>(new byte[1024 * 1024 * 50]);
      System.out.println("gc by user");
      System.gc();  //gc 不会释放内存 只有在OutOfMemoryError之前释放
      TimeUnit.MILLISECONDS.sleep(20);
      System.out.println("after gc size is " + reference.get().length);
      byte[] tmp = new byte[1024 * 1024 * 50];
      System.out.println("memory is full, can't release any more, size is " + reference.get());
    }
  }
```
执行结果:
```
gc by user
[GC (System.gc())  56030K->51928K(147456K), 0.0017256 secs]
[Full GC (System.gc())  51928K->51742K(147456K), 0.0068079 secs]
after gc size is 52428800
[GC (Allocation Failure)  52521K->51838K(147456K), 0.0010480 secs]
[GC (Allocation Failure)  51838K->51870K(147456K), 0.0009696 secs]
[Full GC (Allocation Failure)  51870K->51683K(147456K), 0.0058217 secs]
[GC (Allocation Failure)  51683K->51683K(147456K), 0.0013075 secs]
[Full GC (Allocation Failure)  51683K->465K(128512K), 0.0043549 secs]
memory is full, can't release any more, size is null
```
- 弱引用
```java
  //-Xmx20M -XX:+PrintGCDetails
  private static void test0() {
    WeakHashMap<Integer, byte[]> d = new WeakHashMap<>();
    for (int i = 0; i < 100; i++) {
      System.out.println(i);
      //下面两行区别?
      //int i存在栈中不会被gc,new Integer(i)存放在堆中
      //d.put(i, new byte[1024 * 1024]);//value 导致的OOM
      d.put(new Integer(i), new byte[1024 * 1024]);//正常进行
    }
    System.out.println(d.size());
  }
```

- 虚引用
```java
  /**
   * -verbose:gc -Xmx150M
   * 虚引用
   * 永远无法使用 get() 方法取得对象的强引用从而访问目标对象。
   * 所指向的对象在被系统内存回收前，虚引用自身会被放入ReferenceQueue对象中从而跟踪对象垃圾回收。
   * 不会根据内存情况自动回收目标对象。
   */
  static class Phantom {

    public static void main(String[] args) {
      ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
      PhantomReference<Object> referent = new PhantomReference<>(new Object(), refQueue);
      PhantomReference<Object> referent2 = new PhantomReference<>(new Object(), refQueue);
      System.out.println("referent get" + referent.get());// null
      System.out.println("referent2 get" + referent2.get());// null
      System.gc();
//      System.runFinalization();
      System.out.println("referent=>" + referent);
      System.out.println("referent2=>" + referent2);
      System.out.println("poll=>" + refQueue.poll());
      System.out.println("poll=>" + refQueue.poll());
      System.out.println("poll=>" + refQueue.poll());
    }
  }
```
执行结果:
```
referent getnull
referent2 getnull
[GC (System.gc())  101331K->97930K(147456K), 0.0033503 secs]
[Full GC (System.gc())  97930K->97821K(147456K), 0.0089749 secs]
referent=>java.lang.ref.PhantomReference@28a418fc
referent2=>java.lang.ref.PhantomReference@5305068a
poll=>java.lang.ref.PhantomReference@28a418fc
poll=>java.lang.ref.PhantomReference@5305068a
poll=>null
```
	
### 在Java中使用场景
- 软引用(SoftReference)
	>cache对象需要在即将发生OOM的时候腾出空间,防止OOM;JDK中Class类中reflectionData就是用了软引用
	
- 弱引用(WeakReference)

JDK中WeakHashMap,例子如下:

```java
public class WeakHashMapTest {
  static class Key {
    String id;
    public Key(String id) {
      this.id = id;
    }
    public String toString() {
      return id;
    }
    public int hashCode() {
      return id.hashCode();
    }
    public boolean equals(Object r) {
      return (r instanceof Key)
          && id.equals(((Key) r).id);
    }
    public void finalize() {
      System.out.println("Finalizing Key " + id);
    }
  }

  static class Value {
    String id;
    public Value(String id) {
      this.id = id;
    }
    public String toString() {
      return id;
    }
    public void finalize() {
      System.out.println("Finalizing Value " + id);
    }
  }

  public static void main(String[] args) throws Exception {
    int size = 10;
    if (args.length > 0) {
      size = Integer.parseInt(args[0]);
    }
    Key[] keys = new Key[size];
    WeakHashMap<Key, Value> whm = new WeakHashMap<Key, Value>();
    for (int i = 0; i < size; i++) {
      Key k = new Key(Integer.toString(i));
      Value v = new Value(Integer.toString(i));
      if (i % 3 == 0) {
        keys[i] = k;
      }
      whm.put(k, v);
    }
    System.out.printf("before gc WeakHashMap are %s.%n", whm.toString());
    System.gc();
    Thread.sleep(1000);  //把处理器的时间让给垃圾回收器进行垃圾回收
    System.out.printf("keys are %s.%n", Arrays.toString(keys));
    System.out.printf("after gc WeakHashMap are %s.%n", whm.toString());
  }
}
```
执行结果
```
before gc WeakHashMap are {8=8, 9=9, 4=4, 5=5, 6=6, 7=7, 0=0, 1=1, 2=2, 3=3}.
Finalizing Key 2
Finalizing Key 8
Finalizing Key 7
Finalizing Key 5
Finalizing Key 4
Finalizing Key 1
keys are [0, null, null, 3, null, null, 6, null, null, 9].
after gc WeakHashMap are {9=9, 6=6, 0=0, 3=3}.
```
原理:

WeakHashMap有一个成员变量`Entry<K,V>[] table`, 类似于HashMap中的table,只是Entry中的key是弱引用
`private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V>`, 
上面解释过,弱引用就是在每次GC都会清除该对象,key被清除之后,value是强引用,该怎么办?清理key时把key放在
`private final ReferenceQueue<Object> queue = new ReferenceQueue<>();`, 大部分方法都会调用
`expungeStaleEntries`,该方法主要作用就是清除弱引用key对应的value.

~~所以WeakHashMap的使用场景就是短时间cache的对象,当然这个也是有缺点的,假设刚计算出来的对象碰巧遇到gc,立刻又被回收了,
下次使用又要重新计算;有人说你可以先把这个应用先加入强引用类似于上面的WeakHashMapTest,那么什么时候释放呢?
如果你已经知道确切的释放时间,那么还用WeakReference有什么意义呢...~~

(2018.3.6)
WeakHashMap为什么把key作为弱引用而非value?
用来处理当key被因为弱引用gc回收时map将删除kv,所以用作cache不是很合理,被用来保存一些对象的元数据,当这些对象有生命周期
不需要你认为控制,比如Thread,当线程活的时候你直接可以使用该thread对应的value,一旦销毁之后,map自动回收value
**MapMaker guava提供更多样的选择**

```java
public class WeakHashMapTest {
  public static void main(String[] args) {
    Thread t1 = new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {
      }
    });
    t1.start();
    WeakHashMap<Thread, String> map = new WeakHashMap<>();
    map.put(t1, "metadata");
    t1 = null;
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignore) {
    }
    System.out.println(map);
    System.gc();
    System.out.println(map);

  }
}
```

- 虚引用(PhantomReference)
	>虚引用必须和引用队列(ReferenceQueue)联合使用才有意义,一定程度上它与finalize起到的作用大致相同,都在对象被gc回收之前做一些收尾工作. 如何使用见[利用 PhantomReference 替代 finalize.](https://www.dozer.cc/2015/10/phantom-reference.html)

	*finalize如何使用,借助dubbo里面的源码:*

	```java
private final Object finalizerguardian = new Object() {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            if (!ReferenceConfig.this.destroyed) {
                logger.warn("ReferenceConfig(" + url + ") is not DESTROYED when FINALIZE");

                /* 先不做Destroy操作
                try {
                    ReferenceConfig.this.destroy();
                } catch (Throwable t) {
                        logger.warn("Unexpected err when destroy invoker of ReferenceConfig(" + url + ") in finalize method!", t);
                }
                */
            }
        }
    };
```


## 参考:
[java中的4种reference的差别和使用场景（含理论、代码和执行结果）](http://blog.csdn.net/aitangyong/article/details/39453365)
[Effective Java Item7:Avoid Finalizers,解释为什么finalize是不安全的，不建议使用](http://blog.csdn.net/aitangyong/article/details/39450341)

