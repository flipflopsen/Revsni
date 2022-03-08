package com.revsni.server.encryption;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import com.revsni.common.Configuration.EncMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RSA implements Encri {
    private static final Logger logger = LogManager.getLogger(RSA.class);
    private PrivateKey privKeyHost;
    private PublicKey pubKeyHost;

    private HashMap<String, PublicKey> clientPublicKeys = new HashMap<>();
    private HashMap<String, PrivateKey> clientPrivateKeys = new HashMap<>();
    private Cipher decryptCipher;
    private Cipher encryptCipher;
    private EncMode mode = EncMode.RSA;

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        RSA rsa = new RSA();
        rsa.generateHostKeyPair();
        rsa.saveHostKeyPair();
        rsa.loadHostKeyPair();
        rsa.loadClientKeys();
        rsa.initDecryptionCipher();
    }

    public RSA() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        generateHostKeyPair();
        saveHostKeyPair();
        //loadHostKeyPair();
        //loadClientKeys();
        initDecryptionCipher();
    }

    public void generateHostKeyPair() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(4096);
            KeyPair pair = generator.generateKeyPair();
            privKeyHost = pair.getPrivate();
            pubKeyHost = pair.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            logger.error("Failed to create RSA Key Pair!");
            e.printStackTrace();
        }
    }

    public void generateClientKeyPair(String uuid) {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(4096);
            KeyPair pair = generator.generateKeyPair();
            clientPrivateKeys.put(uuid, pair.getPrivate());
            clientPublicKeys.put(uuid, pair.getPublic());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            logger.error("Failed to create RSA Key Pair!");
            e.printStackTrace();
        }
        saveClientKeyPair(uuid);
    }

    public void saveClientKeyPair(String uuid) {
        Base64.Encoder encoder = Base64.getEncoder();
        try (Writer fos = new FileWriter("revsni/keys/rsa/public,"+uuid+".key")) {
            fos.write(encoder.encodeToString(new String(encoder.encodeToString(clientPublicKeys.get(uuid).getEncoded())).getBytes()));
            //logger.info("Client Public Key ("+uuid+") wrote!");
        } catch (IOException e) {
            logger.error("Failed save Client RSA Public Key!");
            e.printStackTrace();
        }
        try (Writer fos = new FileWriter("revsni/keys/rsa/private,"+uuid+".key")) {
            fos.write(encoder.encodeToString(new String(encoder.encodeToString(clientPrivateKeys.get(uuid).getEncoded())).getBytes()));
            //logger.info("Client Private Key ("+uuid+") wrote!");
        } catch (IOException e) {
            logger.error("Failed save Client RSA Private Key!");
            e.printStackTrace();
        }
    }

    public void saveHostKeyPair() {
        Base64.Encoder encoder = Base64.getEncoder();
        
        try (Writer fos = new FileWriter("revsni/keys/rsa/pubhost.key")) {
            fos.write(encoder.encodeToString(new String(encoder.encodeToString(pubKeyHost.getEncoded())).getBytes()));
            logger.info("Host RSA Public Key wrote!");
            fos.close();
        } catch (IOException e) {
            logger.error("Failed save Host RSA Public Key!");
            e.printStackTrace();
        }
        try (Writer fos = new FileWriter("revsni/keys/rsa/privhost.key")) {
            fos.write(encoder.encodeToString(privKeyHost.getEncoded()));
            logger.info("Host RSA Private Key wrote!");
            fos.close();
        } catch (IOException e) {
            logger.error("Failed save Host RSA Private Key!");
            e.printStackTrace();
        }
    }

    public void loadHostKeyPair() {
        Base64.Decoder decoder = Base64.getDecoder();
        File publicKeyFile = new File("revsni/keys/rsa/pubhost.key");
        File privateKeyFile = new File("revsni/keys/rsa/privhost.key");
        try {
            String pubKeyTmp = new String(decoder.decode(Files.readAllBytes(publicKeyFile.toPath()))).replace("-----BEGIN RSA PUBLIC KEY-----", "").replace("-----END RSA PUBLIC KEY-----", "");
            byte[] publicKeyBytes = decoder.decode(pubKeyTmp);
            byte[] privateKeyBytes = decoder.decode(Files.readAllBytes(privateKeyFile.toPath()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            this.pubKeyHost = keyFactory.generatePublic(publicKeySpec);
            logger.info("RSA Public Key loaded (Host)!");   
            this.privKeyHost = keyFactory.generatePrivate(privateKeySpec);
            logger.info("RSA Private Key loaded (Host)!"); 
        } catch (IOException e) {
            logger.error("Failed to load RSA Keys!");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Failed to generate Key Specs and Keys from RSA Key Files!");
            e.printStackTrace();
        } 
    }

    public void loadClientKeys() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Base64.Decoder decoder = Base64.getDecoder();
            File folder = new File("revsni/keys/rsa");
            for(File file : folder.listFiles()) {
                if(!(file.getName().equals("privhost.key") || file.getName().equals("pubhost.key"))) {
                    String type = file.getName().split(".")[0].split(",")[0];
                    String uuid = file.getName().split(".")[0].split(",")[1];
                    if(type.equals("public")) {
                        File publicKeyFile = new File(folder + "/" + file.getName());
                        byte[] publicKeyBytes = decoder.decode(new String(decoder.decode(Files.readAllBytes(publicKeyFile.toPath()))));
                        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                        clientPublicKeys.put(uuid, keyFactory.generatePublic(publicKeySpec));
                    }
                    if(type.equals("private")) {
                        File privateKeyFile = new File(folder + "/" + file.getName());
                        byte[] privateKeyBytes = decoder.decode(new String(decoder.decode(Files.readAllBytes(privateKeyFile.toPath()))));
                        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                        clientPrivateKeys.put(uuid, keyFactory.generatePrivate(privateKeySpec));
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


    public void initDecryptionCipher() {
        try {
            decryptCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            decryptCipher.init(Cipher.DECRYPT_MODE, privKeyHost);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public Cipher initEncryptionCipher(String uuid) {
        Cipher encryptionCipher = null;
        try {
            encryptionCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            PublicKey pub = clientPublicKeys.get(uuid);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, pub);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return encryptionCipher;
    }

    public String decrypt(String message) {
        String decrypted = "";
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decryptedMessageBytes = Base64.getDecoder().decode(message);
        try {
            decryptedMessageBytes = decryptCipher.doFinal(decryptedMessageBytes);
            decrypted = new String(decoder.decode(decryptedMessageBytes), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Failed to decrypt RSA message!");
            e.printStackTrace();
        }
        return decrypted;
    }

    public String encrypt(String uuid, String message) {
        String encrypted = "";
        Cipher encryptionCipher = initEncryptionCipher(uuid);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes;
        try {
            encryptedMessageBytes = encryptionCipher.doFinal(messageBytes);
            encrypted = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Failed to encrypt message: '"+message+"' with RSA for UUID: " + uuid);
            e.printStackTrace();
        }
        return encrypted;
    }

    public String encrypt(String message) {
        return "";
    }

    public void initCiphers() {

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
    public PrivateKey getPrivKeyHost() {
        return this.privKeyHost;
    }
    public void setPubKeyHost(PublicKey pubKeyHost) {
        this.pubKeyHost = pubKeyHost;
    }

    public String getClientPrivKey(String uuid) {
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(encoder.encode(clientPrivateKeys.get(uuid).getEncoded())));
    }

    public String getClientPubKey(String uuid) {
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(encoder.encode(clientPublicKeys.get(uuid).getEncoded())));
    }
}
