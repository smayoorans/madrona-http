package hms.globe.connector.http.server;

import org.jboss.netty.handler.codec.http.HttpRequest;

public interface ServerHandler {

    void messageReceived(ServerListener serverListener, HttpRequest request);

    void connectionOpen(ServerListener serverListener);

}
