package org.madrona.http.service;



public interface ConnectionChecker {

    void startChecking(String host, int port);

}
