package com.fastchar.annotation;

import com.fastchar.interfaces.IFastInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastAfter {
    Class<? extends IFastInterceptor>[] value();
    int priority() default 0;
}
