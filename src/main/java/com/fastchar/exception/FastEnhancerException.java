package com.fastchar.exception;

public class FastEnhancerException extends RuntimeException {
    private static final long serialVersionUID = 2443853681011977165L;

    public FastEnhancerException() {
        super();
    }

    public FastEnhancerException(String message) {
        super(message);
    }

    public FastEnhancerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastEnhancerException(Throwable cause) {
        super(cause);
    }

    protected FastEnhancerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
