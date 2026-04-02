# TinyURL
- 核心考点：
  - 如何生成唯一的短码？（哈希 vs 自增ID vs 随机字符串）
  - 数据库选型（SQL vs NoSQL）及表结构设计。
  - 高并发读取时的缓存策略（Redis）。
  - 追问：如果短码冲突了怎么办？如何处理自定义短码？
- 关键权衡：自增ID容易预测但需要分布式协调；哈希可能冲突但无需协调。

## 需求分析

`https://www.systeminterview.com/q=chatsystem&c=loggedin&v=v3&l=long`
-> `https://turl.com/xxxxxxxxx`

- QPS: 10K/s
- url 支持量: 10亿
- 需不需要自定义: 是
- timeout

500B -> 20B
62进制(26*2+10)（a-zA-Z0-9） -> 62^6 -> 500亿+



