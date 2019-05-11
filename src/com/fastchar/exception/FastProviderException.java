package com.fastchar.exception;

public class FastProviderException extends RuntimeException {
    public FastProviderException(String message) {
        super(message);
    }

    public FastProviderException(Throwable throwable) {
        super(throwable);
    }
}
