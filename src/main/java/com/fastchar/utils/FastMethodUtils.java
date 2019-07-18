package com.fastchar.utils;

import java.lang.reflect.Method;

public class FastMethodUtils {

    public static boolean isOverride(Method declaredMethod) {
        try {
            declaredMethod.getDeclaringClass().getSuperclass().getDeclaredMethod(declaredMethod.getName());
            return true;
        } catch (NoSuchMethodException ignored) {}
        return false;
    }
}
