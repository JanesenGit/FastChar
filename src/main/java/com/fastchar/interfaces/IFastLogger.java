package com.fastchar.interfaces;

/**
 * @author 沈建（Janesen）
 * @date 2021/12/25 14:50
 */
public interface IFastLogger {

    void debug(Class<?> targetClass, String message, Throwable throwable);

    void info(Class<?> targetClass, String message, Throwable throwable);

    void error(Class<?> targetClass, String message, Throwable throwable);

    void warn(Class<?> targetClass, String message, Throwable throwable);

}
