package org.madrona.http.common;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

import java.util.concurrent.atomic.AtomicLong;

public class MessageCounter extends SimpleChannelInboundHandler<HttpObject> {

    private final AtomicLong writtenMessages;
    private final AtomicLong readMessages;

    public MessageCounter() {
        this.writtenMessages = new AtomicLong();
        this.readMessages = new AtomicLong();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel inactive");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("====================");
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel read complete");
        super.channelReadComplete(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        System.out.println("Chaane readinf....");
        this.readMessages.incrementAndGet();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel() + " -> sent: " + this.getWrittenMessages() + ", recv: " + this.getReadMessages());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel() + " -> sent: " + this.getWrittenMessages() + ", recv: " + this.getReadMessages());
        super.channelWritabilityChanged(ctx);
    }

    public long getWrittenMessages() {
        return writtenMessages.get();
    }

    public long getReadMessages() {
        return readMessages.get();
    }
}
