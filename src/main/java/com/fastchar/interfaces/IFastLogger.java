package com.fastchar.interfaces;

/**
 * @author 沈建（Janesen）
 * @date 2021/12/25 14:50
 */
public interface IFastLogger {

    boolean debug(Class<?> targetClass, String message, Throwable throwable);

    boolean info(Class<?> targetClass, String message, Throwable throwable);

    boolean error(Class<?> targetClass, String message, Throwable throwable);

    boolean warn(Class<?> targetClass, String message, Throwable throwable);

}
