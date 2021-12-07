package com.fastchar.core;

import java.util.LinkedHashMap;

public final class FastHandler extends FastMapWrap {

    public FastHandler() {
        this.setMap(new LinkedHashMap<>());
    }

    public int getCode() {
        return getInt("code", 0);
    }

    public FastHandler setCode(int code) {
        set("code", code);
        return this;
    }

    public String getError() {
        return getString("error");
    }

    public FastHandler setError(String error) {
        set("error", error);
        return this;
    }

}
