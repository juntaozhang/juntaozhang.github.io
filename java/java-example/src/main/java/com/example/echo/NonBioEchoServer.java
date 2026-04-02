package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 同步非阻塞 Echo Server（轮询方式）
 *
 * 使用非阻塞模式但不使用Selector，通过轮询(polling)方式检查所有连接
 * 这是介于BIO和NIO之间的一种方式
 */
public class NonBioEchoServer {

    private static final int PORT = 8083;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            // 打开ServerSocketChannel并设置为非阻塞模式
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));

            // 存储所有已连接的客户端
            List<SocketChannel> clients = new ArrayList<>();

            System.out.println("Non-blocking Echo Server (Polling) started on port " + PORT);

            while (true) {
                // 1. 非阻塞方式接受新连接
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    clients.add(clientChannel);
                }

                // 2. 轮询所有客户端，读取并回显数据
                List<SocketChannel> toRemove = new ArrayList<>();
                for (SocketChannel client : clients) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        int bytesRead = client.read(buffer);

                        if (bytesRead == -1) {
                            // 客户端关闭连接
                            System.out.println("Client disconnected: " + client.getRemoteAddress());
                            toRemove.add(client);
                        } else if (bytesRead > 0) {
                            // 读到数据，进行回显
                            buffer.flip();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);
                            String message = new String(data);

                            System.out.println("Received from " + client.getRemoteAddress() + ": " + message);

                            // Echo back
                            if ("bye".equalsIgnoreCase(message.trim())) {
                                client.write(ByteBuffer.wrap("Goodbye!\n".getBytes()));
                                toRemove.add(client);
                                System.out.println("Client disconnected: " + client.getRemoteAddress());
                            } else {
                                client.write(ByteBuffer.wrap(("Echo: " + message).getBytes()));
                            }
                        }
                        // 如果bytesRead == 0，表示没有数据，继续处理下一个客户端
                    } catch (IOException e) {
                        System.out.println("Error handling client: " + e.getMessage());
                        toRemove.add(client);
                    }
                }

                // 3. 移除已断开的客户端
                for (SocketChannel client : toRemove) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clients.remove(client);
                }

                // 4. 避免CPU过度占用，稍微休眠
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}