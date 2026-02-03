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

![file-layout](https://paimon.apache.org/docs/master/img/file-layout.png)

## 源码结构

### 核心模块
- **paimon-core**: 项目核心功能实现，包含表存储、读写操作、LSM树结构、事务管理等核心逻辑
- **paimon-api**: 提供公共API接口，定义了与Paimon交互的标准方式
- **paimon-format**: 定义数据存储格式，支持ORC、Parquet、Avro等多种文件格式

### 计算引擎集成
- **paimon-flink**: Flink集成模块，支持不同版本的Flink（1.16-2.1），提供流批处理能力
- **paimon-spark**: Spark集成模块，支持不同版本的Spark（3.2-4.0），实现Spark对Paimon表的读写操作
- **paimon-hive**: Hive集成模块，支持Hive查询Paimon表

### 文件系统支持
- **paimon-filesystems**: 支持多种文件系统和对象存储，包括S3、OSS、Azure、GCS、HDFS等

### 扩展功能
- **paimon-python**: 提供Python API，方便Python用户使用Paimon
- **paimon-service**: 服务相关功能模块
- **paimon-benchmark**: 性能基准测试模块，用于评估Paimon的性能
- **paimon-arrow**: Arrow格式支持模块（列式内存布局，将同一列的数据连续存放在内存中，而非传统行式存储）
- **paimon-vfs**: 虚拟文件系统抽象层
- **paimon-hudi**: 与Apache Hudi的集成模块
- **paimon-iceberg**: 与Apache Iceberg的集成模块

### 辅助模块
- **paimon-common**: 通用工具类和常量定义
- **paimon-codegen**: 代码生成模块
- **paimon-test-utils**: 测试工具类
- **paimon-bundle**: 打包模块，用于生成包含所有依赖的bundle

Paimon的核心特性包括：
- 基于LSM树结构的实时更新支持
- 主键表和追加表支持
- 多种Merge Engine支持（last-row、partial-update、aggregate、first-row等）
- 变更日志生成
- 批处理和流处理的统一支持
- 与主流计算引擎和存储系统的集成

### Compaction

完整的LSM树工作流程
- 写入阶段：新数据写入Level 0，生成多个小文件
- 触发Compaction：当Level 0文件数达到阈值（如4个）时触发Compaction
- 文件合并：将Level 0的文件合并为更大的文件
- 层级提升：合并后的文件可能提升到更高层级（如Level 1）
- 元数据更新：创建新快照和元数据文件，记录文件变化
- 查询优化：减少了需要扫描的文件数量，提高查询性能

Compaction的核心实现主要在 paimon-core 模块中，具体分布在以下几个包：
- AppendOnly
- MergeTree
  - 包含多种Compaction策略：
    - UniversalCompaction：通用的Compaction策略
    - ForceUpLevel0Compaction：强制将Level 0文件合并到更高层级的激进策略
  - 实现了多种MergeFunction：
    - DeduplicateMergeFunction：去重合并
    - FirstRowMergeFunction：保留第一行
    - PartialUpdateMergeFunction：部分更新
    - 各种聚合函数（如sum、max、min等）

- 计算引擎集成
  - paimon-flink：CompactAction类实现了Flink的Compaction作业
  - paimon-spark：CompactProcedure类实现了Spark的Compaction过程

## LSM
Paimon创新性地将LSM树（Log-structured merge-tree）结构与湖格式结合，实现了高效的实时更新能力。下面详细介绍其LSM树设计：


分层存储机制：Paimon将数据分为多个层级（Level 0-N），形成一个逻辑上的LSM树，其中：
- Level 0 (L0)：内存中的可变数据（MemTable），对应Flink Writer的内存缓冲区
- Level 1-N (L1+)：磁盘上的不可变数据文件（SSTable），对应Paimon的数据文件（如Parquet）

Paimon没有在本地磁盘实现完整LSM树，而是巧妙地将LSM思想应用于分布式文件系统
将属于同一个Bucket的数据文件组织成一个逻辑上的LSM树，每个Bucket包含独立的LSM结构


## 读写流程类
写入核心流程（以 Flink 写入为例）：
- 初始化阶段：
  - Flink 算子 PaimonSink 创建 TableWrite，初始化 BucketWriter（每个桶一个 Writer）；
  - 加载表的 Schema、分桶规则、写入配置（如文件大小阈值、提交间隔）。
- 数据写入阶段：
  - 数据经 BucketAssigner 分配到对应桶，写入 BucketWriter 的内存缓冲；
  - 当缓冲达到阈值（如文件大小 128MB），触发 flush，将内存数据刷成临时数据文件（.tmp 后缀）；
- 预提交（PreCommit）阶段：
  - 所有桶的临时文件完成刷盘，生成 ManifestEntry（标记 ADD 操作）；
  - 收集所有 ManifestEntry，生成临时 ManifestFile；
- 提交（Commit）阶段：
  - Committer 组件将临时 ManifestFile 重命名为正式文件，生成新的 ManifestList；
  - SnapshotManager 创建新 Snapshot，指向该 ManifestList，更新 snapshot/latest 指针；
- 清理阶段：删除临时文件，清理过期快照（可选）。