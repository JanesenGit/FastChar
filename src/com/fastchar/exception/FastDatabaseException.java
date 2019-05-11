package com.fastchar.exception;

import com.fastchar.core.FastRequestLog;
import org.xml.sax.SAXException;

public class FastDatabaseException extends SAXException {
    public FastDatabaseException(String message) {
        super(message);
    }
}
