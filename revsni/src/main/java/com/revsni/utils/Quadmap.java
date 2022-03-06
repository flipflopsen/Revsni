package com.revsni.utils;

public class Quadmap<K,G,V,E> {
    public K name;
    public G ipAddr;
    public V port;
    public E wild;

    public Quadmap(K name, G ipAddr, V port, E wild) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.port = port;
        this.wild = wild;
    }

    public K getPort() {
        return this.name;
    }

    public G getHandler() {
        return this.ipAddr;
    }

    public V getMode() {
        return this.port;
    }

    public E getSessionNumber() {
        return this.wild;
    }
    
}
