package com.fastchar.exception;

public class FastClassException  extends RuntimeException{
    private static final long serialVersionUID = 3892724971970427300L;

    public FastClassException(String message) {
        super(message);
    }

    public FastClassException(Throwable cause) {
        super(cause);
    }
}
