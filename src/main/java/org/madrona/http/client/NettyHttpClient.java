package org.madrona.http.client;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class NettyHttpClient {

    private static final Logger LOGGER = LogManager.getLogger(NettyHttpClient.class);

    private ClientBootstrap bootstrap;

    private int port = 1234;

    private String host = "127.0.0.1";

    private String clientId;

    private int numberOfConnections = 3;

    private AtomicInteger roundRobbinCounter;

    private AtomicBoolean isConnectionInitDone;

    private int nettyBossPoolSize = 1;

    private int nettyWorkerPoolSize = 4;

    private List<ChannelContainer> channelContainers;

    private Lock connectionReadLock;

    private Lock connectionWriteLock;

    private ScheduledExecutorService channelConnectionChecker;

    private long connectionCheckerInterval = 1000;

    private long initialDelay = 5000;


    public void init(final ResponseHandler responseHandler) {

        LOGGER.info("Initializing Netty client");
        channelConnectionChecker = Executors.newScheduledThreadPool(1);

        roundRobbinCounter = new AtomicInteger(1);
        channelContainers = Collections.synchronizedList(new ArrayList<>());

        ReadWriteLock channelConnectivityLock = new ReentrantReadWriteLock(true);
        connectionReadLock = channelConnectivityLock.readLock();
        connectionWriteLock = channelConnectivityLock.writeLock();

        Executor bossPool = Executors.newFixedThreadPool(nettyBossPoolSize);
        Executor workerPool = Executors.newFixedThreadPool(nettyWorkerPoolSize > numberOfConnections ? nettyWorkerPoolSize : numberOfConnections);

        ChannelFactory factory = new NioClientSocketChannelFactory(bossPool, workerPool);

        this.bootstrap = new ClientBootstrap(factory);
        this.bootstrap.setPipelineFactory(createChannelPipelineFactory());

        this.isConnectionInitDone = new AtomicBoolean(false);

        establishConnections(numberOfConnections, responseHandler);
    }

    private void scheduleNextConnectionCheck(ResponseHandler responseHandler) {
        channelConnectionChecker.schedule(() -> {
            int missingConnectionCount = 0;
            missingConnectionCount = numberOfConnections - channelContainers.size();
            if (missingConnectionCount > 0) {
                LOGGER.info("Missing connection count : [{}]", missingConnectionCount);
                establishConnections(missingConnectionCount, responseHandler);
            } else {
                scheduleNextConnectionCheck(responseHandler);
            }

        }, initialDelay, MILLISECONDS);
    }

    public void destroy() {
        LOGGER.info("Stopping NSN netty client.");
        connectionWriteLock.lock();
        try {
            for (ChannelContainer channelContainer : channelContainers) {
                channelContainer.channel.close().awaitUninterruptibly();
            }
            bootstrap.releaseExternalResources();
        } finally {
            connectionWriteLock.unlock();
        }
    }

    private void establishConnections(int connectionCount, ResponseHandler responseHandler) {
        LOGGER.info("Establishing connections upto the count [{}]", connectionCount);
        AtomicInteger pendingCount = new AtomicInteger(connectionCount);
        for (int i = 0; i < connectionCount; i++) {
            String channelId = "netty-channel-"
                    .concat(String.valueOf(System.currentTimeMillis()))
                    .concat(String.valueOf(i + 1));
            bootstrapConnection(channelId, pendingCount, responseHandler);
        }
    }


    private void bootstrapConnection(String channelId, AtomicInteger pendingCount, ResponseHandler responseHandler) {
        this.bootstrap.connect(new InetSocketAddress(host, port)).addListener(channelFuture -> {
            if (!channelFuture.isSuccess()) {
                LOGGER.info("Netty channel connection attempt failed.");
                channelFuture.getChannel().close();
            } else {
                Channel channel = channelFuture.getChannel();
                channel.getPipeline().addLast("response-handler", new ClientListener(responseHandler, channelId, this));
                ChannelContainer channelContainer = new ChannelContainer(channelId, channel);
                channelContainer.isUsable.set(true);
                connectionWriteLock.lock();
                try {
                    channelContainers.add(channelContainer);
                } finally {
                    connectionWriteLock.unlock();
                }
                LOGGER.info("Netty channel connection attempt succeeded");
            }
            if (pendingCount.decrementAndGet() < 1) {
                LOGGER.debug("Scheduling next connection checker job");
                scheduleNextConnectionCheck(responseHandler);
            }
        });
    }


    public boolean send(final HttpRequest request) {
//        LOGGER.debug("Sending GET request [{}] ", request);
        try {
            return writeToChannel(request);
        } catch (Exception e) {
            LOGGER.debug("Error occurred in request");
            return false;
        }
    }

    private boolean writeToChannel(HttpRequest request) {
        ChannelContainer channelContainer = getNextChannel();
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
        return false;
    }

    private ChannelContainer getNextChannel() {
        int count = Math.abs(roundRobbinCounter.incrementAndGet());
        int size = channelContainers.size();
        if (size < 1) {
            return null;
        }
        int nextIndex = count % size;
        return channelContainers.get(nextIndex);
    }


    private ChannelPipelineFactory createChannelPipelineFactory() {
        return (() -> Channels.pipeline(new HttpClientCodec()));
    }


    public void notifyChannelTermination(String channelId) {
        Optional<ChannelContainer> channelContainerOpt = getChannel(channelId);
        LOGGER.info("Channel termination notification received for channel id [{}]", channelId);
        channelContainerOpt.ifPresent((channelContainer -> {
            if (channelContainer.isUsable.compareAndSet(true, false)) {
                connectionWriteLock.lock();
                try {
                    channelContainers.remove(channelContainer);
                } finally {
                    connectionWriteLock.unlock();
                }
            }
        }));
    }


    private Optional<ChannelContainer> getChannel(String channelId) {
        connectionReadLock.lock();
        try {
            for (ChannelContainer container : channelContainers) {
                if (container.id.equals(channelId)) {
                    return Optional.ofNullable(container);
                }
            }
            return Optional.ofNullable(null);
        } finally {
            connectionReadLock.unlock();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setNumberOfConnections(int numberOfConnections) {
        this.numberOfConnections = numberOfConnections;
    }

    public void setNettyBossPoolSize(int nettyBossPoolSize) {
        this.nettyBossPoolSize = nettyBossPoolSize;
    }

    public void setNettyWorkerPoolSize(int nettyWorkerPoolSize) {
        this.nettyWorkerPoolSize = nettyWorkerPoolSize;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setConnectionCheckerInterval(long connectionCheckerInterval) {
        this.connectionCheckerInterval = connectionCheckerInterval;
    }

    private static class ChannelContainer {

        String id;

        Channel channel;

        AtomicBoolean isUsable = new AtomicBoolean(false);

        AtomicBoolean isChannelExpired;

        Lock channelLock;

        public ChannelContainer(String id, Channel channel) {
            this.id = id;
            this.channel = channel;
            this.isChannelExpired = new AtomicBoolean(false);
            this.channelLock = new ReentrantLock();
        }
    }
}
