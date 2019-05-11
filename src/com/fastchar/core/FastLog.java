package com.fastchar.core;

import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastLog4jLog;

public final class FastLog {
    private boolean isLog4j;

    public FastLog() {
        isLog4j = FastClassUtils.getClass("org.apache.log4j.Logger", false) != null;
    }

    public void info(Class<?> targetClass, String message) {
        info(targetClass, message, null);
    }

    public void info(Class<?> targetClass, String message,Throwable throwable) {
        if (isLog4j) {
            FastLog4jLog.getLog(targetClass).info(message,throwable);
        }else{
            System.out.println("\n" + FastDateUtils.getDateString());
            System.out.println("[INFO]:" + lightStyle(message));
        }
    }

    public void error(Class<?> targetClass, String message) {
        error(targetClass, message, null);
    }

    public void error(Class<?> targetClass, String message,Throwable throwable) {
        if (isLog4j) {
            FastLog4jLog.getLog(targetClass).error(message,throwable);
        }else{
            System.err.println("\n" + FastDateUtils.getDateString());
            System.err.println("[ERROR]:" + errorStyle(message));
        }
    }

    public void warn(Class<?> targetClass, String message) {
        warn(targetClass, message, null);
    }

    public void warn(Class<?> targetClass, String message,Throwable throwable) {
        if (isLog4j) {
            FastLog4jLog.getLog(targetClass).warn(message,throwable);
        }else{
            System.out.println("\n" + FastDateUtils.getDateString());
            System.out.println("[WARN]:" + warnStyle(message));
        }
    }


    public String lightStyle(String content) {
        return "\033[32;1m" + content + "\033[0m";
    }

    public String softStyle(String content) {
        return "\033[36;1m" + content + "\033[0m";
    }

    public String warnStyle(String content) {
        return "\033[33;1m" + content + "\033[0m";
    }

    public String errorStyle(String content) {
        return "\033[31;1m" + content + "\033[0m";
    }

    public static void main(String[] args) {
        new FastLog().info(FastLog.class, "测试消息！");
    }


}
