package com.fastchar.core;

import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式读取对象的值
 *
 * @author 沈建（Janesen）
 * @date 2021/8/20 10:02
 */
public class FastObjectExecute {
    private final Object value;

    public FastObjectExecute(Object value) {
        this.value = value;
    }

    public Object execute(String expression) {
        String regStr = "\\$\\{(.*)}";
        Matcher matcher = Pattern.compile(regStr).matcher(expression);
        if (matcher.find()) {
            String[] inAttrs = matcher.group(1).split("\\.");
            String firstInAttrs = inAttrs[0];
            if (inAttrs.length > 1) {
                Object value = doInvoke(firstInAttrs);
                if (value != null) {
                    String[] nextAttr = FastArrayUtils.subarray(inAttrs, 1, inAttrs.length);
                    return new FastObjectExecute(value).execute("${" + FastStringUtils.join(nextAttr, ".") + "}");
                }
            }
            return doInvoke(firstInAttrs);
        }
        return null;
    }

    private Object doInvoke(String attr) {
        try {
            if (FastStringUtils.isEmpty(attr)) {
                return value;
            }
            String regStr = "(.*)\\[(\\d+)]";
            String realAttr = attr;
            int index = -1;
            Matcher matcher = Pattern.compile(regStr).matcher(attr);
            if (matcher.find()) {
                realAttr = matcher.group(1);
                index = FastNumberUtils.formatToInt(matcher.group(2));
            }
            if (index >= 0) {
                Object arrayValue = doInvoke(realAttr);
                if (isArray(arrayValue)) {
                    return Array.get(arrayValue, index);
                } else if (arrayValue instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) arrayValue;
                    int iteratorIndex = 0;
                    for (Object iteratorValue : iterable) {
                        if (iteratorIndex == index) {
                            return iteratorValue;
                        }
                        iteratorIndex++;
                    }
                }
            } else {
                if (isArray(value)) {
                    if (realAttr.equalsIgnoreCase("length") || realAttr.equalsIgnoreCase("size")) {
                        return Array.getLength(value);
                    }
                }
                FastHandler handler = invokeGet(realAttr);
                if (handler.getCode() == 0) {
                    return handler.get("value");
                }

                handler = invokeGetAttr(realAttr);
                if (handler.getCode() == 0) {
                    return handler.get("value");
                }

                handler = invokeMethodAttr(realAttr);
                if (handler.getCode() == 0) {
                    return handler.get("value");
                }
                if (realAttr.equalsIgnoreCase("length")) {
                    handler = invokeMethodAttr("size");
                    if (handler.getCode() == 0) {
                        return handler.get("value");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private FastHandler invokeGet(String key) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        try {
            List<Method> getMethods = FastClassUtils.getMethod(value.getClass(), "get");
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 1) {
                    Class<?> parameterType = getMethod.getParameterTypes()[0];
                    if (parameterType.isAssignableFrom(String.class)) {
                        handler.setCode(0);
                        handler.set("value", getMethod.invoke(value, key));
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private FastHandler invokeGetAttr(String key) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        try {
            List<Method> getMethods = FastClassUtils.getMethod(value.getClass(), "get" + FastStringUtils.firstCharToUpper(key));
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 0) {
                    handler.setCode(0);
                    handler.set("value", getMethod.invoke(value));
                    return handler;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private FastHandler invokeMethodAttr(String key) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        try {
            List<Method> getMethods = FastClassUtils.getMethod(value.getClass(), key);
            for (Method getMethod : getMethods) {
                if (Modifier.isStatic(getMethod.getModifiers())) {
                    continue;
                }
                if (getMethod.getParameterTypes().length == 0) {
                    handler.setCode(0);
                    handler.set("value", getMethod.invoke(value));
                    return handler;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }

    private boolean isArray(Object value) {
        return (value instanceof Array) || value.getClass().isArray();
    }

}
