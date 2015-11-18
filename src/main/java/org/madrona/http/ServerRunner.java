package org.madrona.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.madrona.http.server.NettyServer;


/**
 * Test class for start the server
 *
 * @author Mayooran
 */

public class ServerRunner {

    private static final Logger LOGGER = LogManager.getLogger(ServerRunner.class);

    private static final int PORT = 8080;

    private NettyServer server;

    public static void main(String[] args) {

        ServerRunner serverRunner = new ServerRunner();

        serverRunner.server = new NettyServer(PORT);

        if (!serverRunner.server.start()) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                serverRunner.server.shutdown();
            }
        });
    }


}
