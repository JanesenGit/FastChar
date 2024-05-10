package com.fastchar.core;

import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;

public final class FastUrl {
    private String methodRoute;
    private List<String> urlParams = new ArrayList<>(16);
    private List<FastRequestParam> params = new ArrayList<>(16);
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

    public String toRequestUrl() {
        StringBuilder stringBuilder = new StringBuilder(methodRoute);
        if (!urlParams.isEmpty()) {
            stringBuilder.append("/").append(FastStringUtils.join(urlParams, "/"));
        }
        return stringBuilder.toString();
    }

    public String getQueryString(String queryString) {
        List<String> urlQueryList = new ArrayList<>();
        if (FastStringUtils.isNotEmpty(queryString)) {
            urlQueryList.add(queryString);
        }
        for (FastRequestParam param : this.params) {
            if (param.isQuery()) {
                //是否来自url中携带的参数
                urlQueryList.add(param.toQueryString());
            }
        }
        return FastStringUtils.join(urlQueryList, "&");
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
