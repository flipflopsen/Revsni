package com.revsni.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
    
    public HTTPServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/outp", new HTTPHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("HTTP Server started on port: 80");
    }

    public HTTPServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/outp", new HTTPHandler());
        server.setExecutor(null); 
        server.start();
        System.out.println("HTTP Server started on port: " + port);
    }
}
