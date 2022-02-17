package com.revsn.server;

import java.io.*;
import java.net.Socket;


public class Handler {
	
    private Server server;
    private Socket connection = null;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Protocol protocol;


    public Handler(Server server, Socket connection) {
        this.connection = connection;
        this.server = server;
        try {
            protocol = new Protocol();
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();

            Object object = in.readObject();
            if (object instanceof String) {
                String message = (String) object;
                protocol.processMessage(message);
            }

        } 
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}