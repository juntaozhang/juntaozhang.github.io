## 编码策略

### 4.1 编码层次

```
编码流程：
Arrow Array
    ↓
Structural Encoding（结构编码）
  分解为更小的单元
  处理 validity、offsets、repetition levels
    ↓
Compression（压缩）
  Bitpacking、RLE、FSST 等
  LZ4、ZSTD 等通用压缩
    ↓
Disk Page（磁盘页）
```

### 4.2 Structural Encodings

- 什么是 structural encoding？与 logical encoding 的区别？


  | 项目             | Structural Encoding（结构编码）    | Logical Encoding / Compression（压缩） |
  | ---------------- | ---------------------------------- | -------------------------------------- |
  | 定义             | 页内数据的布局格式                 | 数据压缩算法                           |
  | 具体类型         | MiniBlock、FullZip、Constant、Blob | Flat、Bitpacking、FSST、LZ4、ZSTD      |
  | 解决什么问题     | 行怎么排列、怎么支持随机访问       | 数据怎么变小                           |
  | 是否关心数据内容 | 否                                 | 是（利用数据特征）                     |
- 为什么 Lance 把 statistics 和 search structures 移出文件格式？

  - 文件格式稳定，statistics 和索引可以单独升级
  - 不强制每个文件都带统计信息
  - 不同场景用不同索引（BTree、Bitmap、ZoneMap）
  - 写文件时不需要计算和维护统计
  
    | 项目       | Parquet                                    | Lance                  |
    | ---------- | ------------------------------------------ | ---------------------- |
    | Statistics | 内嵌在文件内（row group、page 级 min/max） | 移出到 Index 层        |
    | 结果       | 文件格式与统计耦合                         | 文件格式纯净，索引独立 |
- Lance Paimon 在 statistics 比较

  - Paimon 兼容现有格式（ORC/Parquet 有内置统计），额外在 manifest 维护表级统计。Lance 更彻底，文件格式完全无统计，所有搜索结构都是独立索引。
- lance常见 encoding 及其适用场景


  | 编码      | 适用数据大小   | 场景                       | 随机访问 I/O                                 |
  | --------- | -------------- | -------------------------- | -------------------------------------------- |
  | MiniBlock | < 256B         | 整数、浮点、短字符串       | 1 次（读 block，cache page and page header） |
  | FullZip   | 256B ~ 32KB    | 向量 embedding、中等字符串 | 2 次（index + data）                         |
  | Constant  | 任意（值相同） | 全 null、默认值列          | 0 次（cache 命中）                           |
  | Blob      | 1MB+           | 图片、视频、大文本         | 2 次（descriptor + buffer）                  |

#### Mini Block Layout（小数据类型）
适用场景：
- 整数、浮点、布尔、短字符串
- 大多数传统数据类型

data Page 的逻辑视图：
- buffer0: 改layout的metadata
  - 每个块 12 bit， 前8个bit 是 word，后 4个是value count
    - word count$[0,2^{12}-1] * 8$ = $[0,2^{15}-1]$: because of mini blocks are padded to 8 byte boundaries.
    - $log2(num\_values) ∈ [0,15]$ = $[0,2^{16}-1]$:the lower 4 bits store log2(num_values) and num_values must be a power of two.
- buffer1: 多个 mini blocks 组成的
```
  ┌──────────────────────────────────────────┐
  │  Buffer 0: Mini Block Metadata           │
  │  ┌─────────────────────────────────────┐ │
  │  │ 块 0: size=64 words, log2(num)=10   │ │  ← 1024 个值 [1..1024]
  │  │ 块 1: size=63 words, log2(num)=10   │ │  ← 1024 个值 [1025..2048]
  │  │ 块 2: size=64 words, log2(num)=10   │ │  ← 1024 个值 [2049..3072]
  │  │ ...                                 │ │
  │  │ 块 9: size=32 words, log2(num)=0    │ │  ← 最后一块，值数单独算
  │  └─────────────────────────────────────┘ │
  │  总大小: 10 块 × 8 bytes = 80 bytes       │
  ├──────────────────────────────────────────┤
  │  Buffer 1: Mini Blocks（实际压缩数据）      │
  │  ┌─────────────────────────────────────┐ │
  │  │ [块 0: 1, 2, 3, ..., 1024]          │ │  ← 压缩后 256 bytes
  │  │ [块 1: 1025, 1026, ..., 2048]       │ │  ← 压缩后 252 bytes
  │  │ [块 2: 2049, 2050, ..., 3072]       │ │  ← 压缩后 256 bytes
  │  │ ...                                 │ │
  │  │ [块 9: 9217, 9218, ..., 10000]      │ │  ← 压缩后 128 bytes
  │  └─────────────────────────────────────┘ │
  │  总大小: ~2.5KB (原始 40KB，压缩后)         │
  └──────────────────────────────────────────┘
```

假设一个 Page 有 3 个 mini blocks，buffer 物理结构：
- mini-block should be less than 32KiB of compressed data
- Mini blocks are padded to 8 byte boundaries
- 每个 mini-block 包含 128~4096 个 values, a power-of-two number of values
- Mini-Block 类型
  - Buffer 0 = repetition levels
  - Buffer 1 = definition levels
  - Buffer 2 = values
  - Buffer 3 = 可能是 dictionary indices
- Mini Block/Header: 描述这个 mini-block 内部有几个 buffer，每个多大
```text
  Buffer 0 (Metadata):
    Block 0: 100 words, 1024 values   → 2 bytes
    Block 1: 150 words, 1024 values   → 2 bytes
    Block 2: 80 words, 512 values     → 2 bytes

  Buffer 1 (Mini Blocks 实际数据):
    ├─ Mini Block 0 (100 words = 800 bytes)
    │   ├── Header: num_buffers=3, buf0_size=100, buf1_size=50, buf2_size=600
    │   ├── Padding
    │   ├── Buffer 0: rep levels (100 bytes)
    │   ├── Padding
    │   ├── Buffer 1: def levels (50 bytes)
    │   ├── Padding
    │   └── Buffer 2: values (600 bytes)
    │   └── Padding (总大小 ≈ 800 bytes)
    │
    ├─ Mini Block 1 (150 words = 1200 bytes)
    │   ├── Header: num_buffers=3, buf0_size=200, buf1_size=100, buf2_size=800
    │   └── ... (rep, def, values)
    │
    └─ Mini Block 2 (80 words = 640 bytes)
        ├── Header: num_buffers=2, buf0_size=0, buf1_size=40, buf2_size=560
        └── ... (这个 block 没有 rep，def 40 bytes，values 560 bytes)
```

##### 场景 1：最简单的 int32 数组，没有 null，没有 list

num_buffers = 1

Mini Block 内部：
- Buffer 0: values (int32 数据)

没有 rep，没有 def，只有一个 values buffer。

##### 场景 2：有 null 的 int32 数组

num_buffers = 2

Mini Block 内部：
- Buffer 0: definition levels (标记哪些是 null)
- Buffer 1: values (int32 数据，null 的位置可以填任意值)

##### 场景 3：有 list 的 int32 数组

num_buffers = 3

Mini Block 内部：
- Buffer 0: repetition levels (标记新列表开始)
- Buffer 1: definition levels (标记 null/empty)
- Buffer 2: values

##### 场景 4：Dictionary 编码 TODO

num_buffers = 2 或 3

Mini Block 内部：
- Buffer 0: dictionary indices (整数)
- Buffer 1: values (可能还有)

或者 dictionary 放在 page 级别的单独 buffer 里（Buffer 2）。

##### 场景 5：FSST 压缩字符串 + Bitpacking 偏移 TODO

num_buffers = 3

Mini Block 内部：
- Buffer 0: repetition levels
- Buffer 1: definition levels
- Buffer 2: FSST 压缩后的字符串数据
- Buffer 3: bitpacked 偏移（可能作为单独 buffer）

**读取第 9500 个值**：

1. 查 Metadata:

   - 块 0~8: 每块 1024 个，共 9216 个
   - 块 9: log2(num)=0，是最后一块
   - 第 9500 个值在块 #9 (9500 > 9216)
2. 计算块 #9 的偏移:

   - 前面 9 块的总大小 = sum(block_size[0..8])
   - 从该偏移读块 #9 的压缩数据
3. 解压块 #9:

   - 得到 [9217, 9218, ..., 10000]（784 个值）
4. 取块内第 9500 - 9216 = 284 个值

   - 返回 9217 + 283 = 9500

#### Full Zip Layout（大数据类型）

适用场景：

- 向量嵌入（>128 bytes）
- 大字符串
- 张量

结构：
```
  ├──────────────────────────────────┤
  | Data Pages                       |
  |   Data Buffer 0(Data Buffer)     |
  |   Data Buffer 1(Repetition Index)|
  |   Data Buffer 2(Data Buffer)     |
  |   Data Buffer 3(Repetition Index)|
  |   ...                            |
  ├──────────────────────────────────┤
  | Column Metadatas                 |
  |   Column 0 Metadata*             |
  |     ├─ Page 0 {buffer_offsets,   |
  |     │         buffer_sizes,      |
  |     │         length, encoding}  |
  |     ├─ Page 1 {buffer_offsets,   |
  |     │         buffer_sizes, ...} |
  |     └─ ...                       |
  |   Column 1 Metadata*             |
  |   ...                            |
  └──────────────────────────────────┘
```

Column Metadata Details

```
  │  Column Metadata                             │
  │  ┌─────────────────────────────────────────┐ │
  │  │ Page {                                  │ │
  │  │   buffer_offsets: [off0, off1, ...]     │ │
  │  │   buffer_sizes:   [size0, size1, ...]   │ │
  │  │   length: 10000  (num_rows)             │ │
  │  │   priority: 0     (row_number)          │ │
  │  │   encoding: {                           │ │
  │  │     DirectEncoding {                    │ │
  │  │       encoding: <PageLayout protobuf>   │ │
  │  │     }                                   │ │
  │  │   }                                     │ │
  │  │ }                                       │ │
  │  └─────────────────────────────────────────┘ │
```

Data Buffer & Index Buffer

```
  ┌────────────────────────────────────────────┐
  │  Buffer 0: Data Buffer	                   │
  │  ┌───────────────────────────────────────┐ │
  │  │ [Row 0 压缩数据: 4096 bytes]           │ │  ← 原始 16KB → 压缩 4KB
  │  │ [Row 1 压缩数据: 4096 bytes]           │ │
  │  │ [Row 2 压缩数据: 4096 bytes]           │ │
  │  │ ...                                   │ │
  │  │ [Row 9999 压缩数据: 2048 bytes]        │ │  ← 最后一条
  │  └───────────────────────────────────────┘ │
  │  Buffer 1: Repetition Index Buffer	       │
  │  ┌───────────────────────────────────────┐ │
  │  │ Offset for Row 0                      │ │
  │  │ Offset for Row 1                      │ │
  │  │ ...                                   │ │
  │  │ Offset for Row N                      │ │
  │  │ Offset for Row N + 1 (边界，算长度用)   │ │
  │  └───────────────────────────────────────┘ │
  └────────────────────────────────────────────┘
```


具体例子
```text
  数据：List<String>，3 行

  Row 0: ["hello", "world"]
  Row 1: []
  Row 2: ["foo"]
```
Data Buffer 内部（Full Zip）
```
  ┌─────────────────────────────────────────┐
  │ Control Word 0: rep=0, def=0            │  ← Row 0, "hello", 新列表开始
  │ Length: 5                               │
  │ "hello"                                 │
  ├─────────────────────────────────────────┤
  │ Control Word 1: rep=0, def=0            │  ← Row 0, "world", 继续当前列表
  │ Length: 5                               │
  │ "world"                                 │
  ├─────────────────────────────────────────┤
  │ Control Word 2: rep=1, def=2            │  ← Row 1, 空列表
  │ (无 value 数据)                          │
  ├─────────────────────────────────────────┤
  │ Control Word 3: rep=0, def=0            │  ← Row 2, "foo", 新列表开始
  │ Length: 3                               │
  │ "foo"                                   │
  └─────────────────────────────────────────┘

  Control Word 里的 rep 字段 = Repetition Level：
     rep=0 = 继续当前列表
     rep=1 = 新列表开始（第一层 list）
     rep=2 = 新列表开始（第二层 list，嵌套）
```

Repetition Index Buffer
```
  [0, 18, 18, 24]

  含义：
    Row 0 从 Data Buffer 偏移 0 开始
    Row 1 从 Data Buffer 偏移 18 开始（Row 0 结束位置）
    Row 2 从 Data Buffer 偏移 18 开始（Row 1 是空列表，不占空间）
    Row 3 从 Data Buffer 偏移 24 开始（结束边界）
```

读取第 500 个向量

1. 查 Buffer 0 (Row Index):
   - Row 500 的 offset = 2048000 (500 × 4096)
   - Row 500 的 length = 4096
2. 读 Buffer 1:
   - 从偏移 2048000 读 4096 bytes 压缩数据
3. 解压:
   - 得到 4096 个 float = 16KB 原始数据
4. 返回 [0.3, 0.4, 0.5, ..., 1.4096]

两次IO：

- 第 1 次读 Repetition Index 取行偏移
- 第 2 次读 Data Buffer 取实际数据`

#### Blob Layout（超大二进制）

BlobLayout 的数据一定在外部存储
适用场景：

- 图像、视频、音频
- 大文件（>1MB）

什么是 in line/ out of line？

Data Pages 区域:
```text
  │                                       │
  │   Page Buffer (描述符)                 │  ← "in-line" (Page 内部)
  │     {position, size}                  │
  │     {position, size}                  │
  │     ...                               │
  │                                       │
  │   Global Buffer (实际 blob 数据)       │  ← "out-of-line" (Page 外部)
  │     [1MB 图片数据]                     │
  │     [2MB 视频数据]                     │
  │     ...                               │
  │                                       │
  │   其他 Page 的 Buffer                  │
  │     ...                               │
  └───────────────────────────────────────┘
```

### 4.3 压缩技术（Compressive Encodings）


| 类型        | 本质                               |
| ----------- | ---------------------------------- |
| Opaque      | 压缩后变成"一坨"，必须整体解压     |
| Transparent | 压缩后仍有"边界"，能直接定位单个值 |

- Opaque 用于 Mini-Block
- Transparent 用于 full-zip


| Encoding                 | 用途                       | 输入                 | 输出                     |
| ------------------------ | -------------------------- | -------------------- | ------------------------ |
| Flat                     | 无压缩，直接存储           | Fixed-width block    | 单个 buffer              |
| Variable                 | 变长数据（字符串、二进制） | Variable-width block | offsets + values         |
| Constant                 | 所有值相同                 | 任意 block           | 无输出（值在描述中）     |
| Dictionary               | 字典编码                   | 任意 block           | 字典 + 索引              |
| RLE(Run Length Encoding) | 游程编码                   | 任意 block           | 值 + 长度                |
| Bitpacking               | 位压缩                     | Fixed-width block    | 更小的 fixed-width block |
| FSST                     | 字符串压缩                 | Variable-width block | 符号表 + 压缩值          |
| BSS (ByteStreamSplit)    | 字节流拆分                 | Fixed-width block    | 拆分后的 block           |
| General                  | 通用压缩（zstd, lz4）      | 任意 block           | 压缩后的 block           |
| FixedSizeList            | 定长列表展平               | FSL block            | 展平后的 values          |
| PackedStruct             | 结构体打包                 | Struct block         | 打包后的 block           |

- **BSS**：float 向量、数字、Embedding, 数字按字节拆开排队 → 让 zstd 压得更小
- **FSST**：字符串、URL、日志、文本, 字符串找高频片段 → 用符号替换，又小又快
- **FixedSizeList**: 只管嵌套向量结构，不做压缩, 把嵌套列表 [[1,2,3], [4,5,6]] 展平成一维基础数组 [1,2,3,4,5,6]
  - 展平后的一维 float/int 数组，才能交给 BSS 做字节拆分优化

#### BSS (ByteStreamSplit)

**作用**：把数字（float/int/向量）**按字节拆开重组**，让通用压缩（zstd/lz4）能压得更小。
**只处理数字**，不处理字符串。

例子：4个 32位 float 数字，原始数据（内存里的字节）：

```
数字1：11 22 33 44
数字2：11 22 33 55
数字3：11 22 33 66
数字4：11 22 33 77
```

正常存储（交错在一起）

```
11 22 33 44  11 22 33 55  11 22 33 66  11 22 33 77
```

很乱，压缩效果一般。

BSS 拆分后（按字节位置分组）

```
第1字节流：11 11 11 11   ← 全一样！
第2字节流：22 22 22 22   ← 全一样！
第3字节流：33 33 33 33   ← 全一样！
第4字节流：44 55 66 77
```

- 前3个流**极度重复**，zstd 一压就极小
- BSS 本身不压缩，只是**重新排列字节**，让压缩算法发挥最大效果

#### FSST 举例

**作用**：给**字符串**做超快压缩，Lance 字符串默认用它。
**只处理字符串/文本**。

例子：4个相似URL

```
"https://lance.dev/page1"
"https://lance.dev/page2"
"https://lance.dev/page3"
"https://lance.dev/page4"
```

1. FSST 第一步：造符号表（自动生成）
   发现高频串：

```
0x01 → "https://lance.dev/page"
```

2. FSST 第二步：替换压缩

```
page1 → 0x01 + "1"
page2 → 0x01 + "2"
page3 → 0x01 + "3"
page4 → 0x01 + "4"
```

3. 最终存储

```
符号表 + [0x011, 0x012, 0x013, 0x014]
```

体积直接缩小 **80%+**，而且**解码极快**。

#### Bitpacking

```
  原始存储（32 bits × 5 = 160 bits）：
    [00000000000000000000000001100100]  100
    [00000000000000000000000011001000]  200
    [00000000000000000000000000110010]  50
    [00000000000000000000000100101100]  300
    [00000000000000000000000010010110]  150

  Bitpacked（9 bits × 5 = 45 bits）：
    [001100100]  100
    [011001000]  200
    [000110010]  50
    [100101100]  300
    [010010110]  150
```

#### Variable（变长无压缩）

```text
  原始字符串：["hello", "world", "hi"]

  存储：
  ┌─────────────────────────────────────────┐
  │ offsets: [0, 5, 10, 12]                  │
  │ values:  "helloworldhi"                   │
  └─────────────────────────────────────────┘
```

### 4.4 压缩配置

```python
import pyarrow as pa

# 通过字段元数据配置压缩
field = pa.field(
    "data",
    pa.float32(),
    metadata={
        "lance-encoding:compression": "zstd",
        "lance-encoding:compression-level": "3",
        "lance-encoding:bss": "auto",
        "lance-encoding:rle-threshold": "0.5"
    }
)
```

---

### Repetition and Definition Levels

```text
Struct {                           ← 根（最外层）
  info: Struct {                   ← info
    age: int,                      ← age
    name: List<String>             ← name, list元素
  },
  desc: string                     ← desc
}

数据: [
  {info: {age: 20, name: ["Alice", "Bob"]}, desc: "X"},    ← 全有效
  NULL,                                                    ← 根 null
  {info: NULL, desc: "Y"},                                 ← info null
  {info: {age: NULL, name: ["Charlie"]}, desc: "Z"},       ← age null
  {info: {age: 30, name: NULL}, desc: "W"}                 ← name null（不是空list，是name字段null）
]
```

Pages def

```text
Page (age):
├─ Buffer 0: Mini Block Metadata
│   └─ block0: 5 values, 8-byte alignment
│
├─ Buffer 1: Mini Blocks
│   └─ block0:
│       ├─ header: 3 buffers
│       ├─ values: [20, ?, ?, ?, 30]        ← ? 是占位，实际压缩可能不存
│       ├─ validity: [1, 0, 0, 0, 1]        ← validity 是 Arrow 兼容层
│       └─ def_levels: [0, 3, 1, 2, 0]      ← 0=有效, 3=根null, 1=infonull, 2=agenull
│

Page (desc):
├─ Buffer 0: Mini Block Metadata
│   └─ block0: 5 values
│
├─ Buffer 1: Mini Blocks
│   └─ block0:
│       ├─ header: 4 buffers
│       ├─ offsets: [0, 1, 1, 2, 3, 4]        ← 行0="X"(0-1), 行1=null(1-1), 行2="Y"(1-2)...
│       ├─ data: "XYZW"                       ← 实际字符串拼接
│       ├─ validity: [1, 0, 1, 1, 1]          ← validity 是 Arrow 兼容层
│       └─ def_levels: [0, 3, 0, 0, 0]        ← 行1 根 null
│

Page (name): 
├─ Buffer 0: Mini Block Metadata
│   └─ block0: 6 items (5 rows)
│
├─ Buffer 1: Mini Blocks
│   └─ block0:
│       ├─ header: 4 buffers
│       ├─ offsets: [0, 5, 8, 8, 8, 15, 15]     ← 变宽 string offsets
│       ├─ data: "AliceBobCharlie"              ← 实际字符串
│       ├─ validity: [1, 1, 0, 0, 1, 0]         ← validity 是 Arrow 兼容层
│       ├─ def_levels: [0, 0, 3, 1, 0, 2]       ← struct 哪层 null
│       └─ rep_levels: [2, 0, 2, 2, 2, 2]       ← list 哪层重复
│
```
