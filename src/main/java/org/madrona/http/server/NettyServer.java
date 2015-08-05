package org.madrona.http.server;

import org.madrona.http.common.MessageCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Netty Server implementation
 *
 * @author Mayooran
 */
public class NettyServer {

    private static final Logger LOGGER = LogManager.getLogger(NettyServer.class);

    private final ServerHandler listener;

    private ServerBootstrap bootstrap;

    private final int port;

    private Channel channel;


    public NettyServer(int port, ServerHandler listener) {
        this.port = port;
        this.listener = listener;
    }

    public boolean start() {
        LOGGER.info("Starting the HTTP Server on port [{}]", port);
        Executor bossPool = Executors.newFixedThreadPool(4);
        Executor workerPool = Executors.newFixedThreadPool(4);
        ChannelFactory factory = new NioServerSocketChannelFactory(bossPool, workerPool);

        this.bootstrap = new ServerBootstrap(factory);

        this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new HttpRequestDecoder(),
                        new HttpResponseEncoder(),
                        new LoggingHandler(InternalLogLevel.INFO),
                        new MessageCounter(),
                        new ServerListener(listener));
            }
        });

        channel = this.bootstrap.bind(new InetSocketAddress(port));

        if (channel.isBound()) {
            LOGGER.info("HTTP Server bound to port : [{}]", port);
            return true;
        } else {
            LOGGER.info("HTTP Server failed to bind to port :[{}]", port);
            this.bootstrap.releaseExternalResources();
            return false;
        }
    }

    public void stop() {
        try {
            LOGGER.info("Stopping http server, which is running on port [{}]", port);
            this.channel.close();
            this.bootstrap.releaseExternalResources();
            LOGGER.info("Stopped http server, which was running on port [{}]", port);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while stopping the server");
        }
    }
}
