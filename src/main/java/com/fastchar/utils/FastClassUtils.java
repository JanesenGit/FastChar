package com.fastchar.utils;

import com.fastchar.core.FastClassLoader;
import com.fastchar.core.FastFile;
import com.fastchar.exception.FastClassException;
import com.fastchar.exception.FastOverrideException;
import org.terracotta.offheapstore.pinning.PinnableCache;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class FastClassUtils {
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    public static Class<?> getClass(String className) {
        return getClass(className, true);
    }
    public static Class<?> getClass(String className, boolean printException) {
        return getClass(FastClassUtils.class.getClassLoader(), className, printException);
    }

    public static Class<?> getClass(ClassLoader loader, String className, boolean printException) {
        try {
            if (className == null || className.length() == 0) {
                return null;
            }
            return Class.forName(className, true, loader);
        } catch (Throwable e) {
            if (printException) {
                e.printStackTrace();
            } else if (e instanceof UnsupportedClassVersionError) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static boolean checkNewInstance(Class targetClass) {
        if (Modifier.isAbstract(targetClass.getModifiers())) {
            return false;
        }
        if (Modifier.isInterface(targetClass.getModifiers())) {
            return false;
        }
        if (!Modifier.isPublic(targetClass.getModifiers())) {
            return false;
        }
        return true;
    }


    public static Class<?>[] toClass(Object... array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CLASS_ARRAY;
        } else {
            Class<?>[] classes = new Class[array.length];

            for (int i = 0; i < array.length; ++i) {
                classes[i] = array[i] == null ? null : array[i].getClass();
            }

            return classes;
        }
    }

    public static <T> T newInstance(Class<T> targetClass) {
        try {
            if (targetClass != null) {
                for (Constructor<?> declaredConstructor : targetClass.getDeclaredConstructors()) {
                    if (declaredConstructor != null && declaredConstructor.getTypeParameters().length == 0) {
                        declaredConstructor.setAccessible(true);
                        Object newInstance = declaredConstructor.newInstance();
                        declaredConstructor.setAccessible(false);
                        return (T) newInstance;
                    }
                }
                return targetClass.newInstance();
            }
        } catch (Exception e) {
            throw new FastClassException(e);
        }
        return null;
    }

    public static <T> T newInstance(String className) {
        Class<?> aClass = getClass(className);
        return (T) newInstance(aClass);
    }


    public static List<Method> getDeclaredMethod(Class targetClass, String name) {
        List<Method> methods = new ArrayList<>();
        try {
            for (Method declaredMethod : targetClass.getDeclaredMethods()) {
                if (declaredMethod.getName().equals(name)) {
                    methods.add(declaredMethod);
                }
            }
        } catch (Exception ignored) {
        }
        return methods;
    }


    public static <T> T newInstance(Class<T> targetClass, Object... constructorParams) {
        if (constructorParams.length > 0) {
            try {
                for (Constructor<?> declaredConstructor : targetClass.getDeclaredConstructors()) {
                    if (declaredConstructor.getParameterTypes().length == constructorParams.length) {
                        boolean matchParam = true;
                        for (int i = 0; i < declaredConstructor.getParameterTypes().length; i++) {
                            Class<?> parameterType = declaredConstructor.getParameterTypes()[i];
                            if (!parameterType.isAssignableFrom(constructorParams[i].getClass())) {
                                matchParam = false;
                                break;
                            }
                        }
                        if (matchParam) {
                            return (T) declaredConstructor.newInstance(constructorParams);
                        }
                    }
                }
            } catch (Exception e) {
                throw new FastClassException(e);
            }
            return newInstance(targetClass);
        }
        return newInstance(targetClass);
    }


    public static Object invokeMethod(Object object, Method declaredMethod, Object... params) throws Exception {
        List<Object> methodParams = new ArrayList<>();
        for (int i = 0; i < declaredMethod.getParameterTypes().length; i++) {
            if (i < params.length) {
                methodParams.add(params[i]);
            } else {
                methodParams.add(null);
            }
        }
        return declaredMethod.invoke(object, methodParams.toArray());
    }


    public static Field getDeclaredField(Class targetClass, String name) {
        if (targetClass == null) {
            return null;
        }
        if (targetClass == Object.class) {
            return null;
        }
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return getDeclaredField(targetClass.getSuperclass(), name);
    }


    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = FastClassUtils.class.getClassLoader();
        }
        return cl;
    }


    public static Class getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    public static Class getSuperClassGenricType(Class clazz, int index)
            throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    public static void deepCopy(Object fromSource, Object toSource) {
        try {
            for (Field field : fromSource.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object o = field.get(fromSource);
                if (o != null) {
                    Field declaredField = FastClassUtils.getDeclaredField(toSource.getClass(), field.getName());
                    if (declaredField != null) {
                        if (Modifier.isStatic(declaredField.getModifiers())) {
                            continue;
                        }
                        if (Modifier.isFinal(declaredField.getModifiers())) {
                            continue;
                        }
                        declaredField.setAccessible(true);
                        declaredField.set(toSource, o);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isSameRefined(Class<?> classA, Class<?> classB) {
        if (classB == null || classA == null) {
            return true;
        }
        return classA.getName().equals(classB.getName()) && classA != classB;
    }

    public static boolean isRelease(Class<?> targetClass) {
        if (targetClass.getClassLoader() instanceof FastClassLoader) {
            FastClassLoader fastClassLoader = (FastClassLoader) targetClass.getClassLoader();
            return fastClassLoader.isClosed();
        }
        return false;
    }


    public static boolean isRelease(Object targetObject) {
        if (targetObject == null) {
            return true;
        }
        if (targetObject.getClass().getClassLoader() instanceof FastClassLoader) {
            FastClassLoader fastClassLoader = (FastClassLoader) targetObject.getClass().getClassLoader();
            return fastClassLoader.isClosed();
        }
        return false;
    }


}

