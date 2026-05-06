## Systems Design

### Latency numbers
- CPU
  - CPU L1: 0.5 ns
    - 例如：阿里玄铁 C950（RISC-V）：5nm；3.2GHz  -> 1个时钟周期是 0.3 ns
  - CPU L2: 5 ns
  - 用Zippy压缩1K字节：10 µs
  
    | 缓存级别 | 典型容量 (每核/共享) | 访问延迟 (时钟周期) | 相对速度 (对比内存) | 归属关系 |
    | :--- | :--- | :--- | :--- | :--- |
    | L1 Cache | 32KB - 64KB | 1 - 4 周期 | 极快 (~100倍于内存) | 核心独享 |
    | L2 Cache | 256KB - 1MB | 10 - 20 周期 | 快 (~25倍于内存) | 核心独享 (通常) |
    | L3 Cache | 8MB - 64MB+ | 30 - 75 周期 | 较快 (~5-10倍于内存) | 所有核心共享 |
    | 主内存 (RAM)| 64GB+ | 100 - 300+ 周期 | 慢 (基准) | 全局共享 |
    
- 主存
  - 主内存访问：100 ns（相比ssd 强在随机读写）
  - 顺序读/写1MB数据：20 µs
- SSD：
  - 随机读 4KB：50 µs
  - 随机写 4KB：100 µs
  - 顺序 1MB速率：50 µs
- 磁盘（HDD）：
  - 磁盘寻道：10毫秒
  - 顺序读/写1MB：10毫秒
- 网络（1 Gbps）
  - 从网络中顺序读取1KB数据：10微妙
  - 从网络中顺序读取1MB数据：10毫秒（注意与磁盘差不多）
  - 数据中心内发包：100微妙
  - 跨数据中心发包：150毫秒

### 常用组件
- Load Balancer: Nginx, HAProxy
- Cache: Redis, Memcached
- [DB](../../../../../../../misc/db.md) 
- Queue
  - Kafka
    - 利用顺序写磁盘、零拷贝 (Zero Copy)、页缓存等技术，轻松达到百万级甚至千万级QPS
    - 事务性差
  - RocketMQ
    - 扛住“双11”流量，保证数据零丢失：
    - 吞吐量接近 Kafka，延迟接近 RabbitMQ
  - RabbitMQ
    - Exchange（交换机）：可以构建极其复杂的层级分发逻辑，而不需要修改生产者的代码。只要运维人员在后台改一下绑定关系，流量走向就变了。支持Many-to-Many的路由规则。
    - 吞吐量：万级到十万级 QPS
    - 不适合传输超大消息体

| 维度 | RabbitMQ | Kafka | RocketMQ      |
| :--- | :--- | :--- |:--------------|
| 开发语言 | Erlang | Scala/Java | Java          |
| 协议标准 | AMQP (标准) | 私有协议 (类 TCP) | 私有协议 (类 TCP)  |
| 吞吐量 | 万级 ~ 十万级 | 百万级 ~ 千万级 | 十万级 ~ 百万级     |
| 延迟 | 微秒/毫秒级 (最低) | 毫秒级 (依赖配置) | 毫秒级           |
| 可靠性 | 高 (确认机制) | 高 (副本机制) | 极高 (金融级，事务支持) |
| 路由能力 | 极强 (Exchange) | 弱 (Topic 广播) | 中等 (Tag 过滤)   |
| 消息回溯 | 不支持 (消费即删) | 支持 (长期存储) | 支持 (有限时间)     |
| 延迟消息 | 需插件实现 | 需变通实现 | 原生支持 (任意时间)   |
| 事务消息 | 不支持 (需应用层保证) | 支持 (较重) | 原生支持 (杀手锏)    |
| 顺序消息 | 支持 (单队列) | 支持 (Partition 内) | 支持 (严格)       |
| 主要短板 | 大数据量下性能下降，扩展难 | 功能单一，无延迟/事务原生支持 | 社区主要在国內，运维稍重  |

- Fluss：在“实时数仓”和“流式分析”这个特定场景下，Apache Fluss 的设计目标正是为了解决 Kafka 的痛点，并有望成为比 Kafka 更优的“实时表”存储底座
  - 目前主要集中在 Java 和 Flink 集成上
  - 还在孵化阶段
- Storage: S3, HDFS


### 方法论
在开始任何题目之前，请强制自己遵循以下5步法，这也是面试官最看重的流程：
1.  需求澄清 (Requirements Clarification, 5-8 mins)：不要急着画图！问清楚功能需求（Functional）和非功能需求（Non-Functional，如延迟、一致性、可用性）。
2.  估算 (Back-of-the-envelope Estimation, 3-5 mins)：用户量？QPS？存储量？带宽？这决定了你是用单机还是分布式。
3.  高层架构 (High-Level Design, 10-15 mins)：画出核心组件（LB, App Server, DB, Cache, Queue）。
4.  深入细节 (Deep Dive, 15-20 mins)：挑选1-2个难点深入（如：数据分片策略、缓存一致性、唯一ID生成）。
5.  总结与权衡 (Wrap-up & Trade-offs, 5 mins)：主动指出系统的瓶颈和可能的改进方案。

---

### 建议

1. 动手画图：不要只在脑子里想。准备纸笔或白板工具，每道题都要画出架构图。
2. 大声说出来：假装对面坐着面试官。
   - “对于这个场景，我认为读多写少，所以我选择...”
   - “这里可能会成为瓶颈，我的解决方案是...”
3. 关注“为什么”：面试官不在乎你用了什么技术，而在乎你为什么选它而不选另一个。
   - ❌ 错误回答：“我们用 Kafka。”
   - ✅ 正确回答：“我们需要解耦生产者和消费者，并且需要高吞吐量的消息持久化，虽然 RabbitMQ 也可以，但考虑到未来的扩展性和社区生态，我们选择 Kafka，尽管它的延迟稍高...”
4. 熟悉组件的优缺点，来解决问题
---

#### 基础架构与数据存储类
目标：掌握最经典的入门题，理解哈希、存储和读写分离。
- [设计一个 URL 缩短服务 (Design TinyURL / Bit.ly)](tiny-url.md)
  - 侧重唯一ID生成
- [设计一个键值存储系统 (Design a Key-Value Store like Redis/Dynamo)](https://bytebytego.com/courses/system-design-interview/design-a-key-value-store)
    -   核心考点：
        -   数据分片（Sharding）策略：一致性哈希（Consistent Hashing）是必考题。
        -   复制与容错：主从复制、副本数、故障转移。
        -   一致性模型：最终一致性 vs 强一致性（Quorum机制）。
    -   关键权衡：增加副本提高读性能和可用性，但降低写性能并增加一致性难度。

---

#### 第 2 天：社交网络与时间线类
目标：解决“读多写少”与“写多读少”的经典矛盾，掌握推/拉模式。

-   上午题目 3：设计新闻推送系统 (Design News Feed / Twitter Timeline)
    -   核心考点：
        -   推模式 (Push) vs 拉模式 (Pull) vs 混合模式。
        -   大V（名人）发帖时的“扇出”（Fan-out）问题。
        -   数据存储：如何存储海量帖子？如何快速获取用户关注列表？
    -   关键权衡：推模式读快写慢（适合普通人），拉模式写快读慢（适合大V），混合模式是工业界标准解法。

-   下午题目 4：设计即时通讯系统 (Design WhatsApp / WeChat)
    -   核心考点：
        -   通信协议：长连接（WebSocket）vs 轮询。
        -   消息可靠性：如何保证消息不丢失、不重复、有序到达（ACK机制、序列号）。
        -   离线消息处理：消息队列（Kafka）+ 持久化存储。
        -   群聊架构：如何高效分发群消息？
    -   关键权衡：实时性要求高，通常牺牲强一致性换取高可用性（最终一致性）。

---

#### 第 3 天：流媒体与大文件类
目标：处理大文件存储、带宽优化和流式传输。

-   上午题目 5：设计视频流媒体平台 (Design YouTube / Netflix)
    -   核心考点：
        -   大文件存储：对象存储（S3）+ CDN 分发。
        -   视频处理：转码（不同分辨率）、自适应码率（HLS/DASH）。
        -   元数据管理：视频信息、评论、点赞的数据库设计。
    -   关键权衡：存储成本 vs 加载速度（CDN节点分布策略）。

-   下午题目 6：设计图片分享服务 (Design Instagram / Pinterest)
    -   核心考点：
        -   图片上传流程：预处理、压缩、缩略图生成。
        -   全球分发：多区域部署与数据同步。
        -   热点数据：热门图片的缓存策略。
    -   关键权衡：图片质量 vs 加载速度 vs 存储成本。

---

#### 第 4 天：实时性与地理位置类
目标：处理高并发实时数据和空间索引。

-   上午题目 7：设计网约车系统 (Design Uber / Lyft)
    -   核心考点：
        -   地理位置索引：GeoHash 或 S2 Geometry，如何快速查找附近的司机？
        -   实时匹配算法：如何高效匹配乘客和司机？
        -   高并发写：司机位置每秒都在更新，如何写入数据库？（内存数据库 + 异步持久化）。
    -   关键权衡：位置更新的频率 vs 数据库压力；匹配的精确度 vs 延迟。

-   下午题目 8：设计实时趋势/热搜系统 (Design Twitter Trends / Google Trends)
    -   核心考点：
        -   实时数据聚合：滑动窗口算法（Sliding Window）。
        -   数据结构：Top-K 问题（堆、Sketch 算法如 Count-Min Sketch）。
        -   流处理：使用 Flink/Spark Streaming 进行实时计算。
    -   关键权衡：统计的精确度 vs 实时性（允许少量误差以换取速度）。

---

-   设计限流器 (Design an API Rate Limiter)
    -   核心考点：
        -   算法：令牌桶（Token Bucket）、漏桶（Leaky Bucket）、固定窗口、滑动日志。
        -   分布式限流：如何在多个实例间共享计数？（Redis + Lua 脚本）。
        -   限流粒度：按用户、按IP、按接口？
    -   关键权衡：限流的精确度 vs 系统性能（中心化计数的瓶颈）。

-   设计分布式唯一 ID 生成器 (Design a Unique ID Generator like Snowflake)
    -   核心考点：
        -   需求：全局唯一、趋势递增、高可用、低延迟。
        -   方案对比：UUID（无序）、数据库自增（单点瓶颈）、雪花算法（Snowflake）、号段模式。
        -   时钟回拨问题如何处理？
    -   关键权衡：ID 的可排序性（利于数据库索引）vs 生成的分布式独立性。

---

#### 第 6 天：综合模拟与复杂场景
目标：整合所有知识，应对开放性问题。

-   上午题目 11：设计网络爬虫 (Design a Web Crawler)
    -   核心考点：
        -   URL 去重：布隆过滤器（Bloom Filter）。
        -   分布式任务调度：如何避免重复爬取？
        -   礼貌策略：遵守 robots.txt，控制对同一域名的请求频率。
        -   内容解析与存储。
    -   关键权衡：爬取速度 vs 对被爬网站的友好度；去重的内存占用 vs 误判率。

-   下午题目 12：设计在线文档协作系统 (Design Google Docs)
    -   核心考点：
        -   实时协同编辑：操作转换（OT, Operational Transformation）或 CRDT 算法。
        -   冲突解决：多人同时修改同一字符如何处理？
        -   版本控制与历史回溯。
        -   长连接保持与断线重连。
    -   关键权衡：冲突解决的复杂性 vs 用户体验（无感知合并）。




## Reference
- https://github.com/donnemartin/system-design-primer/blob/master/README-zh-Hans.md
- https://github.com/madd86/awesome-system-design
- [Scale From Zero To Millions Of Users](https://bytebytego.com/courses/system-design-interview/scale-from-zero-to-millions-of-users)
- [Back-of-the-envelope Estimation](https://bytebytego.com/courses/system-design-interview/back-of-the-envelope-estimation)
- [Rate Limiter](https://ivov.dev/notes/rate-limiter)


