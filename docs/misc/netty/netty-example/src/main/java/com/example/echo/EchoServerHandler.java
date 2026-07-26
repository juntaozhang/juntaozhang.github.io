package com.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ConcurrentHashMap<ChannelHandlerContext, String> clientNames = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientName = "Client-" + channels.size();
        channels.add(ctx.channel());
        clientNames.put(ctx, clientName);
        System.out.println(clientName + " connected: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientName = clientNames.getOrDefault(ctx, "Unknown client");
        System.out.println(clientName + " disconnected: " + ctx.channel().remoteAddress());
        channels.remove(ctx.channel());
        clientNames.remove(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Write back the received message
        ByteBuf buf = (ByteBuf) msg;
        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response);
        String clientName = clientNames.getOrDefault(ctx, "Unknown client");
        System.out.println(clientName + " sent: " + new String(response));
        sendMessage(ctx, Thread.currentThread().getName() + " " + new String(response));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised
        cause.printStackTrace();
        ctx.close();
    }

    public void sendMessage(ChannelHandlerContext ctx, String message) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.copiedBuffer((message + "\n").getBytes()));
        }
    }

    public static void broadcastShutdown() {
        System.out.println("Notifying all clients that server is shutting down...");
        channels.writeAndFlush(Unpooled.copiedBuffer("Server is shutting down. Goodbye!\n".getBytes()));
    }
}