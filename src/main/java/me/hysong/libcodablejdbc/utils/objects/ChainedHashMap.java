package me.hysong.libcodablejdbc.utils.objects;

import java.util.LinkedHashMap;

public class ChainedHashMap<K, V> extends LinkedHashMap<K, V> {

    public ChainedHashMap<K, V> append(K key, V value) {
        super.put(key, value);
        return this;
    }

}
