package com.fastchar.exception;

public class FastProviderException extends RuntimeException {
    private static final long serialVersionUID = 5478746520897500839L;

    public FastProviderException(String message) {
        super(message);
    }

    public FastProviderException(Throwable throwable) {
        super(throwable);
    }
}
