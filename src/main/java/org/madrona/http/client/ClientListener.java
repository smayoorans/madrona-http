package org.madrona.http.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientListener extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LogManager.getLogger(ClientListener.class);

    private final ResponseHandler responseHandler;
    private final AtomicInteger counter = new AtomicInteger();
    private String channelId;
    private NettyHttpClient nettyHttpClient;

    public ClientListener(ResponseHandler responseHandler, String channelId, NettyHttpClient nettyHttpClient) {
        this.nettyHttpClient = nettyHttpClient;
        this.channelId = channelId;
        this.responseHandler = responseHandler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        this.counter.incrementAndGet();
        HttpResponse response = (HttpResponse) e.getMessage();
        this.responseHandler.messageReceived(response);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        logger.info("CLIENT-HANDLER :: Channel closed, received " + this.counter.get() + " messages: " + e.getChannel());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.error("Channel disconnected  [{}]", channelId);
        this.nettyHttpClient.notifyChannelTermination(this.channelId);
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Error occurred in client  : " + e.getCause());
        this.responseHandler.notifyError();
        super.exceptionCaught(ctx, e);
    }
}
