package org.madrona.http.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class NettyHttpClient {

    private static final Logger LOGGER = LogManager.getLogger(NettyHttpClient.class);

    private static final int PORT = 1234;

    private static final String HOST = "localhost";

    private Bootstrap bootstrap;

    private int nettyBossPoolSize = 1;

    private int nettyWorkerPoolSize = 4;


    private Channel channel;

    public void init() {
        LOGGER.info("Initializing Netty Http Client");
        Executor bossPool = Executors.newFixedThreadPool(nettyBossPoolSize);
        Executor workerPool = Executors.newFixedThreadPool(nettyWorkerPoolSize);

        int workerThreads = Runtime.getRuntime().availableProcessors() * 4;
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads, new TF());

        try {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpClientInitializer());

            /** Make the connection attempt */
            channel = bootstrap.connect(HOST, PORT).sync().channel();

        } catch (InterruptedException e) {
            LOGGER.error("Error occurred while binding port [{}] ", e);
        }

    }

    private static HttpRequest createRequest(URI uri) {
        String url = StringUtils.isBlank(uri.getRawPath()) ? "/" : uri.getRawPath();
        if (StringUtils.isNotBlank(uri.getRawQuery())) {
            url += "?" + uri.getRawQuery();
        }

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url);
        request.headers().add("host", uri.getHost());
        return request;
    }


    public void destroy() {
        LOGGER.info("Stopping Netty Http Client.");

    }


    public boolean send(final HttpRequest request) {
        LOGGER.debug("Sending http request [{}] ", request);
        try {
            return writeToChannel(request);
        } catch (Exception e) {
            LOGGER.debug("Error occurred in request");
            return false;
        }
    }

    private boolean writeToChannel(HttpRequest request) {

        /*ChannelContainer channelContainer = getNextChannel();
        if (channelContainer == null) {
            LOGGER.debug("No active channel found for the request [{}]");
            return false;
        }
        if (channelContainer.isUsable.get() && channelContainer.channel.isConnected()) {
            LOGGER.info("Sending channel {} ", channelContainer.channel);
            channelContainer.channel.write(request);
            return true;
        }
        LOGGER.debug("Selected channel [{}] is not ready to send messages", channelContainer.id);
        return false;*/
        channel.write(request);
        return true;
    }

    public static class TF implements ThreadFactory {
        private int count = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "HttpClient event loop" + ++count);
            thread.setDaemon(true);
            return thread;
        }
    }

}
