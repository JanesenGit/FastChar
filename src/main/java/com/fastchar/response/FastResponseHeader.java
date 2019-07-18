package com.fastchar.response;

import java.io.Serializable;

public class FastResponseHeader implements Serializable {
    private String name;
    private Object value;

    public String getName() {
        return name;
    }

    public FastResponseHeader setName(String name) {
        this.name = name;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public FastResponseHeader setValue(Object value) {
        this.value = value;
        return this;
    }
}
