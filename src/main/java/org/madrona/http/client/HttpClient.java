package org.madrona.http.client;


import io.netty.handler.codec.http.HttpRequest;

/**
 * Http Client
 *
 * @author Mayooran
 */

public interface HttpClient {

    void createConnection(String host, int port, int noOfConnections);

    boolean isConnected();

    boolean send(HttpRequest request);

    void close();

}
