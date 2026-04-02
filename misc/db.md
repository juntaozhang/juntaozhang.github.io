## Relation Database

| 指标 | MySQL 8.4+ | PostgreSQL 17+ | Oracle 26c (Enterprise) |
| :--- | :--- | :--- | :--- |
| 随机读 QPS<br>(主键点查, 数据未缓存) | 12 万 - 16 万 | 9 万 - 13 万 | 15 万 - 22 万 |
| 随机写 TPS<br>(Update/Insert, 含落盘) | 1.8 万 - 2.5 万 | 2.2 万 - 3.2 万 | 3.5 万 - 5.5 万 |
| 顺序扫描吞吐<br>(全表扫描/报表) | 400 MB/s - 600 MB/s | 500 MB/s - 750 MB/s | 800 MB/s - 1.2 GB/s |
| 高并发下的延迟抖动 | 高<br>(锁竞争导致尾延迟飙升) | 中<br>(MVCC 平滑，但 Vacuum 偶发卡顿) | 低<br>(智能预取和异步 IO 极稳) |
| 单表最佳性能区间 | < 2,000 万行 | < 1 亿行 | > 10 亿行 |



## KV

| 特性 | Redis                                | ScyllaDB | TiKV / FDB | MongoDB / ES |
| :--- |:-------------------------------------| :--- | :--- | :--- |
| 存储介质 | 内存 (RAM)                             | 内存 + NVMe SSD | 内存 + NVMe SSD | 内存 + SSD/HDD |
| 单节点 QPS | 10万 - 15万 (简单) <br> 100万+ (Pipeline) | 50万 - 100万 | 2万 - 20万 | 1万 - 5万 |
| 延迟 (P99) | 亚毫秒 (< 0.5ms)                        | < 1ms | 2-10ms | 10-50ms+ |
| 一致性模型 | 最终一致 (Cluster) / 强一致 (Sentinel)      | 可调 (Quorum) | 强一致 (Raft/Paxos) | 可调 |
| 持久化 | 一般作为缓存层，不会完全信任                       | 即时写盘，数据不丢失 | 即时写盘 (WAL)，数据不丢失 | 即时写盘 |
| 核心优势 | 极致速度、丰富数据结构                          | 海量数据吞吐、宽列模型 | ACID 事务、强一致 | 复杂查询、全文检索 |
| 致命弱点 | 内存成本高、单线程阻塞风险                        | 运维复杂度、生态不如 Redis | 事务冲突导致 QPS 暴跌 | 磁盘 IO 瓶颈 |



- redis
- TiKV: 云原生分布式事务 KV，兼容 Raft，强一致
  - 强一致：Raft 协议保证数据不丢且实时可见
  - 底层基于 RocksDB，对 Prefix Scan 优化极好
- FoundationDB: 苹果开源的超强事务型 KV，被广泛用于构建数据库
  - 学习曲线陡峭，客户端开发模式独特
- etcd: 强一致，支持 Watch（可用于通知机制），部署简单
  - 不适合存储海量业务数据。etcd 的设计目标是存配置（MB/GB 级），而不是存亿级对象元数据（TB 级）
- Cassandra : AP
  - 默认是最终一致性：去中心化架构，为“永远可用”和“无限写入”而生，牺牲了一部分一致性来换取可用性
    - 默认策略：Last Write Wins (LWW)。
    - 每条数据都有一个 Timestamp (时间戳)。
    - 合并时，Cassandra 简单粗暴地保留时间戳最大的那个值，丢弃旧的。
    - 如果业务逻辑复杂（不能简单按时间覆盖），Cassandra 允许应用层介入冲突解决（例如将冲突的值合并为一个列表，让应用层决定）
  - 写多读少：写入性能极佳，因为写入只是追加日志（CommitLog）和内存表（MemTable），不需要随机磁盘 IO。
  - 时间序列数据：存储 IoT 传感器数据、监控指标、日志等按时间排序的数据。
  - 社交 Feed 流/消息系统：如 Instagram、Netflix 早期都用它存用户动态。
- ScyllaDB: AP
  - 用 C++ 重写 Cassandra，摒弃 JVM，采用 Shared-Nothing 架构（每个 CPU 核心绑定一个线程和内存分区），彻底消除 GC 和锁竞争

## document
- MongoDB：AP（可配置）
  - 以高性能点查（Key-Value）为基石，通过丰富的索引类型支持复杂多维查询的文档数据库
  - 复合索引、多键索引(比如tags)、地理空间索引、文本索引（底层基于B-Tree）
  - Source of Truth：强一致性，写入可以立即查
  - 单文档事务：无额外开销，原子性由引擎底层保证
  - 单节点：5+万 read
  - 单节点：2+万 write
  - 不适合 join

- Elasticsearch/OpenSearch：AP
  - 倒排索引（基于 Lucene）
  - 插入查询有延迟
  - 不支持事务
  - 单节点读：2+万 QPS
  - 单节点写：1万


## 单机
- SQLite：嵌入式数据库，简单、轻量级
  -  QPS 低于 5000，或者数据量小于 10GB
- DuckDB：嵌入式列存数据库，专为数据分析设计
  - OLAP 场景
- RocksDB：LSM-Tree，顺序写，吞吐量最大。
  - 顺序写：~800 MB/s+
  - 随机写：~500k - 1M+
  

## 向量
- LanceDB: 向量数据库，支持向量相似度搜索
  - 磁盘优先架构，支持 PB 级数据不爆内存；与 Pandas/Arrow 无缝集成；开源免费。
  - 核心架构是 嵌入式（Embedded） 和 无服务器（Serverless）
