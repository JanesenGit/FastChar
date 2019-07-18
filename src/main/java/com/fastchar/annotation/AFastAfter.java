package com.fastchar.annotation;

import com.fastchar.interfaces.IFastInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastChar拦截器注解，用作标注拦截请求后的动作
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastAfter {

    /**
     * 实现IFastInterceptor的拦截器
     * @return 拦截器类数组
     */
    Class<? extends IFastInterceptor>[] value();

    /**
     * 拦截器的优先级
     * @return 优先级数字 ，数字越大级别越高
     */
    int priority() default 0;
}
