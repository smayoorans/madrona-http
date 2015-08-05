package org.madrona.http.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientListener extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LogManager.getLogger(ClientListener.class);

    private final ClientHandler clientHandler;

    private final AtomicInteger counter = new AtomicInteger();

    public ClientListener(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        this.counter.incrementAndGet();
        HttpResponse response  = (HttpResponse) e.getMessage();
        this.clientHandler.messageReceived(response);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        logger.info("CLIENT-HANDLER :: Channel closed, received " + this.counter.get() + " messages: " + e.getChannel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Error occurred in client  : " + e.getCause());
        this.clientHandler.notifyError();
        super.exceptionCaught(ctx, e);
    }
}