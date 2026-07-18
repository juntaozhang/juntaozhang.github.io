## Blob Type
### Blob Data
`INSERT INTO image_table VALUES (1, 'sample1', X'A1'), (6, 'sample6', X'A123456')`
两个 blob 值可以拼接成一个文件持久化。

### custom BlobDescriptor from outside
blob-descriptor-field 单独配置使用: `test("Append Table: BLOB: blob-descriptor-field")`
`INSERT INTO image_table VALUES (1, 'sample1', sys.path_to_descriptor('t1.jpeg'))`

### Blob View
BlobView 是一种"跨表引用"机制，下游表只存储上游表 BLOB 字段的坐标（表名+字段ID+行ID），
读取时自动从上游表解析出实际数据。适用于数据分层、避免重复存储大文件的场景。

```sql
INSERT INTO image_view_table
SELECT
    id,
    name AS label,
    sys.blob_view('image_table', 'image', _ROW_ID)
FROM `image_table$row_tracking`
```


## Blob Structure

### FILE LAYOUT
```text
  Image 0: cat.jpg (5,242,880 bytes = 5.00 MB)
  Image 1: dog.jpg (3,145,728 bytes = 3.00 MB)
  Image 2: bird.jpg (2,097,152 bytes = 2.00 MB)
  
  
  ═══════════════════════════════════════════════════════════════════════════════
                                FILE LAYOUT
  ═══════════════════════════════════════════════════════════════════════════════

  Offset (dec)   Offset (hex)    Size           Content
  ──────────────────────────────────────────────────────────────────────────────
  0              0x00000000      4 bytes        [MAGIC: 0x58554E4F]  ← Blob 0
  4              0x00000004      5,242,880      [cat.jpg raw bytes]
  5242884        0x004FFFF4      8 bytes        [binLength: 5242896]
  5242892        0x004FFFFC      4 bytes        [CRC32: 0xA1B2C3D4]
  ──────────────────────────────────────────────────────────────────────────────
  5242896        0x00500000      4 bytes        [MAGIC: 0x58554E4F]  ← Blob 1
  5242900        0x00500004      3,145,728      [dog.jpg raw bytes]
  8388628        0x00800004      8 bytes        [binLength: 3145744]
  8388636        0x0080000C      4 bytes        [CRC32: 0xE5F6A7B8]
  ──────────────────────────────────────────────────────────────────────────────
  8388640        0x00800010      4 bytes        [MAGIC: 0x58554E4F]  ← Blob 2
  8388644        0x00800014      2,097,152      [bird.jpg raw bytes]
  10485796       0x00A00014      8 bytes        [binLength: 2097168]
  10485804       0x00A0001C      4 bytes        [CRC32: 0x12345678]
  ──────────────────────────────────────────────────────────────────────────────
  10485808       0x00A00020      12 bytes       [Index: DeltaVarint]  ← Footer  [5242896, 3145744, 2097168]
  10485820       0x00A0002C      4 bytes        [indexLength: 12]
  10485824       0x00A00030      1 byte         [VERSION: 1]
  10485825       0x00A00031                     EOF
  ──────────────────────────────────────────────────────────────────────────────
```

### Blob File Details
```text
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  Blob Record N (Variable Size)                                              │
  ├─────────────────────────────────────────────────────────────────────────────┤
  │  ┌─────────────────────────────────────────────────────────────────────┐    │
  │  │ MAGIC_NUMBER        │ 4 bytes │ Little Endian int                   │    │
  │  │                     │         │ Value: 0x58554E4F                   │    │
  │  ├─────────────────────────────────────────────────────────────────────┤    │
  │  │ BLOB DATA           │ N bytes │ Raw binary data                     │    │
  │  │                     │         │ (actual image/video/file content)   │    │
  │  ├─────────────────────────────────────────────────────────────────────┤    │
  │  │ binLength           │ 8 bytes │ Little Endian long                  │    │
  │  │                     │         │ Total size of this blob record      │    │
  │  │                     │         │ (MAGIC + DATA + binLength + CRC32)  │    │
  │  ├─────────────────────────────────────────────────────────────────────┤    │
  │  │ CRC32               │ 4 bytes │ Little Endian int                   │    │
  │  │                     │         │ Checksum of (MAGIC + DATA)          │    │
  │  └─────────────────────────────────────────────────────────────────────┘    │
  │                                                                             │
  │  Total blob record size = 4 + N + 8 + 4 = N + 16 bytes                      │
  └─────────────────────────────────────────────────────────────────────────────┘

  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  Footer (at end of file)                                                    │
  ├─────────────────────────────────────────────────────────────────────────────┤
  │  ┌─────────────────────────────────────────────────────────────────────┐    │
  │  │ Index               │ Variable │ DeltaVarint compressed array       │    │
  │  │                     │          │ of blob lengths                    │    │
  │  │                     │          │                                    │    │
  │  │                     │          │ lengths[i] = binLength of blob i   │    │
  │  │                     │          │ lengths[i] = -1  → null blob       │    │
  │  ├─────────────────────────────────────────────────────────────────────┤    │
  │  │ indexLength         │ 4 bytes  │ Little Endian int                  │    │
  │  │                     │          │ Size of Index in bytes             │    │
  │  ├─────────────────────────────────────────────────────────────────────┤    │
  │  │ VERSION             │ 1 byte   │ Value: 1                           │    │
  │  └─────────────────────────────────────────────────────────────────────┘    │
  │                                                                             │
  │  Footer size = indexLength + 4 + 1 = indexLength + 5 bytes                  │
  └─────────────────────────────────────────────────────────────────────────────┘
```

## Blob filter with parquet
[append-table parquet filter](append-table.md#sql-filter)


```text
  DataEvolutionFileReader -> DataEvolutionIterator
  ├── readers[0]: DataFileRecordReader → VectorizedParquetRecordReader (id, name 列)
  │   └── selection = rowRanges → 跳过部分行
  │
  ├── readers[1]: DataFileRecordReader → BlobFormatReader (image 列)
  │   └── selection = rowRanges → 跳过部分行
  │
  └── DataEvolutionIterator: 按 rowOffsets/fieldOffsets 合并字段
      rowOffsets = [0, 0, 1]   // field0,1 来自 reader0; field2 来自 reader1
      fieldOffsets = [0, 1, 0] // field0→reader0[0], field1→reader0[1], field2→reader1[0]
```

Filter 不会下推到文件，所以blob 的内容会被读取出来，所以为了性能需要 `'blob-as-descriptor' = 'true'`这样可以只需要读取 blob 的描述符，而不是读取 blob 的内容。

Filter 位置(selection TODO)：
- SQL WHERE 过滤: DataEvolutionSplitRead.withFilter() 目前 不支持，返回 this
- RowRange 过滤: 通过 file.toFileSelection(rowRanges) 传给每个 Reader，跳过不在范围内的行
- Parquet 内部过滤: 如果有 selection，Parquet 用 ColumnIndex 跳过 Page
- Blob 过滤: BlobFileMeta 用 selection 只读取选中的 blob



### Paimon PushDown vs Spark FilterExec
谓词下推的本质就是：用文件的元数据（统计信息、索引、ColumnIndex）在读取数据之前或读取过程中过滤掉不需要的数据块，避免全表扫描。
下推得越底层（文件级 > Page级 > 行级）， I/O 节省越多。

Paimon PushDown 下推到文件，元数据（文件级 > Page级）
- FileIndexEvaluator.evaluate
```text
┌────────────────────────────────┬────────────────┬────────────────────┐
│ 元数据类型                       │ 存储位置       │ 作用                 │
├────────────────────────────────┼────────────────┼────────────────────┤
│ DataFileMeta 统计               │ Manifest 文件  │ 跳过整个文件         │
│ FileIndex (BloomFilter/Bitmap) │ .index 文件    │ 跳过文件或定位行      │
│ Parquet ColumnIndex            │ Parquet Footer │ 跳过 Page/RowGroup │
│ Parquet PageIndex              │ Parquet Footer │ 跳过 Page          │
└────────────────────────────────┴────────────────┴────────────────────┘
```


Spark FilterExec 下推到行，内存过滤
- FilterExec -> FilterPartitionEvaluator


## Q&A
### Blob 为什么不能支持 primary key table？
- BLOB 依赖 row-tracking
- BLOB 文件通过 _ROW_ID 与 Parquet 行关联，需要稳定的行标识
- BLOB 文件 不支持追加写，无法处理更新/删除
 
blob-descriptor 感觉是可以直接使用 primary key 啊？社区为什么没有支持？
- 解耦 BLOB 与 data-evolution/row-tracking 的强制绑定？
- 如果是这样 直接用一个string 保存就可以了，Blob 引入就是为了代替用户管理bin文件

### blob 可能得性能问题
`test("Append Table: BLOB: blob-descriptor-field with external storage")`

`'blob-as-descriptor' = 'false'`: raw-data BLOB columns are still rejected

but if `'blob-as-descriptor' = 'true'`, then it will work, 但是会 copy 所有列的 blob 文件？
比如有10个这样的blob 文件，如果要改一个文件，但是却copy 所有文件

