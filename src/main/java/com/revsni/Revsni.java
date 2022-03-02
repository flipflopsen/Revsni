package com.revsni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.revsni.common.Configuration;
import com.revsni.server.Server;

@Deprecated
public class Revsni {

    private static Configuration configuration = new Configuration();
    public static void main(String[] args) throws IOException {
        System.out.println("\n"
                          +"-----------"
                          +"|Welcome|"
                          +"-----------"
                          +"\n"
                          +"Choose an option:\n"
                          +"1. Edit Host and Payload Configuration\n"
                          +"2. Start a Listener and Handler\n"
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
            case("2"): listenHandle(); break;
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

    public static void listenHandle() throws IOException {
        clearConsole();
        try {
            Server.main(new String[1]);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
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
            //  Handle any exceptions.
        }
    }
}   
