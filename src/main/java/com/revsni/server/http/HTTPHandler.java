package com.revsni.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;

public class HTTPHandler implements HttpHandler {
    private List<String> commands = new ArrayList<>();
    private String answer;

    @Override
    public void handle(HttpExchange t) throws IOException {
        Headers headers = new Headers();
        headers.set("Cookie: ", this.answer);
        t.sendResponseHeaders(200, answer.getBytes().length);
        OutputStream os = t.getResponseBody();
        os.write(" ".getBytes());
        os.close();
    }

    public boolean addCommand(String command) {
        commands.add(command);
        if(!commands.contains(command)) {
            return false;
        }
        return true;
    } 

    public void setCommandsOnline() {
        String response = "";
        for(String command : commands) {
            response += command + ";";
        }
        this.answer = Base64.getEncoder().encode(response.getBytes()).toString();
    }
}