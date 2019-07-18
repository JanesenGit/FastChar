package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastEntity注解，用来标注是否启用此FastEntity
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AFastEntity {
    /**
     * 是否启用FastEntity 默认：true
     * @return 布尔值
     */
    boolean value() default true;
}
