package com.revsni.server;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;

import com.revsni.common.Configuration.EncMode;
//import com.revsni.server.encryption.AES;
import com.revsni.server.encryption.Encri;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Protocol {

    private Server server;
    private String os;
    private String uuid;
    private volatile HashMap<Integer, Encri> clientEnc = new HashMap<>();
    public String mode;

    Logger logger = LogManager.getLogger(getClass());

    public Protocol(Server server) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.server = server;
        this.clientEnc = Server.getClientEncryptions();
        
    }

    public boolean processMessage(String msg, String ip, int sessionNumber) {
        updateEncryptionModes();
        String message;
        if(clientEnc.containsKey(sessionNumber)) {
            message = clientEnc.get(sessionNumber).decrypt(msg);
        } else {
            message = Server.initEncri.decrypt(msg);
        }
        if(!message.equals("")){
            if(message.equals("errxuk")) {
                System.out.println("Failed to decrypt message!");
                return false;
            } else if(message.contains("arrived to vacation")) {
                String[] splitted = message.split(":");
                this.uuid = splitted[0];
                this.os = splitted[2].replaceAll("\\s","");
                logger.info("\n" + message);
            } else if(message.equals("give")) {

            } else {
                logger.info("\n" + message);
            }
        }

        return true;
    }

    public String processMessage(String msg, int sessionNumber) {
        updateEncryptionModes();
        String message;
        if(clientEnc.containsKey(sessionNumber)) {
            message = clientEnc.get(sessionNumber).decrypt(msg);
        } else {
            message = Server.initEncri.decrypt(msg);
        }
        if(!message.equals("")){
            if(message.equals("errxuk")) {
                return "Failed to decrypt message!";
            } else if(message.contains("arrived to vacation")) {
                String[] splitted = message.split(":");
                this.uuid = splitted[0];
                this.os = splitted[2].replaceAll("\\s","");
                logger.info("\n" + message);
                return message;
            } else if(message.equals("give")) {
                return "give";
            } else if(message.equals("est")) {
                return "est";
            } else {
                logger.info("\n" + message);
                return message;
            }
        }
        return "Empty answer.";
    }

    public String prepareMessage(String msg, int sessionNumber) {
        updateEncryptionModes();
        String message;
        if(clientEnc.containsKey(sessionNumber)) {
            message = clientEnc.get(sessionNumber).encrypt(msg);
        } else {
            message = Server.initEncri.encrypt(msg);
        }
        return message;
    }
    public String prepareMessage(String msg, int sessionNumber, String uuid) {
        updateEncryptionModes();
        String message;
        if(clientEnc.containsKey(sessionNumber)) {
            message = clientEnc.get(sessionNumber).encrypt(uuid, msg);
        } else {
            message = Server.initEncri.encrypt(uuid, msg);
        }
        return message;
    }

    public String getOs() {
        return this.os;
    }

    public String getuuid() {
        return this.uuid;
    }

    public void updateEncryptionModes() {
        this.clientEnc = Server.getClientEncryptions();

    }
    public void newClientEncryption(int sessionNumber) {
        EncMode encMode = EncMode.RSA;
        if(encMode.name().equals(EncMode.AES.name())) {
            encMode = EncMode.AES;
        }
        if(encMode.name().equals(EncMode.RSA.name())) {
            encMode = EncMode.RSA;
        }
        server.setNewEncryption(encMode, sessionNumber);
    }
}
