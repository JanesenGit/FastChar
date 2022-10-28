package com.fastchar.exception;

public class FastResultSetException extends RuntimeException {
    private static final long serialVersionUID = 8179607995601141455L;

    public FastResultSetException(String message) {
        super(message);
    }

    public FastResultSetException(Exception e) {
        super(e);
    }

    public FastResultSetException(String message, Exception e) {
        super(message, e);
    }

    public FastResultSetException() {
    }
}
