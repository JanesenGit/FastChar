package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类检测注解，一般用在代理类检测，当注册待代理器，FastChar会自动检测标注的类名，如果不存在则取消注册此代理类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AFastClassFind {
    /**
     * 需要检测的类名
     * @return 类名
     */
    String[] value();
}
