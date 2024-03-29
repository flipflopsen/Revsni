package com.revsni;

import java.io.IOException;

import com.revsni.utils.HttpServer;

public class Webserver {
	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
	public final int port;

	/*
		Webserver for initial.txt -> Updater and Connection Info for Clients to connect etc.
	*/

	public Webserver(int portIn) {
		this.port = portIn;
	}

	public void setUpWebUI() {
		HttpServer srv;
		try {
			srv = new HttpServer(port, "revsni/src/main/java/com/revsni/websn/overlay");
			srv.setReadTimeoutMillis(180000);
			System.out.println(ANSI_RESET+"Web-UI "+ANSI_GREEN+"started "+ANSI_RESET +"on Port: "+ANSI_GREEN+ this.port +ANSI_RESET+"!"+ANSI_RESET);
			srv.start();
		} catch (IOException e) {
			System.out.println(ANSI_RESET+"Web-UI "+ANSI_RED+"stopped "+ANSI_RESET +"on Port: "+ANSI_GREEN+ this.port +ANSI_RESET+"!"+ANSI_RESET);
			e.printStackTrace();
		}
	}

	public void setUpFileServer() {
		HttpServer srv;
		try {
			srv = new HttpServer(8082, "revsni/filehosting");
			srv.setReadTimeoutMillis(180000);
			System.out.println(ANSI_RESET+"Fileserver "+ANSI_GREEN+"started "+ANSI_RESET +"on Port: "+ANSI_GREEN+"8082 "+ANSI_RESET+"!"+ANSI_RESET);
			srv.start();
		} catch (IOException e) {
			System.out.println(ANSI_RESET+"Fileserver "+ANSI_RED+"stopped "+ANSI_RESET +"on Port: "+ANSI_GREEN+"8082 "+ANSI_RESET+"!"+ANSI_RESET);
			e.printStackTrace();
		}
	}
}
