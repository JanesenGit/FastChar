package com.fastchar.exception;

public class FastOverrideException extends RuntimeException {
    public FastOverrideException(String message) {
        super(message);
    }

    public FastOverrideException(Throwable throwable) {
        super(throwable);
    }
}
