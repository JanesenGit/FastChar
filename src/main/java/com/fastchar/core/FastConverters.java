package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.asm.FastParameter;
import com.fastchar.interfaces.IFastParamConverter;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 参数转换器
 * @author 沈建（Janesen）
 */
@SuppressWarnings("UnusedReturnValue")
public class FastConverters {

    FastConverters() {
    }


    public Object convertParam(FastAction action, FastParameter parameter) throws Exception {
        Object value = null;
        List<IFastParamConverter> iFastParamConverters = FastChar.getOverrides().singleInstances(false, IFastParamConverter.class);
        for (IFastParamConverter iFastParamConverter : iFastParamConverters) {
            if (iFastParamConverter == null) {
                continue;
            }
            FastHandler handler = new FastHandler();
            value = iFastParamConverter.convertValue(action, parameter, handler);
            if (handler.getCode() == 1) {
                break;
            }
        }
        return value;
    }



}
