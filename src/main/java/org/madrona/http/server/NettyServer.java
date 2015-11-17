package org.madrona.http.server;

import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.madrona.http.common.MessageCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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


    public NettyServer(int port, ServerHandler serverHandler) {
        this.port = port;
        this.listener = serverHandler;
    }

    public boolean start() {
        LOGGER.info("Starting the HTTP Server on port [{}]", port);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            channel = b.bind(port).sync().channel();

            return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }



    }

    public void stop() {
        try {
            LOGGER.info("Stopping http server, which is running on port [{}]", port);
            channel.closeFuture().sync();
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
            LOGGER.info("Stopped http server, which was running on port [{}]", port);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while stopping the server");
        }
    }
}
