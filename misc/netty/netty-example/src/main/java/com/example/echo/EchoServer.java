package com.example.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Echoes back any received data from a client.
 */
public class EchoServer {

    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public EchoServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // EventLoopGroup handles I/O operations and connections
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new EchoServerHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections
            ChannelFuture f = b.bind(port).sync();

            System.out.println("EchoServer started and listening on port " + port);
            System.out.println("Press Ctrl+C to stop the server...");

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received...");
                EchoServerHandler.broadcastShutdown();
                try {
                    // Give some time for the shutdown message to reach clients
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

            // Wait until the server socket is closed
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            System.out.println("EchoServer stopped");
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new EchoServer(port).run();
    }
}