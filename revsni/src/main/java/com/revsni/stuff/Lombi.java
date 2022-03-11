package com.revsni.stuff;

import java.util.HashMap;
import java.util.UUID;

import com.revsni.utils.Quadmap;
import com.revsni.utils.Triplet;

public class Lombi {

    public static final UUID uuid = UUID.randomUUID();
    
    //<SessionNumber<Name, IP, Port<OS, Randomization, UUID>>>
    public static HashMap<Integer, Quadmap<String, String, Integer, Triplet<String, Integer, UUID>>> stuff = new HashMap<>();

    public static void main(String[] args) {
        Lombi nice = new Lombi();
        System.out.println(nice.getMySessionPlease(uuid));
        System.out.println(nice.getMyUUIDPlease(10));
    }

    public void addStuff() {
        Triplet<String, Integer, UUID> trip = new Triplet<String,Integer,UUID>("linux", 2, uuid);
        Quadmap<String, String, Integer, Triplet<String, Integer, UUID>> quad = new Quadmap<String,String,Integer,Triplet<String,Integer,UUID>>("flip", "127.0.0.1", Integer.valueOf(1337), trip);
        stuff.put(10, quad);
    }

    public Integer getMySessionPlease(UUID uuid) {
        int ret = 0;
        //TODO: Implement this Method.

        return ret;
    }

    public UUID getMyUUIDPlease(Integer session) {
        String ret = "";
        //TODO: Implement this Method.
        
        return UUID.fromString(ret);
    }

}
