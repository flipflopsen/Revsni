package com.revsni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revsni.common.Configuration;
import com.revsni.common.Configuration.Mode;
import com.revsni.server.Server;
import com.revsni.utils.ThreadMonitor;
import com.revsni.utils.SessionInfo;


public class Revsni {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static volatile boolean active = false;
    public static volatile boolean activeHelper = true;
    public static Thread serverino = null;
    public static volatile ThreadMonitor threadMonitor = new ThreadMonitor();
    public static int sessionNumber = 0;


    private static Configuration configuration = new Configuration();
    public static void main(String[] args) throws IOException {
        Server servero = null;
        
        System.out.println("\n"
                          +"-----------"
                          +"|Welcome|"
                          +"-----------"
                          +"\n"
                          +"Choose an option:\n"
                          +"1. Edit Host and Payload Configuration\n"
                          +"2. Start a Listener and Handler / Foreground if already active\n"
                          +"3. Start the Builder\n"
                          +"\n"
                          +"Type 'exit' to leave\n"
                          +"\n");

        System.out.print("Revsn [MENU] » ");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String decision = bufferedReader.readLine();
        System.out.println();
        switch(decision) {
            case("1"): configs(); break;
            case("2"): try {
                    if(servero == null) {
                        try {
                            configuration.getMode();
                            sessionNumber++;
                            servero = new Server(Mode.TCP.lHost, Mode.TCP.lPort, "lol123", "lol123", threadMonitor, sessionNumber);
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
                            servero = null;
                        }
                    }
                    listenHandle(servero);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                } break;
            case("3"): break;
            case("exit"): System.exit(1); break;
            default:
        }
        bufferedReader.close();
        inputStreamReader.close();
    }

    public static void configs() throws IOException {
        clearConsole();
        configuration.printConf();


        clearConsole();
        main(new String[1]);
    }

    public static void listenHandle(Server serveroi) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //ExecutorService executorService = Executors.newCachedThreadPool();
        clearConsole();
        activeHelper = false;
        if(serverino != null && serverino.getState().equals(Thread.State.WAITING)) {
            System.out.println("Notifying Thread..");
            synchronized(threadMonitor) {
                threadMonitor.notify();
            }
        }
        if(!active) {
            active = true;
            serverino = new Thread(serveroi);
            System.out.println("Starting Thread..");
            serverino.start();
            System.out.println("Thread started!");
        }

        while(!activeHelper) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {}
        }

        clearConsole();
        main(new String[1]);
    }

    public static void builder() throws IOException {
        clearConsole();

        
        clearConsole();
        main(new String[1]);
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
            
        }
    }

    public static void setActive(boolean state) {
        active = state;
    }

    public static void setActiveHelper(boolean state) {
        activeHelper = state;
    }
}   
