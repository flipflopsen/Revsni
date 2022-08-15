package com.revsni.server.encryption;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.revsni.common.Configuration.EncMode;

public class Blowfish implements Encri {

    private final byte[] key;
    private SecretKeySpec ks;
    private Cipher cipherEnc;
    private Cipher cipherDec;
    private final EncMode mode = EncMode.BLOWFISH;

    public Blowfish(String keyIn) {
        this.key = keyIn.getBytes();
        this.ks = new SecretKeySpec(key, "Blowfish");
    }

    public String encrypt(String toEnc) {
        String message = "-";
        try {
            byte[] data = Base64.getEncoder().encode(toEnc.getBytes(StandardCharsets.UTF_8));
            message  = Base64.getEncoder()
                .encodeToString(cipherEnc.doFinal(data));
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
                .encodeToString(cipherEnc.doFinal(data));
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
            decodedB64 = cipherDec.doFinal(replaced1);
            String message = new String(Base64.getDecoder().decode(decodedB64), StandardCharsets.UTF_8);
            //System.out.println("In AES Dec: " + message);
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException | IllegalArgumentException | OutOfMemoryError e) {
            if(e instanceof IllegalArgumentException) {
                return "errxuk";
            }
            return "errxuk";
        }
    }

    @Override
    public EncMode getEncryption() {
        return this.mode;
    }

    @Override
    public void initCiphers() {
        try {
            this.cipherEnc = Cipher.getInstance("Blowfish");
            this.cipherDec = Cipher.getInstance("Blowfish");
            cipherEnc.init(Cipher.ENCRYPT_MODE, ks);
            cipherDec.init(Cipher.DECRYPT_MODE, ks);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public Cipher getEncCipher() {
        return this.cipherEnc;
    }

    @Override
    public Cipher getDecCipher() {
        return this.cipherDec;
    }

    public byte[] getKeyBytes() {
        return this.key;
    }
    
}
