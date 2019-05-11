package com.fastchar.core;

public class FastRequestParam {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public FastRequestParam setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public FastRequestParam setValue(String value) {
        this.value = value;
        return this;
    }
}
