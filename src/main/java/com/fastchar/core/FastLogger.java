package com.fastchar.core;

import com.fastchar.interfaces.IFastLogger;

/**
 * 日志工具，可对接log4j
 * @author 沈建（Janesen）
 * @date 2021/12/25 15:39
 */
public final class FastLogger {
    FastLogger() {
    }

    public void debug(String message) {
        debug(null, message, null);
    }

    public void debug(Class<?> targetClass, String message) {
        debug(targetClass, message, null);
    }

    public void debug(Class<?> targetClass, String message, Throwable throwable) {
        IFastLogger iFastLog = FastChar.getOverrides().singleInstance(false, IFastLogger.class);
        if (iFastLog != null) {
            iFastLog.debug(targetClass, message, throwable);
            return;
        }
        FastChar.getLog().debug(targetClass, message, throwable);
    }


    public void warn(String message) {
        warn(null, message, null);
    }

    public void warn(Class<?> targetClass, String message) {
        warn(targetClass, message, null);
    }

    public void warn(Class<?> targetClass, String message, Throwable throwable) {
        IFastLogger iFastLog = FastChar.getOverrides().singleInstance(false, IFastLogger.class);
        if (iFastLog != null) {
            iFastLog.warn(targetClass, message, throwable);
            return;
        }
        FastChar.getLog().warn(targetClass, message, throwable);
    }


    public void error(Class<?> targetClass, String message) {
        error(targetClass, message, null);
    }

    public void error(String message) {
        error(null, message, null);
    }

    public void error(Class<?> targetClass, String message, Throwable throwable) {
        IFastLogger iFastLog = FastChar.getOverrides().singleInstance(false, IFastLogger.class);
        if (iFastLog != null) {
            iFastLog.error(targetClass, message, throwable);
            return;
        }
        FastChar.getLog().error(targetClass, message, throwable);
    }

    public void info(String message) {
        info(null, message, null);
    }

    public void info(Class<?> targetClass, String message) {
        info(targetClass, message, null);
    }

    public void info(Class<?> targetClass, String message, Throwable throwable) {
        IFastLogger iFastLog = FastChar.getOverrides().singleInstance(false, IFastLogger.class);
        if (iFastLog != null) {
            iFastLog.info(targetClass, message, throwable);
            return;
        }
        FastChar.getLog().info(targetClass, message, throwable);
    }
}
