# java IO
## 基本概念
* IO是主存和外部设备 ( 硬盘、终端和网络等 ) 拷贝数据的过程。 
* IO是操作系统的底层功能实现，底层通过 I/O 指令进行完成。

## Java标准IO回顾

Java标准IO类库,是io面向对象的一种抽象。基于本地方法的底层实现，我们无须关注底层实现。 
InputStream/OutputStream( 字节流 ) ：一次传送一个字节。 
Reader/Writer( 字符流 ) ：一次一个字符。


## NIO

### 基本概念
Channel和buffer是NIO是两个最基本的数据类型抽象。

#### Buffer
是一块连续的内存块。
是NIO数据读或写的中转地。

#### Channel
数据的源头或者数据的目的地
用于向buffer提供数据或者读取buffer数据,buffer对象的唯一接口。
异步I/O支持

#### Seletor
用来检测多个通道,并能知道通道读写是否准备好,因此一个seletor线程可以管理多个通道
- 创建
>Selector.open();
- 注册事件
>client.register(selector, SelectionKey.OP_READ);
- 选择一组键
其相应的通道已为 I/O 操作准备就绪。
>selector.select();//此方法执行处于阻塞模式的选择操作。
>selectionKeys = selector.selectedKeys();//返回此选择器的已选择事件集。


```java
Selector selector = Selector.open();
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
while(true) {
  int readyChannels = selector.select();
  if(readyChannels == 0) continue;
  Set selectedKeys = selector.selectedKeys();
  Iterator keyIterator = selectedKeys.iterator();
  while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    keyIterator.remove();
  }
}
```

## I/O

磁盘 --DMA--> page cache (页缓存) --DMA--> 网卡\
无cpu copy
sendfile 无epoll 参与

网卡数据到达并通知用户程序进行读取

| 模型      | 类型  | 是否阻塞 | 如何处理IO              | 线程模型    |
  |---------|-----|------|---------------------|---------|
| BIO     | 同步  | 阻塞   | 流式IO                | 一连接一线程  |
| 同步非阻塞轮询 | 同步  | 非阻塞  | Channel轮询           | 单线程轮询   |
| NIO     | 同步  | 非阻塞  | Selector多路复用        | 单线程事件驱动 |
| AIO     | 异步  | 非阻塞  | CompletionHandler回调 | 线程池回调   |


### Linux I/O
让一个线程同时监听多个文件描述符。\
select/poll/epoll 都是 I/O 多路复用的机制，比如：Web 服务器有大量网络连接。\
io_uring 现代异步 I/O：io_uring，比如文件系统。

| 特性维度 | select | poll | epoll | io_uring |
| :--- | :--- | :--- | :--- | :--- |
| 核心机制 | 轮询 (Bitmap) | 轮询 (数组) | 事件驱动 (红黑树+回调) | 异步队列 (共享内存 RingBuffer) |
| 时间复杂度 | O(n) | O(n) | O(1) | O(1) |
| 触发模式 | 水平触发 (LT) | 水平触发 (LT) | 水平触发 (LT) / 边缘触发 (ET) | 支持精细控制 |
| 内存拷贝 | 每次全量拷贝 | 每次全量拷贝 | 首次注册拷贝，后续仅拷贝就绪事件 | 零拷贝 (mmap 共享内存) |
| 文件描述符限制 | 1024 (硬编码) | 无硬限制 (受系统资源限制) | 无硬限制 (受系统资源限制) | 无硬限制 |

#### epoll
```text
  【接收】
  网卡 --DMA--> 内核 ring buffer --(指针传递)--> socket 接收队列
                                             ↓
                                     socket 标记为就绪
                                             ↓
                                     epoll ready list
                                             ↓
  应用程序 --epoll_wait 系统调用--> 内核返回就绪 fd
                                             ↓
  应用程序 --read 系统调用--> 内核 --CPU copy--> 用户态内存

  【处理】
  用户态内存 --CPU 处理--> 用户态内存

  【发送】
  应用程序 --write 系统调用--> 内核 --CPU copy--> 内核发送 buffer --DMA--> 网卡
```

#### io_uring
应用场景：Echo 服务器（收到 4KB → 转大写 → 发回）\
并发连接：10000 个客户端同时连接

```text
  0. 【用户态 + 内核态】注册 buffer
     └─ 应用程序调用 io_uring_register_buffers()
     └─ 传入一个 buffer 地址数组：[0x7000_0000, 0x7000_1000, ...]
     └─ 内核记录这些 buffer 的物理地址
     └─ 内核建立 DMA 映射表，让网卡可以直接访问这些地址
     └─ 这是唯一的系统调用开销

  数据接收阶段：

  1. 【硬件层 - DMA】网卡收到数据包
     └─ 网卡硬件检查：这个 socket 注册了 buffer pool
     └─ 网卡直接从预分配的 buffer pool 取一个：0x7000_0000
     └─ ⚠️ 关键：网卡 DMA 直接写入 0x7000_0000（用户态内存）
     └─ CPU 完全不参与！
     └─ 数据到达用户态内存，无需内核 intermediate buffer

  2. 【内核态 - 中断】网卡触发中断
     └─ 内核中断处理程序执行
     └─ 内核知道：数据在 0x7000_0000
     └─ 内核生成一个 CQE：{buffer_id=0, len=4096, user_data=0xABC}
     └─ CQE 放入完成队列（共享内存）

  3. 【用户态】应用程序检查 CQ
     └─ 用户态读取 CQE（共享内存，无系统调用）
     └─ 发现：user_data=0xABC 对应的请求完成了
     └─ 数据在 buffer_id=0，即 0x7000_0000
     └─ ✨ 关键：数据从未进入内核的 socket buffer！
```

```text
传统 I/O 处理 4KB Echo：
  CPU 执行的指令数：
  1. 内核协议栈处理：~10,000 条指令
  2. CPU copy 1（4KB）：~1,000 条指令（memcpy）
  3. 用户态处理（转大写）：~5,000 条指令
  4. CPU copy 2（4KB）：~1,000 条指令
  5. 内核协议栈封装：~10,000 条指令
  总计：~27,000 条指令
  
网卡 --DMA--> 内核 --CPU copy--> 用户 --CPU 处理--> 用户 --CPU copy--> 内核 --DMA--> 网卡

  io_uring 处理 4KB Echo：
  CPU 执行的指令数：
  1. 内核处理 CQE/SQE：~100 条指令（内存操作）
  2. 用户态处理（转大写）：~5,000 条指令
  3. 内核封装协议头（54 字节）：~200 条指令
  总计：~5,300 条指令

网卡 --DMA--> 用户 --CPU 处理--> 用户 --DMA--> 网卡
  CPU 节省：80%+
```

## Reference
- https://www.bilibili.com/video/BV15X4y1Y7T9

