# Spring Boot HTTP Keep-Alive 示例

这个项目演示了 Spring Boot 应用中 HTTP Keep-Alive 连接复用的行为，以及 Server-Sent Events (SSE) 的长连接特性。

## HTTP Keep-Alive 连接复用测试

### 客户端日志分析

[完整客户端日志](client.log)

#### 连接复用场景 (`lisi` API 调用)

`lisi` API 复用连接的日志如下，可以发现直接获取connection，直接发送get请求：

```log
2025-10-28 11:41:28.408 [main] DEBUG o.a.h.c.h.i.c.InternalHttpClient - ex-0000000002 acquired endpoint ep-0000000002
2025-10-28 11:41:28.408 [main] DEBUG o.a.h.c.h.i.classic.MainClientExec - ex-0000000002 executing GET /hello?name=lisi
```

#### 新建连接场景 (`wangwu` API 调用)

`wangwu` API 发现了create new connection的日志如下：

```log
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.c.InternalHttpClient - ex-0000000003 acquired endpoint ep-0000000003
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.impl.classic.ConnectExec - ex-0000000003 opening connection {}->[http://localhost:8080]
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.c.InternalHttpClient - ep-0000000003 connecting endpoint (null)
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.i.PoolingHttpClientConnectionManager - ep-0000000003 connecting endpoint to http://localhost:8080 (3 MINUTES)
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.i.DefaultHttpClientConnectionOperator - localhost resolving remote address
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.i.DefaultHttpClientConnectionOperator - localhost resolved to [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1]
2025-10-28 11:41:28.913 [main] DEBUG o.a.h.c.h.i.i.DefaultHttpClientConnectionOperator - http://localhost:8080 connecting null->localhost/127.0.0.1:8080 (3 MINUTES)
2025-10-28 11:41:28.914 [main] DEBUG o.a.h.c.h.i.i.DefaultHttpClientConnectionOperator - http-outgoing-1 http://localhost:8080 connected /127.0.0.1:61840->localhost/127.0.0.1:8080
2025-10-28 11:41:28.914 [main] DEBUG o.a.h.c.h.i.i.DefaultManagedHttpClientConnection - http-outgoing-1 set socket timeout to 3 MINUTES
2025-10-28 11:41:28.914 [main] DEBUG o.a.h.c.h.i.i.PoolingHttpClientConnectionManager - ep-0000000003 connected http-outgoing-1
2025-10-28 11:41:28.914 [main] DEBUG o.a.h.c.h.i.c.InternalHttpClient - ep-0000000003 endpoint connected
2025-10-28 11:41:28.914 [main] DEBUG o.a.h.c.h.i.classic.MainClientExec - ex-0000000003 executing GET /hello?name=wangwu
```

### 服务端日志分析

[完整服务端日志](server.log)

We can see whether the connection is reused or new created, worker thread id is different.

Each HTTP request is dispatched to any free thread in the server thread pool;
reuse of a TCP connection does not guarantee reuse of the same thread.

**重要发现**：
- TCP 连接的复用并不保证使用相同的工作线程
- 每个 HTTP 请求都会被分派到线程池中的任何空闲线程

## Server Sent Event (SSE) 长连接测试

[完整 SSE 日志](sse.log)

SSE is not governed by the HTTP Keep-Alive reuse timeout while the stream is active.

Each HTTP request is dispatched to any free thread in the server thread pool;
reuse of a TCP connection does not guarantee reuse of the same thread.
```log
2025-10-28 11:56:59.400 [main] DEBUG o.a.h.c.h.i.c.InternalHttpClient - ex-0000000002 acquired endpoint ep-0000000002
2025-10-28 11:56:59.400 [main] DEBUG o.a.h.c.h.i.classic.MainClientExec - ex-0000000002 executing GET /hello?name=test
```

**SSE 特点**：
- SSE 连接在流处于活动状态时不受 HTTP Keep-Alive 复用超时的限制
- 长时间保持连接开放，持续推送数据给客户端

## 技术要点

1. **HTTP Keep-Alive**: 允许在单个 TCP 连接上发送多个 HTTP 请求/响应
2. **连接池管理**: Apache HttpClient 使用 `PoolingHttpClientConnectionManager` 管理连接池
3. **线程调度**: 服务端使用线程池处理请求，连接复用不等于线程复用
4. **SSE 长连接**: Server-Sent Events 提供服务器到客户端的实时数据推送能力