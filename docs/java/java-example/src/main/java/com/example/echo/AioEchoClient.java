package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * AIO (Asynchronous IO) Echo Client
 * 异步IO客户端示例
 */
public class AioEchoClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            // 连接服务器
            clientChannel.connect(new InetSocketAddress(HOST, PORT), null,
                new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void attachment) {
                        System.out.println("Connected to AIO Echo Server at " + HOST + ":" + PORT);
                        System.out.println("Type messages to echo (type 'bye' to exit):");

                        // 启动读取服务器响应的异步操作
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        clientChannel.read(readBuffer, readBuffer,
                            new CompletionHandler<Integer, ByteBuffer>() {
                                @Override
                                public void completed(Integer bytesRead, ByteBuffer buffer) {
                                    if (bytesRead == -1) {
                                        return;
                                    }

                                    buffer.flip();
                                    byte[] data = new byte[buffer.remaining()];
                                    buffer.get(data);
                                    System.out.print(new String(data));

                                    // 继续读取
                                    buffer.clear();
                                    clientChannel.read(buffer, buffer, this);
                                }

                                @Override
                                public void failed(Throwable exc, ByteBuffer buffer) {
                                    exc.printStackTrace();
                                }
                            });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        exc.printStackTrace();
                    }
                });

            // 等待连接建立
            Thread.sleep(1000);

            // 主线程读取用户输入并发送
            Scanner scanner = new Scanner(System.in);
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                writeBuffer.clear();
                writeBuffer.put(userInput.getBytes());
                writeBuffer.flip();

                clientChannel.write(writeBuffer).get();

                if ("bye".equalsIgnoreCase(userInput)) {
                    Thread.sleep(1000);
                    clientChannel.close();
                    break;
                }
            }

            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}