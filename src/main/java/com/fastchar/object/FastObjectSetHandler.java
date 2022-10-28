package com.fastchar.object;

import com.fastchar.core.FastHandler;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 沈建（Janesen）
 * @date 2021/8/31 15:09
 */
public class FastObjectSetHandler {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private final Object target;
    private final Object property;

    public FastObjectSetHandler(Object target, Object property) {
        this.target = target;
        this.property = property;
    }

    public void set(Object value) {
        if (target == null) {
            return;
        }
        if (property == null) {
            return;
        }
        //纯数字，认为提前数组的数据
        String propertyStr = property.toString();
        if (FastStringUtils.isEmpty(propertyStr)) {
            return;
        }

        if (NUMBER_PATTERN.matcher(propertyStr).matches()) {
            int index = FastNumberUtils.formatToInt(property);
            if (FastArrayUtils.isArray(target)) {
                Array.set(target, index, value);
                return;
            } else if (target instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) target;
                list.set(index, value);
                return;
            }
        }

        FastHandler handler = invokeAttr("put", propertyStr, value);
        if (handler.getCode() == 0) {
            return;
        }
        handler = invokeAttr("add", propertyStr, value);
        if (handler.getCode() == 0) {
            return;
        }
        handler = invokeAttr("set", propertyStr, value);
        if (handler.getCode() == 0) {
            return;
        }
        handler = invokeAttrSetMethod(propertyStr, value);
        if (handler.getCode() == 0) {
            return;
        }
        invokeAttrMethod(propertyStr, value);
    }


    private FastHandler invokeAttr(String methodName, Object key, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (key == null || FastStringUtils.isEmpty(key.toString())) {
            return handler;
        }
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), methodName);
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 2) {
                    if (putMethod.getParameterTypes()[0].isAssignableFrom(String.class)
                            && (putMethod.getParameterTypes()[1].isAssignableFrom(Object.class) || putMethod.getParameterTypes()[1].isAssignableFrom(value.getClass()))) {
                        handler.setCode(0);
                        putMethod.setAccessible(true);
                        putMethod.invoke(target, key.toString(), value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private FastHandler invokeAttrSetMethod(Object key, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (key == null || FastStringUtils.isEmpty(key.toString())) {
            return handler;
        }
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), "set" + FastStringUtils.firstCharToUpper(key.toString()));
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 1) {
                    if (putMethod.getParameterTypes()[0].isAssignableFrom(Object.class) || putMethod.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                        handler.setCode(0);
                        putMethod.setAccessible(true);
                        putMethod.invoke(target, key.toString(), value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }


    private FastHandler invokeAttrMethod(Object key, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (key == null || FastStringUtils.isEmpty(key.toString())) {
            return handler;
        }
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), key.toString());
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 1) {
                    if (putMethod.getParameterTypes()[0].isAssignableFrom(Object.class) || putMethod.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                        handler.setCode(0);
                        putMethod.setAccessible(true);
                        putMethod.invoke(target, key.toString(), value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }


}
