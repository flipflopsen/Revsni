package com.revsni.utils;

public class Triplet<K,G,V> {
    public K name;
    public G ipAddr;
    public V port;

    public Triplet(K name, G ipAddr, V port) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.port = port;
    }

    public K getName() {
        return this.name;
    }

    public G getIpAddr() {
        return this.ipAddr;
    }

    public V getPort() {
        return this.port;
    }
    
}
