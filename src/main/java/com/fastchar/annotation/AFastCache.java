package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastAction的缓存注解，一般标注在路由方法或FastAction类上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AFastCache {

    /**
     * 是否启用 默认 true
     * @return 布尔值
     */
    boolean enable() default true;


    /**
     * 缓存的标签
     * @return String
     */
    String tag() default "";


    /**
     * 缓存的key
     * @return String
     */
    String key() default "";

    /**
     * 是否检测IFastChar缓存插件是否已启用 默认：false
     * @return 布尔值
     */
    boolean checkClass() default false;

    /**
     * 是否检测项目为debug环境，如果为debug环境将不使用缓存，默认：false
     * @return 布尔值
     */
    boolean checkDebug() default false;

    /**
     * 缓存过期时间，单位：秒，默认-1 永不过期
     * @return 过期时间
     */
    long timeout() default -1;//单位秒

}
