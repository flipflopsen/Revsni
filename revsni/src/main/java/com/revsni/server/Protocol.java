package com.revsni.server;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;

import com.revsni.common.Configuration.EncMode;
import com.revsni.server.encryption.AES;
import com.revsni.server.encryption.Encri;


public class Protocol {

    private Server server;
    private String os;
    private String uuid;
    private volatile HashMap<Integer, Encri> clientEnc = new HashMap<>();
    public String mode;

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
            System.out.println("\n" + message);
        }

        if(message.equals("errxuk")) {
            System.out.println("Failed to decrypt message!");
            return false;
        }
        
        if(message.contains("arrived to vacation")) {
            String[] splitted = message.split(":");
            this.uuid = splitted[0];
            this.os = splitted[2].replaceAll("\\s","");
            this.mode = splitted[3].replaceAll("\\s","");
            newClientEncryption(sessionNumber);
        }
        System.out.print("Revsn [TCP]["+ ip +"]["+sessionNumber+"] » ");
        return true;
        
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
        EncMode encMode = null;
        if(mode.equals(EncMode.AES.name())) {
            encMode = EncMode.AES;
        }
        if(mode.equals(EncMode.RSA.name())) {
            encMode = EncMode.RSA;
        }
        server.setNewEncryption(encMode, sessionNumber);
    }
}
