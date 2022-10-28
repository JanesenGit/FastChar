package com.fastchar.exception;

import org.xml.sax.SAXException;

public class FastDatabaseException extends SAXException {
    private static final long serialVersionUID = 8179607995601141455L;

    public FastDatabaseException(String message) {
        super(message);
    }

    public FastDatabaseException(Exception e) {
        super(e);
    }

    public FastDatabaseException(String message, Exception e) {
        super(message, e);
    }

    public FastDatabaseException() {
    }
}
