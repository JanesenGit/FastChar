package com.fastchar.core;

import com.fastchar.interfaces.IFastLogger;

/**
 * 日志工具，对接log4j
 *
 * @author 沈建（Janesen）
 * @date 2021/12/25 15:39
 */
public final class FastLogger {

    private final FastPrinter printer;

    FastLogger() {
        this.printer = new FastPrinter();
    }

    private IFastLogger getFastLoggerProvider() {
        return FastChar.getOverrides().singleInstance(false, IFastLogger.class);
    }

    private void log(String method, Class<?> targetClass, String message, Throwable throwable) {
        if (targetClass == null) {
            targetClass = FastLogger.class;
        }
        IFastLogger iFastLog = getFastLoggerProvider();
        if (method.equalsIgnoreCase("debug")) {
            if (iFastLog != null && iFastLog.debug(targetClass, message, throwable)) {
                return;
            }
            printer.debug(targetClass, message, throwable);
        } else if (method.equalsIgnoreCase("warn")) {
            if (iFastLog != null && iFastLog.warn(targetClass, message, throwable)) {
                return;
            }
            printer.warn(targetClass, message, throwable);
        } else if (method.equalsIgnoreCase("info")) {
            if (iFastLog != null && iFastLog.info(targetClass, message, throwable)) {
                return;
            }
            printer.info(targetClass, message, throwable);
        } else if (method.equalsIgnoreCase("error")) {
            if (iFastLog != null && iFastLog.error(targetClass, message, throwable)) {
                return;
            }
            printer.error(targetClass, message, throwable);
        }
    }

    public void debug(String message) {
        debug(null, message, null);
    }

    public void debug(Class<?> targetClass, String message) {
        debug(targetClass, message, null);
    }

    public void debug(Class<?> targetClass, Throwable throwable) {
        debug(targetClass, null, throwable);
    }

    public void debug(Class<?> targetClass, String message, Throwable throwable) {
        this.log("debug", targetClass, message, throwable);
    }


    public void warn(String message) {
        warn(null, message, null);
    }

    public void warn(Class<?> targetClass, String message) {
        warn(targetClass, message, null);
    }

    public void warn(Class<?> targetClass, Throwable throwable) {
        warn(targetClass, null, throwable);
    }

    public void warn(Class<?> targetClass, String message, Throwable throwable) {
        this.log("warn", targetClass, message, throwable);
    }


    public void error(Class<?> targetClass, String message) {
        error(targetClass, message, null);
    }

    public void error(String message) {
        error(null, message, null);
    }

    public void error(Class<?> targetClass, Throwable throwable) {
        error(targetClass, null, throwable);
    }

    public void error(Class<?> targetClass, String message, Throwable throwable) {
        this.log("error", targetClass, message, throwable);
    }

    public void info(String message) {
        info(null, message, null);
    }

    public void info(Class<?> targetClass, String message) {
        info(targetClass, message, null);
    }

    public void info(Class<?> targetClass, Throwable throwable) {
        info(targetClass, null, throwable);
    }

    public void info(Class<?> targetClass, String message, Throwable throwable) {
        this.log("info", targetClass, message, throwable);
    }
}
