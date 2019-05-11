package com.fastchar.multipart;
/**
 * from com.oreilly.servlet.multipart
 */
public abstract class Part {
    private String name;

    Part(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isFile() {
        return false;
    }

    public boolean isParam() {
        return false;
    }
}


