package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;

public class FastEntityException extends Exception {
    public FastEntityException(String message) {
        super(message);
    }
}
