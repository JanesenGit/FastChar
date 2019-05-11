package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastPriority {
    int P_LOW = 1;
    int P_NORMAL = 2;
    int P_HIGH = 3;

    /**
     * 级别
     *
     * @return 默认返回 1
     */
    int value() default P_LOW;
}
