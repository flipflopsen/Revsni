package com.revsni.utils;

public class SessionInfo<K,G,V,M,T> {
    public K name;
    public G ipAddr;
    public V port;
    public M mode;
    public T thread;

    public SessionInfo(K name, G ipAddr, V port, M mode, T thread) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.port = port;
        this.mode = mode;
        this.thread = thread;
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

    public M getMode() {
        return this.mode;
    }

    public T getThread() {
        return this.thread;
    }

    
}
