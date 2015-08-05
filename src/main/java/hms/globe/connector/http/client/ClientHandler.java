package hms.globe.connector.http.client;


import org.jboss.netty.handler.codec.http.HttpResponse;

public interface ClientHandler {

    void messageReceived(HttpResponse response);

    void notifyError();

}
