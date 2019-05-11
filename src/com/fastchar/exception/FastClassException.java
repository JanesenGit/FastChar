package com.fastchar.exception;

public class FastClassException  extends RuntimeException{
    public FastClassException(String message) {
        super(message);
    }

    public FastClassException(Throwable cause) {
        super(cause);
    }
}
