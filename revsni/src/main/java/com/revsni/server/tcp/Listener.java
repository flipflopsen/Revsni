package com.revsni.server.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.revsni.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Listener implements Runnable{
    private static final Logger logger = LogManager.getLogger(Listener.class);

    Map<String, Integer> handlerinos = new ConcurrentHashMap<>();
    
    public volatile int sessionNumber;
    private ServerSocket serverSocket;
    private Server server;

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Listener() {
        //Default Constructor for Serialization
    }

    public Listener(Server server, int sessionNumber) {
        this.server = server;
        this.sessionNumber = sessionNumber;

        try {
            serverSocket = new ServerSocket(server.getPort(), 10);
            server.setRunning(true);
            logger.info("TCP Socket is up on Port: {}", server.getPort());

        }
        catch (IOException e) {
            server.setRunning(false);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(server.isRunning()) {
            try {
                Socket connection = serverSocket.accept();

                //Handler connectionHandler = new Handler(server,connection);
                Handler connectionHandler = new Handler(server,connection,sessionNumber);

                server.setInteraction(sessionNumber, connectionHandler);
                server.addSession(connection.getInetAddress().getHostAddress(), connection.getLocalPort(),connectionHandler, sessionNumber);
                connectionHandler.callAddSessionHandler();

                
                //Add connection somehow to Server

                logger.info("Connection received from " + connection.getInetAddress().getHostName() + ":" + connection.getLocalPort());
                server.addToPrint(sessionNumber);
                sessionNumber++;

            }
            catch (IOException e) {
                logger.info("Error in TCP Listener Thread!");
                //server.setRunning(false);
            }
        }
    }

    /**
     * Server shuts down gracefully
     */
    public void stopListening() {
        try {
            serverSocket.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
