package com.fastchar.core;

import java.util.concurrent.ConcurrentHashMap;

public final class FastValues {
    private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    FastValues() {
    }

    public FastValues put(String key, Object value) {
        if (value == null || key == null) {
            return this;
        }
        values.put(key, value);
        return this;
    }

    public FastValues remove(String key) {
        values.remove(key);
        return this;
    }

    public <T> T get(String key) {
        return (T) values.get(key);
    }


}
