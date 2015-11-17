package org.madrona.http.server;


public interface ServerHandler {

    void messageReceived(HttpServerHandler httpServerHandler);

    void connectionOpen(HttpServerHandler httpServerHandler);

}
