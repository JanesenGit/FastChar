package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;

public class FastActionException extends Exception {
    public FastActionException(String message) {
        super(message);
    }

    public FastActionException(Throwable throwable) {
        super(throwable);
    }
}
