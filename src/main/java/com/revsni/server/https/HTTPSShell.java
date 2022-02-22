package com.revsni.server.https;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPSShell {
    
    Logger logger = LogManager.getLogger(getClass());

    volatile HTTPSServer server;
    private volatile int port;
    

    public HTTPSShell(SecretKey key, IvParameterSpec iv, int portIn) throws IOException {
        this.port = portIn;
    
        server = new HTTPSServer(key, iv);

        logger.info("HTTPS-Server configured");
    }

    public void sendCommand(String command) {
        server.sendCommand(command);
    }

    public int getPort() {
        return this.port;
    }

    public void fireUp(int portIn) {
        final InetSocketAddress address = new InetSocketAddress("0.0.0.0", portIn);
        new Thread() {
            @Override
            public void run() {
                try {
                    server.fireUp(address);
                } catch (Exception e) {
                    logger.info("Could not start HTTPS Server.");
                    System.exit(1);
                }
            }
        }.start();
    }

    public boolean getConnInf() {
        return server.getConnInf();
    }

}
