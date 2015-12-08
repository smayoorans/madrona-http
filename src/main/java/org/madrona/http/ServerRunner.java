package org.madrona.http;


import org.madrona.http.server.NettyServer;


/**
 * Test class for start the server
 *
 * @author Mayooran
 */

public class ServerRunner {

    private static final int PORT = 8083;

    public static void main(String[] args) {

        NettyServer server = new NettyServer(PORT);

        if (!server.start()) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });
    }


}
