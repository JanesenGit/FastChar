package com.fastchar.exception;

public class FastFindException extends Exception {
    private static final long serialVersionUID = -272296177321281401L;

    public FastFindException() {
    }

    public FastFindException(String message) {
        super(message);
    }

    public FastFindException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastFindException(Throwable cause) {
        super(cause);
    }

    public FastFindException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
