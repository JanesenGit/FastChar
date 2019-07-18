package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;

public class FastSqlException extends RuntimeException {
    private static final long serialVersionUID = -7519948369474426450L;

    public FastSqlException(String message) {
        super(message);
    }

    public FastSqlException(Throwable cause) {
        super(cause);
    }

    public FastSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
