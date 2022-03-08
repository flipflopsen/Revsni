package com.revsni.server.http;

import java.io.IOException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.revsni.server.Interaction;
import com.revsni.common.Configuration.Mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
    

public class HTTPShell implements Interaction {
    public volatile HTTPHandler handler;
    public volatile HTTPServer server = new HTTPServer();
    public volatile int port;
    private static final Logger logger = LogManager.getLogger(HTTPShell.class);
    public volatile Mode mode = Mode.HTTP;
    
    public HTTPShell() {
        //Default constructor for serialization
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHandler(HTTPHandler handler) {
        this.handler = handler;
    }


    public HTTPShell(SecretKey key, IvParameterSpec iv, int portIn, int sessionNumber) throws IOException {
        final int port = portIn;
        this.port = port;
        handler = new HTTPHandler(key, iv ,sessionNumber);
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

    public void sendCommand(String command, String uuid) {
        handler.sendCommand(command);
    }

    public boolean getConnInf() {
        return handler.getConnInf();
    }

    public int getPort() {
        return this.port;
    }
    public Mode getMode() {
        return this.mode;
    }
    public void setAsync() {
        this.mode = Mode.HTTPAsync;
    }
}
