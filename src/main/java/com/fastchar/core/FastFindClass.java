package com.fastchar.core;

import com.fastchar.exception.FastFindException;
import com.fastchar.utils.FastClassUtils;

public final class FastFindClass {
    FastFindClass() {
    }

    public FastFindClass find(String className, String jarUrl) throws FastFindException {
        Class<?> aClass = FastClassUtils.getClass(className, false);
        if (aClass == null) {
            throw new FastFindException(FastChar.getLocal().getInfo("Class_Error4", className, jarUrl));
        }
        return this;
    }

}
