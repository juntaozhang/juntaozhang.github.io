
- 我们可以先处理付费客户的请求，只有在所有优先客户都得到服务后，再将其他请求排队处理
- 一般放到 API网关
- HTTP 429 Too Many Requests
- HTTP 503 Service Unavailable


## 算法
- 固定窗口计数器
- 滑动窗口日志
- 滑动窗口计数器
- 令牌桶算法
  - 不使用定时任务放令牌, 公式： 新令牌数 =（当前时间 - 上次时间） * 令牌速率  + 令牌桶当前令牌数 - 1
  - 每次请求更新上次时间、新令牌数
  - 用 Redis Lua 脚本（Redis 执行 Lua 是原子操作，完美解决并发问题）
  - 缺点：会爆发式消耗桶里所有令牌，给服务器带来压力
- 漏桶算法
  - 请求进入队列排队，固定速率出队
  - 没有真实队列，直接计算
    - 公式： 新桶内容量 = 旧容量 - (当前时间 - 上次时间) * 漏桶速率 + 1
    - 每次请求更新上次时间、新桶内容量
    - 是否超过桶容量，超过则拒绝请求，不更新桶内容量；否则更新桶内容量

工业实践：
- 分布式：令牌桶算法与漏桶算法都需要外部存储，引入了额外的复杂度
- Guava RateLimiter：单机实现
    ```java
    import com.google.common.util.concurrent.RateLimiter;
    
    // 定义：每秒生成5个令牌（QPS=5）
    RateLimiter rateLimiter = RateLimiter.create(5.0);
    
    public void handleRequest() {
        // 尝试获取1个令牌，非阻塞
        if (rateLimiter.tryAcquire()) {
            // 正常处理请求
            System.out.println("请求通过");
        } else {
            // 限流
            System.out.println("请求被限制");
        }
    }
    ```
- 请求携带限流凭证（无状态限流）
  - 用一个密钥（只有服务端知道）把状态加密签名
  - 如果客户端每次请求都携带相同的凭证，那么就无法实现无状态限流


## Reference
- https://ivov.dev/notes/rate-limiter
- https://bytebytego.com/courses/system-design-interview/design-a-rate-limiter
