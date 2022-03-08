package com.revsni.server.encryption;

import javax.crypto.Cipher;

import com.revsni.common.Configuration.EncMode;

public interface Encri {
    public String encrypt(String message);

    public String encrypt(String uuid, String message);

    public String decrypt(String encrypted); 

    public EncMode getEncryption();

    public void initCiphers();

    public Cipher getEncCipher();

    public Cipher getDecCipher();
}
