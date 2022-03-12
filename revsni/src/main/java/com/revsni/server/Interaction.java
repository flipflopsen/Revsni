package com.revsni.server;

import com.revsni.common.Configuration.Mode;

public interface Interaction {

    public Mode getMode();

    public void sendCommand(String command);

    public void sendCommand(String command, String uuid);

    public int getPort();

    public void shutdown();

}
