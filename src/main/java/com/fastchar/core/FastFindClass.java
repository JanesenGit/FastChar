package com.fastchar.core;

import com.fastchar.exception.FastFindException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;

public final class FastFindClass {
    FastFindClass() {
    }

    public FastFindClass find(String className, String jarUrl) throws FastFindException {
        return find(className, jarUrl, true);
    }

    public FastFindClass find(String className, String jarUrl,boolean throwException) throws FastFindException {
        Class<?> aClass = FastClassUtils.getClass(className, false);
        if (aClass == null) {
            if (throwException) {
                throw new FastFindException(FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR4, className, jarUrl));
            }else{
                FastChar.getLogger().error(this.getClass(),FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR4, className, jarUrl));
            }
        }
        return this;
    }

    public boolean test(String className) {
        Class<?> aClass = FastClassUtils.getClass(className, false);
        return aClass != null;
    }

}
