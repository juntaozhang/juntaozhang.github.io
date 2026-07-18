## Table Layout

```text
物理布局

  数据集目录 (s3://bucket/dataset/ 或 /path/to/dataset/)
  ├── _versions/
  │   ├── 1.manifest          # 版本 1 的 Manifest
  │   ├── 2.manifest          # 版本 2 的 Manifest
  │   └── ...
  ├── _indices/
  │   ├── {uuid}/             # 索引数据目录
  │   └── ...
  ├── _deletions/
  │   ├── 0-1-0.arrow         # Fragment 0 的删除文件
  │   └── ...
  ├── _transactions/
  │   ├── 1-{uuid}.txn        # 事务文件
  │   └── ...
  └── data/
      ├── 00000000.lance      # DataFile (Fragment 0)
      ├── 00000001.lance      # DataFile (Fragment 1)
      └── ...
```


| 目录          | 作用                        | 关键文件                   |
| :------------ | :-------------------------- | :------------------------- |
| _versions     | 存储所有历史版本的 Manifest | {version}.manifest         |
| _indices      | 向量索引（IVF_PQ）数据      | {uuid}/ivf_data.lance      |
| _deletions    | 标记哪些行被删除            | {frag_id}-{ver}-{id}.arrow |
| _transactions | 记录写操作事务              | {read_ver}-{uuid}.txn      |
| data          | 实际的列式数据文件          | {id}.lance                 |


| 分类         | 字段                | 类型         | 核心作用                               |
| ------------ | ------------------- | ------------ | -------------------------------------- |
| Schema 定义  | fields              | 重复字段     | 表完整字段结构（含嵌套字段展开）       |
|              | schema_metadata     | KV字节映射   | Schema 维度元数据                      |
| 数据存储     | fragments           | 重复结构体   | 全部数据片段，记录数据文件、删除行信息 |
|              | next_row_id         | uint64       | 自增行ID分配标记                       |
|              | max_fragment_id     | 可选uint32   | 当前最大数据片段ID                     |
| 版本管理     | version             | uint64       | 当前清单对应的数据集版本号             |
|              | timestamp           | 时间戳       | 该版本清单生成时间                     |
|              | tag                 | string       | 版本自定义标签（可选）                 |
| 向量索引     | index_section       | 可选uint64   | 索引元数据在清单文件内的字节偏移       |
| 数据格式     | data_format         | 枚举         | Lance 存储格式版本（v2.0/v2.1等）      |
| 表配置元数据 | config              | 字符串KV映射 | 数据集内置配置项                       |
|              | table_metadata      | 字符串KV映射 | 用户自定义表元数据                     |
| 事务机制     | transaction_file    | string       | 关联事务记录文件路径                   |
|              | transaction_section | 可选uint64   | 事务数据在文件内的字节偏移             |

### Version

manifest 包含完整的 fragments

Version 1 Manifest 内容

```text
  {
    "version": 1,
    "timestamp": "2024-01-15T10:30:00Z",
    "fields": [
      {"id": 1, "name": "id", "type": "INT64"},
      {"id": 2, "name": "vector", "type": "FIXED_SIZE_LIST(128, FLOAT32)"},
      {"id": 3, "name": "category", "type": "STRING"}
    ],
    "schema_metadata": {},
    "fragments": [
      {
        "id": 0,
        "files": [
          {
            "path": "data/00000000.lance",
            "fields": [1, 2, 3],
            "column_indices": [0, 1, 2],
            "file_major_version": 2,
            "file_minor_version": 1,
            "file_size_bytes": 524288
          }
        ],
        "physical_rows": 1000,
        "deletion_file": null
      }
    ],
    "index_section": null,
    "reader_feature_flags": 0,
    "writer_feature_flags": 0,
    "data_format": {"file_format": "lance", "version": "2.1"},
    "config": {},
    "table_metadata": {},
    "next_row_id": 1000,
    "max_fragment_id": 0
  }
```

Version 2 Manifest 内容（追加后）

```
  {
    "version": 2,
    "timestamp": "2024-01-15T10:35:00Z",
    "fields": [...],  // 相同
    "fragments": [
      {
        "id": 0,                      // Fragment 0（来自版本1）
        "files": [{"path": "data/00000000.lance", ...}],
        "physical_rows": 1000,
        "deletion_file": null
      },
      {
        "id": 1,                      // Fragment 1（新增）
        "files": [{"path": "data/00000001.lance", ...}],
        "physical_rows": 500,
        "deletion_file": null
      }
    ],
    "next_row_id": 1500,
    "max_fragment_id": 1
  }
```

Version 3 Manifest 内容（删除后）

```

  {
    "version": 3,
    "timestamp": "2024-01-15T10:40:00Z",
    "fields": [...],
    "fragments": [
      {
        "id": 0,
        "files": [{"path": "data/00000000.lance", ...}],
        "physical_rows": 1000,
        "deletion_file": {              // ← 新增删除标记
          "file_type": "ARROW_ARRAY",
          "name": "0-2-0.arrow",
          "file_size_bytes": 1024,
          "num_deleted_rows": 1
        }
      },
      {
        "id": 1,
        "files": [{"path": "data/00000001.lance", ...}],
        "physical_rows": 500,
        "deletion_file": null
      }
    ],
    "next_row_id": 1500,
    "max_fragment_id": 1
  }
```

### Transaction

```
  Transaction
  ├── read_version: u64                       ← 基于哪个版本
  ├── uuid: String                            ← 唯一ID
  ├── tag: Option<String>                     ← 业务标签
  ├── transaction_properties: HashMap         ← 自定义元数据
  └── operation: Operation                    ← 操作（oneof）
      ├── Append
      │   └── fragments: [...]
      ├── Delete
      │   ├── updated_fragments: [...]
      │   ├── deleted_fragment_ids: [...]
      │   └── predicate: "id > 100"
      ├── Update
      │   ├── removed_fragment_ids: [...]
      │   ├── updated_fragments: [...]
      │   ├── new_fragments: [...]
      │   └── fields_modified: [...]
      └── ...
```

例子：对应之前的 3 次操作

Transaction 1：初始创建（Overwrite）

```text
_transactions/
└── 0-550e8400-e29b-41d4-a716-446655440000.txn
  
{
    "read_version": 0,                    // 从零开始
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "tag": "",
    "operation": {
      "overwrite": {
        "fragments": [
          {
            "id": 0,                      // 新分配
            "files": [{"path": "data/00000000.lance", ...}],
            "physical_rows": 1000
          }
        ],
        "schema": [
          {"id": 1, "name": "id", "type": "INT64"},
          {"id": 2, "name": "vector", "type": "FIXED_SIZE_LIST(128, FLOAT32)"},
          {"id": 3, "name": "category", "type": "STRING"}
        ],
        "schema_metadata": {},
        "config_upsert_values": {}
      }
    }
  }
```

Transaction 2：追加数据（Append）

```text
_transactions/
└── 1-6ba7b810-9dad-11d1-80b4-00c04fd430c8.txn
  {
    "read_version": 1,                    // 基于版本 1
    "uuid": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "operation": {
      "append": {
        "fragments": [
          {
            "id": 1,                      // 新分配
            "files": [{"path": "data/00000001.lance", ...}],
            "physical_rows": 500
          }
        ]
      }
    }
  }
```

Transaction 3：删除行（Delete）

```text
_transactions/
└── 3-7c98c921-aebc-22e2-91c5-11d15fd541d9.txn
  {
    "read_version": 2,                    // 基于版本 2
    "uuid": "7c98c921-aebc-22e2-91c5-11d15fd541d9",
    "operation": {
      "delete": {
        "updated_fragments": [
          {
            "id": 0,                      // 已有 fragment
            "files": [{"path": "data/00000000.lance", ...}],
            "physical_rows": 1000,
            "deletion_file": {
              "file_type": "ARROW_ARRAY",
              "name": "0-2-0.arrow",
              "num_deleted_rows": 1
            }
          }
        ],
        "deleted_fragment_ids": [],       // 没有整段删除
        "predicate": "id = 5"             // 删除条件
      }
    }
  }
```

### Fragment


|          | Transaction.fragments | Manifest.fragments    |
| -------- | --------------------- | --------------------- |
| 存在位置 | _transactions/\*.txn  | _versions/\*.manifest |
| 用途     | 记录变更意图          | 记录最终状态          |
| 生命周期 | 事务文件，可清理      | 版本快照，长期保留    |

## Lance File Layout

- Footer
  - 固定大小 = 40 bytes
  - 这设计的目的是：读取时先读文件末尾 40 bytes（或一个 sector，如 4KB），就能定位到所有元数据。
- Column Metadatas
  - Column Offset
  - Column / Pages offset
- Data Pages

### Q&A
- Lance 为什么避免 Parquet 式的 row group？
  - 整个 row group 全在内存，row group 写完之后flush 到磁盘。
  - lance 存储包含大量图片、视频、大向量等。
  - row group 太小: 列被拆成碎片页，云存储读取效率差。
  - row group 太大: 写内存压力大，整个 row group 必须缓冲在内存才能写。
- 大页（large pages）设计对云存储有什么优势？
  - 减少 I/O 请求数 — 降低云存储按请求计费的成本（云存储按请求收费，大页降低 LIST/GET 次数）
  - 摊平高延迟 — 每次请求带回更多数据，减少 RTT（往返时间） 影响
  - 避免 runt pages（碎片页） — 宽表场景下列页不会过小，column metadata entry 数量同步膨胀
  - 匹配顺序带宽 — 云存储顺序读带宽高，大页充分利用
  - 8MB 是平衡点 — 够大摊平延迟，够小不被拆分

### 2.1 整体布局

```
Lance File Layout:
├──────────────────────────────────┤ ◄── 文件开头
│ Data Pages                       │
│   Data Buffer 0*                 │
│   Data Buffer 1*                 │
│   ...                            │
│   Data Buffer BN*                │
├──────────────────────────────────┤
│ Column Metadatas                 │
│ |A| Column 0 Metadata*           │
│     Column 1 Metadata*           │
│     ...                          │
│     Column CN Metadata*          │
├──────────────────────────────────┤
│ Column Metadata Offset Table     │
│ |B| Column 0 Metadata Position*  │
│     Column 0 Metadata Size       │
│     ...                          │
│     Column CN Metadata Position  │
│     Column CN Metadata Size      │
├──────────────────────────────────┤
│ Global Buffers Offset Table      │
│ |C| Global Buffer 0 Position*    │
│     Global Buffer 0 Size         │
│     ...                          │
│     Global Buffer GN Position    │
│     Global Buffer GN Size        │
├──────────────────────────────────┤
│ Footer                           │
│   u64: Offset to column meta 0   │ ← A
│   u64: Offset to CMO table       │ ← B
│   u64: Offset to GBO table       │ ← C
│   u32: Number of global bufs     │
│   u32: Number of columns         │
│   u16: Major version             │
│   u16: Minor version             │
│   "LANC"                         │ ← Magic Number
├──────────────────────────────────┤ ◄── 文件结尾
```
![file_high_level_overview](https://lance.org/images/file_high_level_overview.png)

### 2.2 关键字段说明


| 字段                     | 大小    | 说明                 |
| ------------------------ | ------- | -------------------- |
| Magic Number             | 4 bytes | "LANC"               |
| Major Version            | 2 bytes | 主版本号             |
| Minor Version            | 2 bytes | 次版本号             |
| Number of columns        | 4 bytes | 列数                 |
| Number of global buffers | 4 bytes | 全局缓冲区数         |
| Offset to CMO table      | 8 bytes | 列元数据偏移表位置   |
| Offset to GBO table      | 8 bytes | 全局缓冲区偏移表位置 |
| Offset to column meta    | 8 bytes | 第一个列元数据位置   |

### 2.3 Column Metadata 结构

每个列的元数据是独立的 protobuf 消息：
列级别的元数据（pages、encodings、buffers）

```protobuf
message ColumnMetadata {
  // 该列的所有页
  repeated PageMetadata pages = 1;
  
  // 列的全局缓冲区索引
  repeated int32 global_buffer_ids = 2;
}

message PageMetadata {
  // 页的行偏移（该页第一行的行号）
  uint64 row_offset = 1;
  
  // 页中的行数
  uint32 num_rows = 2;
  
  // 编码信息
  PageLayout encoding = 3;
  
  // 数据缓冲区的索引
  repeated int32 buffer_ids = 4;
}
```

### 2.5 Column Metadata Offset Table

纯偏移量数组：[col0_pos, col0_size, col1_pos, col1_size, ...]

### 2.5 Global Buffers

目前好像只有 Schema (TODO) 

---

## Page 布局详解

### 3.1 Disk Pages 设计

**Page 大小**：

- 推荐默认大小：**8MB**
- 足够大以支持独立的 I/O 操作（即使在云存储上）
- 不会太大以至于需要拆分成小读取

**Page 特性**：

- 每列可以有不同数量的 Pages
- 不同列的 Page 大小可以不同
- 支持部分 Page 读取

### 3.2 为什么 8MB？

Page 大小的权衡：

太小的问题：

- 需要更多 I/O 操作
- 元数据开销大
- 云存储性能差

太大的问题：

- 写入时需要更多内存
- 大连续读取需要拆分
- 随机访问时读取放大

8MB 的平衡点：

- 适合所有存储系统
- 云存储友好的 I/O 大小
- 内存使用合理

### 3.3 Buffer Alignment

Page 是逻辑容器（8MB），包含多个buffer， buffer 才是压缩和解压的实际单元。
压缩粒度取决于 structural encoding：

- mini-block 是"小块"级别
- full-zip 是"单值"级别，但都不会大到整个 page

对齐要求：

- 缓冲区对齐到 64 字节边界
- 支持 SIMD 操作
- 支持 Direct I/O（如果需要 4096 字节对齐）

实际实现：

- 写入器在缓冲区前插入填充
- 读取器假设可能有可选填充
