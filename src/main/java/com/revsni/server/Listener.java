package com.revsni.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
public class Listener implements Runnable{
    Logger logger = LogManager.getLogger(getClass());
    
    private ServerSocket serverSocket;
    private Server server;

    public Listener(Server server) {
        this.server = server;

        try {
            serverSocket = new ServerSocket(server.getPort());
            server.setRunning(true);
            logger.debug("TCP Socket is up on Port: {}", server.getPort());

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

                Handler connectionHandler = new Handler(server,connection);

                server.addObserver(connectionHandler);
                
                //Add connection somehow to Server

                logger.debug("Connection received from " + connection.getInetAddress().getHostName() + ":" + connection.getLocalPort());

            }
            catch (IOException e) {
                logger.debug("Error in Thread");
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
