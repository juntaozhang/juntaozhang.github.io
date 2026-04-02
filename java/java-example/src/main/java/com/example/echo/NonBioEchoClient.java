package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * 同步非阻塞 Echo Client（轮询方式）
 */
public class NonBioEchoClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8083;

    public static void main(String[] args) {
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);

            // 非阻塞方式连接
            clientChannel.connect(new InetSocketAddress(HOST, PORT));

            // 轮询等待连接完成
            while (!clientChannel.finishConnect()) {
                System.out.println("Connecting...");
                Thread.sleep(100);
            }

            System.out.println("Connected to Non-blocking Echo Server at " + HOST + ":" + PORT);
            System.out.println("Type messages to echo (type 'bye' to exit):");

            Scanner scanner = new Scanner(System.in);

            // 启动一个线程来轮询读取服务器响应
            Thread readerThread = new Thread(() -> {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                try {
                    while (clientChannel.isOpen()) {
                        readBuffer.clear();
                        int bytesRead = clientChannel.read(readBuffer);

                        if (bytesRead == -1) {
                            // 服务器关闭连接
                            break;
                        } else if (bytesRead > 0) {
                            readBuffer.flip();
                            byte[] data = new byte[readBuffer.remaining()];
                            readBuffer.get(data);
                            System.out.print(new String(data));
                        }
                        // 如果bytesRead == 0，没有数据，继续轮询
                        Thread.sleep(100);
                    }
                } catch (IOException | InterruptedException e) {
                    if (clientChannel.isOpen()) {
                        e.printStackTrace();
                    }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // 主线程读取用户输入并发送到服务器
            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();

                ByteBuffer writeBuffer = ByteBuffer.wrap(userInput.getBytes());

                while (writeBuffer.hasRemaining()) {
                    clientChannel.write(writeBuffer);
                }

                if ("bye".equalsIgnoreCase(userInput)) {
                    Thread.sleep(500);
                    clientChannel.close();
                    break;
                }
            }

            scanner.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}