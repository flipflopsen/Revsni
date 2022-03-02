package com.revsni.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Configuration {

    public enum Mode {
        TCP(localAddrTCP, localPortTCP), 
        UDP(localAddrUDP, localPortUDP), 
        HTTP(localAddrHTTP, localPortHTTP, srvAddrHTTP, srvPortHTTP), 
        HTTPS(localAddrHTTPS, localPortHTTPS, srvAddrHTTPS, srvPortHTTPS), 
        HTTPAsync(localAddrHTTP, localPortHTTP, srvAddrHTTP, srvPortHTTP, endpointHTTP, asynchrRandomizeGradeHTTP), 
        HTTPSAsync(localAddrHTTPS, localPortHTTPS, srvAddrHTTPS, srvPortHTTPS, endpointHTTPS,asynchrRandomizeGradeHTTPS);

        private int asynchrRandomizeGrade;
        private int lPort;
        private String lHost;
        private int srvPort;
        private String srvHost;
        private String endpoint;

        private Mode(String laddr, int lport) {
            this.lHost = laddr;
            this.lPort = lport;
        }
        private Mode(String laddr, int lport, String saddr, int sport) {
            this.lHost = laddr;
            this.lPort = lport;
            this.srvHost = saddr;
            this.srvPort = sport;
        }
        private Mode(String laddr, int lport, String saddr, int sport, String endpoint) {
            this.lHost = laddr;
            this.lPort = lport;
            this.srvHost = saddr;
            this.srvPort = sport;
            this.endpoint = endpoint;
        }
        private Mode(String laddr, int lport, String saddr, int sport, String endpoint, int randomization) {
            this.lHost = laddr;
            this.lPort = lport;
            this.srvHost = saddr;
            this.srvPort = sport;
            this.endpoint = endpoint;
            this.asynchrRandomizeGrade = randomization;
        }
    }

    private Mode mode = Mode.TCP;

    private String configurationName = "";
    
    private static String localAddrTCP = "127.0.0.1";
    private static String localAddrUDP = "127.0.0.1";
    private static String srvAddrHTTP = "127.0.0.1";
    private static String localAddrHTTP = "127.0.0.1";
    private static String srvAddrHTTPS = "127.0.0.1";
    private static String localAddrHTTPS = "127.0.0.1";

    public static int localPortTCP = 1331;
    private static int localPortUDP = 1332;
    private static int localPortHTTP = 8085;
    private static int localPortHTTPS = 8443;

    private static int srvPortHTTP = 8081;
    private static int srvPortHTTPS = 443;

    private static int asynchrRandomizeGradeHTTP = 1;
    private static int asynchrRandomizeGradeHTTPS = 1;

    private static String endpointHTTP = "/lit";
    private static String endpointHTTPS = "/litS";

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
        localAddrTCP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[TCP Configuration] Port to listen on: ");
        localPortTCP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setUDP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[UDP Configuration] Local IP: ");
        localAddrUDP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[UDP Configuration] Port to listen on: ");
        localPortUDP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Server IP: ");
        srvAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Server should listen on: ");
        srvPortHTTP = bufferedReader.read();
        System.out.println("");
        System.out.print("[HTTP Configuration] Local IP: ");
        localAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Handler should listen on: ");
        localPortHTTP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Server IP: ");
        srvAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Server should listen on: ");
        srvPortHTTPS = bufferedReader.read();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Local IP: ");
        localAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Handler should listen on: ");
        localPortHTTPS = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setAsyncHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Grade of randomization for GET Requests (1-5): ");
        asynchrRandomizeGradeHTTP = bufferedReader.read();
        System.out.println("");
        bufferedReader.close();
        inputStreamReader.close();
    }

    public void setAsyncHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Grade of randomization for GET Requests (1-5): ");
        asynchrRandomizeGradeHTTP = bufferedReader.read();
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

    public void printConf() throws IOException {
        boolean back = false;
            while(!back) {

            
            System.out.println("\n"
                              +"-----------"
                              +"|Configuration Menu|"
                              +"-----------\n"
                              +"\n"
                              +"-<|Active Mode: " + mode.name() + "|>-\n\n"
                              +"What do you want to configure?:\n"
                              +"1. TCP\n"
                              +"2. UDP\n"
                              +"3. HTTP\n"
                              +"4. HTTPS\n"
                              +"5. HTTP Asynchronous\n"
                              +"6. HTTPS Asynchronous\n"
                              +"7. DNS\n"
                              +"\n"
                              +"Type 'back' to go back\n"
                              +"\n");

            System.out.print("Revsn [CONFIG] » ");
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String decision = bufferedReader.readLine();
            System.out.println();
            switch(decision) {
                case("1"):
                    boolean tcpBack = false;
                    while(!tcpBack) {  
                        printTCP();
                        System.out.print("Revsn [CONFIG]-[TCP]» Wanna edit? (y/n): ");
                    }
                    break;
                case("2"):
                    boolean udpBack = false;
                    while(!udpBack) {   
                        printUDP();
                        System.out.print("Revsn [CONFIG]-[UDP]» Wanna edit? (y/n): ");
                    }
                    break;
                case("3"):
                    boolean httpBack = false;
                    while(!httpBack) { 
                        printHTTP();
                        System.out.print("Revsn [CONFIG]-[HTTP]» Wanna edit? (y/n): ");
                    }
                    break;
                case("4"):  
                    boolean httpsBack = false;
                    while(!httpsBack) { 
                        printHTTPS();
                        System.out.print("Revsn [CONFIG]-[HTTPS]» Wanna edit? (y/n): ");
                    }
                    break;
                case("5"):
                    boolean httpAsBack = false;
                    while(!httpAsBack) { 
                        printHTTPAS();
                        System.out.print("Revsn [CONFIG]-[HTTPAsync]» Wanna edit? (y/n): ");
                    }
                    break;
                case("6"):  
                    boolean httpsAsBack = false;
                    while(!httpsAsBack) { 
                        printHTTPSAS();
                        System.out.print("Revsn [CONFIG]-[HTTPSAsync]» Wanna edit? (y/n): ");
                    }
                    break;
                case("7"):  

                    break;
                case("back"): back = true; break;
                default:
            }
        }
    }
    public void printTCP() {
        System.out.println("\n"
                          +"-----------"
                          +"|TCP - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrTCP + "\n"
                          +"Local Port: " + localPortTCP + "\n"
                          +"\n");

    }
    public void printUDP() {
        System.out.println("\n"
                          +"-----------"
                          +"|UDP - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrUDP + "\n"
                          +"Local Port: " + localPortUDP + "\n"
                          +"\n");
        
    }
    public void printHTTP() {
        System.out.println("\n"
                          +"-----------"
                          +"|HTTP - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrHTTP + "\n"
                          +"Local Port: " + localPortHTTP + "\n"
                          +"Server Host: " + srvAddrHTTP + "\n"
                          +"Server Port: " + srvPortHTTP + "\n"
                          +"Endpoint: " + endpointHTTP + "\n"
                          +"\n");
        
    }
    public void printHTTPS() {
        System.out.println("\n"
                          +"-----------"
                          +"|HTTPS - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrHTTPS + "\n"
                          +"Local Port: " + localPortHTTPS + "\n"
                          +"Server Host: " + srvAddrHTTPS + "\n"
                          +"Server Port: " + srvPortHTTPS + "\n"
                          +"Endpoint: " + endpointHTTPS + "\n"
                          +"\n");
        
    }
    public void printHTTPAS() {
        System.out.println("\n"
                          +"-----------"
                          +"|HTTP Asynchronous - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrHTTP + "\n"
                          +"Local Port: " + localPortHTTP + "\n"
                          +"Server Host: " + srvAddrHTTP + "\n"
                          +"Server Port: " + srvPortHTTP + "\n"
                          +"Endpoint: " + endpointHTTP + "\n"
                          +"Radomization Grade: " + asynchrRandomizeGradeHTTP + "\n"
                          +"\n");
        
    }
    public void printHTTPSAS() {
        System.out.println("\n"
                          +"-----------"
                          +"|HTTPS Asynchronous - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrHTTPS + "\n"
                          +"Local Port: " + localPortHTTPS + "\n"
                          +"Server Host: " + srvAddrHTTPS + "\n"
                          +"Server Port: " + srvPortHTTPS + "\n"
                          +"Endpoint: " + endpointHTTPS + "\n"
                          +"Radomization Grade: " + asynchrRandomizeGradeHTTPS + "\n"
                          +"\n");
        
    }
    public void printDNS() {
        System.out.println("\n"
                          +"-----------"
                          +"|DNS - Configuration|"
                          +"-----------"
                          +"\n"
                          +"Local IP: " + localAddrHTTPS + "\n"
                          +"Local Port: " + localPortHTTPS + "\n"
                          +"Server Host: " + srvAddrHTTPS + "\n"
                          +"Server Port: " + srvPortHTTPS + "\n"
                          +"\n");
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    } 

    public Mode getMode() {
        return this.mode;
    }
}
