package org.madrona.http.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Http response handler which implements SimpleChannelInboundHandler that process incoming data.
 */
public class ResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger LOGGER = LogManager.getLogger(ResponseHandler.class);

    private final AtomicLong writtenMessages;
    private final AtomicLong readMessages;


    private ResponseNotifier responseNotifier;

    public ResponseHandler(ResponseNotifier notifier) {
        this.writtenMessages = new AtomicLong();
        this.readMessages = new AtomicLong();
        responseNotifier = notifier;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        this.readMessages.incrementAndGet();

        LOGGER.debug("Channel Reading message {} ", msg.getClass());
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            this.responseNotifier.messageReceived(response);

            if (HttpHeaders.isTransferEncodingChunked(response)) {
//                System.err.println("CHUNKED CONTENT {");
            } else {
//                System.err.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

//            System.err.print(content.content().toString(CharsetUtil.UTF_8));
//            System.err.flush();

            if (content instanceof LastHttpContent) {
//                System.err.println("} END OF CONTENT");
            }
        }
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
//        System.out.println("Accept in bound");
        return super.acceptInboundMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        responseNotifier.networkErrorOccurred(cause);
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel unregistered  received response: " + readMessages.get()/2);
        super.channelUnregistered(ctx);
    }
}
