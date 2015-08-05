package hms.globe.connector.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http Server Listener
 */
public class ServerListener extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER = LogManager.getLogger(ServerListener.class);

    private final AtomicInteger counter = new AtomicInteger();

    private final ServerHandler serverHandler;

    private Channel channel;

    public ServerListener(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        LOGGER.info("Request received at server [{}]", e.getMessage());
        HttpRequest request = (HttpRequest) e.getMessage();
        this.counter.incrementAndGet();
        this.serverHandler.messageReceived(this, request);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        LOGGER.info("Channel connected with the client " + ctx.getChannel());
        this.channel = ctx.getChannel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOGGER.error("Error occurred in server : " + e.getCause());
        super.exceptionCaught(ctx, e);
    }

    public void writeResponse(HttpRequest request) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer("Hello", CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        this.channel.write(response);

        // Close the non-keep-alive connection after the write operation is done.
        /*if (!keepAlive) {
            c.addListener(ChannelFutureListener.CLOSE);
        }*/
    }
}
