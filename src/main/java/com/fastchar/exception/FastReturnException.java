package com.fastchar.exception;

import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;

public class FastReturnException extends Error {
    private static final long serialVersionUID = 341313412330701767L;

    public FastReturnException() {
        super(FastChar.getLocal().getInfo(FastCharLocal.METHOD_ERROR1));
    }
}
