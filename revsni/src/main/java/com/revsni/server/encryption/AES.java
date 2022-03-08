package com.revsni.server.encryption;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.revsni.common.Configuration.EncMode;
public class AES implements Encri {

    private final String password;
    private final String salt;
    private SecretKeySpec key;
    private IvParameterSpec iv;
    private SecretKey keyS;
    private final EncMode mode = EncMode.AES;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public AES(String password, String salt) {
        this.password = password;
        this.salt = salt;
        this.iv = generateIv();
        try {
            this.keyS = generateKey();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
    
    public SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), this.salt.getBytes(), 65536, 256);
        key = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");
        return key;
    }

    public SecretKey generateKeyRandom(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public String encrypt(String toEnc) {
        String message = "-";
        try {
            byte[] data = Base64.getEncoder().encode(toEnc.getBytes(StandardCharsets.UTF_8));
            message  = Base64.getEncoder()
                .encodeToString(encryptCipher.doFinal(data));
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return message;
    }

    public String encrypt(String uuid, String message) {
        return "";
    }

    public String decrypt(String encrypted) {
        try {
            byte[] replaced1 = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
            byte[] decodedB64;
            decodedB64 = decryptCipher.doFinal(replaced1);
            String repl2 = new String(decodedB64);
            String message = new String(Base64.getDecoder().decode(repl2), StandardCharsets.UTF_8);
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "errxuk";
        }
    }


    public IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public void initCiphers() {
        try {
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {

            e.printStackTrace();
        }
        try {
            encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        }catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {

            e.printStackTrace();
        }
        
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

    public SecretKey getKey() {
        return this.keyS;
    }

    public IvParameterSpec getIV() {
        return this.iv;
    }

    public String getSalt() {
        return this.salt;
    }
    public String getPassword() {
        return this.password;
    }

}
