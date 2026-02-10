# Merge Engine

## Partial Update
example `testPartialUpdateRemoveRecordOnSequenceGroup`

PrimaryKeyFileStoreTable

### write
写入初始化链
```text
TableWriteImpl.write()
└── getWriterWrapper()
    └── createWriterContainer()
        └── KeyValueFileStoreWrite.createWriter()
            └── → WriterContainer(MergeTreeWriter)
```
内存写入与排序
```text
SortBufferWriteBuffer.put()
└── BinaryInMemorySortBuffer.write()
    └── → 写到内存
```

准备提交与合并
```text
MergeTreeWriter.prepareCommit()
├── 内存排序 (sort in memory)
├── SortBufferWriteBuffer.MergeIterator()
|   ├── ReducerMergeFunctionWrapper()
|   |   └── PartialUpdateMergeFunction()
|   |     └── 根据 group 合并字段, 并写入内存
|   └── RollingFileWriterImpl.write()
|       └── 写入文件
|
└── triggerCompaction()
    └── → 触发压缩操作 
```

### read
reader 初始化：
```text
KeyValueTableRead.createReader
    └── KeyValueTableRead.reader()
        └── SplitRead$1.createReader()
            └── MergeFileSplitReadProvider.lambda$create$1()
                └── MergeFileSplitRead.createReader()
                    └── MergeFileSplitRead.createMergeReader()   -- create DropDeleteReader
                        └── ConcatRecordReader.create()
                            └── MergeFileSplitRead.lambda$createMergeReader$1()
                                └── MergeTreeReaders.readerForSection()   -- 为每个sortedRun创建reader，readerForRun 内部也是根据 files 生成 ConcatRecordReader
                                    └── MergeSorter.mergeSort()
                                        └── MergeSorter.mergeSortNoSpill()
                                            └── SortMergeReader.createSortMergeReader()
                                                └── SortMergeReaderWithLoserTree.<init>()
                                                    └── → 初始化败者树合并读取器
```

read iterator：
```text
SimpleTableTestBase.getResult()
└── RecordReaderIterator.hasNext()
    └── RecordReaderIterator.advanceIfNeeded()
        └── ValueContentRowDataRecordIterator.next()
            └── ResetRowKindRecordIterator.nextKeyValue()
                └── RecordReader$RecordIterator$1.next()
                    └── DropDeleteReader$1.next()
                        └── SortMergeReaderWithLoserTree$SortMergeIterator.next()
```

排序与合并
```text
SortMergeReaderWithLoserTree$SortMergeIterator.next() → 获取下一条合并后的记录
└── SortMergeReaderWithLoserTree$SortMergeIterator.merge() → 执行合并逻辑
    ├── 使用败者树（Loser Tree）合并多个有序流
    ├── 按主键分组相同key的记录
    ├── 应用 PartialUpdateMergeFunction 合并 same with write
    └── 返回合并后的完整记录
```

### MergeFunction
ReducerMergeFunctionWrapper -> MergeFunction
- PartialUpdateMergeFunction ： PartialUpdateMergeFunctionTest.testSequenceGroup
- AggregateMergeFunction ： AggregateMergeFunctionTest.tesListAggFunc
- DeduplicateMergeFunction: 保留最后一条记录
- FirstRowMergeFunction：保留第一条记录，不支持删除
- LookupMergeFunction：表启用了 changelog-producer = 'lookup'
  - LookupMergeFunctionUnitTest

### partial-update



### SortEngine
- LOSER_TREE（败者树）:专门用于多路归并排序, [LoserTreeTest.java](../../../algorithm/src/test/java/cn/juntaozhang/leetcode/sort/LoserTreeTest.java)
- MIN_HEAP（最小堆）：[heap test](../../../algorithm/src/test/java/cn/juntaozhang/leetcode/sort/L912_heap.java)

## Aggregation
| 维度 | 'merge-engine' = 'aggregation' | Partial Update 中的 aggregate-function |
|------|--------------------------------|----------------------------------------|
| 作用范围 | 表级别，所有字段 | 字段级别，仅指定字段 |
| 配置方式 | 表级 merge-engine + 字段聚合函数 | 表级 partial-update + 部分字段聚合 |
| 非聚合字段处理 | 必须所有字段都配置聚合函数 | 未配置聚合的字段使用直接更新 |
| 适用场景 | 纯聚合统计场景 | 混合更新与聚合场景 |
| 实现类 | AggregationMergeFunction | PartialUpdateMergeFunction |
