package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * 类代理器异常信息标注，当无法获取到目标类的时候，将抛出自定义的异常信息
 * @author 沈建（Janesen）
 * @date 2020/6/2 20:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AFastOverrideError {
    String value();
}
