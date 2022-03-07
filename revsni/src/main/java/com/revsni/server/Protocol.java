package com.revsni.server;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class Protocol {

    private Cipher cipherDec;
    private Cipher cipherEnc;
    private SecretKeySpec spec;
    private String os;
    private String uuid;

    public Protocol(Server server) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.spec = new SecretKeySpec(server.getKey().getEncoded(), "AES");
        cipherDec = Cipher.getInstance("AES/CBC/NoPadding");
        cipherDec.init(Cipher.DECRYPT_MODE, spec, server.getIv());

        cipherEnc = Cipher.getInstance("AES/CBC/NoPadding");
        cipherEnc.init(Cipher.ENCRYPT_MODE, server.getKey(), server.getIv());
    }

    public boolean processMessage(String msg, String ip, int sessionNumber) {
        try {
            System.out.println(msg);
            byte[] replaced1 = Base64.getDecoder().decode(msg.getBytes("UTF-8"));
            System.out.println(new String(replaced1));
            byte[] decodedB64 = cipherDec.doFinal(replaced1);
            System.out.println(replaced1);
            String repl2 = new String(decodedB64, StandardCharsets.UTF_8);
            System.out.println(repl2);
            String message = new String(Base64.getDecoder().decode(repl2));
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
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String prepareMessage(String msg) {
        String message = "-";
        try {
            byte[] data = Base64.getEncoder().encode(msg.getBytes("UTF-8"));
            int i = 0;
            String s = "0";
            while(data.length % 16 != 0) {
                data = Base64.getEncoder().encode((msg + new String(new char[i]).replace("\0", s)).getBytes("UTF-8"));
            }
            message  = Base64.getEncoder()
                .encodeToString(cipherEnc.doFinal(data));
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
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
