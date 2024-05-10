package com.fastchar.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@interface AFastCheckRepeatable {
    AFastCheck[] value();
}

/**
 * FastAction路由方法形参的参数验证标注
 */
@Repeatable(AFastCheckRepeatable.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface AFastCheck {
    /**
     * 参数验证的规则，例如：@null
     * @return 验证规则数组
     */
    String[] value();

    /**
     * 传入验证器里的参数集合
     * @return 参数名数组
     */
    String[] arguments() default {};

}




