package com.revsni.client.Encri;

import javax.crypto.Cipher;

import com.revsni.common.Configuration.EncMode;

public interface Encri {
    public String encrypt(String message);

    public String decrypt(String encrypted); 

    public EncMode getEncryption();

    public Cipher getEncCipher();

    public Cipher getDecCipher();
}