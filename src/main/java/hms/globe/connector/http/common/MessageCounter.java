package hms.globe.connector.http.common;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.atomic.AtomicLong;

public class MessageCounter extends SimpleChannelHandler {

    private final AtomicLong writtenMessages;
    private final AtomicLong readMessages;

    public MessageCounter() {
        this.writtenMessages = new AtomicLong();
        this.readMessages = new AtomicLong();
    }

    @Override
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
    }

    public long getWrittenMessages() {
        return writtenMessages.get();
    }

    public long getReadMessages() {
        return readMessages.get();
    }
}
