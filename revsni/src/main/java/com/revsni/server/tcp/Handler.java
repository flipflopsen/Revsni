package com.revsni.server.tcp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import com.revsni.common.Configuration.Mode;
import com.revsni.server.Interaction;
import com.revsni.server.Protocol;
import com.revsni.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handler implements Interaction{
    private static final Logger logger = LogManager.getLogger(Handler.class);
    
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

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

        dataIn = new DataInputStream(connection.getInputStream());
        dataOut = new DataOutputStream(connection.getOutputStream());
        dataOut.flush();
        receiveMessages();

    } 

    public void initHandlerJava() {

    }

    public void initHandlerCS() {
        
    }
    
    public void checkMem() {
        Runtime runtime = Runtime.getRuntime();  

        long maxMemory = runtime.maxMemory();  
        long allocatedMemory = runtime.totalMemory();  
        long freeMemory = runtime.freeMemory();  

        System.out.println("free memory: " + freeMemory / 1024);  
        System.out.println("allocated memory: " + allocatedMemory / 1024);  
        System.out.println("max memory: " + maxMemory /1024);  
        System.out.println("total free memory: " +   
        (freeMemory + (maxMemory - allocatedMemory)) / 1024);  
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
        } else if(in.equals("httpSw") || in.equals("httpsSw")) {
            sendMessage(in);
        } else {
            sendMessage(in);
            receiveMessages();
        }
    }

    private void receiveMessages(){
        try {
            int len = dataIn.readInt();
            byte[] bytes = new byte[len];
            dataIn.read(bytes, 0, len);
            String received = new String(bytes, StandardCharsets.UTF_8);
            if(received != null) {
                protocol.processMessage(received, connection.getInetAddress().getHostAddress(), sessionNumber);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        String toSend = protocol.prepareMessage(new String(message.getBytes(), StandardCharsets.UTF_8), sessionNumber);
        byte[] toSendBytes = toSend.getBytes(StandardCharsets.UTF_8);
        try {
            logger.info("Trying to write...");
            dataOut.writeInt(toSendBytes.length);
            dataOut.write(toSendBytes, 0, toSendBytes.length);
        } catch (IOException e) {
            logger.error("Error while sending message: '{}', to: {} ", message, sessionNumber);
            e.printStackTrace();
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