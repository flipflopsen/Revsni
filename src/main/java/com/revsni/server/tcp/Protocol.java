package com.revsni.server.tcp;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.revsni.server.Server;


public class Protocol {

    private Cipher cipherDec;
    private Cipher cipherEnc;
    private SecretKeySpec spec;
    private String os;
    private String uuid;

    public Protocol(Server server) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.spec = new SecretKeySpec(server.getKey().getEncoded(), "AES");
        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDec.init(Cipher.DECRYPT_MODE, spec, server.getIv());

        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherEnc.init(Cipher.ENCRYPT_MODE, server.getKey(), server.getIv());
    }

    public boolean processMessage(String msg, String ip, int sessionNumber) {
        try {
            String message  = new String(cipherDec.doFinal(Base64.getDecoder()
                .decode((String) msg)));
            if(!message.equals("")){
                System.out.println("\n" + message);
            }
            
            if(message.contains("arrived to vacation")) {
                String[] splitted = message.split(":");
                this.uuid = splitted[0];
                this.os = splitted[2].replaceAll("\\s","");
            }
            
            System.out.print("Revsn [TCP]["+ ip +"]["+sessionNumber+"] » ");
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

    public String getOs() {
        return this.os;
    }

    public String getuuid() {
        return this.uuid;
    }
}
