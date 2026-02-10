# Structure
```text
表目录/
├── snapshot/                  # 快照目录
│   ├── LATEST                # 指向最新快照的指针
│   ├── EARLIEST              # 指向最早快照的指针
│   └── snapshot-<id>         # 快照文件（记录表的状态）
├── manifest/                 # 元数据清单目录
│   ├── manifest-list-<id>-0  # Base manifest list（基础元数据清单）
│   ├── manifest-list-<id>-1  # Delta manifest list（增量元数据清单）
│   └── manifest-<id>         # Manifest 文件（记录数据文件元数据）
├── schema/                   # Schema 目录
│   └── schema-<id>           # Schema 文件（记录表结构）
├── partition-key=value/      # 分区目录
│   └── bucket-<n>/           # Bucket 目录
│       ├── data-<id>-0.orc   # 数据文件（实际存储数据）
│       └── data-<id>-1.orc   # 不同层级的数据文件
└── index/                    # 索引目录（可选）
    └── bucket-index-<id>     # 动态 Bucket 索引
```

https://paimon.apache.org/docs/master/learn-paimon/understand-files/
![file-layout](https://paimon.apache.org/docs/master/img/file-layout.png)

## LSM
Paimon创新性地将LSM树（Log-structured merge-tree）结构与湖格式结合，实现了高效的实时更新能力：

| Level | 含义 | 存储位置 | 处理方式 |
|-------|------|----------|----------|
| -1 | 未持久化数据 | 内存（write buffer） | 合并时包含，但不参与高层级选择 |
| 0 | 已持久化的最新数据 | 磁盘（Level 0 文件） | 合并时包含，标记为 containLevel0 |
| >0 | 已持久化的旧数据 | 磁盘（Level 1+ 文件） | 参与高层级选择，作为历史值 |


分层存储机制：Paimon将数据分为多个层级（Level 0-N），形成一个逻辑上的LSM树，其中：
- Level 0 (L0)：内存中的可变数据（MemTable），对应Flink Writer的内存缓冲区
- Level 1-N (L1+)：磁盘上的不可变数据文件（SSTable），对应Paimon的数据文件（如Parquet）
