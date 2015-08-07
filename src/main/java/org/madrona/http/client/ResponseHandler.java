package org.madrona.http.client;


import org.jboss.netty.handler.codec.http.HttpResponse;

public interface ResponseHandler {

    void messageReceived(HttpResponse response);

    void notifyError();

}
