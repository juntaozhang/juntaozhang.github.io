
## spark 中的setInterceptor
ResultTask client端接收文件


## flink 中的fireUserEventTriggered
A→B→C→D→E
B 中的 fireUserEventTriggered，会触发 C 中的方法：userEventTriggered
如果 C 也调用了 fireUserEventTriggered，会触发 D 中的方法：userEventTriggered
如果 C 没有调用 fireUserEventTriggered，不会触发 D 中的方法：userEventTriggered
不会触发B中的方法：userEventTriggered


同理：fireXXX
- fireChannelRead - channelRead
- fireChannelActive - channelActive
- fireChannelInactive - channelInactive
- fireExceptionCaught - exceptionCaught

ctx.executor().execute(() -> ctx.pipeline().fireUserEventTriggered(reader));  会调用 A B C D E中所有的 userEventTriggered 方法


出站链路
Head → A → B → C → D → Tail
当前 HandlerB
ctx.writeAndFlush(msg) vs channel.writeAndFlush(msg)
B → A → Head
Tail→D→C→B→A→Head


