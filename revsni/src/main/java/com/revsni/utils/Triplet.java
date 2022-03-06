package com.revsni.utils;

public class Triplet<K,V,A> {
    public K name;
    public V ipAddr;
    public A port;

    public Triplet(K name, V ipAddr, A port) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.port = port;
    }

    public K getKey() {
        return this.name;
    }

    public V getValue() {
        return this.ipAddr;
    }

    public A getAddition() {
        return this.port;
    }
    
}
