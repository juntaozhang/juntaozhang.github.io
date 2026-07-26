package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * NIO (Non-blocking IO) Echo Client
 * 非阻塞IO客户端示例
 */
public class NioEchoClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8081;

    public static void main(String[] args) {
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(true);
            clientChannel.connect(new InetSocketAddress(HOST, PORT));

            System.out.println("Connected to NIO Echo Server at " + HOST + ":" + PORT);
            System.out.println("Type messages to echo (type 'bye' to exit):");

            Scanner scanner = new Scanner(System.in);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 启动一个线程来读取服务器响应
            Thread readerThread = new Thread(() -> {
                try {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    while (clientChannel.isOpen()) {
                        readBuffer.clear();
                        int bytesRead = clientChannel.read(readBuffer);
                        if (bytesRead == -1) {
                            break;
                        }
                        if (bytesRead > 0) {
                            readBuffer.flip();
                            byte[] data = new byte[readBuffer.remaining()];
                            readBuffer.get(data);
                            System.out.print(new String(data));
                        }
                    }
                } catch (IOException e) {
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
                buffer.clear();
                buffer.put(userInput.getBytes());
                buffer.flip();
                clientChannel.write(buffer);

                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }

            clientChannel.close();
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}