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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        this.readMessages.incrementAndGet();
    }

   /* @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        this.readMessages.incrementAndGet();
        super.messageReceived(ctx, e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        this.writtenMessages.incrementAndGet();
        super.writeRequested(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        System.out.println(ctx.getChannel() + " -> sent: " + this.getWrittenMessages() + ", recv: " + this.getReadMessages());
    }*/

    public long getWrittenMessages() {
        return writtenMessages.get();
    }

    public long getReadMessages() {
        return readMessages.get();
    }
}
