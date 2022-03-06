package com.revsni.server.encryption;

import com.revsni.common.Configuration.EncMode;

public interface Encri {
    public String encrypt(String uuid, String message);

    public String decrypt(String encrypted); 

    public EncMode getEncryption();
}
