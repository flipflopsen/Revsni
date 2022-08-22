package com.revsni.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Configuration {

    public enum OperationalMode {
        STEALTH(Mode.HTTPAsync, EncMode.AES),
        MINIMAL(Mode.TCP, EncMode.BLOWFISH),
        FULL(Mode.TCP, EncMode.AES),
        NOENC(Mode.TCP, EncMode.NONE),
        DEV(Mode.TCP, EncMode.BLOWFISH);

        public Mode mode;
        public EncMode encMode;

        private OperationalMode(Mode mode, EncMode encMode) {
            this.mode = mode;
            this.encMode = encMode;
        }
    }

    public enum EncMode {
        NONE(),
        AES(saltAES, passwordAES), //Pass and Salt!
        RSA(passwordRSA, rsaPrivPath, rsaPubPath),
        TWOFISH(),
        SERPENT(),
        SSL(),
        BLOWFISH(keyGeneral),
        TRIPLE_DES();

        public String salt;
        public String pass;
        public String privPath;
        public String pubPath;

        private EncMode(String pass) {
            this.pass = pass;
        }

        private EncMode(String salt, String pass) {
            this.salt = salt;
            this.pass = pass;
        }
        private EncMode(String pass, String privPath, String pubPath) {
            this.pass = pass;
            this.privPath = privPath;
            this.pubPath = pubPath;
        }
        private EncMode() {}
    }

    public enum Mode {
        TCP(localAddrTCP, localPortTCP), 
        UDP(localAddrUDP, localPortUDP), 
        HTTP(localAddrHTTP, localPortHTTP, srvAddrHTTP, srvPortHTTP), 
        HTTPS(localAddrHTTPS, localPortHTTPS, srvAddrHTTPS, srvPortHTTPS), 
        HTTPAsync(localAddrHTTP, localPortHTTP, srvAddrHTTP, srvPortHTTP, endpointHTTP, asynchrRandomizeGradeHTTP), 
        HTTPSAsync(localAddrHTTPS, localPortHTTPS, srvAddrHTTPS, srvPortHTTPS, endpointHTTPS,asynchrRandomizeGradeHTTPS),
        DNS(localAddrTCP, localPortTCP);
        

        public int asynchrRandomizeGrade;
        public int lPort;
        public String lHost;
        public int srvPort;
        public String srvHost;
        public String endpoint;

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
    private EncMode encMode = EncMode.AES;
    private OperationalMode opMode = OperationalMode.DEV;

    //private String configurationName = "";
    
    public static String localAddrTCP = "192.168.62.131";
    public static String localAddrUDP = "192.168.62.131";
    public static String srvAddrHTTP = "192.168.62.131";
    public static String localAddrHTTP = "192.168.62.131";
    public static String srvAddrHTTPS = "192.168.62.131";
    public static String localAddrHTTPS = "192.168.62.131";

    public static String keyGeneral = "lol123";

    public static String passwordAES = "lol123";
    public static String saltAES = "lol123";

    public static String passwordRSA = "lol123";

    public static String rsaPrivPath = "revsni\\keys\\rsa\\privhost.key"; 
    public static String rsaPubPath = "revsni\\keys\\rsa\\pubhost.key";

    public static int localPortTCP = 1331;
    public static int localPortUDP = 1332;
    public static int localPortHTTP = 8085;
    public static int localPortHTTPS = 8443;

    public static int srvPortHTTP = 8081;
    public static int srvPortHTTPS = 443;

    public static int asynchrRandomizeGradeHTTP = 1;
    public static int asynchrRandomizeGradeHTTPS = 1;

    public static String endpointHTTP = "/lit";
    public static String endpointHTTPS = "/litS";

    public Configuration() {
    }


    public void saveConfiguration(String name) {
        //this.configurationName = name;
    }

    public void loadConfiguraion(String name) {

    }

     
    /*
        Setter
    */

    public void setTCP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[TCP Configuration] Local IP: ");
        localAddrTCP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[TCP Configuration] Port to listen on: ");
        localPortTCP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
    }

    public void setUDP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[UDP Configuration] Local IP: ");
        localAddrUDP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[UDP Configuration] Port to listen on: ");
        localPortUDP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
    }

    public void setHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Server IP: ");
        srvAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Server should listen on: ");
        srvPortHTTP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
        System.out.print("[HTTP Configuration] Local IP: ");
        localAddrHTTP = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTP Configuration] Port the Handler should listen on: ");
        localPortHTTP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
        System.out.print("[HTTP Configuration] Endpoint (without /): ");
        endpointHTTP = "/" + bufferedReader.readLine();
        System.out.println("");
    }

    public void setHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Server IP: ");
        srvAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Server should listen on: ");
        srvPortHTTPS = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
        System.out.print("[HTTPS Configuration] Local IP: ");
        localAddrHTTPS = bufferedReader.readLine();
        System.out.println("");
        System.out.print("[HTTPS Configuration] Port the Handler should listen on: ");
        localPortHTTPS = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
        System.out.print("[HTTPS Configuration] Endpoint (without /): ");
        endpointHTTPS = "/" + bufferedReader.readLine();
        System.out.println("");
    }

    public void setAsyncHTTP() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTP Configuration] Grade of randomization for GET Requests (1-5): ");
        asynchrRandomizeGradeHTTP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
    }

    public void setAsyncHTTPS() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.print("[HTTPS Configuration] Grade of randomization for GET Requests (1-5): ");
        asynchrRandomizeGradeHTTP = Integer.parseInt(bufferedReader.readLine());
        System.out.println("");
    }
    public void setEncMode() {
        System.out.println("\n"
                          +"-----------"
                          +"|Encryption Mode - Configuration|"
                          +"-----------"
                          +"\n"
                          +"-<|Active Mode: " + encMode.name() + "|>-\n\n"
                          +"Set mode:\n"
                          +"\n"
                          +"aes         -   Change to AES in CBC Mode\n"
                          +"rsa         -   Change to RSA with 4096 keysize\n"
                          +"twofish     -   Change to Twofish\n"
                          +"blowfish    -   Change to Blowfish\n"
                          +"serpent     -   Change to Serpent\n"
                          +"tripledes   -   Change to TripleDES\n"
                          +"\n"
                          +"\n"
                          +"Type 'back' to go back\n"
                          +"\n");

        System.out.print("Revsn [CONFIG]-[ENCMODE] » ");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String decisionMode;
        try {
            decisionMode = bufferedReader.readLine();
        } catch (IOException e) {
            decisionMode = "back";
        }
        System.out.println();
        switch(decisionMode) {
            case("aes"): encMode = EncMode.AES; break;
            case("rsa"): encMode = EncMode.RSA; break;
            case("twofish"): encMode = EncMode.TWOFISH; break;
            case("blowfish"): encMode = EncMode.BLOWFISH; break;
            case("serpent"): encMode = EncMode.SERPENT; break;
            case("tripledes"): encMode = EncMode.TRIPLE_DES; break;
            case("back"): return;
            default: 
        }
        clearConsole();
        
    }

    public void setMode() {
        System.out.println("\n"
                          +"-----------"
                          +"|Mode - Configuration|"
                          +"-----------"
                          +"\n"
                          +"-<|Active Mode: " + mode.name() + "|>-\n\n"
                          +"Set mode:\n"
                          +"\n"
                          +"tcp         -   Change to TCP Mode\n"
                          +"udp         -   Change to UDP Mode\n"
                          +"http        -   Change to HTTP Mode\n"
                          +"https       -   Change to HTTPS Mode\n"
                          +"httpas      -   Change to Asynchronous HTTP Mode\n"
                          +"httpsas     -   Change to Asynchronous HTTPS Mode\n"
                          +"dns         -   Change to DNS Mode\n"
                          +"\n"
                          +"\n"
                          +"Type 'back' to go back\n"
                          +"\n");

        System.out.print("Revsn [CONFIG]-[MODE] » ");
        InputStreamReader inputStreamReaderMode = new InputStreamReader(System.in);
        BufferedReader bufferedReaderMode = new BufferedReader(inputStreamReaderMode);
        String decisionMode;
        try {
            decisionMode = bufferedReaderMode.readLine();
        } catch (IOException e) {
            decisionMode = "back";
        }
        System.out.println();
        switch(decisionMode) {
            case("tcp"): mode = Mode.TCP; break;
            case("udp"): mode = Mode.UDP; break;
            case("http"): mode = Mode.HTTP; break;
            case("https"): mode = Mode.HTTPS; break;
            case("httpas"): mode = Mode.HTTPAsync; break;
            case("httpsas"): mode = Mode.HTTPSAsync; break;
            case("dns"): mode = Mode.DNS; break;
            case("back"): return;
            default: 
        }
        clearConsole();
    } 

    /*
        Getter
    */

    public Mode getTCPConf() {
        return Mode.TCP;
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

    public EncMode getEncMode() {
        return this.encMode;
    }

    public Mode getMode() {
            return this.mode;
    }


    /*
        Menu
    */

    public void printOperational() throws IOException {
        
    }
    public void printConf() throws IOException {
        boolean back = false;
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        while(!back) {
            clearConsole();

            System.out.println("\n"
                              +"-----------"
                              +"|Configuration Menu|"
                              +"-----------\n"
                              +"\n"
                              +"-<|Active Mode: " + mode.name() + "|>-\n\n"
                              +"What do you want to configure?:\n"
                              +"0. Mode\n"
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
            String decision = bufferedReader.readLine();
            System.out.println();
            switch(decision) {
                case("0"):
                    clearConsole();
                    setMode();
                    break;
                case("1"):
                    boolean tcpBack = false;
                    while(!tcpBack) {  
                        clearConsole();
                        printTCP();
                        System.out.print("Revsn [CONFIG]-[TCP]» Wanna edit / Done? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): tcpBack = true; break;
                            case("y"): setTCP(); tcpBack = true; break;
                        }
                    }
                    break;
                case("2"):
                    boolean udpBack = false;
                    while(!udpBack) { 
                        clearConsole();  
                        printUDP();
                        System.out.print("Revsn [CONFIG]-[UDP]» Wanna edit / Done? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): udpBack = true; break;
                            case("y"): setUDP(); break;
                        }
                    }
                    break;
                case("3"):
                    boolean httpBack = false;
                    while(!httpBack) { 
                        clearConsole();
                        printHTTP();
                        System.out.print("Revsn [CONFIG]-[HTTP]» Wanna edit / Done? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): httpBack = true; break;
                            case("y"): setHTTP(); break;
                        }
                    }
                    break;
                case("4"):  
                    boolean httpsBack = false;
                    while(!httpsBack) { 
                        clearConsole();
                        printHTTPS();
                        System.out.print("Revsn [CONFIG]-[HTTPS]» Wanna edit / Done? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): httpsBack = true; break;
                            case("y"): setHTTPS(); break;
                        }
                    }
                    break;
                case("5"):
                    boolean httpAsBack = false;
                    while(!httpAsBack) { 
                        clearConsole();
                        printHTTPAS();
                        System.out.print("Revsn [CONFIG]-[HTTPAsync]» Wanna edit / Done? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): httpAsBack = true; break;
                            case("y"): setAsyncHTTP(); break;
                        }
                    }
                    break;
                case("6"):  
                    boolean httpsAsBack = false;
                    while(!httpsAsBack) { 
                        clearConsole();
                        printHTTPSAS();
                        System.out.print("Revsn [CONFIG]-[HTTPSAsync]» Wanna edit? (y/n): ");
                        String dec = bufferedReader.readLine();
                        switch(dec) {
                            case("n"): httpsAsBack = true; break;
                            case("y"): setAsyncHTTPS(); break;
                        }
                    }
                    break;
                case("7"):  

                    break;
                case("back"): clearConsole(); back = true; break;
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

    

    public final static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            }
            else {
                Runtime.getRuntime().exec("clear");
            }
        }
        catch (final Exception e) {
            //  Handle any exceptions.
        }
    }
}
