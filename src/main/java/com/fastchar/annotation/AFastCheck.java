package com.fastchar.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FastAction路由方法形参的参数验证标注
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface AFastCheck {
    /**
     * 参数验证的规则，例如：@null:参数不可为空
     * @return 验证规则数组
     */
    String[] value();

    /**
     * 指定参数名验证
     * @return 参数名数组
     */
    String[] params() default {};
}
