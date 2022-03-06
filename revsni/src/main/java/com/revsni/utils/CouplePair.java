package com.revsni.utils;

public class CouplePair<K,V> {
    public K key;
    public V value;

    public CouplePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

}
