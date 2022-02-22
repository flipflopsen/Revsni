package com.revsni.server.http;

import java.io.IOException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
    

public class HTTPShell {
    volatile HTTPHandler handler;
    volatile HTTPServer server = new HTTPServer();
    private volatile int port;
    Logger logger = LogManager.getLogger(getClass());
    


    public HTTPShell(SecretKey key, IvParameterSpec iv, int portIn) throws IOException {
        final int port = portIn;
        this.port = port;
        handler = new HTTPHandler(key, iv);
        server.setHandler(handler);
        server.setup(port);

        new Thread() {
            @Override
            public void run() {
                try {
                    server.main(port, handler);
                } catch (Exception e) {
                    logger.info("Could not start HTTP Server.");
                    System.exit(1);
                }
            }
        }.start();
    }

    public void sendCommand(String command) {
        handler.sendCommand(command);
    }

    public boolean getConnInf() {
        return handler.getConnInf();
    }

    public int getPort() {
        return this.port;
    }
}
