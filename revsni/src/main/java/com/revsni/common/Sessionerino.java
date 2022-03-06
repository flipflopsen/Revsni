package com.revsni.common;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.revsni.common.Configuration.Mode;
import com.revsni.server.Interaction;
import com.revsni.utils.CouplePair;
import com.revsni.utils.Triplet;

public class Sessionerino {
    public int sessionNumberStart;
    public ArrayList<Triplet<Mode, Integer, String>> modePortUUID = new ArrayList<>();
    public ArrayList<CouplePair<String, String>> ipOs = new ArrayList<>();
    public ConcurrentHashMap<Integer, Interaction> sessionHandlers = new ConcurrentHashMap<>();

    public Sessionerino() {
        //Default constructor for serialization
    }

    public Sessionerino(int startSessionID, ArrayList<Triplet<Mode, Integer, String>> modePortUUID, ArrayList<CouplePair<String, String>> ipOs, ConcurrentHashMap<Integer, Interaction> sessionHandlers) {
        this.modePortUUID = modePortUUID;
        this.ipOs = ipOs;
        this.sessionHandlers = sessionHandlers;
        this.sessionNumberStart = startSessionID;
    }

    public ArrayList<Triplet<Mode, Integer, String>> getModePortUUID() {
        return this.modePortUUID;
    }

    public ArrayList<CouplePair<String, String>> getIpOs() {
        return this.ipOs;
    }

    public ConcurrentHashMap<Integer, Interaction> getSessionHandlers() {
        return this.sessionHandlers;
    }

    public int getSessionNumberStart() {
        return this.sessionNumberStart;
    }
}
