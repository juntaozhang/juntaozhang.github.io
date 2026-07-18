## MOSAIC FILE
```text
  ┌──────────────────────────────────────────────────────────────────────────────┐
  │                              MOSAIC FILE                                     │
  ├──────────────────────────────────────────────────────────────────────────────┤
  │                                                                              │
  │  ┌─────────────────────────────────────────────────────────────────────────┐ │
  │  │  ROW GROUP 0                                                            │ │→ 有10K cols 和 100K rows 数据
  │  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
  │  │  │  BUCKET 0 DATA BLOCK (zstd compressed)                          │    │ │→ 100 个 cols
  │  │  │                                                                 │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  ENCODING FLAGS (2 bits × 100 cols = 25 bytes)          │    │    │ │
  │  │  │  │  col_0000: 00 (PLAIN)                                   │    │    │ │→ 直接读原始值
  │  │  │  │  col_0001: 10 (DICT)                                    │    │    │ │
  │  │  │  │  col_0002: 10 (DICT)                                    │    │    │ │→ 读 bit-packed 索引，查字典
  │  │  │  │  ...                                                    │    │    │ │
  │  │  │  │  col_0024: 11 (ALL_NULL)                                │    │    │ │→ 直接返回 null
  │  │  │  │  col_0025: 01 (CONST)                                   │    │    │ │→ 值在 CONST METADATA 中，无需读数据
  │  │  │  │  ...                                                    │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  HAS-NULLS FLAGS (1 bit × 100 cols = 13 bytes)          │    │    │ │
  │  │  │  │  col_0000: 0 (no nulls)                                 │    │    │ │
  │  │  │  │  col_0001: 1 (has nulls)                                │    │    │ │
  │  │  │  │  ...                                                    │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  CONST METADATA                                         │    │    │ │
  │  │  │  │  col_0025: 8 bytes [42.0 as DOUBLE]                     │    │    │ │
  │  │  │  │  col_0026: 8 bytes [42.0 as DOUBLE]                     │    │    │ │
  │  │  │  │  ... (20 CONST columns)                                 │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  DICT METADATA                                          │    │    │ │
  │  │  │  │  col_0001: varint(5) + [10.0, 20.0, 30.0, 40.0, 50.0]   │    │    │ │
  │  │  │  │  col_0002: varint(3) + [a, b, c]                        │    │    │ │
  │  │  │  │  ... (40 DICT columns)                                  │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  NULL BITMAPS                                           │    │    │ │→ 1 → row is null，0 → row has value
  │  │  │  │  col_0000: 12,500 bytes (all 0)                         │    │    │ │
  │  │  │  │  col_0001: 12,500 bytes (some 1s)                       │    │    │ │
  │  │  │  │  ... (skip ALL_NULL columns)                            │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │  ┌─────────────────────────────────────────────────────────┐    │    │ │
  │  │  │  │  COLUMN DATA                                            │    │    │ │
  │  │  │  │  col_0000 (PLAIN): [v0][v1]...[v99999] = 800 KB         │    │    │ │
  │  │  │  │  col_0001 (DICT): [idx0][idx1]... = 37.5 KB             │    │    │ │
  │  │  │  │  col_0002 (DICT): [idx0][idx1]... = 25 KB               │    │    │ │
  │  │  │  │  ...                                                    │    │    │ │
  │  │  │  │  col_0024 (ALL_NULL): (empty)                           │    │    │ │
  │  │  │  │  col_0025 (CONST): (empty)                              │    │    │ │
  │  │  │  │  ...                                                    │    │    │ │
  │  │  │  │  col_0099 (PLAIN): [v0][v1]...[v99999] = 800 KB         │    │    │ │
  │  │  │  └─────────────────────────────────────────────────────────┘    │    │ │
  │  │  │                                                                 │    │ │
  │  │  └─────────────────────────────────────────────────────────────────┘    │ │
  │  │                                                                         │ │
  │  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
  │  │  │  BUCKET 1 DATA BLOCK (zstd compressed)                          │    │ │
  │  │  │  Columns: col_0100 ~ col_0199                                   │    │ │
  │  │  │  ... (same structure)                                           │    │ │
  │  │  └─────────────────────────────────────────────────────────────────┘    │ │
  │  │                                                                         │ │
  │  │  ... repeat to BUCKET 99 ...                                            │ │
  │  │                                                                         │ │
  │  │  ┌─────────────────────────────────────────────────────────────────┐    │ │
  │  │  │  BUCKET 42 (EMPTY — not written to file)                        │    │ │
  │  │  └─────────────────────────────────────────────────────────────────┘    │ │
  │  │                                                                         │ │
  │  └─────────────────────────────────────────────────────────────────────────┘ │
  │                                                                              │
  │  ┌─────────────────────────────────────────────────────────────────────────┐ │
  │  │  ROW GROUP 1 (same structure)                                           │ │
  │  └─────────────────────────────────────────────────────────────────────────┘ │
  │                                                                              │
  ├──────────────────────────────────────────────────────────────────────────────┤
  │                                                                              │
  │  ┌─────────────────────────────────────────────────────────────────────────┐ │
  │  │  SCHEMA BLOCK                                                           │ │
  │  │  [4 bytes: uncompressed_size = 50,000]                                  │ │
  │  │  [zstd compressed data = 8,000 bytes]                                   │ │
  │  │                                                                         │ │
  │  │  After decompression:                                                   │ │
  │  │  numColumns:    varint(10000)                                           │ │
  │  │  numBuckets:    varint(100)                                             │ │
  │  │  nameEncoding:  1 (BPE + front-coding)                                  │ │
  │  │                                                                         │ │
  │  │  BPE Rules (128 rules × 2 bytes = 256 bytes):                           │ │
  │  │    rule_0:  ('s','e') → token 0x80                                      │ │
  │  │    rule_1:  (0x80,'n') → token 0x81                                     │ │
  │  │    ...                                                                  │ │
  │  │    rule_127: (0xD0,'n') → token 0xFF                                    │ │
  │  │                                                                         │ │
  │  │  Columns (name-sorted order):                                           │ │
  │  │    fieldId=0,   sharedPrefix=0,  suffix="sensor...0001"                 │ │
  │  │    fieldId=1,   sharedPrefix=35, suffix="2"                             │ │
  │  │    fieldId=2,   sharedPrefix=35, suffix="3"                             │ │
  │  │    ...                                                                  │ │
  │  │    fieldId=9999, sharedPrefix=35, suffix="10000"                        │ │
  │  │                                                                         │ │
  │  │    TypeDescriptor per column:                                           │ │
  │  │      typeId (1 byte) + nullable (1 byte) + [params]                     │ │
  │  │                                                                         │ │
  │  │  Bucket assignment formula (not stored):                                │ │
  │  │    bucketId = sortedPosition × numBuckets / numColumns                  │ │
  │  └─────────────────────────────────────────────────────────────────────────┘ │
  │                                                                              │
  ├──────────────────────────────────────────────────────────────────────────────┤
  │                                                                              │
  │  ┌─────────────────────────────────────────────────────────────────────────┐ │
  │  │  ROW GROUP INDEX                                                        │ │
  │  │                                                                         │ │
  │  │  Row Group 0:                                                           │ │
  │  │    numRows:        varint(100000)                                       │ │
  │  │    nonEmptyCount:  varint(85)                                           │ │→ Row Group 中有数据的 Bucket 数量，空 Bucket 不记录，减小 Index 大小。
  │  │                                                                         │ │
  │  │    bucketId=0:     varint(0)                                            │ │→ bucket 0 偏移量 和 压缩大小
  │  │    bucketOffset:   8 bytes → 0                                          │ │
  │  │    compressedSize: varint(12000)                                        │ │
  │  │    uncompressedSize: varint(45000)                                      │ │
  │  │                                                                         │ │
  │  │    bucketId=1:     varint(1)                                            │ │→ bucket 1 偏移量 和 压缩大小
  │  │    bucketOffset:   8 bytes → 12000                                      │ │
  │  │    compressedSize: varint(10000)                                        │ │
  │  │    uncompressedSize: varint(38000)                                      │ │
  │  │                                                                         │ │
  │  │    ... (85 non-empty buckets)                                           │ │
  │  │                                                                         │ │
  │  │  Row Group 1:                                                           │ │
  │  │    ...                                                                  │ │
  │  │                                                                         │ │
  │  └─────────────────────────────────────────────────────────────────────────┘ │
  │                                                                              │
  ├──────────────────────────────────────────────────────────────────────────────┤
  │                                                                              │
  │  ┌─────────────────────────────────────────────────────────────────────────┐ │
  │  │  FOOTER (32 bytes, fixed)                                               │ │
  │  │┌────────────────┬────────────────┬──────────┬──────────┬────┬────┬─────┐│ │
  │  ││ indexOffset    │ schemaBlockOff │ numBuck- │ numRow-  │comp│vers│magic││ │
  │  ││ (8 bytes)      │ set (8 bytes)  │ ets (4)  │ Groups(4)│(1) │(1) │(4)  ││ │
  │  │├────────────────┼────────────────┼──────────┼──────────┼────┼────┼─────┤│ │
  │  ││ 0x0000000000AB │ 0x0000000000A3 │ 100      │ 2        │ 1  │ 1  │MOSA ││ │
  │  ││ 2F00           │ 1000           │          │          │    │    │     ││ │
  │  │└────────────────┴────────────────┴──────────┴──────────┴────┴────┴─────┘│ │
  │  └─────────────────────────────────────────────────────────────────────────┘ │
  │                                                                              │
  └──────────────────────────────────────────────────────────────────────────────┘

  ┌───────────────────┬─────────┬─────────────────────────────────────────────────────────┐
  │ Field             │ Size    │ Description                                             │
  ├───────────────────┼─────────┼─────────────────────────────────────────────────────────┤
  │ indexOffset       │ 8 bytes │ Absolute file offset of Row Group Index                 │
  │ schemaBlockOffset │ 8 bytes │ Absolute file offset of Schema Block                    │
  │ numBuckets        │ 4 bytes │ Total number of buckets (default: min(100, numColumns)) │
  │ numRowGroups      │ 4 bytes │ Total number of row groups in the file                  │
  │ compression       │ 1 byte  │ Compression codec: 0 = none, 1 = zstd                   │
  │ version           │ 1 byte  │ Format version (currently 1)                            │
  │ magic             │ 4 bytes │ Magic number: "MOSA" (0x4D4F5341)                       │
  └───────────────────┴─────────┴─────────────────────────────────────────────────────────┘
```

## mosaic vs other format

### data structure
```text
┌─────────────────────────────────────────────────────────────────────────────┐
│  PARQUET                                                                    │
│                                                                             │
│  File → Row Group → Column Chunk → Page → Values                            │
│                         ↑                                                   │
│                         └── 10,000 个 Column Chunk 每个列独立                 │
│                                                                             │
│  Footer: 10,000 ColumnChunkMetaData (offset, size, stats...)                │
└─────────────────────────────────────────────────────────────────────────────┘
parquet 通过 Column Chunk 和 Column Index 来实现快速定位到指定的 Page。


┌─────────────────────────────────────────────────────────────────────────────┐
│  ORC                                                                        │
│                                                                             │
│  File → Stripe → Stream (per column/kind) ──→ Values                        │
│                    ↑                                                        │
│                    └── 每个 Column 有多个 Stream（DATA/LENGTH/PRESENT 等）     │
│                                                                             │
│  Stripe Footer: 每 Stripe 内所有 Stream 的 (kind, column, length) 列表        │
│  File Footer: 所有 Stripe 信息 + 全局列统计                                    │
│  RowIndex: 纯索引标记（默认 10,000 行一个），记录各 Stream 内的 seek positions    │
│            用于在连续的 Stream 中定位到某个 Row Group 的起始位置                 │
└─────────────────────────────────────────────────────────────────────────────┘
orc 通过 RowIndex 来实现快速定位到指定的 Row Group。

┌─────────────────────────────────────────────────────────────────────────────┐
│  MOSAIC                                                                     │
│                                                                             │
│  File → Row Group → Bucket → Column Data → Values                           │
│                         ↑                                                   │
│                         └── 100 Bucket，每个 Bucket 包含 ~20 列               │
│                                                                             │
│  Footer: 32 bytes (固定)                                                     │
│  Index: 100 个 Bucket 的 offset/size                                         │
│  Schema: 独立的压缩块                                                         │
└─────────────────────────────────────────────────────────────────────────────┘
假设有2000个列，有100个Bucket，每个Bucket包含20个列。
每个row group 默认 128M
```

### Parquet vs Mosaic Footer
```text
  Parquet Footer 结构:
  ┌─────────────────────────────────────────────────────────────┐
  │ FileMetadata (Thrift 编码)                                   │
  │   - schema: List<SchemaElement>                             │
  │     - 每个列: name, type, num_children, ...                  │
  │     - 10,000 列 × ~100 bytes = ~1 MB                        │
  │   - row_groups: List<RowGroup>                              │
  │     - 每个 RowGroup: List<ColumnChunk>                       │
  │     - 每个 ColumnChunk: meta_data, offset, size, stats       │
  │     - 10,000 列 × ~200 bytes = ~2 MB                         │
  │   - 总计 Footer: 3-5 MB                                      │
  └─────────────────────────────────────────────────────────────┘

  读取 1 列的开销:
    - 读取 Footer: 3-5 MB I/O
    - Thrift 反序列化: 解析 10,000 个对象
    - 构建内存结构: 10,000 个 ColumnChunk 对象

  Mosaic Footer 结构:
  ┌─────────────────────────────────────────────────────────────┐
  │ Footer (32 bytes 固定)                                       │
  │   - indexOffset: 8 bytes                                    │
  │   - schemaBlockOffset: 8 bytes                              │
  │   - numBuckets: 4 bytes                                     │
  │   - numRowGroups: 4 bytes                                   │
  │   - compression: 1 byte                                     │
  │   - version: 1 byte                                         │
  │   - reserved: 2 bytes                                       │
  │   - magic: 4 bytes                                          │
  └─────────────────────────────────────────────────────────────┘

  读取 1 列的开销:
    - 读取 Footer: 32 bytes I/O
    - 计算桶 ID: hash(columnName) % numBuckets
    - 读取 Row Group Index: 只解析相关桶的条目（~1/100）
```

### ORC vs Mosaic

ORC 的 Stripe Footer 确实包含每个列的元数据（Stream、Encoding、Statistics），10,000 列时每个 Stripe Footer 可能达数百 KB。
读取时需要反序列化这些元数据，即使只读 1 列也要解析所有 10,000 个列的条目，这是 O(n) 开销。

### Mosaic Schema
Mosaic Schema 相比其他格式做了很多优化：
- 压缩列名（front coding + BPE） TODO
  - paimon 为什么不 把 column name 抽到 schema 中？
    - 既然有 manifest 管理，为什么还把冗余scheme 信息放到 file中？
- 桶化投影 (Bucket-based Projection)
    - 10000 列分 100 桶，每桶 ~100 列
    - 查询 10 列可能只触及 1-2 个桶
    - 只需解压 1-2% 的数据，而非 100%
- 小 Footer
    - 固定 32 bytes 
    - Parquet/ORC 的 Footer 随列数线性增长，10000 列时可能达 MB 级
    - 没有 stat



## Mosaic Stat TODO
- 但 Mosaic 文件内部没有任何 Row Group 级或更细粒度的统计，所以打开文件后必须读取所有 Row Group，无法跳过。
- 为什么 manifest里面的 stat 粒度是 file？ 为什么不能是 row group / strip 级别？
  - 感觉还是工程问题
  - 既然把统计和 data 分开为什么不能更彻底一点？

