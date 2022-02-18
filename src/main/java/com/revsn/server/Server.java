package com.revsn.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private boolean running;
    private String salt;
    private String pass;
    private String ip;
    private int port;
    private SecretKey key;
    private IvParameterSpec iv;
    private Cipher cipher;

    protected static final Logger parentLogger = LogManager.getLogger();
    private Logger LOG = parentLogger;


    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Server server = new Server();
        server.initServer("127.0.0.1", 1331, "lol123", "lol123");

        final Listener listener = new Listener(server);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        server.startServer();
        listener.stopListening();

    }

    public boolean initServer(String ip, int port, String password, String saltForPass) {
        this.port = port;
        this.ip = ip;
        this.pass = password;
        this.salt = saltForPass;

        Updater updater = new Updater(ip, port, password);

        try {
            //this.key = updater.generateKeyFromGivenPassAndSalt(saltForPass);
            this.key = updater.generateKeyRandom(128);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return false;
        }
        this.iv = updater.generateIv();

        updater.setKey(key);
        updater.setIv(iv);

        updater.generateOutputString();
        try {
            if(updater.writeOut()) {
                System.out.println("File wrote!");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startServer() {
        while(isRunning()) {
            try {
                String message;
                do {
                    //printMenu();
                    message = "Nice";
                    //InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                    //BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    //CipherInputReader
                    //Base64 stuff
                    //message = bufferedReader.readLine();
                    /*
                    cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
            
                    String encoded = new String(cipher.doFinal(Base64.getEncoder()
                    .encode(message.getBytes())));

                    System.out.println(encoded);

                    */



                    //if (message.equals("quit")){
                        //Handling notify Observers
                    //}
                    //else {
                        //Handling notify Observers
                    //}
                } 
                while(!message.equals("quit"));
            }
            finally {
                setRunning(false);
                System.out.println("Server is shutting down");
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SecretKey getKey() {
        return this.key;
    }
    public IvParameterSpec getIv() {
        return this.iv;
    }

}
