package org.madrona.http.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
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
        LOGGER.debug("Channel Reading message {} ", msg.getClass());
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            this.responseNotifier.messageReceived(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Error occurred in http netty client", cause.getCause());
        ctx.close();
        responseNotifier.networkErrorOccurred(cause);
        super.exceptionCaught(ctx, cause);
    }
}
