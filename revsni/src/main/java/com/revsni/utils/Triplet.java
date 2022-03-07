package com.revsni.utils;

public class Triplet<K,V,A> {
    public K key;
    public V value;
    public A addition;

    public Triplet(K key, V value, A addition) {
        this.key = key;
        this.value = value;
        this.addition = addition;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public A getAddition() {
        return this.addition;
    }
    
}
