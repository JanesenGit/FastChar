package com.fastchar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 观察者注解，标注后，FastChar扫描会自动注册标注此注解的类，为观察者
 * 在使用FastOverrides创建实例时，为了保证对象能正常释放，当且仅当调用【单例singleInstance】时才会自动注册观察者
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AFastObserver {

    /**
     * 注册观察器时的优先级别
     */
    int priority() default AFastPriority.P_LOW;

}
