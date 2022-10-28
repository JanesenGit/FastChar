package com.fastchar.servlet.http.cache;

import java.io.Serializable;

public class FastResponseCacheHeader implements Serializable {
    private String name;
    private Object value;

    public String getName() {
        return name;
    }

    public FastResponseCacheHeader setName(String name) {
        this.name = name;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public FastResponseCacheHeader setValue(Object value) {
        this.value = value;
        return this;
    }
}
