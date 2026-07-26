# file format

![vortex_file_format](https://docs.vortex.dev/_images/vortex_file_format.svg)

表结构

```text
  ┌──────────┬────────────────────────┬───────────────────────────────────────┐
  │ 字段      │ 类型                   │ 编码策略                                │
  ├──────────┼────────────────────────┼───────────────────────────────────────┤
  │ id       │ Int32                  │ 原始值 + ZonedLayout（zone_len=8192）  │
  │ name     │ Utf8（可变长度字符串）    │ FSST 压缩 + ZonedLayout               │
  │ category │ Utf8（低基数）           │ Dictionary 编码 + ZonedLayout         │
  └──────────┴────────────────────────┴───────────────────────────────────────┘
```

## file layout

```text
  StructLayout ("root", row_count=100000)
  ├── ZonedLayout ("id", zone_len=8192)
  │   ├── ChunkedLayout ("data")
  │   │   ├── FlatLayout (row=8192) → Segment 0
  │   │   ├── FlatLayout (row=8192) → Segment 1
  │   │   ├── FlatLayout (row=8192) → Segment 2
  │   │   └── ... (13 chunks)
  │   └── FlatLayout ("zones", row=13) → Segment 13   ← Zone Map！
  │
  ├── ZonedLayout ("name", zone_len=8192)
  │   ├── ChunkedLayout ("data")
  │   │   ├── FlatLayout → Segment 14
  │   │   ├── FlatLayout → Segment 15
  │   │   └── ...
  │   └── FlatLayout ("zones", row=13) → Segment 27
  │
  └── ZonedLayout ("category", zone_len=8192)
      ├── DictLayout ("data")           ← 字典编码！
      │   ├── FlatLayout (dict values) → Segment 28   ← 共享字典，只存一份
      │   └── ChunkedLayout (codes)
      │       ├── FlatLayout (u8 codes) → Segment 29
      │       ├── FlatLayout (u8 codes) → Segment 30
      │       └── ...
      └── FlatLayout ("zones", row=13) → Segment 42

  my_table.vortex  (单个文件)
  │
  ├─ [Segment 0]  id 的数据 chunk 0 (row 0-8191)          ~32KB
  ├─ [Segment 1]  id 的数据 chunk 1 (row 8192-16383)      ~32KB
  ├─ [Segment 2]  id 的数据 chunk 2 (row 16384-24575)     ~32KB
  ├─ ...          (共 13 个 chunks)
  │
  ├─ [Segment 13] id 的 zone stats (13 行 × min/max/null)  ~512B
  │
  ├─ [Segment 14] name 的数据 chunk 0 (FSST 压缩)         ~80KB
  ├─ [Segment 15] name 的数据 chunk 1 (FSST 压缩)         ~78KB
  ├─ ...          (共 13 个 chunks)
  │
  ├─ [Segment 27] name 的 zone stats                      ~512B
  │
  ├─ [Segment 28] category 的 dictionary values (去重后)  ~200B  ← 只存一份！
  ├─ [Segment 29] category 的数据 chunk 0 (codes: u8)     ~8KB
  ├─ [Segment 30] category 的数据 chunk 1 (codes: u8)     ~8KB
  ├─ ...          (共 13 个 chunks)
  │
  ├─ [Segment 42] category 的 zone stats                  ~512B
  │
  ├─ [Segment 43] 文件级统计 (所有列的 min/max)           ~1KB
  │
  ├─ [Segment 44] Schema FlatBuffer (DType 定义)          ~200B
  │
  ├─ [Segment 45] Layout FlatBuffer (递归树，见下文)      ~2KB
  │
  ├─ [Segment 46] Footer FlatBuffer (segment 映射表)      ~1KB
  │
  ├─ [Segment 47] Postscript FlatBuffer (各段位置)        ~256B
  │
  └─ [EOF Marker] 8 bytes: version + postscript_len + "VTXF"
```

### ZonedLayout vs ChunkedLayout vs FlatLayout

```text
  ZonedLayout (zone_len = 8192)
  ├── child 0: ChunkedLayout / FlatLayout  ← 数据（FlatLayout 存储实际值）
  └── child 1: FlatLayout                  ← Zone Map（存储每 zone 的统计）
```

```text
  child 0 — 数据（可能是 ChunkedLayout → 多个 FlatLayout）
  ────────────────────────────────────────────────────────

  ChunkedLayout
  ├── FlatLayout (row 0-8191)     → Segment 0: [实际压缩数据，约 16KB]
  ├── FlatLayout (row 8192-16383) → Segment 1: [实际压缩数据，约 16KB]
  ├── FlatLayout (row 16384-24575)→ Segment 2: [实际压缩数据，约 16KB]
  └── ... (共 13 个 chunks)
  
  child 1 — Zone Map（一个 FlatLayout）
  ─────────────────────────────────────

  FlatLayout (13 rows) → Segment N: [
      // 每行是一个 zone 的统计
      {min: 10, max: 100, null_count: 0},   // zone 0
      {min: 5,  max: 80,  null_count: 2},   // zone 1
      {min: 200,max: 500, null_count: 0},   // zone 2
      ...
  ]
```

### 查询

WHERE value > 150：

1. 先读 Zone Map（child 1，很小，可能 1-2KB）
   - zone 0: max=100 → 全部不满足，跳过
   - zone 1: max=80 → 全部不满足，跳过
   - zone 2: max=500 → 可能满足，保留
   - ...
2. 只读保留的 zone 的数据（child 0 中对应的 FlatLayout）
   - 不需要读 Segment 0、1
   - 只读 Segment 2 及后续可能满足的

## IPC Message

```plaintext
  FlatLayout (Layout 树中的叶子)
      │
      └─ segment_id: 5
          │
          └─ Segment 5 (文件中的字节范围)
              │
              └─ IPC Message (序列化的 Array)
                  ├─ FlatBuffer: ArrayNode 树 (编码、元数据、统计)
                  └─ Buffers: 实际数据

```

FlatLayout 是 Layout 树的叶子节点，它包含一个 segment_id，指向文件中的一个 Segment，

那个 Segment 的内容就是一个 IPC Message（序列化的 Array）。

非 FlatLayout（Struct/Chunked/Zoned）都是中间节点，负责组织其他 Layout，不直接存储数据。

```plaintext
  PrimitiveArray
  ├── data: PrimitiveData
  │   └── buffer: [1, 2, 3, 4, 5] (20 bytes, Buffer<i32>)
  │
  ├── validity: Validity::Array(BoolArray)
  │   └── buffer: BitBuffer [1, 1, 0, 1, 1] (1 byte + padding)
  │
  └── dtype: Int32, Nullable
  
  ┌─────────────────────────────────────────────────────────────┐
  │ IPC Message (序列化后的二进制)                                 │
  ├─────────────────────────────────────────────────────────────┤
  │                                                             │
  │  [Buffer 0] values buffer (20 bytes)                        │
  │  ┌─────────────────────────────────────────────────────┐    │
  │  │ 01 00 00 00  │ 02 00 00 00  │ 03 00 00 00  │ ...    │    │
  │  │ i32=1        │ i32=2        │ i32=3        │        │    │
  │  └─────────────────────────────────────────────────────┘    │
  │  偏移: 0, 长度: 20, 对齐: 4 bytes                             │
  │                                                             │
  ├─────────────────────────────────────────────────────────────┤
  │  [Padding] 12 bytes (对齐到 16 bytes)                        │
  │  00 00 00 00 00 00 00 00 00 00 00 00                        │
  │                                                             │
  ├─────────────────────────────────────────────────────────────┤
  │  [Buffer 1] validity bitmap (1 byte)                        │
  │  ┌─────────────────────────────────────────────────────┐    │
  │  │ 1B 00 00 00 00 00 00 00  │  (BitBuffer: 5 bits)     │    │
  │  │ [1,1,0,1,1,0,0,0]        │  低 5 位是 validity       │    │
  │  └─────────────────────────────────────────────────────┘    │
  │  偏移: 32, 长度: 1, 对齐: 1 byte                              │
  │                                                             │
  ├─────────────────────────────────────────────────────────────┤
  │  [Padding] 15 bytes (对齐到 16 bytes)                        │
  │  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00               │
  │                                                             │
  ├─────────────────────────────────────────────────────────────┤
  │  [FlatBuffer] Array 消息 (元数据)                            │
  │  ┌──────────────────────────────────────────────────────┐   │
  │  │ Array {                                              │   │
  │  │   root: ArrayNode {                                  │   │
  │  │     encoding: 0,      // "vortex.primitive" 索引     │   │
  │  │     metadata: [],     // PrimitiveArray 无元数据      │   │
  │  │     children: [                                      │   │
  │  │       ArrayNode {                                    │   │
  │  │         encoding: 1,  // "vortex.bool" 索引           │   │
  │  │         metadata: [],                                │   │
  │  │         children: [],                                │   │
  │  │         buffers: [1], // 引用 Buffer 1 (validity)     │   │
  │  │         stats: null                                  │   │
  │  │       }                                              │   │
  │  │     ],                                               │   │
  │  │     buffers: [0],     // 引用 Buffer 0 (values)       │   │
  │  │     stats: {                                         │   │
  │  │       min: 1, max: 5, null_count: 1, ...             │   │
  │  │     }                                                │   │
  │  │   },                                                 │   │
  │  │   buffers: [                                         │   │
  │  │     BufferInfo { padding: 0, align: 2, comp: None, len: 20 },  // Buffer 0
  │  │     BufferInfo { padding: 12, align: 0, comp: None, len: 1 }    // Buffer 1
  │  │   ]                                                  │   │
  │  │ }                                                    │   │
  │  └──────────────────────────────────────────────────────┘   │
  │  偏移: ~48, 长度: ~80 bytes (FlatBuffer 编码)                 │
  │                                                             │
  ├─────────────────────────────────────────────────────────────┤
  │  [u32] FlatBuffer 长度 (4 bytes, little-endian)              │
  │  50 00 00 00  // = 80                                       │
  │                                                             │
  └─────────────────────────────────────────────────────────────┘
```
为了零拷贝内存映射（mmap）TODO ：对齐后，文件中的 buffer 可以直接映射到内存地址，CPU 可以直接读取，不需要拷贝或解析。

## Vortex vs ORC

ORC

```text
  Stripe 0 (假设 ~64MB，可能包含 100万+ 行，这里简化)
  ├── Index Streams
  │   └── Column 0 Row Index (10 entries, 每 10000 行)
  │       ├── Entry 0: positions=[0,0,0], stats={min:1, max:100}
  │       ├── Entry 1: positions=[...], stats={min:101, max:200}
  │       └── ...
  ├── Data Streams
  │   └── Column 0 DATA Stream (所有 10 万行的压缩数据)
  └── Stripe Footer

```

查询 WHERE id > 90000：

1. 读 File Footer → 知道 Stripe 0 位置
2. 读 Stripe 0 的 Index Streams → 拿到 10 个 RowIndexEntry
3. 检查每个 Entry 的 stats：
   - Entry 0-8: max < 90000 → 跳过
   - Entry 9: max = 1000, min = 901 → 可能满足
4. Entry 与 Data Streams 的 block 不是一一对应的

Vortex

```text
  ZonedLayout (zone_len = 8192)
  ├── ChunkedLayout ("data")
  │   ├── FlatLayout (0-8191)     → Segment 0
  │   ├── FlatLayout (8192-16383) → Segment 1
  │   ├── ...
  │   └── FlatLayout (98304-99999)→ Segment 12
  └── FlatLayout ("zones")        → Segment 13
      └── 13 行: [{min:1,max:100}, {min:101,max:200}, ...]
```

查询 WHERE id > 90000：

1. 读 Zone Map（Segment 13，~512B）
2. 检查 13 个 zone：
   - Zone 0-10 (0-90111): max < 90000 → 跳过
   - Zone 11 (90112-98303): max = 95000 → 保留
   - Zone 12 (98304-99999): max = 99999 → 保留
3. 直接读 Segment 11 和 12（两个独立的 I/O 请求）
4. 其他 Segment 完全不读

### PRESENT Stream LENGTH Stream

Vortex: 压缩是为了查询更快，不是为了磁盘更小

- ORC/Parquet 的设计是"先压缩存盘，查询时解压"
- Vortex 的设计是"用查询友好的方式压缩，查询时尽量不解压"

```text
  StructArray (root, 100000 rows)
  ├── id: PrimitiveArray (Int32, nullable)
  │   ├── data: Buffer<i32> [1, 2, 0, 4, ...]     ← 400KB
  │   └── validity: Validity::Array(BoolArray)     ← slot 0: [T, T, F, T, ...]
  │       └── data: BitBuffer                      ← 12.5KB
  │
  ├── name: VarBinViewArray (Utf8, nullable)
  │   ├── views: Buffer<BinaryView> (100000 entries)
  │   │   ├── [0]: {size: 5, data: "Alice"}        ← 内联！
  │   │   ├── [1]: {size: 3, data: "Bob"}          ← 内联！
  │   │   ├── [2]: null                            ← 通过 validity 标记
  │   │   ├── [3]: {size: 5, data: "David"}        ← 内联！
  │   │   ├── [4]: {size: 20, prefix: "this", buf: 0, off: 0}  ← 引用
  │   │   └── ...                                  ← 1600000 bytes (1.6MB)
  │   ├── buffers: [Buffer<u8>]                     ← 长字符串数据
  │   │   └── [0]: "this is a long string\0..."    ← ~500KB
  │   └── validity: Validity::Array(BoolArray)      ← slot 0
  │       └── data: BitBuffer                      ← 12.5KB
  │
  └── category: DictArray (Utf8, nullable)
      ├── codes: PrimitiveArray<u8> [0, 1, 0, 2, ...]  ← 100KB
      │   └── validity: Validity::Array(BoolArray)     ← slot 0
      ├── values: VarBinViewArray ["A", "B", "C"]      ← child 1: 共享字典
      │   ├── views: [3 entries]                        ← 48 bytes
      │   └── buffers: []                               ← 全部内联！
      └── validity: Validity::Array(BoolArray)          ← slot 0
```

### VarBinView 的压缩问题

```text
  1. 固定的 16 字节开销
  ─────────────────────

  每个字符串，不管长短，都有一个 16 字节的 BinaryView：

  短字符串 "hi"（2 字节）：
  ┌────────────────────────────────────────┐
  │ BinaryView (16 bytes)                  │
  │ ├─ size: 2                             │
  │ └─ data: "hi\0\0\0\0\0\0\0\0\0\0\0"    │
  └────────────────────────────────────────┘
  实际数据 2 字节， overhead: 16 字节 → **8x 膨胀！**

  长字符串 "this is a very long string"（26 字节）：
  ┌────────────────────────────────────────┐
  │ BinaryView (16 bytes)                  │
  │ ├─ size: 26                            │
  │ ├─ prefix: "this"                      │
  │ ├─ buffer_index: 0                     │
  │ └─ offset: 0                           │
  └────────────────────────────────────────┘
  实际数据 26 字节， overhead: 16 字节 → 1.6x 膨胀
```

例子：name 列 ["Alice", "Bob", "Charlie", "Alice", "Bob", ...]

```text
  ORC
  ───
  原始数据: "Alice\0Bob\0Charlie\0Alice\0Bob\0..."
  → LENGTH Stream: [5, 3, 7, 5, 3, ...]
  → DATA Stream: "AliceBobCharlieAliceBob..."
  → ZLIB 压缩整个 DATA Stream


  Vortex VarBinView
  ─────────────────
  方案 A：直接 VarBinView
  - views: [{size:5, data:"Alice"}, {size:3, data:"Bob"}, {size:7, ref:(0,0)}, ...]
  - buffers: ["Charlie..."]
  → 小字符串内联，大字符串引用

  方案 B：Dict 编码（如果基数低）
  - codes: [0, 1, 2, 0, 1, ...] (u8)
  - values: ["Alice", "Bob", "Charlie"]
  → codes 可以用 BitPacking 压缩

  方案 C：FSST 编码（字符串专用）
  - 构建有限状态转换表
  - 替换常见子串
```
