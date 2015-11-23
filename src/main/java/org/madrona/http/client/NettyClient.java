package org.madrona.http.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class NettyClient {

    private static final Logger LOGGER = LogManager.getLogger(NettyClient.class);

    private Bootstrap bootstrap;

    private Channel channel;

    private int timeoutInMillis = 0;

    private ResponseNotifier responseNotifier;

    public void init(final String host, final int port) {
        LOGGER.info("Initializing Netty Http Client");
        int workerThreads = Runtime.getRuntime().availableProcessors() * 4;
//        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads, new TF());
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            bootstrap = new Bootstrap();
  /*          bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
            if (timeoutInMillis != 0) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutInMillis);
            }
*/
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(responseNotifier));

            /** Make the connection attempt */
            channel = bootstrap.connect(host, port).sync().channel();
            /*if(future.isSuccess()){
                channel = future.channel();
            } else {
                LOGGER.error("Couldn't connect to server " + future.isSuccess());
            }*/
            LOGGER.info("Client connected " + channel.isActive());

        } catch (Exception e) {
            LOGGER.error("Error occurred while binding port [{}] ", e);
        }

    }

    public boolean send(final String uri) {
        LOGGER.info("Sending http request [{}] ", uri);
        try {
            HttpRequest request = createRequest(new URI(uri));
            ChannelFuture future = channel.writeAndFlush(request);
            System.out.println("=====>" + future.sync());
        } catch (Exception e) {
            LOGGER.error("Error occurred in request {} ", e);
        }
        return true;
    }


    private static HttpRequest createRequest(URI uri) {
        String url = StringUtils.isBlank(uri.getRawPath()) ? "/" : uri.getRawPath();
        if (StringUtils.isNotBlank(uri.getRawQuery())) {
            url += "?" + uri.getRawQuery();
        }
//        System.out.println("url " + url);
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url);
        request.headers().add(Names.HOST, uri.getHost()+":"+uri.getPort());
        request.headers().add(Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().add(Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        System.out.println("======request===");
        System.out.println("======request===" + request);
        return request;
    }


    /**
     * Shut down any running connections
     */
    public void shutdown() {
        LOGGER.info("Stopping Netty Http Client");
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EventLoopGroup group = bootstrap.group();
        if (group != null) {
            group.shutdownGracefully(0, 10, TimeUnit.SECONDS);
            if (!group.isTerminated()) {
                group.shutdownGracefully();
            }
        }
        LOGGER.info("Stopped Netty Http Client");
    }

    public void setTimeoutInMillis(int timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
    }

    public void setResponseNotifier(ResponseNotifier responseNotifier) {
        this.responseNotifier = responseNotifier;
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
