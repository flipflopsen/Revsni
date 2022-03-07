package com.revsni.server;

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

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

public class Updater {

    //private static final Logger logger = LogManager.getLogger(Updater.class);
    //private Logger LOG = parentLogger;
    
    private int method;

    private String output;

    private String[] address = new String[2];
    private String password;
    private String salt;
    private String shellType;

    private SecretKey key;
    private IvParameterSpec iv;

    public Updater(String ip, int port, String password, String salt) {
        address[0] = ip;
        address[1] = Integer.toString(port);
        this.password = password;
        this.salt = salt;
        this.shellType = "TCP";

        method = 1;
    }

    public Updater(String password) {
        this.password = password;
        //To update AES Encryption only
        method = 3;
        this.shellType = "TCP";
    }

    public SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), this.salt.getBytes(), 65536, 256);
        SecretKeySpec key = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");
        return key;
    }

    public SecretKey generateKeyRandom(int n) throws NoSuchAlgorithmException {
        if(this.method == 1 || this.method == 3) {
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
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
        output = address[0] + ";" + address[1] + ";" + Base64.getEncoder().encodeToString(shellType.getBytes()) + ";" + Base64.getEncoder().encodeToString(key.getEncoded()) + ";" + Base64.getEncoder().encodeToString(iv.getIV());
        return output;
    }

    public boolean writeOut() throws IOException {
        try {
            Writer fileWriter = new FileWriter("revsni/filehosting/initial.txt");
            fileWriter.write(output);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean writeOut(String filename) throws IOException {
        try {
            Writer fileWriter = new FileWriter("revsni/filehosting/" + filename + ".txt");
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

    public void setShellType(String type, String port) {
        this.shellType = type;
        this.address[1] = port;
    }

}
