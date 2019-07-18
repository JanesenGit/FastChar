package com.fastchar.exception;

import javax.servlet.ServletException;

public class FastWebException  extends ServletException {
    private static final long serialVersionUID = 6685331861357178775L;

    public FastWebException(String message) {
        super(message);
    }
    public FastWebException(Throwable rootCause) {
        super(rootCause);
    }
}
