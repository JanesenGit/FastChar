package com.fastchar.utils;

import java.io.Serializable;
import java.io.Writer;

/**
 * from org.apache.commons.io
 */
public class FastStringBuilderWriter extends Writer implements Serializable {
    private final StringBuilder builder;

    public FastStringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    public FastStringBuilderWriter(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public FastStringBuilderWriter(StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    public Writer append(char value) {
        this.builder.append(value);
        return this;
    }

    public Writer append(CharSequence value) {
        this.builder.append(value);
        return this;
    }

    public Writer append(CharSequence value, int start, int end) {
        this.builder.append(value, start, end);
        return this;
    }

    public void close() {
    }

    public void flush() {
    }

    public void write(String value) {
        if (value != null) {
            this.builder.append(value);
        }

    }

    public void write(char[] value, int offset, int length) {
        if (value != null) {
            this.builder.append(value, offset, length);
        }

    }

    public StringBuilder getBuilder() {
        return this.builder;
    }

    public String toString() {
        return this.builder.toString();
    }
}

