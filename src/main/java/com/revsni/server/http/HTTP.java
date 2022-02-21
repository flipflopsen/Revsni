package com.revsni.server.http;

import java.io.IOException;

public class HTTP {
    static HTTPHandler handler = new HTTPHandler();
    static final HTTPServer server = new HTTPServer();
    static int port = 8081;
    public static void main(String[] args) throws IOException {
        handler.addCommand("ls");
        handler.addCommand("whoami");
        handler.addCommand("id");
        handler.setCommandsOnline();
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
}
