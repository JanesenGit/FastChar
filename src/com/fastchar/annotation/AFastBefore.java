package com.fastchar.annotation;

import com.fastchar.interfaces.IFastInterceptor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastBefore {
    Class<? extends IFastInterceptor>[] value();
    int priority() default 0;
}
