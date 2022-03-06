package com.revsni.server.encryption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RSA implements Encri {
    private static final Logger logger = LogManager.getLogger(RSA.class);
    private PrivateKey privKey;
    private PublicKey pubKey;

    public RSA() {
        
    }

    public void generateKeys() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            privKey = pair.getPrivate();
            pubKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to create RSA Key Pair!");
            e.printStackTrace();
        }
    }

    public void saveKeys() {
        try (FileOutputStream fos = new FileOutputStream("public.key")) {
            fos.write(pubKey.getEncoded());
        } catch (IOException e) {
            logger.error("Failed save RSA Public Key!");
            e.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream("private.key")) {
            fos.write(privKey.getEncoded());
        } catch (IOException e) {
            logger.error("Failed save RSA Private Key!");
            e.printStackTrace();
        }
    }

    public void loadKeys() {
        File publicKeyFile = new File("public.key");
        File privateKeyFile = new File("private.key");
        try {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(privateKeyBytes);
            this.pubKey = keyFactory.generatePublic(publicKeySpec);   
            this.privKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (IOException e) {
            logger.error("Failed to load RSA Keys!");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Failed to generate Key Specs and Keys from RSA Key Files!");
            e.printStackTrace();
        } 
    }

    public String encrypt(String message) {
        return "lul";
    }

    public String decrypt(String encrypted) {
        return "";
    }


}
