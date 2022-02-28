package com.revsni.server;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@Deprecated
public class Protocol {

    Logger logger = LogManager.getLogger(getClass());

    //private Logger LOG = parentLogger;
    private Cipher cipherDec;
    private Cipher cipherEnc;
    private SecretKeySpec spec;

    public Protocol(Server server) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.spec = new SecretKeySpec(server.getKey().getEncoded(), "AES");
        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDec.init(Cipher.DECRYPT_MODE, spec, server.getIv());

        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherEnc.init(Cipher.ENCRYPT_MODE, server.getKey(), server.getIv());
    }

    public boolean processMessage(String msg) {
        try {
            String message  = new String(cipherDec.doFinal(Base64.getDecoder()
                .decode((String) msg)));
            logger.info("\n" + message);
            System.out.print("Revsn [TCP] » ");
            return true;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String prepareMessage(String msg) {
        String message = "-";
        try {
            message  = new String(Base64.getEncoder()
                .encode(cipherEnc.doFinal(msg.getBytes())));
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return message;
    }
}
