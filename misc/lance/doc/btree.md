# BTree

## Index Layout
```text
day5_vectors.lance/
  _versions/
    latest_version_hint.json (13 bytes)
    18446744073709551612.manifest (1,127 bytes)
    18446744073709551613.manifest (1,253 bytes)
    18446744073709551614.manifest (647 bytes)
  _transactions/
    2-4adb62f2-3e98-4814-873e-81752344ab7d.txn (190 bytes)
    1-74134bf3-eb00-4287-aff7-ec57be89fb07.txn (462 bytes)
    0-7d25fb29-4063-4a18-8c0c-1d333f7908c8.txn (281 bytes)
  data/
    111011010001001010100110e1289448998ec5dfb7cec83e1a.lance (52,234,415 bytes)
  _indices/
  24051e20-7445-4866-ac85-fd197e6b970c/
    auxiliary.idx (1,952,135 bytes)
    index.idx (132,678 bytes)
  efcf181e-19ea-46d2-a1a5-a33f0f4d1dd3/
    page_lookup.lance (1,709 bytes)
    page_data.lance (398,089 bytes)
```

| 文件        | 大小   | 缓存策略           |
| ----------- | ------ | ------------------ |
| page_lookup | 1.7 KB | 常驻内存（很小）   |
| page_data   | 398 KB | 按需加载（按 page）|


为什么分两个文件？
1. page_lookup 小，可以全量缓存，快速定位
2. page_data 大，按 page 加载，减少 I/O
3. 类似 B+Tree 的设计：内部节点存 key，叶子节点存数据

### page_lookup.lance
> cargo run --bin lance-tools -- file meta -s /tmp/day5_vectors.lance/_indices/efcf181e-19ea-46d2-a1a5-a33f0f4d1dd3/page_lookup.lance
```
Finished `dev` profile [unoptimized + debuginfo] target(s) in 1.41s
Running `target/debug/lance-tools file meta -s /tmp/day5_vectors.lance/_indices/efcf181e-19ea-46d2-a1a5-a33f0f4d1dd3/page_lookup.lance`
version: 2.1
num_rows: 25
num_data_bytes: 960
num_column_metadata_bytes: 508
num_footer_bytes: 749
schema:
Field(id=0, name=min, type=int64)
Field(id=1, name=max, type=int64)
Field(id=2, name=null_count, type=uint32)
Field(id=3, name=page_idx, type=uint32)
columns:
column_metadatas 0: 1 pages
page 0: 25 rows, 2 buffers
buffer 0: 2 bytes
buffer 1: 208 bytes
column_metadatas 1: 1 pages
page 0: 25 rows, 2 buffers
buffer 0: 2 bytes
buffer 1: 208 bytes
column_metadatas 2: 1 pages
page 0: 25 rows, 2 buffers
buffer 0: 2 bytes
buffer 1: 16 bytes
column_metadatas 3: 1 pages
page 0: 25 rows, 2 buffers
buffer 0: 2 bytes
buffer 1: 112 bytes
column-infos index=0, len=1
page-info 0: rows=25, priority=0
position=0, size=2
position=64, size=208
column-infos index=1, len=1
page-info 0: rows=25, priority=0
position=320, size=2
position=384, size=208
column-infos index=2, len=1
page-info 0: rows=25, priority=0
position=640, size=2
position=704, size=16
column-infos index=3, len=1
page-info 0: rows=25, priority=0
position=768, size=2
position=832, size=112
Global Buffers: len=1
buffer0:position=960, size=185
```

```
num_rows: 25                    # 25 个 zone/page 的索引项
schema:
Field(id=0, name=min, type=int64)      # 该 zone 的 min 值
Field(id=1, name=max, type=int64)      # 该 zone 的 max 值
Field(id=2, name=null_count, type=uint32)  # null 数量
Field(id=3, name=page_idx, type=uint32)    # 指向 page_data 的页码
```

每行代表一个数据 zone 的统计信息

作用：快速定位

查询 id = 100
↓
加载 page_lookup（25 行，很小）
↓
查找 min <= 100 <= max 的 zone
↓
获取 page_idx
↓
去 page_data.lance 加载对应数据

---

### page_data.lance（BTree 叶子节点）
> cargo run --bin lance-tools -- file meta -s /tmp/day5_vectors.lance/_indices/efcf181e-19ea-46d2-a1a5-a33f0f4d1dd3/page_data.lance
```text
Finished `dev` profile [unoptimized + debuginfo] target(s) in 1.74s
Running `target/debug/lance-tools file meta -s /tmp/day5_vectors.lance/_indices/efcf181e-19ea-46d2-a1a5-a33f0f4d1dd3/page_data.lance`
version: 2.1
num_rows: 100000
num_data_bytes: 397696
num_column_metadata_bytes: 269
num_footer_bytes: 393
schema:
Field(id=0, name=values, type=int64)
Field(id=1, name=ids, type=uint64)
columns:
column_metadatas 0: 1 pages
page 0: 100000 rows, 2 buffers
buffer 0: 196 bytes
buffer 1: 198560 bytes
column_metadatas 1: 1 pages
page 0: 100000 rows, 2 buffers
buffer 0: 196 bytes
buffer 1: 198560 bytes
column-infos index=0, len=1
page-info 0: rows=100000, priority=0
position=0, size=196
position=256, size=198560
column-infos index=1, len=1
page-info 0: rows=100000, priority=0
position=198848, size=196
position=199104, size=198560
Global Buffers: len=1
buffer0:position=397696, size=68
```

```
num_rows: 100000                # 所有原始数据的 row_id
schema:
Field(id=0, name=values, type=int64)   # 实际值（id 列的值）
Field(id=1, name=ids, type=uint64)     # 对应的 row_id
```
每行是一个 (value, row_id) 对

作用：存储实际数据

page_idx → 找到对应 page
↓
加载该 page 的 values 和 ids
↓
二分查找 value = 100
↓
返回对应的 row_id

BTree 查询流程图
```

  ┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
  │  page_lookup    │────▶│   page_data      │────▶│  data/*.lance   │
  │  (zone → page)  │     │  (value→row_id)  │     │ (原始列数据)     │
  └─────────────────┘     └──────────────────┘     └─────────────────┘
         │                        │                       │
         │                        │                       │
     "id=500在    ──────────▶   "id=500的           "取row_id=500
      zone 0"                    row_id=500"           的完整数据"
```
## ZONEMAP

|          | BTREE               | ZONEMAP                  |
| -------- |---------------------| ------------------------ |
| 数据顺序 | ✅ 按 value 排序        | ❌ 原始顺序              |
| 存储内容 | value + row_id      | 只有 min/max/null_count  |
| 查找方式 | 二分查找（精确）            | 范围过滤（不精确）       |
| 回表     | 找到 row_id 后直接取      | 必须回表验证             |
| 文件数   | 2 个（lookup + data）  | 1 个（zonemap.lance）    |
| 适用场景 | 等值查询、范围查询           | 大范围过滤、近似有序列   |




