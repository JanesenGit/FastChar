package com.fastchar.core;

public class FastRequestParam {
    private String name;
    private String value;
    private boolean doSet;

    private boolean query;

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

    public boolean isDoSet() {
        return doSet;
    }

    public FastRequestParam setDoSet(boolean doSet) {
        this.doSet = doSet;
        return this;
    }

    public boolean isQuery() {
        return query;
    }

    public FastRequestParam setQuery(boolean query) {
        this.query = query;
        return this;
    }

    public String toQueryString() {
        return name + "=" + value;
    }
}
