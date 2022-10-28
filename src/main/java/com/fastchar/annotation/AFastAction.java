package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastAction类配置注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AFastAction {

    /**
     * 是否启用FastAction 默认 启用
     * @return 布尔值
     */
    boolean enable() default true;
}
