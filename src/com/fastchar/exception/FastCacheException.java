package com.fastchar.exception;

public class FastCacheException extends RuntimeException {
    public FastCacheException(String message) {
        super(message);
    }

    public FastCacheException(Throwable throwable) {
        super(throwable);
    }
}
