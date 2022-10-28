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
 * 获取对象的属性值
 *
 * @author 沈建（Janesen）
 * @date 2021/8/31 15:05
 */
public class FastObjectGetHandler {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private final Object target;
    private final Object property;

    public FastObjectGetHandler(Object target, Object property) {
        this.target = target;
        this.property = property;
    }

    public Object get() {
        if (property == null) {
            return target;
        }
        if (target == null) {
            return null;
        }
        String propertyStr = property.toString();
        if (FastStringUtils.isEmpty(propertyStr)) {
            return null;
        }
        //纯数字，认为提取数组的数据
        if (NUMBER_PATTERN.matcher(propertyStr).matches()) {
            int index = FastNumberUtils.formatToInt(property);
            if (target instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) target;
                int iteratorIndex = 0;
                for (Object iteratorValue : iterable) {
                    if (iteratorIndex == index) {
                        return iteratorValue;
                    }
                    iteratorIndex++;
                }
            } else if (FastArrayUtils.isArray(target)) {
                return Array.get(target, index);
            }
        }
        boolean isCheckSize = propertyStr.equalsIgnoreCase("length")
                || propertyStr.equalsIgnoreCase("size")
                || propertyStr.equalsIgnoreCase("count");
        if (FastArrayUtils.isArray(target)) {
            if (isCheckSize) {
                return Array.getLength(target);
            }
        }

        FastHandler handler = invokeAttrGet(property);
        if (handler.getCode() == 0) {
            return handler.get("value");
        }

        handler = invokeAttrGetMethod(property);
        if (handler.getCode() == 0) {
            return handler.get("value");
        }

        handler = invokeAttrMethod(property);
        if (handler.getCode() == 0) {
            return handler.get("value");
        }

        if (isCheckSize) {
            handler = invokeAttrMethod("size");
            if (handler.getCode() == 0) {
                return handler.get("value");
            }
        }
        return null;
    }


    private FastHandler invokeAttrGet(Object key) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (target == null) {
            return handler;
        }
        try {
            List<Method> getMethods = FastClassUtils.getMethod(target.getClass(), "get");
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 1) {
                    Class<?> parameterType = getMethod.getParameterTypes()[0];
                    if (parameterType.isAssignableFrom(String.class)) {
                        getMethod.setAccessible(true);
                        handler.setCode(0);
                        handler.set("value", getMethod.invoke(target, key.toString()));
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private FastHandler invokeAttrGetMethod(Object attrName) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (target == null) {
            return handler;
        }
        try {
            List<Method> getMethods = FastClassUtils.getMethod(target.getClass(), "get" + FastStringUtils.firstCharToUpper(attrName.toString()));
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 0) {
                    getMethod.setAccessible(true);
                    handler.setCode(0);
                    handler.set("value", getMethod.invoke(target));
                    return handler;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private FastHandler invokeAttrMethod(Object methodName) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (target == null) {
            return handler;
        }
        try {
            List<Method> getMethods = FastClassUtils.getMethod(target.getClass(), methodName.toString());
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 0) {
                    getMethod.setAccessible(true);
                    handler.setCode(0);
                    handler.set("value", getMethod.invoke(target));
                    return handler;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

}
