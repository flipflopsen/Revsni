package com.revsni;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.revsni.common.Configuration;
import com.revsni.common.Configuration.Mode;
import com.revsni.server.Server;
import com.revsni.stuff.ShutDownHookThread;
import com.revsni.utils.ThreadMonitor;


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
    public static int sessionNumberStart = 1;
    public static boolean loaded = false;
    public static Server servero = null;
    public static volatile boolean serverError = false;


    private static Configuration configuration = new Configuration();
    public static void main(String[] args) throws IOException {
        /*
        if(args.length < 2) {
            printUsage();
            System.exit(0);
        } else if(args.length == 3) {
            printUsage();
            System.exit(0);
        }
        */

        Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());
        
        System.out.println("\n"
                          +"-----------"
                          +"|Welcome|"
                          +"-----------"
                          +"\n"
                          +"Choose an option:\n"
                          +"1. Edit Host and Payload Configuration\n"
                          +"2. Start a Listener and Handler / Foreground if already active\n"
                          +"3. Start the Builder\n"
                          +"4. Save Sessions to a File\n"
                          +"5. Load Sessions from a File\n"
                          +"6. Clear Filehost Folder\n"
                          +"7. Clear Keys Folder\n"
                          +"0. Usage\n"
                          +"\n"
                          +"Type 'exit' to leave\n"
                          +"\n");

        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        System.out.println();
        menu(bufferedReader);
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
            } catch (InterruptedException e) {

            }
        }
        if(serverError) {
            serverino.interrupt();
            serverino = null;
            servero = null;
            threadMonitor = new ThreadMonitor();
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

    //create method to clear folder revsni/filehost
    public static void clearFilehost() throws IOException {
        File folder = new File("revsni/filehosting");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if(file.isDirectory()) {
                for (File lul : file.listFiles()) {
                    if(!lul.isDirectory()) {
                        lul.delete();
                    }
                }
            } else {
                file.delete();
            }
            
        }
        System.out.println("Filehost folder cleared!");
        clearConsole();
        main(new String[1]);
    }
    //create method to clear folder revsni/keys
    public static void clearKeyFolder() throws IOException {
        File folder = new File("revsni/keys");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if(file.isDirectory()) {
                for (File lul : file.listFiles()) {
                    if(!lul.isDirectory()) {
                        lul.delete();
                    }
                }
            } else {
                file.delete();
            }
            
        }
        System.out.println("Keys folder cleared!");
        clearConsole();
        main(new String[1]);
    }

    public static void setServerError() {
        serverError = true;
    }

    public static void setActive(boolean state) {
        active = state;
    }

    public static void setActiveHelper(boolean state) {
        activeHelper = state;
    }

    private static void printUsage() {
        System.out.println("\n"
                          +"-----------"
                          +"|Usage|"
                          +"-----------"
                          +"\n"
                          +"For now Revsni needs AES for initial connection\n you don't have to specify a password\n  nor a salt\n   but they will be generated and saved into a file called inital.txt!\n"
                          +"\nMinimal:\n\tjava -jar server.jar <Server-IP> <Server-Port>\n"
                          +"\nRecommendend:\n\tjava -jar server.jar <Server-IP> <Server-Port> <Password> <Salt>\n"
                          +"\nIt's okay to not specify a salt as it will be generated anyways. :) \n"
                          +"\n");
    }

    public static void menu(BufferedReader bufferedReader) throws IOException {
        System.out.print("Revsn [MENU] » ");
        BufferedReader buffRead = bufferedReader;
        String decision = buffRead.readLine();
        switch(decision) {
            case("0"): printUsage(); break;
            case("1"): configs(); break;
            case("2"): try {
                    if(servero == null) {
                        try {
                            configuration.getMode();
                            servero = new Server("lol123", "lol123", threadMonitor, sessionNumberStart, loaded, configuration);
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
                            servero = null;
                        }
                    }
                    listenHandle(servero);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    System.out.println("Exception");
                    e.printStackTrace();
                } break;
            case("3"): break;
            case("4"): 
                if(servero == null) {
                    System.out.println("Please start a Listener first before you want to save sessions.");
                } else if(serverino != null && serverino.getState().equals(Thread.State.WAITING)) {
                    System.out.print("Specify a filename to save sessions (leave empty for standard name): ");
                    String name = bufferedReader.readLine();
                    if(name.equals("")) {
                        servero.saveSessions();
                        break;
                    } else {
                        servero.saveSessions(name);
                        break;
                    }
                }
            case("5"): 
                System.out.print("Specify a filename to load sessions (leave empty for standard name): ");
                String name = bufferedReader.readLine();
                try {
                    servero = new Server("lol123", "lol123", threadMonitor, sessionNumberStart, true, configuration);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if(name.equals("")) {
                    if(servero.loadSessions()) {
                        loaded = true;
                    } else {
                        loaded = false;
                    }
                    
                } else {
                    if(servero.loadSessions(name)) {
                        loaded = true;
                    } else {
                        loaded = false;
                    }
                }
                if(loaded) {
                    try {
                        listenHandle(servero);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        
                    }
                }
                break;
            case("6"): clearFilehost(); break;
            case("7"): clearKeyFolder(); break;
            case("exit"): System.exit(1); break;
            default:
                menu(buffRead);
        }
    }


}   
