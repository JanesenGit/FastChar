package com.fastchar.exception;

public class FastTemplateException extends RuntimeException{

    private static final long serialVersionUID = -3225829631821884066L;

    public FastTemplateException(String message) {
        super(message);
    }

    public FastTemplateException(Throwable cause) {
        super(cause);
    }
}
