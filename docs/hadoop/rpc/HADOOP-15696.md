# HADOOP-15696 — Jetty 迁移后 KMS 因 FD 过多性能退化

## Commit

- **Hash**: `d1ea7df43d6`（branch-3.1，2018-09-05，Wei-Chiu Chuang）
- **涉及文件**（4 个，+42/-12）：`HttpServer2.java`、`TestHttpServer.java`、`kms-default.xml`、`httpfs-default.xml`

## Diff 机制分析

Hadoop 3 的 HttpServer2 从老的 Jetty 6/7 迁到 Jetty 9 后，KMS/HttpFS 出现"too many open file descriptors"：老代码 `configureChannelConnector` 给 connector 设了固定 10s idle timeout，但 Jetty 9 的 `ServerConnector` 默认 keep-alive 行为变化，半开/空闲 HTTP 连接（KMS 客户端每密钥操作一个连接）**不能及时被服务端回收**，FD 堆积到耗尽，KMS 吞吐骤降。

修复：

1. idle timeout 从硬编码 10s 改为可配置 `hadoop.http.idle_timeout.ms`（默认 10000）；
2. `configureChannelConnector` 内联到 connector 创建路径，timeout 在 `build()` 阶段统一下发；
3. KMS/HttpFS 的默认配置里显式设 `hadoop.http.idle_timeout.ms=1000`，让空闲连接 1 秒即回收。

代码路径：KMS/HttpFS/Namenode HTTP 服务 → Jetty connector 连接生命周期管理。

## 影响评估

**评级：中**。KMS 是加密集群密钥操作的关键服务，FD 耗尽直接导致 HDFS 客户端卡死；属于服务稳定性修复（虽是 HTTP 层，但 KMS 故障会级联阻塞所有加密 IO）。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
