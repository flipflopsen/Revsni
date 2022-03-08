package com.revsni.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Base64;

import com.revsni.server.encryption.AES;
import com.revsni.server.encryption.Encri;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

public class Updater {

    //private static final Logger logger = LogManager.getLogger(Updater.class);
    //private Logger LOG = parentLogger;
    

    private String output;

    private String[] address = new String[2];
    private String shellType;
    private Encri initEncri;
    private AES aesReal;


    public Updater(String ip, int port, AES init) {
        address[0] = ip;
        address[1] = Integer.toString(port);
        initEncri = init;
        aesReal = init;
        this.shellType = "TCP";

    }


    public String generateOutputStringC() {
        output = null;
        output = address[0] + ";" + address[1] + ";" + Base64.getEncoder().encodeToString(shellType.getBytes()) + ";" + Base64.getEncoder().encodeToString(aesReal.getKey().getEncoded()) + ";" + Base64.getEncoder().encodeToString(aesReal.getIV().getIV());
        return output;
    }
    public String generateOutputString() {
        output = null;
        output = address[0] + ";" + address[1] + ";" + Base64.getEncoder().encodeToString(shellType.getBytes()) + ";" + Base64.getEncoder().encodeToString(aesReal.getKey().getEncoded()) + ";" + Base64.getEncoder().encodeToString(aesReal.getIV().getIV()) + ";" + aesReal.getPassword()+ ";" + Base64.getEncoder().encodeToString(aesReal.getSalt().getBytes());
        return output;
    }

    public boolean writeOut() throws IOException {
        try {
            Writer fileWriter = new FileWriter("revsni/filehosting/initial.txt");
            fileWriter.write(output);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean writeOutC() throws IOException {
        try {
            Writer fileWriter = new FileWriter("revsni/filehosting/initialC.txt");
            fileWriter.write(output);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean writeOut(String filename) throws IOException {
        try {
            Writer fileWriter = new FileWriter("revsni/filehosting/" + filename + ".txt");
            fileWriter.write(output);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void setShellType(String type, String port) {
        this.shellType = type;
        this.address[1] = port;
    }

    public Encri getInitEnc() {
        return this.initEncri;
    }

}
