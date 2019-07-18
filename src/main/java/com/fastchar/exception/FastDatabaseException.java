package com.fastchar.exception;

import org.xml.sax.SAXException;

public class FastDatabaseException extends SAXException {
    private static final long serialVersionUID = 8179607995601141455L;

    public FastDatabaseException(String message) {
        super(message);
    }
}
