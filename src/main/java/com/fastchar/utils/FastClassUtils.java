package com.fastchar.utils;

import com.fastchar.core.FastClassLoader;
import com.fastchar.exception.FastClassException;

import java.io.InputStream;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

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
        return findClass(loader, className, printException);
    }


    public static Class<?> findClass(String className) {
        return findClass(className, true);
    }

    public static Class<?> findClass(String className, boolean printException) {
        return findClass(FastClassUtils.class.getClassLoader(), className, printException);
    }

    public static Class<?> findClass(ClassLoader loader, String className, boolean printException) {
        try {
            if (className == null || className.length() == 0) {
                return null;
            }
            Method findClass = ClassLoader.class.getDeclaredMethod("findClass", String.class);
            findClass.setAccessible(true);
            Object invoke = findClass.invoke(loader, className);
            findClass.setAccessible(false);
            if (invoke != null) {
                return (Class<?>) invoke;
            }
        } catch (Throwable e) {
            if (printException) {
                e.printStackTrace();
            } else if (e instanceof UnsupportedClassVersionError) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static boolean checkNewInstance(Class<?> targetClass) {
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


    public static List<Method> getDeclaredMethod(Class<?> targetClass, String name) {
        List<Method> methods = new ArrayList<>(16);
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

    public static List<Method> getMethod(Class<?> targetClass, String methodName) {
        List<Method> methods = new ArrayList<>(16);
        try {
            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    methods.add(method);
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
                    int length = declaredConstructor.getParameterTypes().length;
                    if (length == constructorParams.length) {
                        boolean matchParam = true;
                        for (int i = 0; i < length; i++) {
                            if (constructorParams[i] == null) {
                                continue;
                            }
                            Class<?> parameterType = declaredConstructor.getParameterTypes()[i];
                            if (!parameterType.isAssignableFrom(constructorParams[i].getClass())) {
                                matchParam = false;
                                break;
                            }
                        }
                        if (matchParam) {
                            declaredConstructor.setAccessible(true);
                            return (T) declaredConstructor.newInstance(constructorParams);
                        }
                    }
                }
            } catch (Exception e) {
                throw new FastClassException(e);
            }
        }
        return newInstance(targetClass);
    }


    public static Object invokeMethod(Object object, Method declaredMethod, Object... params) throws Exception {
        List<Object> methodParams = new ArrayList<>(5);
        int length = declaredMethod.getParameterTypes().length;
        for (int i = 0; i < length; i++) {
            if (i < params.length) {
                methodParams.add(params[i]);
            } else {
                methodParams.add(null);
            }
        }
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(object, methodParams.toArray());
    }

    public static Object safeInvokeMethod(Object object, Method declaredMethod, Object... params) {
        try {
            return invokeMethod(object, declaredMethod, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object invokeMethod(Object target, String name, Object... params) throws Exception {
        List<Class<?>> paramsClass = new ArrayList<>();
        for (Object param : params) {
            paramsClass.add(param.getClass());
        }
        Method declaredMethod = target.getClass().getDeclaredMethod(name, paramsClass.toArray(new Class[]{}));
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(target, params);
    }

    public static Object safeInvokeMethod(Object target, String name, Object... params) {
        try {
            return invokeMethod(target, name, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Field getDeclaredField(Class<?> targetClass, String name) {
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


    public static Class<?> getSuperClassGenericType(Class<?> clazz) {
        return getSuperClassGenericType(clazz, 0);
    }

    public static Class<?> getSuperClassGenericType(Class<?> clazz, int index)
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
        return (Class<?>) params[index];
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


    /**
     * 相同的类名，但是类加载器不同
     *
     * @param classA
     * @param classB
     * @return
     */
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


    public static boolean isSameClass(Class<?> classA, Class<?> classB) {
        if (classA == null || classB == null) {
            return false;
        }
        if (classA == classB) {
            return true;
        }
        return isSameRefined(classA, classB);
    }


    public static String readClassResource(Class<?> targetClass, String sourceName) {
        try {
            InputStream resourceAsStream = targetClass.getResourceAsStream(sourceName);
            if (resourceAsStream != null) {
                List<String> list = FastFileUtils.readLines(resourceAsStream, StandardCharsets.UTF_8);
                String content = FastStringUtils.join(list, "");
                FastFileUtils.closeQuietly(resourceAsStream);
                return content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<Class<?>> getAllSuperClasses(Class<?> targetClass) {
        List<Class<?>> classes = new ArrayList<>(Arrays.asList(targetClass.getInterfaces()));
        Class<?> superclass = targetClass.getSuperclass();
        if (superclass != null) {
            classes.add(superclass);
            classes.addAll(getAllSuperClasses(superclass));
        }
        return classes;
    }


    public static <T> JarFile findClassJarFile(Class<T> targetClass) {
        try {
            URL url = targetClass.getResource(targetClass.getSimpleName() + ".class");
            if (url != null) {
                String protocol = url.getProtocol();
                if ("jar".equals(protocol)) {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    return jarURLConnection.getJarFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

