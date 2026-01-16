# [Parquet](https://parquet.apache.org/docs/file-format/)

## 1. Parquet 概述
File (文件) → Row Group (行组) → Column Chunk (列块) → Page (页)

| 组成部分 | 大小/位置 | 作用 |
| :--- | :--- | :--- |
| Header (文件头) | 前 4 字节 | 魔数 `PAR1`，标识文件类型。 |
| Body (数据体) | 中间部分 | 包含一个或多个 Row Group。 |
| Footer (文件尾) | 末尾部分 | 包含 File Metadata (元数据) + 4 字节 Footer 长度 + 4 字节魔数 `PAR1`。 |

Parquet 是一种列式存储格式，专为高效存储和处理大规模分析数据而设计。它提供了良好的压缩性能和高效的查询能力，广泛应用于大数据生态系统中（如 Spark、Hive、Presto 等）。

## 2. Parquet 文件结构
Parquet 文件采用分层结构，从整体到局部依次为：
- Row group 大小通常与 HDFS 对齐

![Parquet](https://parquet.apache.org/images/FileLayout.gif)

![File metadata](https://parquet.apache.org/images/FileMetaData.svg)


### Page Header
- Page size 一般是 1 MB
- next page header offset = current page header offset + compressed page size

![Page header](https://parquet.apache.org/images/PageHeader.svg)

通过 data page offset 与 index page offset 读取 PageHeader，通过 compressed page size 获取 PageData 的大小，这样就可以确定 下一个page 的 offset



`[page header][page data][page header][page data][page header][page data]...`

- 只要知道下一个页的起始偏移（next page offset），直接从这个偏移位置开始读取字节流，就能完整解析出 PageHeader，完全不需要提前知道 PageHeader 的长度？
- 在 Parquet 官网定义的基础结构下（无 PageLocation），要跳过前面的 Page，确实需要逐个解析这些 Page 的 PageHeader


### Page Data

- PLAIN 编码 + 无空值 + 非嵌套
    - `[字符串1长度][字符串1字节内容][字符串2长度][字符串2字节内容]...[字符串N长度][字符串N字节内容]`
- PLAIN 编码 + 有空值（新增空值位图）
    - [空值位图（BIT_PACKED 编码）][有效字符串1长度][有效字符串1内容][有效字符串2长度][有效字符串2内容]...

## 3. Parquet 与 ORC 比较
| 特性 | Parquet | ORC |
|-----|---------|-----|
| 文件结构 | 更灵活，支持多种嵌套结构 | 更适合扁平表结构 |
| 压缩性能 | 优秀 | 优秀，通常略好 |
| 查询性能 | 优秀 | 优秀，特别是对复杂查询 |
| 元数据丰富度 | 丰富 | 非常丰富 |
| 索引支持 | 基本索引（统计信息+行组级定位），页级索引需动态构建 | 更高级的索引（如 Bloom Filter、内置 RowIndex 行级索引） |
| **随机定位能力** | 1. 粗粒度（RowGroup 级）：直接定位，无需解析前序页<br>2. 细粒度（Page 级）：首次需解析前序 PageHeader 构建内存索引，后续可内存定位 | 1. 原生细粒度（Stripe/行级）：依赖内置 RowIndex，直接定位目标页/行<br>2. 无需解析前序页，首次定位效率更高 |
| **空值处理** | 非嵌套列：通过 `Definition Levels` 位图标记空值，仅存储非空数据<br>嵌套列：结合 `Repetition Levels` 处理 | 通过 `NULL` 位图标记空值，设计更简洁，位图与数据的对应关系更直观 |
| **变长字符串存储** | PLAIN 编码：`[Varint 长度][字节内容]`，有空值时前缀加空值位图<br>字典编码：存储字典 ID 序列，适合低基数字符串 | 直接存储 `[长度][字节内容]`，内置索引可快速定位字符串偏移，高基数字符串读取更高效 |
| **内存开销** | 默认低（仅加载文件/行组级元数据），页级索引构建后内存开销中等 | 略高（需加载 Stripe 级 RowIndex 和 Bloom Filter 等索引元数据） |
| **适用场景** | 跨平台数据交换、嵌套数据存储、批量读取为主的场景 | 交互式查询、频繁随机读取、扁平大表分析 |

## 在 Parquet 和 ORC 这两种列式存储格式中，通过ID值点查 Name 等字段信息的过程
**Parquet**
1. 定位 Row Group：根据 ID 范围确定所在 Row Group。
2. 解析 ID Page ：顺序加载每个 Page Header，根据max/min 确定ID范围所在 Page Data，然后获取对应的行数。
   - 顺序加载 Page Data，顺序读取 ID 数据，找到目标 ID 所在行。
3. 解析 Name Page：顺序加载每个 Page Header，根据 num_values确定 Name 的对应行数的 Data Page。
   - 加载 Page Data 所有数据，顺序找到第几行的 Name 数据。

**ORC**
1. 找大区块（Stripe）
   - 读 ORC 文件末尾的元数据，筛选出包含 id=1001 的 Stripe2（靠 Stripe 级 id 列 min/max 统计：1001~2000）。
2. 找小区块（Row Group）
   - 读 Stripe2 的行索引（RowIndex），筛选出包含 id=1001 的 RowGroup1（靠 Row Group 级 id 列 min/max 统计：1001~1100），锁定这个 1000 行的小区块。
3. 读 id 列找目标行
   - 只读取 RowGroup1 里 id 列的少量数据，解码后找到 id=1001（对应 RowGroup1 里的第 1 行）。
4. 读 name 列取对应值
   - 只读取 RowGroup1 里 name 列的少量数据，根据 id=1001 的行位置，直接解析出对应的 name 值。

