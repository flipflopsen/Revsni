package com.revsni.server.http;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext; 

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;

public class HTTPServer {
    public static HttpServer server;
    int port;
    public HTTPHandler handler;
    
    public void main(int port, HTTPHandler handler) {
        try {
            server.start();
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            server.shutdown(5, TimeUnit.SECONDS);
          }
        });
    }

    public void setup(int port) {
        SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(15000)
            .setTcpNoDelay(true)
            .build();
        SSLContext sslcontext = null;
        server = ServerBootstrap.bootstrap()
            .setListenerPort(port)
            .setServerInfo("Test/1.1")
            .setSocketConfig(socketConfig)
            .setSslContext(sslcontext)
            .registerHandler("*", this.handler)
            .create();
    }

    public void setHandler(HTTPHandler handler) {
        this.handler = handler;
    }

    public HTTPHandler geHandler() {
        return this.handler;
    }

    public void addCommand(String command) {
        this.handler.addCommand(command);
    }
}
