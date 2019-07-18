package com.fastchar.exception;

public class FastOverrideException extends RuntimeException {
    private static final long serialVersionUID = 5413547741979443815L;

    public FastOverrideException(String message) {
        super(message);
    }

    public FastOverrideException(Throwable throwable) {
        super(throwable);
    }
}
