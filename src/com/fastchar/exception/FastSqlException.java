package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;

public class FastSqlException extends RuntimeException {
    public FastSqlException(String message) {
        super(message);
    }

    public FastSqlException(Throwable cause) {
        super(cause);
    }
}
