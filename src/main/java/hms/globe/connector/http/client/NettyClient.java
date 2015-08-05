package hms.globe.connector.http.client;

import hms.globe.connector.http.common.MessageCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NettyClient implements HttpClient {

    private static final Logger LOGGER = LogManager.getLogger(NettyClient.class);

    private ClientBootstrap bootstrap;

    private int bossThreadCount = 1;

    private int workerThreadCount = 3;

    private Iterator<Channel> channelIterator;

    private List<Channel> channelHolder;

    public NettyClient(final ClientHandler clientHandler) {
        LOGGER.info("Initializing netty client bootstrap with " +
                "boss thread count [{}] and worker thread count [{}]", bossThreadCount, workerThreadCount);

        Executor bossPool = Executors.newFixedThreadPool(bossThreadCount);
        Executor workerPool = Executors.newFixedThreadPool(workerThreadCount);

        this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossPool, workerPool));

        this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new MessageCounter(),
                        new HttpClientCodec(),
                        new ClientListener(clientHandler));
            }
        });
        LOGGER.debug("Initialized client bootstrap with call back handler");
    }

    public void createConnection(String host, int port, int noOfConnections) {
        LOGGER.info("Creating [{}] number of client channel for host [{}] and port [{}] ",
                noOfConnections, host, port);

        channelHolder = new ArrayList<>();
        for (int i = 0; i < noOfConnections; i++) {

            ChannelFuture future = this.bootstrap.connect(new InetSocketAddress(host, port));

            if (!future.awaitUninterruptibly().isSuccess()) {
                LOGGER.info("Client failed to connect to server at http://{}:{}", host, port);
                this.bootstrap.releaseExternalResources();
            }

            Channel channel = future.getChannel();
            channelHolder.add(channel);
        }
        LOGGER.debug("Client channels created successfully! [{}]", channelHolder);
        channelIterator = channelHolder.iterator();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public boolean send(HttpRequest request) {
        if (!channelIterator.hasNext()) {
            channelIterator = channelHolder.iterator();
        }
        Channel channel = channelIterator.next();
        if (channel.isConnected()) {
            LOGGER.debug("Sending the http request [{}] through channel [{}]" , request, channel);
            channel.write(request);
            LOGGER.info("=================REQUEST SENT TO SERVER==========================");
            LOGGER.info("Http request successfully send to the server");
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        for (Channel channel : channelHolder) {
            if (channel != null) {
                channel.close().awaitUninterruptibly();
                LOGGER.info("client channel connection [{}] has been closed", channel);
            }
        }
        this.bootstrap.releaseExternalResources();

    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }


}
