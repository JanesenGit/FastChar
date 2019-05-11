package com.fastchar.exception;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastRequestLog;


public class FastReturnException extends Error {
    public FastReturnException() {
        super(FastChar.getLocal().getInfo("Method_Error1"));
    }
}
