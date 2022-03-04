package com.revsni.server;

import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handler  {
    Logger logger = LogManager.getLogger(getClass());

    //private Logger LOG = parentLogger;
	
    private ObjectInputStream dataIn;
    private ObjectOutputStream dataOut;

    private int sessionNumber;

    private Server server;
    private Socket connection = null;


    private Protocol protocol;


    public Handler(Server server, Socket connection, int sessionNumber) throws IOException {
        this.server = server;
        this.connection = connection;
        this.sessionNumber = sessionNumber;

        try {
            protocol = new Protocol(this.server);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        dataIn = new ObjectInputStream(connection.getInputStream());
        dataOut = new ObjectOutputStream(connection.getOutputStream());
        dataOut.flush();

        Object object;
        try {
            object = dataIn.readObject();
            if (object instanceof String) {
                protocol.processMessage((String) object);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    } 
    

    void sendMessage(String msg) {
        try{
            dataOut.writeObject(msg);
            dataOut.flush();
        }
        catch(IOException ioException) {
            String error = "Connection Was Lost While Writing - " + connection.getRemoteSocketAddress();
            logger.info(error);
        }
    }

    private void closeConnection() {
        logger.info("Closing Connection - " + connection.getRemoteSocketAddress());

        try {
            dataIn.close();
            dataOut.close();
            connection.close();
        }
        catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void update(Object arg) {
        String in = (String) arg;
        if(in.equals("exit")) {
            server.handlerinos.remove(sessionNumber);
            closeConnection();
        } else {
            String message = protocol.prepareMessage((String) arg);
            sendMessage(message);
            receiveMessages();
        }
    }

    private void receiveMessages() {
        logger.info("Waiting for response");

        boolean done = false;
        while (!done) {
            try {
                Object object = dataIn.readObject();
                if (object instanceof String) {
                    String message = (String) object;
                    done = protocol.processMessage(message);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                done = true;
                String error = "Connection Was Lost While Reading - " + connection.getRemoteSocketAddress();
                logger.info(error);
                server.handlerinos.remove(sessionNumber);
            }
        }
    }

   

}