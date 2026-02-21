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
- next page header offset = current page header offset + page_header_size + compressed_page_size

![Page header](https://parquet.apache.org/images/PageHeader.svg)

通过 data page offset 与 index page offset 读取 PageHeader，通过 compressed page size 获取 PageData 的大小，这样就可以确定 下一个page 的 offset

```text
  ┌─────────────────────────────────────────────────────────┐
  │ [PageHeader][PageData][PageHeader][PageData]...         │
  └─────────────────────────────────────────────────────────┘
  单个 Page 的结构：
  ┌────────────────┬──────────────────────┐
  │ PageHeader     │ PageData             │
  │ (Thrift序列化)  │ (compressed_size)    │
  │ 不固定          │                      │
  └────────────────┴──────────────────────┘
       ↑                    ↑
       需要读取              跳过这部分
```

- 只要知道下一个页的起始偏移（next page offset），直接从这个偏移位置开始读取字节流，就能完整解析出 PageHeader，完全不需要提前知道 PageHeader 的长度？
- 在 Parquet 官网定义的基础结构下（无 PageLocation），要跳过前面的 Page，确实需要逐个解析这些 Page 的 PageHeader


### Page Data

- PLAIN 编码 + 无空值 + 非嵌套
    - `[字符串1长度][字符串1字节内容][字符串2长度][字符串2字节内容]...[字符串N长度][字符串N字节内容]`
- PLAIN 编码 + 有空值（新增空值位图）
    - [空值位图（BIT_PACKED 编码）][有效字符串1长度][有效字符串1内容][有效字符串2长度][有效字符串2内容]...

```text
output.parquet, file length:2,242,618
========================================= File Magic 4 ===========================================
[0,3]: PAR1
========================================= Data 2,231,878==========================================
[4,826537]: RowGroup1 Data, total_byte_size=840598, num_rows=37125
  [4, 309]: Column 0
     [4, 62]: DataPageV2
     [63, 123]: DataPageV2
     [124, 185]: DataPageV2
     [186, 247]: DataPageV2
     [248, 309]: DataPageV2
  [310, 14662]: Column 1
     DataPageV2
     ...
  [14663, 146557]: Column 2
     DataPageV2
     ...
  [146558, 197492]: Column 3
     DataPageV2
     ...
  [197493, 500336]
     DataPageV2
     ...
  [500337, 514557]
     DataPageV2
     ...
  [514558, 528928]: Column 6
     DataPageV2
     ...
  [528929, 547437]: Column 7
     DataPageV2
     ...
  [547438, 642034]: Column 8
    Dictionary Page
    DataPageV2
    DataPageV2
    ...
    DataPageV2
  [642035, 794369]: Column 9
     DataPageV2
     ...
  [794370, 826537]: Column 10
     DataPageV2
     ...
[826538,1654525]: RowGroup2 Data, total_byte_size=841963, num_rows=37125
...
[1654526,2231882]: RowGroup3 Data, total_byte_size=589732, num_rows=25750
...
============================= Column Chunk Column Index size=3,858 ===============================
[2231883,2231993]: RowGroup1 Column Chunk 1 column index
    minValues=[ 0, 7760, 15204, 22584, 30195 ]
    maxValues=[ 7759, 15203, 22583, 30194, 37124 ]
    nullPages=[ false, false, false, false, false ]
    nullCounts=[ 0, 0, 0, 0, 0 ]
    pageIndexes=[ 0, 1, 2, 3, 4 ]
[2231994,2232109]: RowGroup1 Column Chunk 2 column index
[2232110,2232220]: RowGroup1 Column Chunk 3 column index
[2232221,2232375]: RowGroup1 Column Chunk 4 column index
[2232376,2232543]: RowGroup1 Column Chunk 5 column index
[2232544,2232586]: RowGroup1 Column Chunk 6 column index
[2232587,2232723]: RowGroup1 Column Chunk 7 column index
[2232724,2232860]: RowGroup1 Column Chunk 8 column index
[2232861,2233182]: RowGroup1 Column Chunk 9 column index
[2233183,2233256]: RowGroup1 Column Chunk 10 column index
[2233257,2233295]: RowGroup1 Column Chunk 11 column index
[2233296,2233406]: RowGroup2 Column Chunk 1 column index
[2233407,2233522]: RowGroup2 Column Chunk 2 column index
[2233523,2233633]: RowGroup2 Column Chunk 3 column index
[2233634,2233788]: RowGroup2 Column Chunk 4 column index
[2233789,2233941]: RowGroup2 Column Chunk 5 column index
[2233942,2233984]: RowGroup2 Column Chunk 6 column index
[2233985,2234121]: RowGroup2 Column Chunk 7 column index
[2234122,2234258]: RowGroup2 Column Chunk 8 column index
[2234259,2234580]: RowGroup2 Column Chunk 9 column index
[2234581,2234654]: RowGroup2 Column Chunk 10 column index
[2234655,2234693]: RowGroup2 Column Chunk 11 column index
[2234694,2234784]: RowGroup3 Column Chunk 1 column index
[2234785,2234870]: RowGroup3 Column Chunk 2 column index
[2234871,2234961]: RowGroup3 Column Chunk 3 column index
[2234962,2235082]: RowGroup3 Column Chunk 4 column index
[2235083,2235195]: RowGroup3 Column Chunk 5 column index
[2235196,2235230]: RowGroup3 Column Chunk 6 column index
[2235231,2235331]: RowGroup3 Column Chunk 7 column index
[2235332,2235426]: RowGroup3 Column Chunk 8 column index
[2235427,2235649]: RowGroup3 Column Chunk 9 column index
[2235650,2235702]: RowGroup3 Column Chunk 10 column index
[2235703,2235740]: RowGroup3 Column Chunk 11 column index
=========================== Column Chunk Offset Index, size=2,932 ================================
[2235741,2235788]: RowGroup1 Column Chunk 1 offset index
    offsets=[ 4, 63, 124, 186, 248 ]
    compressedPageSizes=[ 59, 61, 62, 62, 62 ]
    firstRowIndexes=[ 0, 7760, 15204, 22584, 30195 ]
[2235789,2235891]: RowGroup1 Column Chunk 2 offset index
[2235892,2235956]: RowGroup1 Column Chunk 3 offset index
[2235957,2236078]: RowGroup1 Column Chunk 4 offset index
[2236079,2236206]: RowGroup1 Column Chunk 5 offset index
[2236207,2236269]: RowGroup1 Column Chunk 6 offset index
[2236270,2236376]: RowGroup1 Column Chunk 7 offset index
[2236377,2236468]: RowGroup1 Column Chunk 8 offset index
[2236469,2236710]: RowGroup1 Column Chunk 9 offset index
[2236711,2236750]: RowGroup1 Column Chunk 10 offset index
[2236751,2236777]: RowGroup1 Column Chunk 11 offset index
[2236778,2236832]: RowGroup2 Column Chunk 1 offset index
[2236833,2236939]: RowGroup2 Column Chunk 2 offset index
[2236940,2237004]: RowGroup2 Column Chunk 3 offset index
[2237005,2237126]: RowGroup2 Column Chunk 4 offset index
[2237127,2237261]: RowGroup2 Column Chunk 5 offset index
[2237262,2237328]: RowGroup2 Column Chunk 6 offset index
[2237329,2237442]: RowGroup2 Column Chunk 7 offset index
[2237443,2237540]: RowGroup2 Column Chunk 8 offset index
[2237541,2237798]: RowGroup2 Column Chunk 9 offset index
[2237799,2237841]: RowGroup2 Column Chunk 10 offset index
[2237842,2237870]: RowGroup2 Column Chunk 11 offset index
[2237871,2237918]: RowGroup3 Column Chunk 1 offset index
[2237919,2238000]: RowGroup3 Column Chunk 2 offset index
[2238001,2238056]: RowGroup3 Column Chunk 3 offset index
[2238057,2238153]: RowGroup3 Column Chunk 4 offset index
[2238154,2238240]: RowGroup3 Column Chunk 5 offset index
[2238241,2238291]: RowGroup3 Column Chunk 6 offset index
[2238292,2238373]: RowGroup3 Column Chunk 7 offset index
[2238374,2238439]: RowGroup3 Column Chunk 8 offset index
[2238440,2238615]: RowGroup3 Column Chunk 9 offset index
[2238616,2238644]: RowGroup3 Column Chunk 10 offset index
[2238645,2238672]: RowGroup3 Column Chunk 11 offset index
====================================== File Metadata 3,937 =======================================
[2238673,2242609]:
schema
num_rows=100000
row_groups=[
    RowGroup1,
    RowGroup2,
    RowGroup3
]
========================================= Metadata Length 4 ======================================
[2242610,2242613]: 3937
=========================================== File Magic 4 =========================================
[2242614,2242617]: PAR1
```

### parquet 读流程
```text
readFooter:
main:62, FilteredParquetReaderExample (com.example.parquet)
└── filterByRecentTimestamp:130, FilteredParquetReaderExample (com.example.parquet)
    └── readWithFilter:181, FilteredParquetReaderExample (com.example.parquet)
        └── read:139, ParquetReader (org.apache.parquet.hadoop)
            └── initReader:166, ParquetReader (org.apache.parquet.hadoop)
                └── open:730, ParquetFileReader (org.apache.parquet.hadoop)
                    └── <init>:971, ParquetFileReader (org.apache.parquet.hadoop)
                        └── readFooter:593, ParquetFileReader (org.apache.parquet.hadoop)

readNextRowGroup:
main:45, VectorizedParquetReaderExample (com.example.parquet)
└── read:140, ParquetReader (org.apache.parquet.hadoop)
    └── read:136, ParquetReader (org.apache.parquet.hadoop)
        └── nextKeyValue:245, InternalParquetRecordReader (org.apache.parquet.hadoop)
            └── checkRead:140, InternalParquetRecordReader (org.apache.parquet.hadoop)
                └── readNextFilteredRowGroup:1380, ParquetFileReader (org.apache.parquet.hadoop)
                    └── readNextRowGroup:1135, ParquetFileReader (org.apache.parquet.hadoop)
                        └── internalReadRowGroup:1185, ParquetFileReader (org.apache.parquet.hadoop)
                             └── readChunkPages, ParquetFileReader (read pages)

readNextFilteredRowGroup:
getRowRanges:
main:49, FilteredParquetReaderExample (com.example.parquet)
└── filterByEventType:96, FilteredParquetReaderExample (com.example.parquet)
    └── readWithFilter:178, FilteredParquetReaderExample (com.example.parquet)
        └── read:139, ParquetReader (org.apache.parquet.hadoop)
            └── initReader:170, ParquetReader (org.apache.parquet.hadoop)
                └── initialize:207, InternalParquetRecordReader (org.apache.parquet.hadoop)
                    └── getFilteredRecordCount:1055, ParquetFileReader (org.apache.parquet.hadoop)
                        └── getRowRanges:1488, ParquetFileReader (org.apache.parquet.hadoop)
                            └── calculateRowRanges:78, ColumnIndexFilter (org.apache.parquet.internal.filter2.columnindex)
                                ├── accept:157, FilterCompat$FilterPredicateCompat (org.apache.parquet)
                                │   ├── visit:57, ColumnIndexFilter (org.apache.parquet.internal.filter2.columnindex)    
                                │   └── visit:201, ColumnIndexFilter (org.apache.parquet.internal.filter2.columnindex)     here filter by column index
                                │       └── applyPredicate:189, ColumnIndexFilter (org.apache.parquet.internal.filter2.columnindex)
                                │           ├── getColumnIndex:72, ColumnIndexStoreImpl$IndexStoreImpl (org.apache.parquet)
                                │           └── getColumnIndex:147, ColumnIndexStoreImpl (org.apache.parquet.parquet.hadoop)
                                ├── visit:78, ColumnIndexFilter$1 (org.apache.parquet.internal.filter2.columnindex)
                                ├── visit:84, ColumnIndexFilter$1 (org.apache.parquet.internal.filter2.columnindex)
                                └── accept:189, Operators$Eq (org.apache.parquet.filter2.predicate)

main:76, FilteredParquetReaderExample (com.example.parquet)
└── filterByEventTypes:163, FilteredParquetReaderExample (com.example.parquet)
    └── readWithFilter:183, FilteredParquetReaderExample (com.example.parquet)
        └── read:140, ParquetReader (org.apache.parquet.hadoop)
            └── read:136, ParquetReader (org.apache.parquet.hadoop)
                └── nextKeyValue:245, InternalParquetRecordReader (org.apache.parquet.hadoop)
                    └── checkRead:140, InternalParquetRecordReader (org.apache.parquet.hadoop)
                        └── readNextFilteredRowGroup:1389, ParquetFileReader (org.apache.parquet.hadoop)
                            └── getRowRanges:1487, ParquetFileReader (org.apache.parquet.hadoop)
```


```java
// 1. 读取文件最后字节，获取Metadata Length和Magic
ByteBuffer tailBuffer = readFile(fileLength - 8, 8);

// 验证Magic
byte[] magic = new byte[4];
tailBuffer.position(4);
tailBuffer.get(magic);
assert "PAR1".equals(new String(magic));

// 2. 获取Metadata Length（最后8-4字节）
tailBuffer.position(0);
int metadataLength = BytesUtils.readIntLittleEndian(tailBuffer);

// 3. 读取File Metadata
ByteBuffer metadataBuffer = readFile(
    fileLength - 8 - metadataLength,
    metadataLength
);

// 4. 解析File Metadata (Thrift)
FileMetaData fileMetaData = parseFileMetaData(metadataBuffer);

// 5. 获取Schema和Row Groups
MessageType schema = fileMetaData.getSchema();
List<RowGroup> rowGroups = fileMetaData.getRowGroups();

// 6. 读取指定列
for (RowGroup rowGroup : rowGroups) {
    List<ColumnChunk> columnChunks = rowGroup.getColumns();

    // 6.1 读取需要的列（列裁剪）
    for (ColumnChunk columnChunk : selectedColumns) {
        // 6.2 检查Bloom Filter（如果存在）
        if (columnChunk.hasBloomFilter()) {
            BloomFilter bloomFilter = readBloomFilter(
                columnChunk.getBloomFilterOffset()
            );

            if (!bloomFilter.mightContain(searchValue)) {
                // 跳过此列
                continue;
            }
        }

        // 6.3 读取Data Pages
        List<DataPage> pages = readDataPages(
            columnChunk.getDataPageOffset(),
            columnChunk.getNumValues()
        );

        // 6.4 解码数据
        for (DataPage page : pages) {
            Object values = decodePage(page, columnChunk.getEncoding());
            // 处理数据...
        }
    }
}
```

### Column Chunk 1: name (STRING, 字典编码)

```
OFFSET 400,004 - 438,121
══════════════════════════════════════════════════════════════════
                COLUMN CHUNK 1: name (STRING, 字典编码)
══════════════════════════════════════════════════════════════════

Column Metadata (存储在File Metadata中):
┌───────────────────────────────────────────────────────────────────┐
│ Type: BINARY                                                      │
│ Encodings: [PLAIN_DICTIONARY, RLE, BIT_PACKED]                    │
│ DictionarySize: 100                                               │
│ Codec: UNCOMPRESSED                                               │
│ NumValues: 100,000                                                │
│ TotalUncompressedSize: 38,117                                     │
│ TotalCompressedSize: 38,117                                       │
│ DictionaryPageOffset: 400,004                                     │
│ DataPageOffset: 400,621                                           │
│ Statistics:                                                       │
│   max: "Zoe"                                                      │
│   min: "Alice"                                                    │
│   null_count: 0                                                   │
└───────────────────────────────────────────────────────────────────┘

Dictionary Page:
┌───────────────────────────────────────────────────────────────────┐
│ Page Header (Thrift)                                              │
│ type: DICTIONARY_PAGE                                             │
│ uncompressed_size: 600                                            │
│ compressed_size: 600                                              │
│ num_values: 100                                                   │
│ encoding: PLAIN                                                   │
│                                                                   │
│ Size: ~17 bytes                                                   │
├───────────────────────────────────────────────────────────────────┤
│ Dictionary Content (PLAIN encoding, 600 bytes)                    │
│                                                                   │
│ Entry 0 (Alice):                                                  │
│   05 00 00 00    ; length: 5 bytes                                │
│   41 6C 69 63 65  ; "Alice"                                       │
│                                                                   │
│ Entry 1 (Bob):                                                    │
│   03 00 00 00    ; length: 3 bytes                                │
│   42 6F 62       ; "Bob"                                          │
│                                                                   │
│ Entry 2 (Charlie):                                                │
│   07 00 00 00    ; length: 7 bytes                                │
│   43 68 61 72 6C 69 65  ; "Charlie"                               │
│                                                                   │
│ Entry 3 (David):                                                  │
│   05 00 00 00    ; length: 5 bytes                                │
│   44 61 76 69 64  ; "David"                                       │
│                                                                   │
│ ...                                                               │
│ Entry 99 (Zoe):                                                   │
│   03 00 00 00    ; length: 3 bytes                                │
│   5A 6F 65       ; "Zoe"                                          │
│                                                                   │
│ mapping:                                                          │
│   0 -> Alice                                                      │
│   1 -> Bob                                                        │
│   2 -> Charlie                                                    │
│   3 -> David                                                      │
│   ...                                                             │
│   99 -> Zoe                                                       │
│                                                                   │
│ Size: 100 entries × avg 6 = 600 bytes                             │
└───────────────────────────────────────────────────────────────────┘

Dictionary Page Total: ~617 bytes

Data Page 0:
┌───────────────────────────────────────────────────────────────────┐
│ Page Header (Thrift)                                              │
│ type: DATA_PAGE                                                   │
│ uncompressed_size: 37,500                                         │
│ compressed_size: 37,500                                           │
│ num_values: 100,000                                               │
│ encoding:                                                         │
│   - data_encoding: RLE_DICTIONARY                                 │
│                                                                   │
│ Size: ~17 bytes                                                   │
├───────────────────────────────────────────────────────────────────┤
│ Repetition Levels (0 bytes)                                       │
├───────────────────────────────────────────────────────────────────┤
│ Definition Levels (0 bytes)                                       │
├───────────────────────────────────────────────────────────────────┤
│ Values (RLE_DICTIONARY encoding, ~37,500 bytes)                   │
│                                                                   │
│ Store dictionary indices using RLE v2 encoding:                   │
│                                                                   │
│ Original index: [0, 5, 0, 2, 0, 99, 1, 88, ...]                   │
│   mapping:      [Alice, Frank, Alice, Charlie, ...]               │
│                                                                   │
│ RLE encoding example:                                             │
│   [0, 0, 0]  →  bit-packed (3个索引，每个需要7位)                    │
│   [99]Repeat → RLE encoding                                       │
│                                                                   │
│ Size: ~37.5KB (100K个索引，平均3.7位/索引)                           │
└───────────────────────────────────────────────────────────────────┘

Data Page 1

Data Page 2

Data Page 3
```

### File Metadata (Footer)

```
OFFSET 3,288,441 - 3,296,633
══════════════════════════════════════════════════════════════════
                        FILE METADATA (Thrift)
══════════════════════════════════════════════════════════════════

Thrift 结构 (序列化后约8KB):

FileMetaData {
  version: 1
  schema: [
    {
      name: "user_events"
      type: GROUP
      num_children: 5
      converted_type: LIST
    }
    {
      name: "id"
      type: INT32
      repetition_type: REQUIRED
      type_length: 4
    }
    {
      name: "name"
      type: BINARY
      repetition_type: REQUIRED
      converted_type: UTF8
    }
    {
      name: "country"
      type: BINARY
      repetition_type: REQUIRED
      converted_type: UTF8
    }
    {
      name: "salary"
      type: DOUBLE
      repetition_type: REQUIRED
      type_length: 8
    }
    {
      name: "sessionId"
      type: BINARY
      repetition_type: REQUIRED
      converted_type: UTF8
    }
  ]
  num_rows: 100000
  row_groups: [
    {
      columns: [
        {
          file_path: ""
          file_offset: 4
          meta_data: {
            type: INT32
            encodings: [BIT_PACKED, PLAIN, RLE]
            codec: UNCOMPRESSED
            num_values: 100000
            total_uncompressed_size: 400000
            total_compressed_size: 400000
            data_page_offset: 4
            statistics: {
              max: 100000
              min: 1
              null_count: 0
            }
          }
        }
        {
          file_offset: 400004
          meta_data: {
            type: BINARY
            encodings: [PLAIN_DICTIONARY, RLE, BIT_PACKED]
            codec: UNCOMPRESSED
            num_values: 100000
            total_uncompressed_size: 38117
            total_compressed_size: 38117
            dictionary_page_offset: 400004
            data_page_offset: 400621
            statistics: {
              max: "Zoe"
              min: "Alice"
              null_count: 0
            }
          }
        }
        {
          file_offset: 438121
          meta_data: {
            type: BINARY
            encodings: [PLAIN, RLE]
            codec: UNCOMPRESSED
            num_values: 100000
            total_uncompressed_size: 250000
            total_compressed_size: 250000
            data_page_offset: 438121
            bloom_filter_offset: 688121
            statistics: {
              max: "ZA"
              min: "AF"
              null_count: 0
            }
          }
        }
        {
          file_offset: 688397
          meta_data: {
            type: DOUBLE
            encodings: [BIT_PACKED, PLAIN, RLE]
            codec: UNCOMPRESSED
            num_values: 100000
            total_uncompressed_size: 800000
            total_compressed_size: 800000
            data_page_offset: 688397
            statistics: {
              max: 150000.0
              min: 50000.0
              null_count: 0
            }
          }
        }
        {
          file_offset: 1488397
          meta_data: {
            type: BINARY
            encodings: [PLAIN, RLE]
            codec: UNCOMPRESSED
            num_values: 100000
            total_uncompressed_size: 1800000
            total_compressed_size: 1800000
            data_page_offset: 1488397
            bloom_filter_offset: 3288397
            statistics: {
              max: "sess_100000_xyz789"
              min: "sess_00001_abc123"
              null_count: 0
            }
          }
        }
      ]
      total_byte_size: 3288000
      num_rows: 100000
    }
  ]
  created_by: "parquet-mr version 1.14.0 (build 0000000)"
}
```



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

