package com.fastchar.utils;

import java.lang.reflect.*;

public class FastTypeUtils {


    public static Class<?> getMapping(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getMapping(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getMapping(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getMapping(upperBounds[0]);
            }
        }

        if (type instanceof GenericArrayType) {
            Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(genericComponentType);
            return getArrayClass(componentClass);
        }

        return Object.class;
    }

    public static Class<?> getArrayClass(Class<?> componentClass) {
        if (componentClass == int.class) {
            return int[].class;
        }
        if (componentClass == byte.class) {
            return byte[].class;
        }
        if (componentClass == short.class) {
            return short[].class;
        }
        if (componentClass == long.class) {
            return long[].class;
        }
        if (componentClass == String.class) {
            return String[].class;
        }
        if (componentClass == Object.class) {
            return Object[].class;
        }
        return Array.newInstance(componentClass, 1).getClass();
    }


    public static Class<?> getClass(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getClass(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type componentType = genericArrayType.getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            return getArrayClass(componentClass);
        }

        return Object.class;
    }
}
