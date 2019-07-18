package com.fastchar.exception;


public class FastActionException extends Exception {
    private static final long serialVersionUID = -7921349891207050087L;

    public FastActionException(String message) {
        super(message);
    }

    public FastActionException(Throwable throwable) {
        super(throwable);
    }
}
