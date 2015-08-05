package org.madrona.http;

import org.madrona.http.server.NettyServer;
import org.madrona.http.server.ServerListener;
import org.madrona.http.server.ServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Test class for start the server
 *
 * @author Mayooran
 */

public class ServerRunner {

    private static final Logger LOGGER = LogManager.getLogger(ServerRunner.class);

    private static final int PORT = 1234;

    public static void main(String[] args) {

        final NettyServer server = new NettyServer(PORT, new ServerHandlerImpl());

        if (!server.start()) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
            }
        });
    }


    private static class ServerHandlerImpl implements ServerHandler {

        @Override
        public void messageReceived(ServerListener serverListener, HttpRequest request) {
            LOGGER.info("Http Request received" + request);
            serverListener.writeResponse(request);
            LOGGER.info("Sending response back to client ");
        }

        @Override
        public void connectionOpen(ServerListener serverListener) {
            LOGGER.info("opened & ready to send/receive data.");
        }

    }
}
