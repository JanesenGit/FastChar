package com.fastchar.exception;

public class FastWebException  extends RuntimeException {
    public FastWebException() {
    }

    public FastWebException(String message) {
        super(message);
    }

    public FastWebException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastWebException(Throwable cause) {
        super(cause);
    }

    public FastWebException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
