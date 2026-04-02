    package com.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler implementation for the echo client.
 */
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ChannelHandlerContext ctx;
    private final AtomicBoolean running;

    public EchoClientHandler(AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        System.out.println("Connected to server");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // Print the server response
        byte[] response = new byte[msg.readableBytes()];
        msg.readBytes(response);
        String message = new String(response);
        System.out.println("Server response: " + message);

        // Check if server is shutting down
        if (message.contains("Server is shutting down")) {
            System.out.println("Server is shutting down. Closing connection...");
            running.set(false);
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connection closed by server");
        running.set(false);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        running.set(false);
        ctx.close();
    }

    public void sendMessage(String message) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.copiedBuffer((message + "\n").getBytes()));
        }
    }
}