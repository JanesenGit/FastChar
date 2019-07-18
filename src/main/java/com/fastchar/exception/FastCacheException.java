package com.fastchar.exception;

public class FastCacheException extends RuntimeException {
    private static final long serialVersionUID = -6750897673553402953L;

    public FastCacheException(String message) {
        super(message);
    }

    public FastCacheException(Throwable throwable) {
        super(throwable);
    }
}
