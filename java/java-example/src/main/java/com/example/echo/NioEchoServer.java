package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO (Non-blocking IO) Echo Server
 * 使用Selector实现多路复用，单线程处理多个连接
 */
public class NioEchoServer {

    private static final int PORT = 8081;

    public static void main(String[] args) {
        try {
            // 打开ServerSocketChannel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));

            // 打开Selector
            Selector selector = Selector.open();
            // 将ServerSocketChannel注册到Selector，监听连接事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO Echo Server started on port " + PORT);

            while (true) {
                // 阻塞等待事件
                selector.select();

                // 获取就绪的 SelectionKey 集合
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        // 处理连接就绪事件
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        // 处理读就绪事件
                        handleRead(key);
                    }

                    // 移除已处理的 SelectionKey
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector)
            throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("New client connected: " + clientChannel.getRemoteAddress());
        }
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            return;
        }

        // 处理读取到的数据
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String message = new String(data);

        System.out.println("Received from " + clientChannel.getRemoteAddress() + ": " + message);

        // Echo back
        if ("bye".equalsIgnoreCase(message.trim())) {
            clientChannel.write(ByteBuffer.wrap("Goodbye!\n".getBytes()));
            clientChannel.close();
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
        } else {
            clientChannel.write(ByteBuffer.wrap(("Echo: " + message).getBytes()));
        }
    }
}