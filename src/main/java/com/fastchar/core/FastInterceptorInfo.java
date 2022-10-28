package com.fastchar.core;

public class FastInterceptorInfo<T> {
   int priority;
   int index;
   int firstMethodLineNumber;
   int lastMethodLineNumber;
    String url;
   Class<? extends T> interceptor;

    public int getPriority() {
        return priority;
    }

    public FastInterceptorInfo<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public FastInterceptorInfo<T> setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getFirstMethodLineNumber() {
        return firstMethodLineNumber;
    }

    public FastInterceptorInfo<T> setFirstMethodLineNumber(int firstMethodLineNumber) {
        this.firstMethodLineNumber = firstMethodLineNumber;
        return this;
    }

    public int getLastMethodLineNumber() {
        return lastMethodLineNumber;
    }

    public FastInterceptorInfo<T> setLastMethodLineNumber(int lastMethodLineNumber) {
        this.lastMethodLineNumber = lastMethodLineNumber;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public FastInterceptorInfo<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public Class<? extends T> getInterceptor() {
        return interceptor;
    }

    public FastInterceptorInfo<T> setInterceptor(Class<? extends T> interceptor) {
        this.interceptor = interceptor;
        return this;
    }
}
