package com.fastchar.annotation;

import com.fastchar.out.FastOut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastAction注解，标注此FastAction默认响应的类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastResponse {

    /**
     * 响应的类型
     * @return
     */
    FastOut.Type value();

}
