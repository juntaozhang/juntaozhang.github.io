# java NIO

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

### NIO与IO区别
* 阻塞与非阻塞
这里的阻塞说的操作线程
IO阻塞意味着当一个线程调用read/write方法时,知道数据被读取或数据被完全写入,否则该线程只能被阻塞
NIO当数据没有准备好之前`selector`不会返回事件集,所以操作线程不会处于阻塞状态,所以NIO通过选择器来监控通道,避免线程阻塞

* 面向流与面向缓冲


