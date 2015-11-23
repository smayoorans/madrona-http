package org.madrona.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.madrona.http.common.MessageCounter;

/**
 * Netty Http Server which receives the http requests.
 */
public class NettyServer {

    private static final Logger LOGGER = LogManager.getLogger(NettyServer.class);

    private ServerBootstrap bootstrap;

    private int port = 8080;

    private Channel channel;

    public NettyServer(final int port) {
        this.port = port;
        this.bootstrap = new ServerBootstrap();
    }

    public boolean start() {
        LOGGER.info("Starting the HTTP Server on port [{}]", port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Configure the server.
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ServerInitializer());

            channel = bootstrap.bind(port).sync().channel();
            LOGGER.info("Server bound on port [{}]", port);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting the server on port [{}], [{}]", port, e);
            return false;
        }

    }

    public void shutdown() {
        try {
            LOGGER.info("Stopping http server, which is running on port [{}]", port);
            channel.close();
            bootstrap.group().shutdownGracefully();
            LOGGER.info("Stopped http server, which was running on port [{}]", port);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while stopping the server {} ", ex);
        }
    }
}
