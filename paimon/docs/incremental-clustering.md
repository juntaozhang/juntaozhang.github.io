# Incremental Clustering
只支持 bucket=-1 (BUCKET_UNAWARE)
## SortCompact
SortCompactAction 本质上是一个特殊的写入操作，它读取现有数据，排序后重新写入(CommitKind.OVERWRITE)\
将所有新生成的文件都视为 "新写入"，统一放入 level 0\

- 只支持 bucket=-1 (BUCKET_UNAWARE)
    ```text
    写入方式：直接写入文件，无 bucket 概念  
    LSM 结构：每个分区一个 LSM 树       
    SortCompact 流程：读取→排序→覆盖写回         
    覆盖写问题：直接 overwrite 整个表/分区  
    ```
- SortCompact vs Incremental Clustering
  - SortCompact：手动 CALL 触发，全量数据，需要采样，一次性全局排序
  - Incremental Clustering：自动触发，增量数据，无需采样，增量排序，这个是paimon中传统的compaction。

最终目标：
```text
  未聚类：
  SELECT * FROM T WHERE user_id = 123 AND product_id = 456
    扫描文件：100 个
    耗时：10 秒

  Z-Order 聚类后：
  SELECT * FROM T WHERE user_id = 123 AND product_id = 456
    扫描文件：5 个
    耗时：1 秒
```

## IncrementalClusterManager

传统 Full Compaction, Incremental Clustering 每次只选择部分文件进行排序

### IncrementalClusterStrategy
clusterIncrementalUnAwareBucketTable

## sort strategy
解决多维度排序存储，假设有两个维度 a, b
- ORDER：先按照a排序，再按照b排序
- ZORDER：牺牲单一维度的极致性能，换取多维度的均衡性能
  - 优点是‘Z-Value’计算方便，单点查询性能下降，缺点是两个相邻的点可能在二维空间相隔比较远（长距离跳跃）
- HILBERT
  - 解决 Z-Order 的长距离跳跃
  - Hilbert 编码涉及到递归或查表，计算开销要大得多

# Reference
- [zorder](../../misc/geohash.md#zorder)

