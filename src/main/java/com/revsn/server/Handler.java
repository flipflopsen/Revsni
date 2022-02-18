package com.revsn.server;

import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;


public class Handler {
	
    private Server server;
    private Socket connection;
    private Base64OutputStream out;
    private CipherInputStream ciIn;
    private ObjectInputStream dataIn;
    private Protocol protocol;
    private Cipher cipherDec;
    private Cipher cipherEnc;
    private SecretKeySpec spec;


    public Handler(Server server, Socket connection) {
        this.connection = connection;
        this.server = server;
        try {
            this.spec = new SecretKeySpec(server.getKey().getEncoded(), "AES");
            cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherDec.init(Cipher.DECRYPT_MODE, spec, server.getIv());

            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherEnc.init(Cipher.ENCRYPT_MODE, server.getKey(), server.getIv());

            System.out.println("Hello I am the handler!");
            protocol = new Protocol();

            dataIn = new ObjectInputStream(connection.getInputStream());
            //ciIn = new CipherInputStream(new Base64InputStream(connection.getInputStream()), cipherDec);
            //out = new Base64OutputStream(new CipherOutputStream(connection.getOutputStream(), cipherEnc));
            //out.flush();

            //System.out.println(ciIn.readAllBytes());
            Object object = dataIn.readObject();

            System.out.println(object);
            if (object instanceof String) {
                String message = new String(cipherDec.doFinal(Base64.getDecoder()
                .decode((String) object)));
                protocol.processMessage(message);
            }

        } 
        catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}