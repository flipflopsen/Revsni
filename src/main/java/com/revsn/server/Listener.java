package com.revsn.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Listener implements Runnable {
    private ServerSocket serverSocket;
    private Server server;

    public Listener(Server server) {
        this.server = server;

        try {
            serverSocket = new ServerSocket(server.getPort());
            server.setRunning(true);
            System.out.println("ServerSocket izda");

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
                System.out.println("Connection local port: " + connection.getLocalPort());

                Handler connectionHandler = new Handler(server,connection);
                
                //Add connection somehow to Server

                System.out.println("Connection received from " + connection.getInetAddress().getHostName());

            }
            catch (IOException e) {
                System.out.println("Error in Thread");
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
