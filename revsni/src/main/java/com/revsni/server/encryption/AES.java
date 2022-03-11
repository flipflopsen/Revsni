package com.revsni.server.encryption;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.revsni.common.Configuration.EncMode;
public class AES implements Encri {

    private final String password;
    private final byte[] salt;
    private SecretKeySpec key;
    private IvParameterSpec iv;
    private SecretKey keyS;
    private final EncMode mode = EncMode.AES;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public AES(String password, String salt) {
        this.password = password;
        this.salt = new byte[]{-84, -119, 25, 56, -100, 100, -120, -45, 84, 67, 96, 10, 24, 111, 112, -119, 3};
        this.iv = generateIv();
        try {
            this.keyS = generateKey();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        initCiphers();
    }
    
    public SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(this.password.toCharArray(), salt, 1024, 128);
        key = new SecretKeySpec(factory.generateSecret(spec)
            .getEncoded(), "AES");
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
        String message1 = "-";
        try {
            byte[] data = Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
            message  = Base64.getEncoder()
                .encodeToString(encryptCipher.doFinal(data));
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return message1;
    }

    public String decrypt(String encrypted) {
        try {
            //System.out.println("In AES Dec: " + encrypted);
            byte[] replaced1 = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
            byte[] decodedB64;
            decodedB64 = decryptCipher.doFinal(replaced1);
            String message = new String(Base64.getDecoder().decode(decodedB64), StandardCharsets.UTF_8);
            //System.out.println("In AES Dec: " + message);
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "errxuk";
        }
    }


    public IvParameterSpec generateIv() {
        byte[] iv = new byte[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        this.iv = new IvParameterSpec(iv);
        return this.iv;
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

    public byte[] getSalt() {
        return this.salt;
    }
    public String getPassword() {
        return this.password;
    }

}
