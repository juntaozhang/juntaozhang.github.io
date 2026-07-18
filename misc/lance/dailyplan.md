# Lance Format 14天学习复习计划（每天30分钟）

> 基于 [Lance Lakehouse Format Specification](https://lance.io/format/) 官方文档
> 目标：建立 Lance 格式的体系化认知，不深入源码，聚焦设计哲学与架构决策

---

## 文档结构总览

| 模块 | 文档 | 行数 | 难度 | 核心问题 |
|------|------|------|------|----------|
| **File Format** | `file/index.md` + `encoding.md` + `versioning.md` | 886 | ⭐⭐⭐ | 如何存储列数据？随机访问如何优化？ |
| **Table Format** | `table/index.md` + `layout.md` + `schema.md` + `transaction.md` + `versioning.md` + `branch_tag.md` + `row_id_lineage.md` + `mem_wal.md` | 2719 | ⭐⭐⭐⭐ | 如何实现 ACID？Schema 如何演进？ |
| **Index Formats** | `index/index.md` + scalar(7篇) + vector(1篇) + system(2篇) | 1068 | ⭐⭐⭐ | 索引为何是 first-class table object？ |

---

## Week 1：基础层 — File + Table 核心

### ~~Day 1：File Format 概览~~
- **内容**：`file/index.md` (169行)
- **重点**：文件头结构、页布局、随机访问设计哲学
- **思考问题**：
  - Lance 为什么避免 Parquet 式的 row group？
  - 大页（large pages）设计对云存储有什么优势？
- **输出建议**：画一张 Lance File 的物理结构草图（Header → Pages → Footer）

### Day 2：File Encoding（上）
- **内容**：`file/encoding.md` 前半 (~350行)
- **重点**：structural encoding、page layout、plain encoding
- **思考问题**：
  - 什么是 structural encoding？与 logical encoding 的区别？
  - 为什么 Lance 把 statistics 和 search structures 移出文件格式？
- **输出建议**：列出 3 种常见 encoding 及其适用场景

### Day 3：File Encoding（下）
- **内容**：`file/encoding.md` 后半 (~350行)
- **重点**：压缩算法、dictionary encoding、bitpacking、FSST
- **思考问题**：
  - FSST 压缩在什么场景下最有效？
  - Lance 如何选择 encoding？是自动还是手动？
- **输出建议**：对比 Parquet 的 encoding 选择，Lance 有何不同

### Day 4：File Versioning + 复习
- **内容**：`file/versioning.md` + 回顾 Day 1-3
- **重点**：文件版本演进、向后兼容性
- **思考问题**：
  - Lance 文件格式的版本如何管理？
  - 读取旧版本文件时会发生什么？
- **输出建议**：整理 File Format 知识卡片（一张 A4 纸）

### Day 5：Table Format 概览
- **内容**：`table/index.md` + `table/layout.md` (417行)
- **重点**：Fragment、Manifest、ACID 提交机制、目录布局
- **思考问题**：
  - Fragment 是什么？为什么行被分组到 fragment 中？
  - 一个 fragment 可以有多个 data file，这意味着什么？
  - 列添加为什么是 metadata-heavy 而非 rewrite-heavy？
- **输出建议**：画出 Table 的目录结构树

### Day 6：Table Schema + Versioning
- **内容**：`table/schema.md` + `table/versioning.md` (476行)
- **重点**：Schema evolution、字段增删改、类型转换
- **思考问题**：
  - Lance 支持哪些 schema evolution 操作？
  - 字段重命名 vs 字段删除再添加，有何区别？
  - Schema 版本如何与 Manifest 版本关联？
- **输出建议**：列举 5 种 schema change 场景及 Lance 的处理方式

### Day 7：Week 1 复习
- **内容**：回顾 File + Table 核心概念
- **任务**：
  1. 用自己的话解释 File Format 和 Table Format 的分工边界
  2. 回答："为什么 Lance 说只有 table readers/writers 和 index readers/writers 需要知道 on-disk 布局？"
  3. 画一张 Lance 存储层的分层图
- **检验标准**：能向他人解释 Lance 的核心设计决策

---

## Week 2：进阶层 — Transaction + Index + 系统机制

### Day 8：Transaction（上）
- **内容**：`table/transaction.md` 前半 (~350行)
- **重点**：事务类型、冲突检测、隔离级别
- **思考问题**：
  - Lance 支持哪些事务操作类型？（Append、Overwrite、Delete...）
  - 冲突检测是如何实现的？乐观锁还是悲观锁？
  - 什么是 "manifest-level" 的冲突？
- **输出建议**：列出事务类型与冲突矩阵

### Day 9：Transaction（下）
- **内容**：`table/transaction.md` 后半 (~350行)
- **重点**：提交协议、回滚机制、并发控制
- **思考问题**：
  - 事务提交失败时如何回滚？
  - 多个 writer 同时写入时，Lance 如何保证一致性？
  - 与 Delta Lake / Iceberg 的事务机制相比有何异同？
- **输出建议**：画出事务提交的状态机图

### Day 10：Row ID + Branch/Tag
- **内容**：`table/row_id_lineage.md` + `table/branch_tag.md` (488行)
- **重点**：Row ID 分配、版本血缘、分支标签、时间旅行
- **思考问题**：
  - Row ID 是全局唯一还是 fragment 内唯一？
  - 删除操作如何影响 Row ID？
  - Branch 和 Tag 的区别是什么？Git 类比是否准确？
- **输出建议**：画出 Row ID 的生命周期图

### Day 11：Table WAL + 复习
- **内容**：`table/mem_wal.md` (690行，快速浏览)
- **重点**：内存 WAL 设计、恢复机制、性能优化
- **思考问题**：
  - 为什么 Table 层需要 WAL？File 层不需要？
  - WAL 是内存中的，崩溃时如何保证不丢数据？
  - WAL 与 Manifest 的关系是什么？
- **输出建议**：对比 Table WAL 和 Index System WAL（Day 13）

### Day 12：Index 概览 + Scalar Index
- **内容**：`index/index.md` + 选 2-3 篇 scalar index（推荐：btree、zonemap、bitmap）
- **重点**：索引作为 first-class table object、索引版本管理
- **思考问题**：
  - 为什么说索引是 "first-class table object"？
  - 索引如何与 Table 的事务机制协同？
  - Scalar index 和 Vector index 的管理有何统一之处？
- **输出建议**：列出 3 种 scalar index 的适用查询模式

### Day 13：Vector Index + System Index
- **内容**：`index/vector/index.md` + `system/frag_reuse.md` + `system/mem_wal.md`
- **重点**：ANN 索引（IVF_PQ / HNSW）、fragment reuse、index WAL
- **思考问题**：
  - Lance 支持哪些 ANN 算法？如何选择？
  - Vector index 的构建是同步还是异步？
  - Fragment reuse 索引的作用是什么？
- **输出建议**：对比 Vector Index 和 Table WAL 的设计差异

### Day 14：总复习 + 知识串联
- **内容**：全模块回顾
- **任务**：
  1. 画一张 Lance 格式全景图：File → Table → Index → Catalog 的层级关系
  2. 回答："Lance 的各层如何保持独立演进？"
  3. 总结 Lance 相比 Parquet + Iceberg 的独特设计决策
- **检验标准**：能画出架构图并解释每层职责

---

## 学习方法建议

1. **带着问题读**：每篇文档先快速扫标题，问自己"这个设计解决了什么问题？"
2. **对比记忆**：
   - Lance vs Parquet：无 row group、列添加成本低
   - Lance vs Iceberg：更紧密的 file-table 集成、原生向量支持
   - Lance vs Delta Lake：事务模型差异、索引原生支持
3. **画图辅助**：每天留 5-8 分钟做笔记/画图，视觉记忆比纯文字更有效
4. **不求甚解**：遇到具体 bit 布局、编码细节先标记，不深入源码
5. **周末复习**：Day 7 和 Day 14 是整合日，把碎片连成体系

---

## 进度检查点

| 检查点 | 日期 | 目标 |
|--------|------|------|
| Week 1 检查 | Day 7 | 能向他人解释 Lance File Format 和 Table Format 的核心区别 |
| Week 2 检查 | Day 14 | 能画出 Lance Lakehouse 四层架构图，并说明每层职责 |

---

---

# 深度面试问题集

> 以下问题按模块分类，难度从 ⭐ 到 ⭐⭐⭐⭐⭐ 递增。
> 建议在学习过程中尝试回答，Day 14 总复习时作为自测。

---

## 一、File Format（文件格式）

### 基础题 ⭐⭐

1. **Lance 文件格式为什么避免使用 Parquet 式的 row group？**
   - *提示：思考云存储的随机访问模式、page size、延迟差异*

2. **Lance 的 page 设计如何支持高效的随机访问？**
   - *提示：大页、offset 索引、不依赖 row group 的 statistics*

3. **Lance 把 statistics 和 search structures 移出文件格式，这个决策的利弊是什么？**
   - *提示：文件格式的稳定性 vs 索引的演进速度*

### 进阶题 ⭐⭐⭐

4. **structural encoding 和 logical encoding 的区别是什么？为什么需要这种分层？**
   - *提示：数据布局 vs 数据语义*

5. **Lance 的 dictionary encoding 与 Parquet 的 dictionary encoding 有何不同？**
   - *提示：page-level vs chunk-level、字典更新策略*

6. **FSST 压缩在什么场景下优于 LZ4/ZSTD？在什么场景下不适用？**
   - *提示：字符串数据、压缩率 vs 解压速度、字典大小*

### 深度题 ⭐⭐⭐⭐

7. **Lance 文件格式的版本演进策略是什么？如何保证向后兼容性？**
   - *提示：版本号、feature flags、reader 的降级策略*

8. **如果让你设计一个新的 encoding 来支持嵌套结构（如 List<Struct>）的高效随机访问，你会如何设计？**
   - *提示：validity bitmap、offset 数组、层级编码*

9. **Lance 的 file format 与 Arrow IPC format 的关系是什么？为什么选择自定义格式而非直接复用？**
   - *提示：随机访问、云存储优化、版本控制需求*

---

## 二、Table Format（表格式）

### 基础题 ⭐⭐

10. **Fragment 是什么？为什么 Lance 将行分组到 fragment 中，而不是直接使用文件？**
    - *提示：metadata-heavy operations、column addition、backfill*

11. **一个 fragment 包含多个 data file，每个 data file 只包含部分列，这种设计有什么好处？**
    - *提示：column addition、partial read、I/O 隔离*

12. **Manifest 在 Lance 中扮演什么角色？与 Delta Lake 的 transaction log 有何异同？**
    - *提示：版本列表、schema、fragment 集合、ACID 语义*

### 进阶题 ⭐⭐⭐

13. **Lance 如何实现列添加（column addition）而不重写现有数据？**
    - *提示：新 data file、schema evolution、manifest 更新*

14. **Schema evolution 中，字段重命名和字段删除再添加，在 Lance 中有何本质区别？**
    - *提示：field ID、lineage、向后兼容性*

15. **Lance 的删除机制（deletion file）如何工作？为什么使用 deletion file 而非直接重写数据？**
    - *提示：write amplification、row ID 稳定性、VACUUM 操作*

### 深度题 ⭐⭐⭐⭐

16. **Lance 的事务冲突检测是如何实现的？乐观锁还是悲观锁？为什么选择这种策略？**
    - *提示：manifest 的 CAS 操作、重试策略、writer 并发模型*

17. **多个 writer 同时写入同一个表时，Lance 如何保证 ACID 语义？与 Iceberg 的乐观并发控制相比有何差异？**
    - *提示：commit 协议、冲突解决、隔离级别*

18. **Row ID 的分配策略是什么？全局唯一还是 fragment 内唯一？删除后是否复用？**
    - *提示：64-bit 编码、高 32-bit / 低 32-bit、fragment 增长*

19. **Branch 和 Tag 的实现机制是什么？与 Git 的分支标签有何本质区别？**
    - *提示：manifest 引用、时间旅行、写操作权限*

20. **如果表有 1000 个 fragment，每个 fragment 有 10 个 data file，查询只涉及 2 列，Lance 如何优化 I/O？**
    - *提示：column projection、fragment pruning、data file 选择*

### 挑战题 ⭐⭐⭐⭐⭐

21. **Lance 的内存 WAL（mem_wal）设计是为了解决什么问题？既然是内存中的，崩溃时如何保证不丢数据？**
    - *提示：异步操作、manifest 持久化、恢复机制、性能与一致性的权衡*

22. **设计一个场景：高并发写入（1000+ writes/second）+ 频繁 schema evolution，Lance 的瓶颈会在哪里？如何优化？**
    - *提示：manifest 大小、fragment 数量、compaction 策略、metadata 缓存*

---

## 三、Index Formats（索引格式）

### 基础题 ⭐⭐

23. **为什么说 Lance 的索引是 "first-class table object"？这与传统数据库的索引有何不同？**
    - *提示：版本管理、事务协同、目录结构、生命周期*

24. **Lance 支持哪些 scalar index 类型？各适用于什么查询模式？**
    - *提示：BTree、Bitmap、ZoneMap、BloomFilter、RTree、LabelList、NGram*

25. **Vector index（ANN）在 Lance 中是如何存储的？与数据文件的关系是什么？**
    - *提示：独立 index file、index manifest、异步构建*

### 进阶题 ⭐⭐⭐

26. **索引如何与 Table 的事务机制协同？创建索引时是否阻塞写入？**
    - *提示：index transaction、background build、index manifest 版本*

27. **ZoneMap index 和 DataSkipping 的关系是什么？为什么 Lance 把它作为独立索引而非内嵌在文件格式中？**
    - *提示：统计信息演进、多 fragment 聚合、灵活更新*

28. **IVF_PQ 和 HNSW 在 Lance 中的实现有何差异？选择时需要考虑哪些因素？**
    - *提示：构建成本、内存占用、召回率、增量更新*

### 深度题 ⭐⭐⭐⭐

29. **Fragment reuse 索引（system index）的作用是什么？如何加速增量查询？**
    - *提示：新增 fragment 识别、避免全表扫描、与 manifest 的协同*

30. **如果向量数据持续增量写入，LNSW（Layered Navigable Small World）索引如何维护？是否需要全量重建？**
    - *提示：增量 HNSW、layer 更新、邻居重新分配、性能退化*

31. **全文检索（FTS）索引的 NGram 实现与倒排索引有何不同？在 Lance 中如何与 scalar index 统一？**
    - *提示：tokenization、posting list、与 scalar index 的复用*

---

## 四、架构与设计哲学

### 综合题 ⭐⭐⭐⭐

32. **Lance 的四层架构（File → Table → Index → Catalog）为何要保持独立演进？这种解耦带来了什么代价？**
    - *提示：版本兼容性、测试复杂度、性能开销、生态集成*

33. **Lance 定位为 "ML workloads and datasets" 的列式格式，相比通用格式（Parquet/ORC）做了哪些特定优化？**
    - *提示：嵌套结构、向量类型、随机访问、版本控制、特征工程工作流*

34. **Directory Catalog 和 REST Catalog 的选择场景是什么？Directory Catalog 如何在对象存储上实现原子性？**
    - *提示：S3 的 PUT 原子性、list 一致性、manifest 命名策略*

35. **Namespace Client Spec 的设计目标是什么？为什么需要这一抽象层？**
    - *提示：多 catalog 兼容、跨语言 SDK、零代码切换*

### 开放题 ⭐⭐⭐⭐⭐

36. **对比 Lance、Delta Lake、Iceberg、Hudi 的设计哲学，Lance 最独特的决策是什么？**
    - *提示：向量原生、无 row group、索引 first-class、ML 工作流优化*

37. **如果让你将 Lance 格式适配到边缘设备（资源受限、无云存储），哪些层需要修改？**
    - *提示：文件格式不变、catalog 简化、索引可选、WAL 调整*

38. **Lance 的 "storage-native" 设计理念如何体现在各层实现中？这对测试策略有何影响？**
    - *提示：对象存储接口、本地/S3/Azure/GCS 统一、mock 策略*

39. **在 Lance 中，"zero-copy automatic versioning" 是如何实现的？versioning 的代价是什么？**
    - *提示：manifest 链、fragment 共享、时间旅行查询、存储增长*

40. **设计一个评估框架：如何量化 Lance 相比 Parquet + Iceberg 在 "ML 特征存储" 场景下的优势？**
    - *提示：列添加延迟、随机访问延迟、向量查询召回率、版本切换开销*

---

## 面试准备建议

### 回答结构（STAR 变体）

回答技术问题时，建议采用以下结构：

1. **核心概念**（What）：先用 1-2 句话定义关键概念
2. **设计动机**（Why）：解释为什么这样设计，解决了什么问题
3. **工作机制**（How）：简述实现原理，可画图辅助
4. **对比分析**（Compare）：与竞品/替代方案对比
5. **权衡取舍**（Trade-off）：说明这种设计的代价和适用边界

### 示例回答框架（以 Q1 为例）

> **Q：Lance 为什么避免使用 Parquet 式的 row group？**
>
> **A**：
> - **What**：Parquet 将数据切分为 row group（通常 128MB-1GB），每个 row group 内按列存储；Lance 则使用大页（large pages），避免 row group 层级。
> - **Why**：Lance 面向云存储和高度选择性读取（如 ML 训练中的随机采样），row group 的 statistics 和边界会增加元数据开销，且不利于细粒度随机访问。
> - **How**：Lance 将统计信息和搜索结构外移到索引层，文件格式本身只关注高效存储和随机访问，页大小设计为对云存储友好（减少 LIST/GET 请求）。
> - **Compare**：Parquet 的 row group 优化的是批量扫描（analytical workloads），Lance 优化的是随机访问和列增删（ML workloads）。
> - **Trade-off**：Lance 的全表扫描性能可能略逊于调优后的 Parquet，但在 ML 场景下的列操作和随机访问显著更优。

### 自测清单

- [ ] 能画出 Lance File 的物理结构
- [ ] 能解释 Fragment / Data File / Manifest 的关系
- [ ] 能描述事务提交的冲突检测机制
- [ ] 能列举 5+ 种 index 类型及其适用场景
- [ ] 能对比 Lance vs Delta Lake vs Iceberg 的核心差异
- [ ] 能解释 "first-class table object" 的含义和意义

---

*计划制定时间：2026-06-17*
*建议配合官方文档图片（`overview.png`、`lakehouse_stack.png`）一起学习*
