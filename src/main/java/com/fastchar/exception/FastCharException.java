package com.fastchar.exception;

public class FastCharException extends RuntimeException {
    public FastCharException() {
    }

    public FastCharException(String message) {
        super(message);
    }

    public FastCharException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastCharException(Throwable cause) {
        super(cause);
    }

    public FastCharException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
