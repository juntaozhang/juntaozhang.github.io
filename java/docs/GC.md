# Java 垃圾回收（GC）

## 垃圾对象判定

### 引用计数算法

就是给对象添加一个计数器，每当有一个地方引用它的时候，计数器就加 1。每当有一个引用失效的时候，计数器就减 1。

这种算法是比较直接的找到垃圾，然后去回收，也被称为"直接垃圾收集"。

**缺点**：不能处理对象循环引用。

### 可达性分析法

JVM 垃圾回收器会：

1. 从 GC Roots 开始
2. 遍历所有引用
3. 标记所有可达对象为"活"
4. 其他未被访问到的 → 判定为垃圾

这个过程就是所谓的"标记-清除"机制的第一步：**标记阶段**。

## 引用类型

| 引用类型 | 回收时机 | 适用场景 |
|----------|----------|----------|
| **强引用** | 不会回收 | 默认引用，日常代码中使用最多 |
| **软引用** | 内存不足才回收 | 内存敏感缓存（如图片缓存） |
| **弱引用** | GC 就回收 | 映射表、注册中心、ThreadLocal 等 |
| **虚引用** | GC 就回收 | 跟踪对象回收、实现清理/资源释放 |

### 强引用

日常代码中最常见的引用类型，如 `Object obj = new Object()`。

### 软引用

**使用场景**：内存敏感的缓存系统。

当你希望缓存某些对象（比如图片、页面数据、配置等），但是内存不足时允许 JVM 回收它们，可以用软引用。

### 弱引用

#### WeakHashMap

```java
Map<Thread, Object> map = new WeakHashMap<>();
Thread t = new Thread(() -> {
    // do something
});
map.put(t, "some value");
t.start();

// GC1：t 还存在
// map 还有数据吗？有，变量 t 仍有强引用，Thread 还没结束

// GC2：t = null，但线程还在运行
// map 还有数据吗？有，Thread 虽然没有变量引用，但线程自身仍存活 → 被 JVM 强引用（GC Roots）

t = null;
// GC3：t = null，线程已结束
// map 还有数据吗？没有，Thread 无引用 & 已终止 → 不再是 GC Root → key 会被 GC → entry 被清除
```

#### ThreadLocal

`ThreadLocal` 中的 `Entry` 使用弱引用，详见[线程.md](./线程.md)。

### 虚引用

#### JDK 9 的 Cleaner

`Cleaner` 是 JDK 官方为代替 `finalize()` 提供的清理机制，它基于 `PhantomReference` 实现，但对开发者隐藏了复杂性，用于对象 GC 前自动、安全地执行资源清理任务，是现代 Java 清理资源的推荐方案。

```java
import java.lang.ref.Cleaner;

public class CleanerDemo {
    static class Resource {
        private final Cleaner.Cleanable cleanable;

        Resource(Cleaner cleaner) {
            cleanable = cleaner.register(this, () -> {
                System.out.println("清理资源！");
            });
        }

        public void doSomething() {
            System.out.println("使用中...");
        }
    }

    public static void main(String[] args) {
        Cleaner cleaner = Cleaner.create();
        Resource res = new Resource(cleaner);
        res.doSomething();

        // 手动取消强引用
        res = null;

        // 触发 GC
        System.gc();

        // 等待清理任务执行
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## 分代收集理论

| 区域 | 存放内容 | 特点 |
|------|----------|------|
| **新生代（Young Generation）** | 存放新创建的对象 | 短命对象多 |
| **老年代（Old Generation）** | 放从新生代熬过多次 GC 的对象 | 长寿对象 |
| **永久代/元空间** | 存放类信息、常量池、方法元数据 | Java 8 后移到 native memory |

### 新生代内部结构

| 区域 | 说明 |
|------|------|
| **Eden** | 所有新对象最先分配在这里 |
| **Survivor from** | 活下来的对象（复制时作为源） |
| **Survivor to** | 存放复制后的对象（目标） |

**每次 GC：**

1. 复制存活对象从 Eden + from → to
2. 然后 to 和 from 角色交换
3. 经历 N 次后（默认 15 次）才会晋升到老年代

### GC 类型

| 区域 | GC 类型 | 特点 |
|------|---------|------|
| **新生代** | Minor GC | 快速、频繁、复制算法 |
| **老年代** | Major / Full GC | 代价高、标记-清除/压缩 |

## 垃圾回收算法

### 标记-清除

标记清除后会导致碎片化，如果有大对象分配很有可能分配不下而触发另一次的垃圾收集动作。

### 标记-复制

主要用于**新生代**。

**缺点**：浪费过多的内存，使现有的可用空间变为原先的一半。

### 标记-整理

这种算法就是通过标记清除算法找到存活的对象，然后将所有存活的对象，向空间的一端移动，然后回收掉其他的内存。

### 三色标记算法

| 颜色 | 含义 |
|------|------|
| **白色（White）** | 未被标记，对象默认是白色，GC 认为是"垃圾候选" |
| **灰色（Gray）** | 已被发现（可达），但其引用的对象尚未扫描 |
| **黑色（Black）** | 已被标记，且它引用的对象也都扫描过了 |

引用的对象被扫描的意思是：分析过到哪些对象可达。

### SATB（Snapshot At The Beginning）

SATB 是基于三色标记思想的一种具体实现策略，是一种用于**并发标记阶段**的垃圾回收算法策略。

- 构建标记快照
- 基于快照记录那些可能导致"对象从可达 → 不可达"的变化

## 垃圾回收器

### JDK 1.8 之前

#### 新生代

| 回收器 | 说明 |
|--------|------|
| **ParNew** | 多线程的复制算法，与 CMS 配合使用，低延迟、响应时间优先 |
| **Parallel Scavenge** | 多线程的复制算法，与 ParOldGen 配合，侧重吞吐 |

#### 老年代

| 回收器 | 说明 |
|--------|------|
| **Serial Old** | 单线程，标记-整理算法 |
| **ParOldGen** | 多线程的垃圾回收器，标记-整理算法 |
| **CMS** | Concurrent Mark Sweep，并发标记-清除 |

**CMS 详解：**

CMS 在 JDK 9 中已被标记为 deprecated（过时），JDK 14 中正式移除，推荐使用 G1 或 ZGC 等更先进的收集器替代。

1. **初始标记（Initial Mark）** — STW
   - 需要快速确定 GC Roots 直接引用的对象，避免长时间停顿
   - 耗时极短，仅标记"根对象"（如类静态变量、常量、JNI 引用等），不遍历对象引用链

2. **并发标记（Concurrent Mark）** — 并发
   - 用户线程与 GC 线程同时运行，GC 线程从初始标记的根对象出发，遍历所有可达对象并标记
   - 问题：期间用户线程可能修改对象引用（如创建新对象、断开引用），导致部分标记结果不准确（产生"浮动垃圾"）

3. **重新标记（Remark）** — STW
   - 必须修正并发标记期间的标记误差，确保存活对象标记准确
   - 优化手段：通过"增量更新"或"原始快照"等算法，快速追溯并发期间的引用变化，减少遍历范围
   - 此阶段耗时通常比初始标记长，但远短于 Full GC

4. **并发清除（Concurrent Sweep）** — 并发
   - GC 线程直接清除未标记的垃圾对象，用户线程正常运行
   - 缺点：只清除不整理内存，会产生大量内存碎片，可能导致后续大对象分配失败，触发 Full GC

**为什么要重新标记？**

重新标记（Remark）并不能解决浮动垃圾问题，它解决的是"标记遗漏"问题。

"标记"的含义是：标记为"存活对象"（而非标记为垃圾）。未被标记的对象，会被视为垃圾并在清除阶段被回收。

```
// 构建初始引用链：root → A → B → C
// root 是强引用

// GC 线程的标记步骤（并发执行中）：
1. 标记 root（完成）
2. 标记 A（完成）
3. 准备从 A 出发标记 B（尚未执行）

// 同一时间，用户线程执行：
A.next = null;    // 断开 A → B 的引用
root.next = B;    // 新增 root → B 的引用

// GC 线程继续执行时，会按原计划从 A 出发遍历，但此时 A.next 已为 null，
// 因此不会标记 B 和 C（不可达）。
// 而 root.next = B 这个新引用是在 GC 线程遍历 root 之后发生的，
// GC 线程没有重新检查 root 的引用变化，因此 B 和 C 均未被标记。
```

**CMS 缺点：**

- **内存碎片**：并发清除不整理内存，长期运行可能导致大对象无法分配，触发 Full GC（Serial Old）
- **CPU 消耗高**：并发阶段需要额外的 GC 线程，可能占用 20% 左右的 CPU 资源，影响用户线程性能
- **浮动垃圾**：并发标记期间产生的新垃圾无法被当前周期回收，需留到下一次收集，可能导致内存溢出风险

### G1（Garbage First）

G1 将整个堆划分为若干个大小相等的 Region（区域），每个 Region 可以动态扮演不同角色（Eden、Survivor、Old、Humongous）。

**优先回收垃圾最多的 Region**。

#### RSet

每个 Region 都维护了一个 RSet，记录有哪些其它 Region 的对象引用了它的内部对象。

#### G1 回收流程

1. **初始标记（Initial Mark）** — STW
   - 标记 GC Roots 直接可达的对象

2. **并发标记（Concurrent Mark）** — 并发
   - 遍历整个堆对象图，标记所有可达对象

3. **最终标记（Remark）** — STW
   - 补全并发标记期间发生变化的引用，确保准确性

4. **筛选回收（Cleanup + Evacuation）**
   - 根据回收收益（Garbage-First）策略，优先选择垃圾最多的 Region 回收，并整理对象

#### G1 vs CMS

老年代与 CMS 流程相同，只是算法不一样：前者是标记-清除，后者是 SATB 算法。

```
root → A → B → C

初始标记：A 是灰色，没有被分析
并发标记：此时用户线程把 A.next = null，STAB 会通过写屏障记录下来
最后标记：处理写屏障中记录的对象变更（例如旧值 B）
```

#### TAMS 指针

G1 在并发标记时允许新对象分配，并通过每个 Region 的 TAMS 指针把"并发前已有对象"和"并发后新对象"区分开。新对象（在 TAMS 之后）默认认为是存活的，无需标记，从而保证并发标记的正确性。
