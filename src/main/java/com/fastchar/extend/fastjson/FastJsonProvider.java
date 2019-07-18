package com.fastchar.extend.fastjson;

import com.alibaba.fastjson.JSON;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastJson;

import java.lang.reflect.Type;

/**
 * FastJson https://github.com/alibaba/fastjson
 */
@AFastClassFind("com.alibaba.fastjson.JSON")
public class FastJsonProvider implements IFastJson {
    @Override
    public String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public <T> T fromJson(String json, Class<T> targetClass) {
        return JSON.parseObject(json, targetClass);
    }

    @Override
    public <T> T fromJson(String json, Type targetType) {
        return JSON.parseObject(json, targetType);
    }

}
