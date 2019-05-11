package com.fastchar.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class FastParameter {
    private String name;
    private Class<?> type;
    private int index;
    private Type parameterizedType;
    private Annotation[] annotations;

    public String getName() {
        return name;
    }

    public FastParameter setName(String name) {
        this.name = name;
        return this;
    }

    public Class<?> getType() {
        return type;
    }

    public FastParameter setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public Type getParameterizedType() {
        return parameterizedType;
    }

    public FastParameter setParameterizedType(Type parameterizedType) {
        this.parameterizedType = parameterizedType;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public FastParameter setIndex(int index) {
        this.index = index;
        return this;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public FastParameter setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
        return this;
    }
}
