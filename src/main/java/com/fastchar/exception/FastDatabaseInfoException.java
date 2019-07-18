package com.fastchar.exception;

public class FastDatabaseInfoException extends RuntimeException {
    private static final long serialVersionUID = 2602916209137745383L;

    public FastDatabaseInfoException(String message) {
        super(message);
    }

    public FastDatabaseInfoException(Throwable throwable) {
        super(throwable);
    }
}
