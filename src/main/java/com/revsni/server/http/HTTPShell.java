package com.revsni.server.http;

import java.io.IOException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class HTTPShell {
    volatile HTTPHandler handler;
    volatile HTTPServer server = new HTTPServer();
    private volatile int port;


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
                    System.out.println("Could not start HTTP Server.");
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