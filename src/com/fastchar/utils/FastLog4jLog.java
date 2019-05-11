package com.fastchar.utils;

import com.fastchar.core.FastRequestLog;
import org.apache.log4j.*;

public class FastLog4jLog {
    private org.apache.log4j.Logger log;
    private static final String callerFQCN = FastLog4jLog.class.getName();
    private FastLog4jLog(Class<?> clazz) {
        try {
            log = org.apache.log4j.Logger.getLogger(clazz);
        } catch (Throwable ignored) {}
    }
    static {
        try {
            Logger root = Logger.getRootLogger();
            ConsoleAppender newAppender = new ConsoleAppender(new PatternLayout("%n%-d{yyyy-MM-dd HH:mm:ss}%n[%p]-: %m%n"));
            root.addAppender(newAppender);
        } catch (Exception ignored) {
        }
    }

    private FastLog4jLog(String name) {
        log = org.apache.log4j.Logger.getLogger(name);
    }

    public static FastLog4jLog getLog(Class<?> clazz) {
        return new FastLog4jLog(clazz);
    }

    public static FastLog4jLog getLog(String name) {
        return new FastLog4jLog(name);
    }

    public void info(String message) {
        log.log(callerFQCN, Level.INFO, message, null);
    }

    public void info(String message, Throwable t) {
        log.log(callerFQCN, Level.INFO, message, t);
    }

    public void debug(String message) {
        log.log(callerFQCN, Level.DEBUG,  message, null);
    }

    public void debug(String message, Throwable t) {
        log.log(callerFQCN, Level.DEBUG, message, t);
    }

    public void warn(String message) {
        log.log(callerFQCN, Level.WARN,message, null);
    }

    public void warn(String message, Throwable t) {
        log.log(callerFQCN, Level.WARN, message, t);
    }

    public void error(String message) {
        log.log(callerFQCN, Level.ERROR, message, null);
    }

    public void error(String message, Throwable t) {
        log.log(callerFQCN, Level.ERROR,  message, t);
    }

    public void fatal(String message) {
        log.log(callerFQCN, Level.FATAL, message, null);
    }

    public void fatal(String message, Throwable t) {
        log.log(callerFQCN, Level.FATAL, message, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isEnabledFor(Level.WARN);
    }

    public boolean isErrorEnabled() {
        return log.isEnabledFor(Level.ERROR);
    }

    public boolean isFatalEnabled() {
        return log.isEnabledFor(Level.FATAL);
    }
}
