package com.fastchar.object;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastHandler;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author 沈建（Janesen）
 * @date 2021/8/31 15:13
 */
@SuppressWarnings("UnusedReturnValue")
public class FastObjectAddHandler {

    private transient final Object target;
    private transient final Object property;

    public FastObjectAddHandler(Object target, Object property) {
        this.target = target;
        this.property = property;
    }

    public void add(Object value) {
        add(-1, value);
    }

    public void add(int index, Object value) {
        Object propertyValue = new FastObjectGetHandler(target, property).get();
        if (FastArrayUtils.isArray(propertyValue)) {
            List<Object> objects = new ArrayList<>(5);
            int length = Array.getLength(propertyValue);
            for (int i = 0; i < length; i++) {
                objects.add(Array.get(propertyValue, i));
            }
            if (index < 0) {
                objects.add(value);
            } else {
                objects.add(index, value);
            }
            new FastObjectSetHandler(target, property).set(objects);
        } else {
            FastHandler handler = invokeAttrAddIndexMethod(propertyValue, index, value);
            if (handler.getCode() == 0) {
                return;
            }
            invokeAttrAddMethod(propertyValue, value);
        }
    }


    public void addKeyValue(String key, Object value) {
        Object propertyValue = new FastObjectGetHandler(target, property).get();

        FastHandler handler = invokeAttrAddKeyValueMethod("put", propertyValue, key, value);
        if (handler.getCode() == 0) {
            return;
        }
        handler = invokeAttrAddKeyValueMethod("addProperty", propertyValue, key, value);
        if (handler.getCode() == 0) {
            return;
        }
        handler = invokeAttrAddKeyValueMethod("add", propertyValue, key, value);
        if (handler.getCode() == 0) {
            return;
        }
        Map<String, Object> objectMap = new HashMap<>(16);
        objectMap.put(key, value);
        add(objectMap);
    }



    private FastHandler invokeAttrAddMethod(Object target, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), "add");
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 1) {
                    if (putMethod.getParameterTypes()[0].isAssignableFrom(Object.class) || putMethod.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                        handler.setCode(0);
                        putMethod.invoke(target, value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return handler;
    }

    private FastHandler invokeAttrAddIndexMethod(Object target, int index, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        if (index < 0) {
            return handler;
        }
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), "add");
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 2) {
                    if ((putMethod.getParameterTypes()[0].isAssignableFrom(int.class) || putMethod.getParameterTypes()[0].isAssignableFrom(Integer.class))
                            && (putMethod.getParameterTypes()[1].isAssignableFrom(Object.class) || putMethod.getParameterTypes()[1].isAssignableFrom(value.getClass()))) {
                        handler.setCode(0);
                        putMethod.invoke(target, index, value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return handler;
    }


    private FastHandler invokeAttrAddKeyValueMethod(String methodName, Object target, Object key, Object value) {
        FastHandler handler = new FastHandler();
        handler.setCode(-1);
        try {
            List<Method> putMethods = FastClassUtils.getMethod(target.getClass(), methodName);
            for (Method putMethod : putMethods) {
                if (Modifier.isStatic(putMethod.getModifiers())) {
                    continue;
                }
                if (putMethod.getParameterTypes().length == 2) {
                    if (putMethod.getParameterTypes()[0].isAssignableFrom(String.class)
                            && (putMethod.getParameterTypes()[1].isAssignableFrom(Object.class)
                            || putMethod.getParameterTypes()[1].isAssignableFrom(value.getClass()))) {
                        handler.setCode(0);
                        putMethod.invoke(target, key, value);
                        return handler;
                    }
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return handler;
    }
}
