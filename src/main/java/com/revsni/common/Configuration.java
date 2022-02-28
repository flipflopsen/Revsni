package com.revsni.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Configuration {

    private String configurationName;
    
    private String localAddrTCP;
    private String localAddrUDP;
    private String srvAddrHTTP;
    private String localAddrHTTP;
    private String srvAddrHTTPS;
    private String localAddrHTTPS;

    private int localPortTCP;
    private int localPortUDP;
    private int localPortHTTP;
    private int localPortHTTPS;

    private int srvPortHTTP;
    private int srvPortHTTPS;

    private int asynchrRandomizeGradeHTTP;
    private int asynchrRandomizeGradeHTTPS;

    private String endpointHTTP;
    private String endpointHTTPS;

    public Configuration() {

    }


    public void saveConfiguration(String name) {
        this.configurationName = name;
    }

    public void loadConfiguraion(String name) {

    }


    public void setTCP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[TCP Configuration] Local IP: ");
        this.localAddrTCP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[TCP Configuration] Port to listen on: ");
        this.localPortTCP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setUDP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[UDP Configuration] Local IP: ");
        this.localAddrUDP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[UDP Configuration] Port to listen on: ");
        this.localPortUDP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Server IP: ");
        this.srvAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Server should listen on: ");
        this.srvPortHTTP = bufferedReader.read();
        System.out.println("");
        System.out.print("[HTTP Configuration] Local IP: ");
        this.localAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Handler should listen on: ");
        this.localPortHTTP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Server IP: ");
        this.srvAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Server should listen on: ");
        this.srvPortHTTPS = bufferedReader.read();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Local IP: ");
        this.localAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Handler should listen on: ");
        this.localPortHTTPS = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setAsyncHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Grade of randomization for GET Requests (1-5): ");
        this.asynchrRandomizeGradeHTTP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setAsyncHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Grade of randomization for GET Requests (1-5): ");
        this.asynchrRandomizeGradeHTTP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public String getTCPConf() {
        String conf = "";

        return conf;
    }

    public String getUDPConf() {
        String conf = "";

        return conf;
    }

    public String getHTTPConf() {
        String conf = "";

        return conf;
    }

    public String getHTTPSConf() {
        String conf = "";

        return conf;
    }

    public String printConf() {
        String conf = "";

        return conf;
    }
}
