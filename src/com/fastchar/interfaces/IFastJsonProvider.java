package com.fastchar.interfaces;

import java.lang.reflect.Type;

public interface IFastJsonProvider {

    String toJson(Object value);

    <T> T fromJson(String json, Class<T> targetClass);

    <T> T fromJson(String json, Type targetType);
}
