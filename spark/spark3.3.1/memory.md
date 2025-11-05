# Spark Memory Management

## spark 默认内存管理？分配策略是什么？

**默认使用UnifiedMemoryManager（统一内存管理）**

- ✅ **动态边界**: Execution和Storage内存之间可以相互借用
- ✅ **软边界**: 不是硬性分割，而是动态调整的边界
- ✅ **默认启用**: Spark 1.6+后的默认选择，替代了StaticMemoryManager

**内存总体划分**
```text
┌─────────────────────────────────────────────────────────────┐
│                    JVM Heap (例如: 1GB)                      │
├─────────────────────────────────────────────────────────────┤
│ Reserved Memory (300MB)                                     │ 40%
│ - 系统元数据、内部数据结构、OOM保护                              │
├─────────────────────────────────────────────────────────────┤
│ Unified Memory Region (420MB = (1024-300) * 0.6)            │ 60%
│ ┌─────────────────┬─────────────────────────────────────┐   │
│ │ Storage Memory  │ Execution Memory                    │   │
│ │ (210MB默认)      │ (210MB默认)                         │   │
│ │ ↕ 可借用         │ ↕ 可借用                             │   │
│ └─────────────────┴─────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**动态借用策略**
- **Execution借用Storage**: 可回收Storage的空闲内存和Storage超出基础配额的部分
- **Storage借用Execution**: 可使用Execution的空闲内存，但不能驱逐正在执行的任务


## Unroll是什么？在persist StorageLevel设置成MEMORY_AND_DISK，如果一个block unroll失败？会怎么样？

### **Unroll机制详解**

**什么是Unroll**:
- Unroll是将Iterator数据逐步展开到内存的过程
- 默认先申请1MB内存(`spark.storage.unrollMemoryThreshold`)
- 在不知道数据总大小的情况下，渐进式地评估是否能完全缓存到内存

**Unroll过程**: Iterator数据 → 预先申请1MB → 逐步unroll → 检查大小 → 决定继续或降级

### **MEMORY_AND_DISK模式下的Unroll失败处理**

**Unroll失败的情况**:
- 内存不足无法完成unroll过程
- 数据大小超过可用内存限制
- Storage内存被Execution借用导致空间不够

**失败后的自动降级策略** (源码 `MemoryStore.scala`):
1. **检查StorageLevel**: 如果设置了`useDisk = true`
2. **降级到磁盘**: 自动调用DiskStore进行磁盘存储
3. **保证可用性**: 后续访问时从磁盘读取(性能较低但数据不丢失)

**完整降级流程**:
```
RDD.persist(MEMORY_AND_DISK) → 尝试内存缓存 → [内存不足] → 自动降级到磁盘存储 → 保证数据可用性
```

**用途**: 通过渐进式unroll避免盲目消耗内存，通过降级策略确保数据在内存不足时仍然可用。


## Spark的统一内存管理器中，RDD 缓存是如何 与 Shuffle 数据竞争内存空间的？

**内存竞争的本质**：
- **RDD缓存**：使用Storage Memory，存储persist/cache的数据
- **Shuffle数据**：使用Execution Memory，处理joins、sorts、aggregations等操作
- 两者共享统一内存池，会发生激烈竞争

### **竞争场景分析**

**场景1: Shuffle任务抢占缓存内存**
```text
初始状态: Storage=300MB(借用了90MB), Execution=150MB
         Storage基础配额=210MB, 已借用=90MB
大量Shuffle操作需要额外100MB内存
         ↓
Execution只能回收Storage借用的90MB + Storage空闲的10MB = 100MB
不能触碰Storage的210MB基础配额
         ↓
最终状态: Storage=210MB(基础配额受保护), Execution=240MB
```

**场景2: 缓存任务竞争执行内存**
```text
初始状态: Storage=50MB, Execution=200MB(Shuffle在使用)
新RDD需要缓存150MB数据
         ↓
Storage只能借用Execution的空闲内存(0MB)
         ↓
内存unroll失败，根据StorageLevel决定后续处理:
- MEMORY_ONLY: 缓存失败，返回PartiallyUnrolledIterator用于重新计算
- MEMORY_AND_DISK: 自动降级到磁盘存储
```

### **不对称竞争机制** (源码 `UnifiedMemoryManager.scala`)

**Execution Memory的优势**：
- ✅ **可以回收借用内存**: 能够收回Storage超出基础配额的部分
- ✅ **可以回收空闲内存**: 能够使用Storage的空闲内存空间
- ✅ **优先级更高**: 但受限于Storage的基础配额保护

**Storage Memory的劣势**：
- ❌ **无法驱逐执行任务**: 不能中断正在执行的Shuffle操作
- ❌ **被动释放内存**: 只能被动地释放超出基础配额的部分
- ❌ **借用部分易被回收**: 超出基础配额的cached blocks会被优先回收

**重要限制**：
- Storage有一个受保护的**基础配额**（默认为统一内存的50%）
- Execution只能回收Storage**超出基础配额的部分**，不能无限制抢占
- 这确保了Storage有最低的内存保障

### **实际影响**

**对RDD缓存的影响**：
- Shuffle操作只能回收Storage超出基础配额的缓存数据
- Storage基础配额内的缓存受到保护，不会被驱逐
- 但借用部分的缓存可能被回收，影响缓存效果

**对Shuffle数据的影响**：
- Shuffle操作几乎总能获得所需内存（通过驱逐缓存）
- 但如果Storage占用过多内存，可能导致Shuffle spill到磁盘
- 整体上Shuffle操作的内存保障更强

## MEMORY_ONLY_SER 与 MEMORY_ONLY 的性能权衡是什么？

### **核心差异**

**MEMORY_ONLY (反序列化存储)**：
- ✅ **访问速度快**: 对象直接存储在堆内存中，无需反序列化
- ✅ **CPU开销低**: 直接访问Java对象，无额外计算
- ❌ **内存占用大**: Java对象有对象头、指针等额外开销
- ❌ **GC压力大**: 大量对象增加垃圾回收负担

**MEMORY_ONLY_SER (序列化存储)**：
- ✅ **内存占用小**: 序列化后数据更紧凑，可节省2-5倍内存
- ✅ **GC友好**: 减少堆中对象数量，降低GC压力
- ❌ **访问速度慢**: 每次访问需要反序列化操作
- ❌ **CPU开销高**: 序列化/反序列化消耗CPU资源

### **性能权衡分析**

**内存使用对比**:
```text
同样的1GB数据：
MEMORY_ONLY:     ~2-3GB内存占用 (对象开销)
MEMORY_ONLY_SER: ~1GB内存占用   (紧凑存储)

内存节省: 50%-70%
```

**访问性能对比**:
```text
频繁访问场景(100次读取):
MEMORY_ONLY:     100ms (直接访问)
MEMORY_ONLY_SER: 300ms (含反序列化开销)

CPU开销增加: 2-3倍
```

### **选择策略**

**选择MEMORY_ONLY的场景**:
- ✅ 数据会被频繁访问 (>3次)
- ✅ 内存资源充足
- ✅ 对延迟敏感的实时应用
- ✅ 数据结构复杂，序列化开销大

**选择MEMORY_ONLY_SER的场景**:
- ✅ 内存资源紧张
- ✅ 数据访问频率较低 (1-2次)
- ✅ 需要缓存大量数据
- ✅ GC成为性能瓶颈
- ✅ 集群内存不足，需要避免spill到磁盘

**存储级别对比表**：

| StorageLevel | 序列化 | 内存使用 | CPU 开销 | GC影响 | 适用场景 |
|--------------|--------|----------|----------|--------|----------|
| `MEMORY_ONLY` | 否 | 高 | 低 | 高 | 频繁访问，内存充足 |
| `MEMORY_ONLY_SER` | 是 | 低 | 中 | 低 | 内存不足，访问频率中等 |
| `MEMORY_AND_DISK` | 否 | 中 | 低 | 中 | 数据量大，容错要求高 |
| `MEMORY_AND_DISK_SER` | 是 | 低 | 中 | 低 | 内存紧张，需要容错 |

### **最佳实践建议**

**内存充足时**: 优先选择`MEMORY_ONLY`，获得最佳访问性能
**内存紧张时**: 选择`MEMORY_ONLY_SER`，避免数据被驱逐或spill
**生产环境**: 建议先用`MEMORY_AND_DISK_SER`，平衡性能和稳定性

**性能权衡**：
- **MEMORY_ONLY**: 对象直接存储，访问快但占用内存多
- **MEMORY_ONLY_SER**: 序列化后存储，节省内存但增加 CPU 开销
