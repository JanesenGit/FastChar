package com.fastchar.exception;

public class FastFindClassException extends RuntimeException {

    public FastFindClassException() {
    }

    public FastFindClassException(String message) {
        super(message);
    }

    public FastFindClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastFindClassException(Throwable cause) {
        super(cause);
    }

    public FastFindClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
