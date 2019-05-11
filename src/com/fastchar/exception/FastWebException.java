package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;

import javax.servlet.ServletException;

public class FastWebException  extends ServletException {
    public FastWebException(String message) {
        super(message);
    }
    public FastWebException(Throwable rootCause) {
        super(rootCause);
    }
}
