package com.revsn.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Server {
    private boolean running;
    private int port;

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {


    }
    public boolean initServer(String ip, int port, String password, String saltForPass) {
        this.port = port;
        Updater updater = new Updater(ip, port, password);

        SecretKey key;
        try {
            key = updater.generateKeyFromGivenPassAndSalt(saltForPass);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
            e1.printStackTrace();
            return false;
        }
        IvParameterSpec iv = updater.generateIv();

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

                    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    //CipherInputReader
                    //Base64 stuff
                    message = bufferedReader.readLine();

                    //message.decrypt


                    if (message.equals("quit")){
                        //Handling notify Observers
                    }
                    else {
                        //Handling notify Observers
                    }
                } 
                while(!message.equals("quit"));
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
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
}
