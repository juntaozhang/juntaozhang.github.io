# Spark Tungsten

## Memory Management and Binary Processing

### Java 对象

| 包装类            | 数据大小    | 占用大小     | 占用细节                              |
|----------------|---------|----------|-----------------------------------|
| `Boolean`      | 1 bit   | 16 B | 对象头 12 B +  1 B → 13 B → 对齐 16 B  |
| `Byte`         | 1 B     | 16 B | 12 B + 1 B → 13 B → 对齐 16 B       |
| `Short`        | 2 B     | 16 B | 12 B + 2 B → 14 B → 对齐 16 B       |
| `Character`    | 2 B     | 16 B | 同上                                |
| `Integer`      | 4 B     | 16 B | 12 B + 4 B = 16 B（已对齐）            |
| `Float`        | 4 B     | 16 B | 同上                                |
| `Long`         | 8 B     | 24 B | 12 B + 8 B = 20 B → 对齐 24 B       |
| `Double`       | 8 B     | 24 B | 同上                                |


Java中考虑一个简单的字符串`String a = "abcd"`，使用UTF-8编码只需要4字节。然而，JVM的原生String实现为了支持更多通用工作负载，使用UTF-16编码每个字符（2字节），每个String对象还包含12字节的对象头和8字节的哈希码：

| 内存区域               | 具体组成部分                             | 大小 (Bytes) | 说明                                                               |
|--------------------|------------------------------------|------------|------------------------------------------------------------------|
| String 对象本身 (Heap) | 对象头 (Object Header)                | 12         | JVM 对象的基本元数据（锁信息、哈希码、类指针等），64 位 JVM 开启指针压缩后固定为 12 字节。            |
|                    | `private final byte[] value;` (引用) | 4          | 指向存储字符的 `byte[]` 数组的引用，开启指针压缩后占 4 字节。                            |
|                    | `private final byte coder;` (实例字段) | 1          | 标识字符编码（0 = LATIN-1，1 = UTF-16），`byte` 类型占 1 字节。                  |
|                    | `private int hash;` (实例字段)         | 4          | 缓存字符串的哈希码（默认 0），`int` 类型占 4 字节。                                  |
|                    | 对齐填充 (Padding)                     | 3          | JVM 要求对象大小为 8 字节的整数倍。当前总大小：12+4+1+4=21 字节，需填充 3 字节至 24 字节。       |
|                    | String 对象小计                        | 24         |                                                                  |
| 内部 `byte[]` 数组 (Heap) | 数组对象头 (Array Header)               | 16         | 数组对象的元数据，包含基础对象头（12 字节）+ 数组长度（4 字节），共 16 字节。                     |
|                    | 数组数据 (Actual Data)                 | 4          | 存储字符串的实际字符。`abcd` 为纯 ASCII，采用 LATIN-1 编码（1 字节 / 字符），4 个字符占 4 字节。 |
|                    | 对齐填充 (Padding)                     | 4          | 数组总大小：16+4=20 字节，需填充 4 字节至 24 字节（8 的整数倍）。                        |
|                    | `byte[]` 数组小计                      | 24         |                                                                  |
| 引用变量 a (Stack)     | 局部变量 `a`                           | 4          | 栈上的引用变量，指向堆中的 String 对象，开启指针压缩后占 4 字节。                           |
| 字符串常量池 (Metaspace) | 常量池中的引用                            | 忽略         | 常量池仅存储指向堆中 String 对象的引用，不存储数据本身，开销可忽略。                           |
| 总计 (堆内存)           |                                    | 48         | String 对象 (24 字节) + `byte[]` 数组 (24 字节) = 48 字节。                 |

在 C 语言里，字符串的大小 只算字符数组本身（含结尾的 '\0'）

| 组成部分   | 大小（字节） | 说明                      |
| ------ | ------ | ----------------------- |
| 字符数据   | 4      | `'a'` `'b'` `'c'` `'d'` |
| 终止空字节  | 1      | `'\0'`                  |
| 总计 | 5  | 数组 `s` 占 5 字节。          |


java.lang.Long 对象在 JVM 中的内存占用情况：

| 区域        | 内容                          | 字节数      | 说明                                                                              |
| --------- | --------------------------- |----------|---------------------------------------------------------------------------------|
| 对象头       | Mark Word                   | 8 B      | 哈希、锁、GC 年龄等                                                                     |
| 对象头       | Klass 指针                    | 4 B      | 压缩指针开启时 4 B                                                                     |
| 实例数据      | `private final long value;` | 8 B      | 存真正的 long 值                                                                     |
| 对齐填充      | —                           | 4 B      | JVM 要求对象大小必须是 8 字节的整数倍。当前总大小为 12 (对象头) + 8 (value) = 20 字节。需要填充 4 字节以达到 24 字节。  |
| 堆内存合计 |                             | 24 B | 一个 `new Long(123L)` 对象本身                                                        |





### GC 低效性

#### 一、分代 GC 的核心假设与工作原理
分代垃圾回收器（如 Java Parallel Scavenge、CMS、G1）的设计核心依赖两大底层假设，也是其性能优化的基础：
1. **弱代假设**：
    - 绝大多数对象“朝生暮死”：分配后短期内即变为不可达，可在新生代快速回收；
    - 少数对象会长期存活：这类对象最终会进入老年代，减少频繁扫描开销。
2. **晋升假设**：
    - 若一个对象能在多次 Minor GC 中持续存活，说明其生命周期大概率较长，因此将其晋升（Promote）到老年代，避免后续 Minor GC 重复扫描。

其核心工作逻辑：新对象优先分配至新生代（Eden 区），Minor GC 回收新生代中死亡对象，存活对象逐步向 Survivor 区转移，达到晋升阈值后进入老年代；老年代满时触发 Major GC/Full GC，回收长期存活的对象。

---

#### 二、Spark 工作负载的对象生命周期特征（与分代 GC 假设冲突）
Spark 核心场景（Cache、Shuffle）的对象生命周期，与分代 GC 的设计假设存在显著矛盾，导致 GC 优化失效：
1. **Cache 场景：长周期存活的大对象**
    - Spark 的 `cache()`/`persist()` 机制会将 RDD/Dataset 数据块（多为 `byte[]` 序列化数组）长期缓存于内存，生命周期跨越多个 Job，而非“朝生暮死”；
    - 这些缓存对象多为大尺寸（如 GB 级数据块），且被 `BlockManager` 强引用，无法被 GC 主动回收，直接占用老年代空间，造成持续内存压力。

2. **Shuffle 场景：阶段性存活的临时大对象**
    - Shuffle Write/Read 阶段会创建大量临时数据结构（如 `ExternalSorter` 的 `SortBuffer`、聚合哈希表、数据拉取缓冲区），均为大尺寸对象；
    - 这些对象的生命周期与单个 Task 强绑定（Task 执行期间需持续存活，任务结束后应立即回收），属于“阶段性活跃”，但需在短时间内占用大量内存；
    - 当内存不足时，Spark 会通过 **主动 Spill 机制**（而非 JVM 内存溢出）将冷数据写入本地磁盘，待后续需要时再读回，这一过程依赖 Spark 手动内存管理，而非 JVM 自动内存分配。

---

#### 三、分代 GC 适配 Spark 工作负载的核心问题
由于对象生命周期与分代 GC 假设的冲突，直接导致 GC 性能恶化，具体表现为：
1. **临时大对象被错误晋升老年代**：Shuffle 阶段的临时大对象，因 Task 执行时间较长（或数据量巨大），会在多次 Minor GC 中持续存活，触发分代 GC 的“晋升假设”，被错误移入老年代；而这些对象本应在 Task 结束后回收，最终导致老年代膨胀，频繁触发 Full GC。

2. **GC 复制操作的额外性能损耗**：新生代采用复制算法，Minor GC 时需将存活对象从 Eden 区复制到 Survivor 区；若对象晋升老年代，还需再次复制。Spark 的缓存对象、Shuffle 临时对象均为大尺寸，复制过程需占用大量内存带宽和 CPU 资源，直接拉长 Minor GC 执行时间。

3. **长时间 STW 停顿影响任务稳定性**：无论是新生代的 Minor GC（复制大对象），还是老年代的 Full GC（扫描/整理大量无效晋升的临时对象、长期存活的缓存对象），都会产生长时间的 Stop-The-World 停顿；尤其数据倾斜时，部分 Task 的大对象尺寸会呈指数级增长，导致 GC 停顿从毫秒级延长至秒级，严重影响 Spark 任务的吞吐量和响应时间。

### 解决办法
为解决分代 GC 与 Spark 工作负载的适配问题，Spark 基于 Tungsten 引擎构建了**显式内存管理体系**，通过“手动控制内存分配/释放+主动 Spill”替代 JVM 自动管理，核心依赖以下组件与机制：

1. **底层基础能力：高效内存访问与数据结构**
    - [Platform](tungsten.md#platform)：封装 Unsafe API 与跨平台内存操作，提供堆内/堆外内存的统一、高效访问能力（如直接内存读写、复制），规避 JVM 对象开销；
    - [LongArray](tungsten.md#longarray)：基于连续内存块实现的高效变长数组，底层依赖 `MemoryAllocator` 分配内存，支持低开销的元素读写，替代传统 JVM 数组（减少对象头、GC 扫描成本）。

2. **核心内存管理组件：分配与回收控制**
    - [MemoryAllocator](memory.md#memoryallocator)：负责分配连续内存块（堆内/堆外可选），屏蔽底层内存源差异，为上层组件提供统一的内存申请入口；
    - [MemoryConsumer](memory.md#memoryconsumer)：抽象的内存使用者接口，统一管理内存块的申请（`allocatePage`/`allocateArray`）与释放（`freePage`/`freeArray`），并核心实现**主动 Spill 机制**——当任务内存不足时，主动将冷数据溢写到磁盘，释放内存以继续处理新数据，避免 JVM OOM。

3. **上层具体实现：面向核心场景的内存优化结构**
    - [BytesToBytesMap](memory.md#bytestobytesmap)：基于显式内存管理的高效哈希表，适用于聚合、Join 等场景，支持内存不足时的 Spill，避免传统哈希表的 GC 压力；
    - [ExternalAppendOnlyMap](memory.md#externalappendonlymap)：面向 Shuffle 聚合场景的Append-Only 映射表，整合显式内存分配与 Spill 逻辑，可高效处理超内存数据，避免临时大对象晋升老年代。


## Cache-aware Computation
缓存感知计算通过更有效地利用L1/L2/L3 CPU缓存来提高数据处理速度，因为它们的速度比主内存快几个数量级。

| 缓存级    | 常见容量范围                     | 备注              |
| ------ |----------------------------|-----------------|
| **L1** | 32 KB–64 KB 数据 + 32 KB 指令  | i 每核独占，1-4 周期延迟 |
| **L2** | 256 KB–2 MB                | 每核独占，10-20 周期   |
| **L3** | 3 MB–8 MB **共享片**          | 全核共享，30-50 周期   |



标准的排序过程会存储一个指向记录的指针数组，并使用快速排序交换指针，直到所有记录都被排序。排序通常具有良好的缓存命中率，因为顺序扫描访问模式。然而，对指针列表排序的缓存命中率很差，因为每个比较操作都需要解引用两个指向内存中随机位置记录的指针。
### 指针链式随机内存访问，缓存不友好

```text
Record {
    byte[] data;    // 记录实际数据 (约400字节)
    long key;       // 排序键 (8字节)
}
long[] ptrs = {0x1000, 0x3000, 0x2000, 0x1190, 0x31C2, 0x2388, 0x1492, 0x34E8... };
      view => [Rec A , Rec I , Rec E , Rec B , Rec J , Rec G , Rec D , Rec L ... ]
       key => [25    , 32    , 35    , 30    , 26    , 40    , 45    , 44    ... ]
排序时访问ptrs数组（顺序访问），比较时访问实际记录（随机访问）
Rec A 和 Rec G具体排序如下：
┌─────────────────────────────────────────────────────────────────┐
│                    CPU Cache Access Process                     │
├─────────────────────────────────────────────────────────────────┤
│ Scenario: Comparing ptrs[0] and ptrs[5]                         │
│ - ptrs[0] → 0x1000 (Page 1 in Rec A)                            │
│ - ptrs[5] → 0x2388 (Page 2 in Rec G)                            │
├─────────────────────────────────────────────────────────────────┤
│ 1. Reading Rec A data from 0x1000:                              │
│    - Check L1 cache: Is memory at 0x1000 cached?                │
│    - If not → Cache Miss → Load memory from main memory →       │
│               64-byte cache line                                │
│    - Key located at Rec A offset? → Read key value              │
├─────────────────────────────────────────────────────────────────┤
│ 2. Reading Rec G data from 0x2388:                              │
│    - Check L1 cache: Is memory at 0x2388 cached?                │
│    - If not → Cache Miss → Load memory from main memory →       │
│               64-byte cache line                                │
│    - Key located at Rec G offset → Read key value               │
├─────────────────────────────────────────────────────────────────┤
│ 3. Compare two key values                                       │
└─────────────────────────────────────────────────────────────────┘

指针数组
┌─────────────────────────────────────────────────────────────────┐
│                        Page 1 (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ [0x1000] [0x3000] [0x2000] [0x1190] [0x31C2] [0x2388]   │    │
│  └─────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                        Page 2 (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ [0x1492] [0x34E8] [0x21F4] [0x3346] ...                 │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
主内存 - 实际记录所在页面
┌─────────────────────────────────────────────────────────────────┐
│                        Page 1 (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ 0x1000        0x1190        0x130E        0x1492        │    │
│  │ [Rec A: 400B] [Rec B: 350B] [Rec C: 380B] [Rec D: 420B] │    │
│  │ [key:25]      [key:30]      [key:22]      [key:45]      │    │
│  └─────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                        Page 2 (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ 0x2000        0x21F4        0x2388        0x24F6        │    │
│  │ [Rec E: 500B] [Rec F: 400B] [Rec G: 350B] [Rec H: 380B] │    │
│  │ [key:35]      [key:28]      [key:40]      [key:20]      │    │
│  └─────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                        Page 3 (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ 0x3000        0x31C2        0x3346        0x34E8        │    │
│  │ [Rec I: 450B] [Rec J: 380B] [Rec K: 420B] [Rec L: 360B] │    │
│  │ [key:32]      [key:26]      [key:38]      [key:44]      │    │
│  └─────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                        Page N (4KB)                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ [Rec ...] [Rec ...] [Rec ...] [Rec ...]                 │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```



### 缓存局部性改进方案

那么我们如何提高排序的缓存局部性？一个非常简单的方法是将每个记录的排序键存储在指针旁边。例如，如果排序键是64位整数，那么我们使用128位（64位指针和64位键）在指针数组中存储每个记录。这样，每个快速排序比较操作只以线性方式查找指针-键对，不需要随机内存查找。

```text
long[] ptrKeyArray = {
    0x1000, 25,
    0x3000, 32,
    0x2000, 35,
    0x1190, 30,
    0x31C2, 26,
    0x2388, 40,
    0x1492, 45,
    0x34E8, 44,
    ... 
};

┌─────────────────────────────────────────────────────────────────────────┐
│                    CPU Cache Access Process                             │
├─────────────────────────────────────────────────────────────────────────┤
│ Scenario: Comparing ptrKeyArray[0-1] and ptrKeyArray[10-11]             │
│ - ptrKeyArray[0-1] → (0x1000, 25) （Rec A 的指针+键）                     │
│ - ptrKeyArray[10-11] → (0x2388, 40)（Rec G 的指针+键）                    │
│ - ptrKeyArray 是连续 long[]（每个元素8字节），内存地址连续                    │
├─────────────────────────────────────────────────────────────────────────┤
│ 1. Reading key of Rec A from ptrKeyArray[1]:                            │
│    - Check L1 cache: Is ptrKeyArray[1] (memory address) cached?         │
│    - Not cached → Trigger load from main memory:                        │
│      Load 64-byte cache line (covers ptrKeyArray[0-7]                   │
│    - Cache Hit after loading → Directly read key value 25 from L1 cache │
├─────────────────────────────────────────────────────────────────────────┤
│ 2. Reading key of Rec G from ptrKeyArray[11]:                           │
│    - Check L1 cache: Is ptrKeyArray[11] (memory address) cached?        │
│    - ptrKeyArray[11] is in the next 64-byte cache line                  │
│      (ptrKeyArray[8-15]，8个long=4个键值对)                               │
│      This cache line is loaded when accessing adjacent elements         │
│    - Cache Hit → Directly read key value 40 from L1 cache               │
├─────────────────────────────────────────────────────────────────────────┤
│ 3. Compare two key values (25 vs 40)                                    │
│    - No additional main memory access during entire process             │
└─────────────────────────────────────────────────────────────────────────┘
```


### 在Spark中的应用
- [UnsafeInMemorySorter](memory.md#unsafeinmemorysorter)

## Code Generation
### [Virtual Function](../../%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F-Visitor.md)
### JIT
Just-In-Time（即时编译）
在程序运行过程中把热点字节码一次性编译成机器码，之后 CPU 直接执行，
JIT 除了“去虚函数”，还会:
- 方法内联：不只是虚方法，所有热点方法（静态、私有、构造器）只要不太大都会被内联。
    ```java
    public int addOneIfSmall(int i) {
        if (i < 100) {
            return i + 1;
        }
        return i;
    }
    public int compute(int i) {
        return addOneIfSmall(i); // 函数调用
    }
    ```
    转换成
    ```java
    public int compute(int i) {
        if (i < 100) {
            return i + 1;
        }
        return i;
    }
    ```
- 逃逸分析: 如果发现一个对象不会逃出当前线程/方法，就：
  - 栈上分配（不进堆）
  - 标量替换（把对象拆成几个局部变量）
    ```java
    public int test(int a, int b) {
        Point p = new Point(a, b);   // 临时对象
        return p.x + p.y;
    }
    // 转换成
    public int test(int a, int b) {
        return a + b;     // 无需任何对象
    }
    ```
- 消灭锁（锁消除/偏向）
- 消灭循环开销（展开+向量化）
- 消灭多余运算

### Spark Code Generation
Spark为如下表达式动态生成字节码，而不是为每行逐步使用较慢的解释器。与解释相比，避免了昂贵的多态函数分发。
用 Whole-Stage CodeGen 先把火山模型里成百上千次虚调用压缩成一段手写的平直 Java 代码，再让 JIT 去编译。
JIT 的“视野”只有单个热点方法，跨算子内联很难

比如 `EqualTo(Literal(1), Literal(1))`表达式，逐行展开, 4 次虚函数调用：
```text
EqualTo.eval(...)
类型：BinaryExpression.eval → 虚方法 #1
内部 left.eval(...)
实际接收者是 Literal → 虚方法 #2
内部 right.eval(...)
同样是 Literal → 虚方法 #3
进入 nullSafeEval(left, right)
实际实现是 EqualTo.nullSafeEval → 虚方法 #4
```

```scala
abstract class Expression extends TreeNode[Expression] {
  def eval(input: InternalRow = null): Any
}
abstract class LeafExpression extends Expression with LeafLike[Expression]
case class Literal (value: Any, dataType: DataType) extends LeafExpression {
  override def eval(input: InternalRow): Any = value
}

abstract class BinaryExpression extends Expression with BinaryLike[Expression] {
  override def eval(input: InternalRow): Any = {
    val value1 = left.eval(input)
    if (value1 == null) {
      null
    } else {
      val value2 = right.eval(input)
      if (value2 == null) {
        null
      } else {
        nullSafeEval(value1, value2)
      }
    }
  }
}
abstract class BinaryOperator extends BinaryExpression with ExpectsInputTypes
abstract class BinaryComparison extends BinaryOperator with Predicate {
  protected lazy val ordering: Ordering[Any] = TypeUtils.getInterpretedOrdering(left.dataType)
}
case class EqualTo(left: Expression, right: Expression)
  extends BinaryComparison with NullIntolerant {
  protected override def nullSafeEval(left: Any, right: Any): Any = ordering.equiv(left, right)
}
```

CodeGen 优化后的代码，无虚函数调用：
```java
// Generated predicate '(1 = 1)':
/* 001 */ public SpecificPredicate generate(Object[] references) {
/* 002 */   return new SpecificPredicate(references);
/* 003 */ }
/* 004 */
/* 005 */ class SpecificPredicate extends org.apache.spark.sql.catalyst.expressions.BasePredicate {
/* 006 */   private final Object[] references;
/* 007 */
/* 008 */
/* 009 */   public SpecificPredicate(Object[] references) {
/* 010 */     this.references = references;
/* 011 */
/* 012 */   }
/* 013 */
/* 014 */   public void initialize(int partitionIndex) {
/* 015 */
/* 016 */   }
/* 017 */
/* 018 */   public boolean eval(InternalRow i) {
/* 019 */
/* 020 */     boolean value_0 = false;
/* 021 */     value_0 = 1 == 1;
/* 022 */     return !false && value_0;
/* 023 */   }
/* 024 */
/* 025 */
/* 026 */ }
```

Reference:
- [Catalyst Code Generation](catalyst.md#code-generation)
### CodeGen技术优化序列化/反序列化
TODO


## Platform
Platform 仅依赖 JVM 核心 API（如 Unsafe、System），
Spark 大量使用 sun.misc.Unsafe 进行直接内存操作（避免 JVM 对象开销、GC 压力），
Platform 对其进行了安全封装，如`allocateMemory`: allocate off-heap memory.

## LongArray
LongArray是一个专门用于存储long类型数据的数组类，它：
- 支持堆内和堆外内存
- 没有边界检查（性能考虑）
- 与Java原生数组相比，性能更高
```text
                  ┌─────────────────────────────────────┐
                  │        LongArray 类结构              │
                  └─────────────────────────────────────┘
                             │
                  ┌─────────────────────────────────────┐
                  │ MemoryBlock memory                  │ ← 指向内存块
                  │ Object baseObj                      │ ← 基础对象引用
                  │ long baseOffset                     │ ← 基础偏移量
                  │ long length = size / 8              │ ← 元素数量
                  └─────────────────────────────────────┘
                             │
                             ▼
  ┌──────────────────────────────────────────────────────────────────┐
  │                    内存块 (MemoryBlock)                           │
  │   ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐  │
  │   │ 0-7 │8-15 │16-23│24-31│32-39│40-47│48-55│56-63│64-71│...  │  │
  │   │long1│long2│long3│long4│long5│long6│long7│long8│long9│     │  │
  │   └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘  │
  │    ↑                                                         ↑   │
  │ baseOffset                                        baseOffset+size│
  └──────────────────────────────────────────────────────────────────┘
```
在 [BytesToBytesMap](memory.md#bytestobytesmap) 中的使用。


# Reference
- [Spark SQL内核剖析-Tungsten技术实现](https://www.dedao.cn/ebook/reader?id=pqvNQ1KRJa7EmgG8MPKrzykNVbDpBWZPed6wQA1xO54nlvZq296YodejLXVJE5eA)
- [Project Tungsten: Bringing Apache Spark Closer to Bare Metal](https://www.databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html)