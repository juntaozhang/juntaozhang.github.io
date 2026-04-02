package com.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO (Asynchronous IO) Echo Server
 * 使用异步通道和CompletionHandler回调实现
 */
public class AioEchoServer {

    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));

            System.out.println("AIO Echo Server started on port " + PORT);

            CountDownLatch latch = new CountDownLatch(1);

            // 开始接受连接
            AcceptHandler acceptHandler = new AcceptHandler(serverChannel, latch);
            serverChannel.accept(null, acceptHandler);

            // 保持主线程运行
            latch.await();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理连接接受
     */
    private static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
        private final AsynchronousServerSocketChannel serverChannel;
        private final CountDownLatch latch;

        public AcceptHandler(AsynchronousServerSocketChannel serverChannel, CountDownLatch latch) {
            this.serverChannel = serverChannel;
            this.latch = latch;
        }

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
            // 继续接受新的连接
            serverChannel.accept(null, this);

            // 为新连接创建读取处理器
            new ClientSession(clientChannel);

            try {
                System.out.println("New client connected: " + clientChannel.getRemoteAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            exc.printStackTrace();
            latch.countDown();
        }
    }

    /**
     * 客户端会话，管理一个连接的读写
     */
    private static class ClientSession {
        private final AsynchronousSocketChannel channel;
        private final ByteBuffer buffer;

        public ClientSession(AsynchronousSocketChannel channel) {
            this.channel = channel;
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            startRead();
        }

        /**
         * 开始读取数据
         */
        private void startRead() {
            buffer.clear();
            channel.read(buffer, buffer, new ReadHandler(this));
        }

        /**
         * 处理读取到的数据并发送响应
         */
        public void handleReadResult(int bytesRead) {
            if (bytesRead == -1) {
                closeChannel();
                return;
            }

            // 处理读取到的数据
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data);

            logMessage("Received", message);

            // 准备响应
            String response;
            if ("bye".equalsIgnoreCase(message.trim())) {
                response = "Goodbye!\n";
            } else {
                response = "Echo: " + message;
            }

            // 发送响应
            ByteBuffer writeBuffer = ByteBuffer.wrap(response.getBytes());
            channel.write(writeBuffer, writeBuffer, new WriteHandler(this, message.trim()));
        }

        /**
         * 继续读取下一个消息
         */
        public void continueReading() {
            startRead();
        }

        /**
         * 关闭连接
         */
        public void closeChannel() {
            try {
                if (channel.isOpen()) {
                    System.out.println("Client disconnected: " + channel.getRemoteAddress());
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 记录消息
         */
        private void logMessage(String prefix, String message) {
            try {
                System.out.println(prefix + " from " + channel.getRemoteAddress() + ": " + message);
            } catch (IOException e) {
                System.out.println(prefix + ": " + message);
            }
        }
    }

    /**
     * 处理读取完成
     */
    private static class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final ClientSession session;

        public ReadHandler(ClientSession session) {
            this.session = session;
        }

        @Override
        public void completed(Integer bytesRead, ByteBuffer attachment) {
            session.handleReadResult(bytesRead);
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
            session.closeChannel();
        }
    }

    /**
     * 处理写入完成
     */
    private static class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final ClientSession session;
        private final String message;

        public WriteHandler(ClientSession session, String message) {
            this.session = session;
            this.message = message;
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            if ("bye".equalsIgnoreCase(message)) {
                session.closeChannel();
            } else {
                // 继续读取下一个消息
                session.continueReading();
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
            session.closeChannel();
        }
    }
}