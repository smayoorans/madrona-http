package org.madrona.http.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.madrona.http.common.MessageCounter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class NettyClient {

    private static final Logger LOGGER = LogManager.getLogger(NettyClient.class);

    private Bootstrap bootstrap;

    private Channel channel;

    private List<ChannelContainer> channelContainerList;

    private int timeoutInMillis = 0;

    private ResponseNotifier responseNotifier;

    private AtomicInteger roundRobbinCounter;

    private int numberOfConnections;

    public void init(final String host, final int port) {
        LOGGER.info("Initializing Netty Http Client");
        int workerThreads = Runtime.getRuntime().availableProcessors() * 4;
        roundRobbinCounter = new AtomicInteger(1);
        channelContainerList = new ArrayList<>();
        numberOfConnections = 20;
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads, new TF());

        try {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
            bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            if (timeoutInMillis != 0) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutInMillis);
            }
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(responseNotifier));

            createChannelConnections(host, port);
        } catch (Exception e) {
            LOGGER.error("Error occurred while binding port [{}] ", e);
        }

    }

    public void createChannelConnections(final String host, final int port){
        /** Make the connection attempt */
        for (int i = 0 ; i < numberOfConnections ; i++){
            try {
                channel = bootstrap.connect(host, port).sync().channel();
                String channelId = "channel-".concat(String.valueOf(System.currentTimeMillis()).concat(String.valueOf(i + 1)));
                ChannelContainer channelContainer = new ChannelContainer(channelId, channel);
                channelContainerList.add(channelContainer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /*try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }


    }

    public boolean send(final String uri) {
        LOGGER.debug("Sending http request [{}] ", uri);
        try {
            HttpRequest request = createRequest(new URI(uri));
            ChannelContainer channelContainer = getNextChannel();
            if (channelContainer == null) {
                LOGGER.debug("No active channel found for the request [{}]", request);
                return false;
            }
            if (channelContainer.channel.isActive() && channelContainer.channel.isOpen()) {
                channelContainer.channel.writeAndFlush(request);
                return true;
            }
            LOGGER.debug("Selected channel [{}] is not ready to send messages", channelContainer.id);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error occurred in request {} ", e);
            return false;
        }
    }


    private static HttpRequest createRequest(URI uri) {
        String url = StringUtils.isBlank(uri.getRawPath()) ? "/" : uri.getRawPath();
        if (StringUtils.isNotBlank(uri.getRawQuery())) {
            url += "?" + uri.getRawQuery();
        }
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url);
        request.headers().add(Names.HOST, uri.getHost()+":"+uri.getPort());
        request.headers().add(Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().add(Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        return request;
    }


    /**
     * Shut down any running connections
     */
    public void shutdown() {
        LOGGER.info("Stopping Netty Http Client");
        ChannelFuture closeFuture = channel.close();
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

    /**
     * <p>
     * Returns the next channel. The index is calculated in a round robin fashion.
     * </p>
     *
     * @return Channel
     */
    private ChannelContainer getNextChannel() {
        int count = Math.abs(roundRobbinCounter.incrementAndGet());
        int size = channelContainerList.size();
        if (size < 1) {
            return null;
        }
        int nextIndex = count % size;
        return  channelContainerList.get(nextIndex);
    }

    private static class ChannelContainer {

        String id;

        Channel channel;

        AtomicBoolean isUsable = new AtomicBoolean(false);

//        Queue<RequestDetail> transactionIdQueue;

        Lock channelLock;

        public ChannelContainer(String id, Channel channel) {
            this.id = id;
            this.channel = channel;
            this.channelLock = new ReentrantLock();
        }

        public boolean isChannelExpired(long currentTime, int httpRequestTimeout) {
           /* RequestDetail requestDetail = transactionIdQueue.peek();
            boolean isChannelExpired = false;
            if (requestDetail != null) {
                isChannelExpired = (currentTime - requestDetail.sentTime > httpRequestTimeout);
            }
            return isChannelExpired;*/
            return true;
        }

   /*     public Optional<RequestDetail> getNextTransactionId() {
            return Optional.ofNullable(transactionIdQueue.poll());
        }

        public void putRequestDetail(String transactionId, HtocsMethodType methodType) {
            transactionIdQueue.add(new RequestDetail(transactionId, methodType));
        }*/
    }

}
