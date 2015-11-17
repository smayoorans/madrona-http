package org.madrona.http;

import org.madrona.http.server.NettyServer;
import org.madrona.http.server.HttpServerHandler;
import org.madrona.http.server.ServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Test class for start the server
 *
 * @author Mayooran
 */

public class ServerRunner {

    private static final Logger LOGGER = LogManager.getLogger(ServerRunner.class);

    private static final int PORT = 1234;

    public static void main(String[] args) {

        final NettyServer server = new NettyServer(PORT, null);

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



}
