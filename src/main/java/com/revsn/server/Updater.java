package com.revsn.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.jsoup.Connection.Base;

public class Updater {
    private int method;

    private String output;

    private String[] address = new String[2];
    private String password;

    private SecretKey key;
    private IvParameterSpec iv;

    public Updater(String ip, int port, String password) {
        address[0] = ip;
        address[1] = Integer.toString(port);
        this.password = password;

        method = 1;
    }

    public Updater(String ip, int port) {
        address[0] = ip;
        address[1] = Integer.toString(port);

        method = 2;
    }

    public Updater(String password) {
        this.password = password;

        method = 3;
    }

    public SecretKey generateKeyFromGivenPassAndSalt(String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");

        return key;
    }

    public SecretKey generateKeyRandom(int n) throws NoSuchAlgorithmException {
        if(this.method == 1 || this.method == 3) {
            System.out.println("Use method 2 next time douchebag");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES/CBC/PKCS5Padding");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }


    public IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public String generateOutputString() {
        output = null;
        output = address[0] + ";" + address[1] + ";" + Base64.getEncoder().encodeToString(key.getEncoded()) + ";" + Base64.getEncoder().encodeToString(iv.getIV());
        return output;
    }

    public boolean writeOut() throws IOException {
        try {
            Writer fileWriter = new FileWriter("output.txt");
            fileWriter.write(output);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public void setKey(SecretKey key) {
        this.key = key;
    }

    public void setIv(IvParameterSpec iv) {
        this.iv = iv;
    }

}
