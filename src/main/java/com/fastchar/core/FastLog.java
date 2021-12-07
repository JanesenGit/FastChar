package com.fastchar.core;

import com.fastchar.utils.FastDateUtils;

/**
 * 日志操作类
 * @author 沈建（Janesen）
 */
public final class FastLog {

    FastLog() {
    }

    public void info(String message) {
        info(null, message, null);
    }

    public void info(Class<?> targetClass, String message) {
        info(targetClass, message, null);
    }

    public void info(Class<?> targetClass, String message,Throwable throwable) {
        if (FastChar.getConstant().isLog()) {
            System.out.println("\n" + FastDateUtils.getDateString());
            System.out.println("[INFO]:" + lightStyle(message));
        }
    }

    public void error(Class<?> targetClass, String message) {
        error(targetClass, message, null);
    }
    public void error(String message) {
        error(null, message, null);
    }

    public void error(Class<?> targetClass, String message,Throwable throwable) {
        if (FastChar.getConstant().isLog()) {
            System.err.println("\n" + FastDateUtils.getDateString());
            System.err.println("[ERROR]:" + errorStyle(message));
        }
    }

    public void warn(String message) {
        warn(null, message, null);
    }

    public void warn(Class<?> targetClass, String message) {
        warn(targetClass, message, null);
    }

    public void warn(Class<?> targetClass, String message,Throwable throwable) {
        if (FastChar.getConstant().isLog()) {
            System.out.println("\n" + FastDateUtils.getDateString());
            System.out.println("[WARN]:" + warnStyle(message));
        }
    }


    public String lightStyle(String content) {
        if (FastChar.getConstant().isAnsi()) {
            return "\033[32;1m" + content + "\033[0m";
        }
        return content;
    }

    public String softStyle(String content) {
        if (FastChar.getConstant().isAnsi()) {
            return "\033[36;1m" + content + "\033[0m";
        }
        return content;
    }

    public String warnStyle(String content) {
        if (FastChar.getConstant().isAnsi()) {
            return "\033[33;1m" + content + "\033[0m";
        }
        return content;
    }

    public String errorStyle(String content) {
        if (FastChar.getConstant().isAnsi()) {
            return "\033[31;1m" + content + "\033[0m";
        }
        return content;
    }

    public String tipStyle(String content) {
        if (FastChar.getConstant().isAnsi()) {
            return "\033[35;1m" + content + "\033[0m";
        }
        return content;
    }
}
