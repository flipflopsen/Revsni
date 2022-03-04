package com.revsni.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Listener implements Runnable{
    Logger logger = LogManager.getLogger(getClass());

    Map<String, Integer> handlerinos = new ConcurrentHashMap<>();
    
    public volatile int sessionNumber;
    private ServerSocket serverSocket;
    private Server server;

    public Listener(Server server, int sessionNumber) {
        this.server = server;
        this.sessionNumber = sessionNumber;

        try {
            serverSocket = new ServerSocket(server.getPort());
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

                server.addHandlerinoSess(connection.getInetAddress().getHostAddress(), connection.getLocalPort(),connectionHandler, sessionNumber);
                connectionHandler.callAddSessionHandler();

                
                //Add connection somehow to Server

                logger.info("Connection received from " + connection.getInetAddress().getHostName() + ":" + connection.getLocalPort());
                server.addToPrint(sessionNumber);
                sessionNumber++;

            }
            catch (IOException e) {
                logger.info("Error in Thread");
                server.setRunning(false);
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
