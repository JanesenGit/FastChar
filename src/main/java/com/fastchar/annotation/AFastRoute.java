package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由标注，可标注在FastAction和内部公开的路由方法上，用作追加其他路由地址 指向相同的FastAction的路由方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AFastRoute {
    /**
     * 其他额外的路由地址
     * @return 路由地址数组
     */
    String[] value() default {};

    /**
     * 是否以追加到当前路由地址的头部 默认：false
     * @return boolean
     */
    boolean head() default false;

    /**
     * 是否允许跨域 默认：false
     * @return boolean
     */
    boolean cross() default false;

    /**
     * 设置允许跨域的域名
     * @return 域名数组
     */
    String[] crossDomains() default {};
}
