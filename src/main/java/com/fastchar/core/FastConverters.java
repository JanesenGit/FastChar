package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.asm.FastParameter;
import com.fastchar.interfaces.IFastParamConverter;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 参数转换器
 */
@SuppressWarnings("UnusedReturnValue")
public class FastConverters {
    private List<Class<? extends IFastParamConverter>> paramConverters = new ArrayList<>();

    FastConverters() {
    }

    public FastConverters add(Class<? extends IFastParamConverter> converters) {
        if (!FastClassUtils.checkNewInstance(converters)) {
            return this;
        }
        paramConverters.add(converters);
        sortConverter();
        return this;
    }

    private void sortConverter() {
        Comparator<Class<? extends IFastParamConverter>> comparator = new Comparator<Class<? extends IFastParamConverter>>() {
            @Override
            public int compare(Class<? extends IFastParamConverter> o1, Class<? extends IFastParamConverter> o2) {
                int priority1 = 0;
                int priority2 = 0;

                if (o1.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority aFastPriority = o1.getAnnotation(AFastPriority.class);
                    priority1 = aFastPriority.value();
                }
                if (o2.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority aFastPriority = o2.getAnnotation(AFastPriority.class);
                    priority2 = aFastPriority.value();
                }
                return Integer.compare(priority2, priority1);
            }
        };
        Collections.sort(paramConverters, comparator);
    }



    public Object convertParam(FastAction action, FastParameter parameter) throws Exception {
        Object value = null;
        for (Class<? extends IFastParamConverter> paramConverter : paramConverters) {
            IFastParamConverter iFastParamConverter = FastChar.getOverrides().singleInstance(paramConverter);
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


    public void flush() {
        List<Class<? extends IFastParamConverter>> waitRemove = new ArrayList<>();
        for (Class<? extends IFastParamConverter> paramConverter : paramConverters) {
            if (FastClassUtils.isRelease(paramConverter)) {
                waitRemove.add(paramConverter);
            }
        }
        paramConverters.removeAll(waitRemove);
    }


}
