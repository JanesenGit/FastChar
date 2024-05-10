package com.fastchar.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@interface AFastHttpMethodRepeatable {
    AFastHttpMethod[] value();
}


/**
 * FastAction注解，可指定路由的Http方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Repeatable(AFastHttpMethodRepeatable.class)
public @interface AFastHttpMethod {
    String[] value() default "";
}

