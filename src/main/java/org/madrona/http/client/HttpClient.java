package org.madrona.http.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.List;

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
