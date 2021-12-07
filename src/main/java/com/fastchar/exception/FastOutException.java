package com.fastchar.exception;

public class FastOutException extends Exception{

    public FastOutException(String message) {
        super(message);
    }

    public FastOutException(Throwable throwable) {
        super(throwable);
    }
}
