package com.fastchar.extend.fastjson2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastJson;

import java.lang.reflect.Type;

/**
 * FastJson2 <a href="https://github.com/alibaba/fastjson2">FastJson2</a>
 */
@AFastClassFind("com.alibaba.fastjson2.JSON")
public class FastJson2Provider implements IFastJson {
    @Override
    public String toJson(Object value) {
        return JSON.toJSONString(value, FastChar.getConstant().getDateFormat(), JSONWriter.Feature.IgnoreErrorGetter);
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
