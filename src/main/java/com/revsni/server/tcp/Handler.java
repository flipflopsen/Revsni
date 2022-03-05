package com.revsni.server.tcp;

import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import com.revsni.common.Configuration.Mode;
import com.revsni.server.Interaction;
import com.revsni.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handler implements Interaction{
    private static final Logger logger = LogManager.getLogger(Handler.class);


    //private Logger LOG = parentLogger;
	
    private ObjectInputStream dataIn;
    private ObjectOutputStream dataOut;

    private int sessionNumber;

    private Server server;
    private Socket connection = null;

    private final Mode mode = Mode.TCP;

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
                protocol.processMessage((String) object, connection.getInetAddress().getHostAddress(), sessionNumber);
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

    public void sendCommand(String arg) {
        String in = (String) arg;
        if(in.equals("exit")) {
            server.removeSession(sessionNumber);
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
                    done = protocol.processMessage(message, connection.getInetAddress().getHostAddress(), sessionNumber);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                done = true;
                String error = "Connection Was Lost While Reading - " + connection.getRemoteSocketAddress();
                logger.info(error);
                server.removeSession(sessionNumber);
            }
        }
    }

    public void callAddSessionHandler() {
        Server.addSession(protocol.getuuid(), protocol.getOs(), sessionNumber);
    }

    public int getPort() {
        return connection.getPort();
    }

    public int getSessionNumber() {
        return this.sessionNumber;
    }

    public Mode getMode() {
        return this.mode;
    }

    public ObjectInputStream getDataIn() {
        return this.dataIn;
    }

    public void setDataIn(ObjectInputStream dataIn) {
        this.dataIn = dataIn;
    }

    public ObjectOutputStream getDataOut() {
        return this.dataOut;
    }

    public void setDataOut(ObjectOutputStream dataOut) {
        this.dataOut = dataOut;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Socket getConnection() {
        return this.connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }



    public Protocol getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }


}