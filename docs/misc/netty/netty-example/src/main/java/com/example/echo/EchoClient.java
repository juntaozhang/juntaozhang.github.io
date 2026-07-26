package com.example.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends a message to the server and waits for the response.
 */
public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final AtomicBoolean running = new AtomicBoolean(true);
        final EchoClientHandler[] handlerHolder = new EchoClientHandler[1];
        final Thread mainThread = Thread.currentThread();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     handlerHolder[0] = new EchoClientHandler(running);
                     ch.pipeline().addLast(handlerHolder[0]);
                 }
             });

            // Start the client
            ChannelFuture f = b.connect(host, port).sync();

            System.out.println("EchoClient connected to " + host + ":" + port);
            System.out.println("Type messages and press Enter to send (type 'bye' to quit):");

            // Wait a bit for connection to be fully established
            Thread.sleep(500);

            // Start a monitor thread to watch for connection close
            Thread monitorThread = new Thread(() -> {
                try {
                    while (running.get() && f.channel().isActive()) {
                        Thread.sleep(1000);
                    }
                    f.channel().close().sync();
                    mainThread.interrupt();
                } catch (InterruptedException e) {
                    // Thread was interrupted, expected behavior
                }
            });
            monitorThread.setDaemon(true);
            monitorThread.start();

            // Read user input and send to server
            Scanner scanner = new Scanner(System.in);
            EchoClientHandler handler = handlerHolder[0];

            try {
                while (running.get() && f.channel().isActive()) {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.equalsIgnoreCase("bye")) {
                            break;
                        }
                        handler.sendMessage(line);
                    } else {
                        break;
                    }
                }
            } catch (IllegalStateException e) {
                // Scanner was closed due to interrupt
                System.out.println("\nClient shutting down...");
            }

            scanner.close();

            // Close the connection
            if (f.channel().isActive()) {
                f.channel().close().sync();
            }
        } finally {
            workerGroup.shutdownGracefully();
            System.out.println("EchoClient stopped");
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        new EchoClient(host, port).run();
    }
}