package com.fastchar.interfaces;

import java.lang.reflect.Type;

/**
 * Json转换接口
 */
public interface IFastJson {

    /**
     * 将对象转成json字符串
     * @param value 对象值
     * @return json字符串
     */
    String toJson(Object value);

    /**
     * 将json字符串转对象
     * @param json json字符串
     * @param targetClass 目标类
     * @param <T> 泛型类型
     * @return 泛型对象
     */
    <T> T fromJson(String json, Class<T> targetClass);

    /**
     * 将json字符串转对象
     * @param json json字符串
     * @param targetType 目标类型
     * @param <T> 泛型类型
     * @return 泛型对象
     */
    <T> T fromJson(String json, Type targetType);
}
