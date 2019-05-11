package com.fastchar.exception;

public class FastDatabaseInfoException extends RuntimeException {
    public FastDatabaseInfoException(String message) {
        super(message);
    }

    public FastDatabaseInfoException(Throwable throwable) {
        super(throwable);
    }
}
