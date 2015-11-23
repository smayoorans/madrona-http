package org.madrona.http.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Http response handler which implements SimpleChannelInboundHandler that process incoming data.
 */
public class ResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger LOGGER = LogManager.getLogger(ResponseHandler.class);

    private ResponseNotifier responseNotifier;

    public ResponseHandler(ResponseNotifier notifier) {
        responseNotifier = notifier;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        LOGGER.info("Channel0 Reading message {} ", msg.getClass());
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.err.println("STATUS: " + response.getStatus());
            System.err.println("VERSION: " + response.getProtocolVersion());
            System.err.println();

            if (!response.headers().isEmpty()) {
                for (String name : response.headers().names()) {
                    for (String value : response.headers().getAll(name)) {
                        System.err.println("HEADER: " + name + " = " + value);
                    }
                }
                System.err.println();
            }

            if (HttpHeaders.isTransferEncodingChunked(response)) {
                System.err.println("CHUNKED CONTENT {");
            } else {
                System.err.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            System.err.print(content.content().toString(CharsetUtil.UTF_8));
            System.err.flush();

            if (content instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
//                ctx.close();
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        responseNotifier.networkErrorOccurred(cause);
        ctx.close();
        try {
            super.exceptionCaught(ctx,cause);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
