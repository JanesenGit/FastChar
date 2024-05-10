package com.fastchar.core;

import com.fastchar.utils.FastConsoleUtils;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastStringUtils;

/**
 * 内置日志
 *
 * @author 沈建（Janesen）
 */
public final class FastPrinter {

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
        print("debug", targetClass, softStyle(message), throwable);
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
        print("info", targetClass, lightStyle(message), throwable);
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
        print("error", targetClass, errorStyle(message), throwable);
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
        print("warn", targetClass, warnStyle(message), throwable);
    }


    private void print(String level, Class<?> targetClass, String message, Throwable throwable) {
        String classSimpleName = FastLogger.class.getName();
        if (targetClass != null) {
            classSimpleName = targetClass.getName();
        }

        if (FastStringUtils.isNotEmpty(classSimpleName)) {
            System.out.println("\n" + colorStyle(0, "[" + classSimpleName + "] " + FastDateUtils.getDateString()));
        }else{
            System.out.println("\n" + colorStyle(0,FastDateUtils.getDateString()));
        }
        if (FastStringUtils.isEmpty(message)) {
            message = FastStringUtils.getThrowableMessage(throwable);
        }
        if (FastStringUtils.isEmpty(message)) {
            message = "";
        }
        System.out.println(colorStyle(7, "[" + level.toUpperCase() + "] ") + message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }


    public String lightStyle(String content) {
        return colorStyle(34, content);
    }

    public String softStyle(String content) {
        return colorStyle(44, content);
    }

    public String warnStyle(String content) {
        return colorStyle(11, content);
    }

    public String errorStyle(String content) {
        return colorStyle(9, content);
    }



    /**
     * 颜色打印控制台
     * @param colorValue 颜色值 1-256
     * @param content 打印的内容
     * @return 打印的内容
     */
    public String colorStyle(int colorValue, String content) {
        if (!FastChar.getConstant().isAnsi()) {
            return content;
        }
        return FastConsoleUtils.colorStyle(colorValue, content);
    }

}
