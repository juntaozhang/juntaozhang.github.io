# API

## ArrayList 和 LinkedList 区别？

## Map
* [WeakReference与WeakHashMap](/java/WeakReference与WeakHashMap.md)
* [关于 HashMap 1.8 的重大更新](http://blog.csdn.net/carson_ho/article/details/79373134)
* [彻头彻尾理解 LinkedHashMap](http://blog.csdn.net/justloveyou_/article/details/71713781)

### LinkedHashMap
* 1.Entry 是双链表
* 2.Entry可以定义两种顺序：插入顺序和LRU顺序
  [LinkedHashMapInsertTest](https://gitee.com/zjt_hans/hello-java/blob/master/JavaSE/src/main/java/org/example/java/util/colls/LinkedHashMapInsertTest.java)
  [LinkedHashMapLRUTest](https://gitee.com/zjt_hans/hello-java/blob/master/JavaSE/src/main/java/org/example/java/util/colls/LinkedHashMapLRUTest.java)

### HashMap

#### 在 JDK 1.7 中，HashMap 的扩容使用头插法，如果多个线程并发执行 resize，会由于链表反转与指针污染导致构造出循环链表，进而在 get() 操作中死循环。JDK 1.8 改为尾插法并加强了安全性，但 HashMap 始终不是线程安全的容器。

### ConcurrentHashMap

#### JDK 8 中的 ConcurrentHashMap 是一种精细化锁 + 无锁机制结合的高性能并发容器：
插入空桶用 CAS，无锁；
插入非空桶用 synchronized，小粒度加锁；
链表变红黑树时加 synchronized；
扩容时多个线程一起搬桶，每个线程搬多个桶，提升并发效率。

- 而在 JDK 1.8 中，读操作基本是**无锁（non-blocking）**的，不会 block 写线程，也不会被写线程 block。
#### JDK 1.7 中的 ConcurrentHashMap

底层由 Segment数组 + HashEntry数组 构成。
Segment 实际上继承了 ReentrantLock，是一个可重入锁，每个 Segment 管理一部分 Hash 桶。
默认有 16 个 Segment，相当于默认支持 16 个线程无锁并发访问。

- 并发机制
使用分段锁（Segment Locking），通过锁住某个 Segment 来实现线程安全。
每次操作只需要锁住一个 Segment，粒度较小，但每个 Segment 内部依然是用链表+数组结构。
- Segment 数量固定（不可动态扩容），限制了并发性能上限。
实现复杂，维护成本高。
不能支持高并发下的非阻塞读（依然需要一定程度加锁）。