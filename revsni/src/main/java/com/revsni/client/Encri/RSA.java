package com.revsni.client.Encri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import com.revsni.common.Configuration.EncMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RSA implements Encri {
    private static final Logger logger = LogManager.getLogger(RSA.class);

    private volatile PublicKey pubHost;
    private volatile PrivateKey privKey;
    private Cipher decryptCipher;
    private Cipher encryptCipher;
    private EncMode mode = EncMode.RSA;

    public RSA() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    public void saveKeyPair(String uuid) {
        Base64.Encoder encoder = Base64.getEncoder();
        try (Writer fos = new FileWriter("revsni/client/keys/rsa/public,"+uuid+".key")) {
            fos.write(encoder.encodeToString(new String(encoder.encodeToString(pubHost.getEncoded())).getBytes()));
            //logger.info("Client Public Key ("+uuid+") wrote!");
        } catch (IOException e) {
            logger.error("Failed save Client RSA Public Key!");
            e.printStackTrace();
        }
        try (Writer fos = new FileWriter("revsni/client/keys/rsa/private,"+uuid+".key")) {
            fos.write(encoder.encodeToString(new String(encoder.encodeToString(privKey.getEncoded())).getBytes()));
            //logger.info("Client Private Key ("+uuid+") wrote!");
        } catch (IOException e) {
            logger.error("Failed save Client RSA Private Key!");
            e.printStackTrace();
        }
    }


    public void loadKeyFiles(String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Base64.Decoder decoder = Base64.getDecoder();
            File folder = new File("revsni/keys/rsa");
            for(File file : folder.listFiles()) {
                if(!(file.getName().equals("privhost.key") || file.getName().equals("pubhost.key"))) {
                    String type = file.getName().split(".")[0].split(",")[0];
                    //String uuid = file.getName().split(".")[0].split(",")[1];
                    if(type.equals("public")) {
                        File publicKeyFile = new File(folder + "/" + file.getName());
                        byte[] publicKeyBytes = decoder.decode(new String(decoder.decode(Files.readAllBytes(publicKeyFile.toPath()))));
                        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                        pubHost = keyFactory.generatePublic(publicKeySpec);
                    }
                    if(type.equals("private")) {
                        File privateKeyFile = new File(folder + "/" + file.getName());
                        byte[] privateKeyBytes = decoder.decode(new String(decoder.decode(Files.readAllBytes(privateKeyFile.toPath()))));
                        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                        privKey = keyFactory.generatePrivate(privateKeySpec);
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("RSA not found, maybe lib error");
        } catch (IOException e) {
            logger.error("Failed to read File for ClientKeyLoad | " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            logger.error("Failed generate KeySpec in ClientKeyLoad | " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadKey(String key, String type) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Base64.Decoder decoder = Base64.getDecoder();
            if(type.equals("public")) {
                byte[] publicKeyBytes = decoder.decode(new String(decoder.decode(key.getBytes())));
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                pubHost = keyFactory.generatePublic(publicKeySpec);
            }
            if(type.equals("private")) {
                byte[] privateKeyBytes = decoder.decode(new String(decoder.decode(key.getBytes())));
                EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                privKey = keyFactory.generatePrivate(privateKeySpec);
            }
                
        } catch (NoSuchAlgorithmException e) {
            logger.error("RSA not found, maybe lib error");
        } catch (InvalidKeySpecException e) {
            logger.error("Failed generate KeySpec in ClientKeyLoad | " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void initDecryptionCipher() {
        try {
            decryptCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            decryptCipher.init(Cipher.DECRYPT_MODE, privKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public Cipher initEncryptionCipher() {
        Cipher encryptionCipher = null;
        try {
            encryptionCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, pubHost);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
        }
        return encryptionCipher;
    }

    public String decrypt(String message) {
        String decrypted = "";
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decryptedMessageBytes = decoder.decode(message);
        try {
            decryptedMessageBytes = decryptCipher.doFinal(decryptedMessageBytes);
            decrypted = new String((decryptedMessageBytes), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Failed to decrypt RSA message!");
        }
        return decrypted;
    }

    public String encrypt(String message) {
        String encrypted = "";
        Cipher encryptionCipher = initEncryptionCipher();
        byte[] messageBytes = Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedMessageBytes;
        try {
            encryptedMessageBytes = encryptionCipher.doFinal(messageBytes);
            encrypted = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Failed to encrypt message: '"+message+"' with RSA");
            e.printStackTrace();
        }
        return encrypted;
    }

    public EncMode getEncryption() {
        return this.mode;
    }

    public Cipher getEncCipher() {
        return this.encryptCipher;
    }

    public Cipher getDecCipher() {
        return this.decryptCipher;
    }
    public PrivateKey getPrivKey() {
        return this.privKey;
    }
    public void setPubHost(PublicKey pubKeyHost) {
        this.pubHost = pubKeyHost;
    }

    public void setKey(String key, String type) {
        loadKey(key, type);
    }

}