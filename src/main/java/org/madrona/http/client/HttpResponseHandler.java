package org.madrona.http.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Http response handler which implements SimpleChannelInboundHandler that
 * process incoming data.
 */
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(HttpResponseHandler.class);


    public HttpResponseHandler() {

    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        LOGGER.info("Accepting inbound message {}", msg);
        return super.acceptInboundMessage(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("Channel Reading message {}", msg);
//        super.channelRead(ctx, msg);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse httpResponse) throws Exception {
        LOGGER.info("Channel0 Reading message {} ", httpResponse);

        byte[] content = null;
        ByteBuf byteBuf = httpResponse.content();
        if (byteBuf.hasArray()) {
            content = byteBuf.array();
        } else {
            content = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(content);
        }
        System.err.println(new String(content));
    }
}
