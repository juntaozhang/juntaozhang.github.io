# Apache Iceberg Flink 1.20 示例合集

这是一个完整的 Apache Iceberg + Flink 1.20 集成示例集合，**每个示例都是独立的类**，专注于特定功能。

## 📁 项目结构

```
flink1.20/
├── pom.xml
├── README.md
└── src/main/java/com/example/iceberg/
    ├── BasicCRUDExample.java           # 基本 CRUD 操作
    ├── SchemaEvolutionExample.java     # Schema 演进
    ├── StreamingWriteExample.java      # 流式写入
    ├── StreamingReadExample.java       # 流式读取
    ├── MySQLCDCExample.java            # MySQL CDC
    ├── KafkaCDCExample.java            # Kafka Debezium CDC
    ├── IcebergFlinkCDCExample.java     # CDC 完整示例
    └── DataMaintenanceExample.java     # 表维护
```

## 🚀 快速开始

### 1. 编译项目

```bash
cd /Users/juntao/src/github.com/juntaozhang/juntaozhang.github.io/bigdata/iceberg/flink1.20

mvn clean package
```

### 2. 运行示例

每个示例都是独立的，可以单独运行：

```bash
# 方式一：运行特定示例
java -cp target/iceberg-flink-example-1.0-SNAPSHOT.jar com.example.iceberg.BasicCRUDExample

# 方式二：通过 Maven 运行
mvn exec:java -Dexec.mainClass="com.example.iceberg.BasicCRUDExample"
```

## 📚 示例说明

### 1. BasicCRUDExample.java - 基本 CRUD 操作

**功能：**
- ✅ 创建表
- ✅ 插入数据
- ✅ 查询数据
- ✅ 更新数据
- ✅ 删除数据

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.BasicCRUDExample"
```

**适用场景：**
- Iceberg 入门
- 学习基本操作
- 测试环境配置

---

### 2. SchemaEvolutionExample.java - Schema 演进

**功能：**
- ✅ 添加新列（无需重写数据）
- ✅ 修改表属性
- ✅ 分区演进
- ✅ 填充新列数据

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.SchemaEvolutionExample"
```

**适用场景：**
- 需要频繁修改表结构
- 学习 Schema 演进特性
- 了解分区演进

---

### 3. StreamingWriteExample.java - 流式写入

**功能：**
- ✅ 从 DataGen 流式生成数据
- ✅ 流式写入 Iceberg 表
- ✅ 检查点配置（Exactly-Once）
- ✅ 自动文件管理

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.StreamingWriteExample"
```

**适用场景：**
- 实时数据摄入
- 流批一体化架构
- 学习流式写入配置

---

### 4. StreamingReadExample.java - 流式读取

**功能：**
- ✅ 从 Iceberg 表流式读取
- ✅ 监控表变更
- ✅ 配置不同的读取模式
- ✅ 实时聚合统计

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.StreamingReadExample"
```

**适用场景：**
- 实时数据监控
- 增量数据处理
- 事件驱动架构

---

### 5. MySQLCDCExample.java - MySQL CDC

**功能：**
- ✅ 从 MySQL 读取 CDC 数据
- ✅ 处理 INSERT/UPDATE/DELETE
- ✅ 实时同步到 Iceberg
- ✅ Schema 映射

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.MySQLCDCExample"
```

**前提条件：**
```sql
-- 在 MySQL 中创建测试表
CREATE DATABASE ecommerce;
USE ecommerce;

CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY,
    customer_id BIGINT,
    product_id BIGINT,
    quantity INT,
    unit_price DECIMAL(10, 2),
    discount DECIMAL(5, 2),
    order_date DATE,
    order_ts TIMESTAMP,
    customer_name VARCHAR(100),
    product_name VARCHAR(100)
);
```

**配置说明：**
取消注释示例中的 MySQL CDC 源表配置，修改连接信息：
```java
'hostname' = 'localhost',
'port' = '3306',
'username' = 'root',
'password' = 'password',
'database-name' = 'ecommerce',
'table-name' = 'orders'
```

**适用场景：**
- MySQL 数据库实时同步
- 数据湖构建
- OLTP 到 OLAP 同步

---

### 6. KafkaCDCExample.java - Kafka Debezium CDC

**功能：**
- ✅ 从 Kafka 读取 Debezium JSON
- ✅ 解析 CDC 操作类型（c/u/d/r）
- ✅ 实时同步到 Iceberg
- ✅ 数据清洗和转换

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.KafkaCDCExample"
```

**前提条件：**
```bash
# 1. 启动 Kafka
docker-compose up -d kafka zookeeper

# 2. 配置 Debezium 连接器
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "inventory-connector",
    "config": {
      "connector.class": "io.debezium.connector.mysql.MySqlConnector",
      "database.hostname": "mysql",
      "database.port": "3306",
      "database.user": "debezium",
      "database.password": "dbz",
      "database.server.id": "184054",
      "database.server.name": "dbserver1",
      "database.include.list": "ecommerce",
      "database.history.kafka.bootstrap.servers": "kafka:29092",
      "database.history.kafka.topic": "schema-changes.ecommerce"
    }
  }'
```

**配置说明：**
取消注释示例中的 Kafka CDC 源表配置：
```java
'properties.bootstrap.servers' = 'localhost:9092',
'properties.group.id' = 'iceberg-cdc-customers',
'topic' = 'dbserver1.ecommerce.customers',
'format' = 'debezium-json'
```

**适用场景：**
- 多数据库 CDC 统一接入
- 解耦源数据库和数据湖
- 微服务数据同步

---

### 7. IcebergFlinkCDCExample.java - CDC 完整示例

**功能：**
- ✅ 多种 CDC 场景演示
- ✅ 数据清洗和转换
- ✅ 实时聚合统计
- ✅ 数据质量检查

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.IcebergFlinkCDCExample"
```

**包含场景：**
- MySQL CDC → Iceberg
- PostgreSQL CDC → Iceberg
- Kafka Debezium → Iceberg
- CDC 数据清洗
- 实时聚合
- 数据质量检查

**适用场景：**
- 学习完整 CDC 流程
- 生产环境参考实现
- 最佳实践学习

---

### 8. DataMaintenanceExample.java - 表维护

**功能：**
- ✅ 快照管理
- ✅ 文件分析
- ✅ 分区统计
- ✅ 数据质量检查
- ✅ 维护建议

**运行：**
```bash
mvn exec:java -Dexec.mainClass="com.example.iceberg.DataMaintenanceExample"
```

**适用场景：**
- 日常表维护
- 性能优化
- 存储管理
- 数据质量监控

---

## 🌟 流式 CDC 完整流程

### 架构示意

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   MySQL     │─────▶│ Debezium     │─────▶│   Kafka     │
│  (Source)   │ CDC  │  Connector   │ JSON  │            │
└─────────────┘      └──────────────┘      └──────┬──────┘
                                                  │
                                                  ▼
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  Iceberg    │◀─────│  Flink CDC   │◀─────│   Kafka     │
│   (Lake)    │ Sync │     Job      │ Read │  Consumer   │
└─────────────┘      └──────────────┘      └─────────────┘
```

### 完整步骤

#### 1. 配置 MySQL

```sql
-- 启用 binlog
-- my.cnf
[mysqld]
server-id = 1
log_bin = mysql-bin
binlog_format = ROW
binlog_row_image = FULL
expire_logs_days = 10
```

#### 2. 配置 Debezium

```bash
# 创建 Debezium 连接器
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" -d @debezium-mysql-connector.json
```

#### 3. 启动 Flink CDC 作业

```bash
# 提交到 Flink 集群
./bin/flink run -c com.example.iceberg.MySQLCDCExample \
    target/iceberg-flink-example-1.0-SNAPSHOT.jar
```

#### 4. 监控 CDC 作业

```bash
# Flink Web UI
http://localhost:8081

# 查看作业状态和指标
- Records In/Out
- Checkpoint 状态
- Lag 指标
```

---

## 🔧 环境配置

### 1. S3/MinIO 配置

```java
tEnv.executeSql("""
    CREATE CATALOG my_catalog WITH (
        'type' = 'iceberg',
        'catalog-type' = 'hadoop',
        'warehouse' = 's3a://warehouse/iceberg'
    )
    """);
```

### 2. REST Catalog 配置

```java
tEnv.executeSql("""
    CREATE CATALOG my_catalog WITH (
        'type' = 'iceberg',
        'catalog-impl' = 'org.apache.iceberg.rest.RESTCatalog',
        'uri' = 'http://localhost:8181',
        'warehouse' = 's3a://warehouse/iceberg'
    )
    """);
```

### 3. Hive Catalog 配置

```java
tEnv.executeSql("""
    CREATE CATALOG my_catalog WITH (
        'type' = 'iceberg',
        'catalog-type' = 'hive',
        'uri' = 'thrift://localhost:9083',
        'warehouse' = 'hdfs://namenode:8020/warehouse'
    )
    """);
```

---

## 📊 常用 SQL 操作

### 查看表信息

```sql
-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESCRIBE table_name;

-- 查看创建语句
SHOW CREATE TABLE table_name;

-- 查看快照历史
SELECT * FROM table_name.snapshots;

-- 查看数据文件
SELECT * FROM table_name.files;

-- 查看分区
SELECT * FROM table_name.partitions;

-- 查看表历史
SELECT * FROM table_name.history;

-- 查看 Manifests
SELECT * FROM table_name.manifests;
```

### 数据操作

```sql
-- 插入数据
INSERT INTO table_name VALUES (...);

-- 批量插入
INSERT INTO table_name VALUES (...), (...), (...);

-- 更新数据
UPDATE table_name SET column = value WHERE condition;

-- 删除数据
DELETE FROM table_name WHERE condition;

-- 查询数据
SELECT * FROM table_name WHERE condition;
```

### Schema 和分区演进

```sql
-- 添加列
ALTER TABLE table_name ADD COLUMN column_name TYPE;

-- 修改表属性
ALTER TABLE table_name SET ('key' = 'value');

-- 添加分区
ALTER TABLE table_name ADD PARTITION FIELD bucket(4, column_name);
ALTER TABLE table_name ADD PARTITION FIELD days(column_name);
ALTER TABLE table_name ADD PARTITION FIELD truncate(2, column_name);
```

---

## 🎯 学习路径

### 初级

1. **BasicCRUDExample** - 学习基本操作
2. **SchemaEvolutionExample** - 了解 Schema 演进
3. **DataMaintenanceExample** - 学习表维护

### 中级

4. **StreamingWriteExample** - 学习流式写入
5. **StreamingReadExample** - 学习流式读取
6. **MySQLCDCExample** - 学习 MySQL CDC

### 高级

7. **KafkaCDCExample** - 学习 Kafka CDC
8. **IcebergFlinkCDCExample** - 学习完整 CDC 流程

---

## 🔍 故障排查

### 常见问题

1. **ClassNotFoundException**
   - 确保所有依赖都打包到 JAR 中
   - 检查 Maven Shade Plugin 配置

2. **S3 连接失败**
   - 检查 S3/MinIO 配置
   - 验证访问密钥和端点

3. **Checkpoint 超时**
   - 增加 checkpoint 超时时间
   - 检查网络和存储性能

4. **CDC 数据丢失**
   - 确保检查点已启用
   - 使用 EXACTLY_ONCE 语义

---

## 📖 参考资源

- [Apache Iceberg 官方文档](https://iceberg.apache.org/)
- [Flink 文档](https://flink.apache.org/)
- [Iceberg Flink 集成指南](https://iceberg.apache.org/flink/)
- [Flink CDC 文档](https://ververica.github.io/flink-cdc-connectors/)
- [Debezium 文档](https://debezium.io/documentation/)

---

## 📝 许可证

Apache License 2.0
