package com.fastchar.core;

import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FastUrl {
    private String methodRoute;
    private List<String> urlParams = new ArrayList<>();
    private List<FastRequestParam> params = new ArrayList<>();
    private int level;
    public String getMethodRoute() {
        return methodRoute;
    }

    public String getMethodRouteIndex() {
        return FastStringUtils.stripEnd(methodRoute, "/") + "/index";
    }

    public FastUrl setMethodRoute(String methodRoute) {
        this.methodRoute = methodRoute;
        return this;
    }

    public List<String> getUrlParams() {
        return urlParams;
    }

    public FastUrl setUrlParams(List<String> urlParams) {
        this.urlParams = urlParams;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public FastUrl setLevel(int level) {
        this.level = level;
        return this;
    }

    public List<FastRequestParam> getParams() {
        return params;
    }

    public FastUrl setParams(List<FastRequestParam> params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        return "FastUrl{" +
                "methodRoute='" + methodRoute + '\'' +
                ", urlParams=" + urlParams +
                ", params=" + params +
                ", level=" + level +
                '}';
    }
}
