package com.revsni.websn;

import com.revsni.Webserver;

public class Websn {
    public static void main(String[] args) {
        Webserver webserver = new Webserver(8115);
        webserver.setUpWebUI();
        webserver.setUpFileServer();
    }
}
