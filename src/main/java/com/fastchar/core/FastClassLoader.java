package com.fastchar.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.ProtectionDomain;

/**
 * 类加载器
 * @author 沈建（Janesen）
 */
public final class FastClassLoader extends URLClassLoader {

    private boolean closed;
    private String desc;

    public FastClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public FastClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public FastClassLoader(URL[] urls) {
        super(urls);
    }

    public FastClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            try {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass != null) {
                    return loadedClass;
                }

                Class<?> c = findClass(name);
                if (c != null) {
                    return c;
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
            }
            return super.loadClass(name, resolve);
        }
    }


    public Class<?> loadClass(String name,
                              byte[] b, int off, int len,
                              ProtectionDomain protectionDomain) {
        return super.defineClass(name, b, off, len, protectionDomain);
    }

    public Class<?> loadClass(String name, java.nio.ByteBuffer b,
                                         ProtectionDomain protectionDomain) {
        return super.defineClass(name, b, protectionDomain);
    }


    /**
     * 判断Class是否重新加载了
     *
     * @param targetClass 目标Class
     * @return 布尔值
     */
    public static boolean isRedefined(Class<?> targetClass) {
        return isRedefined(targetClass, targetClass.getName());
    }

    /**
     * 判断Class是否重新加载了
     *
     * @param targetClass 目标Class
     * @return 布尔值
     */
    public static boolean isRedefined(Class<?> targetClass, String targetClassName) {
        ClassLoader classLoader = targetClass.getClassLoader();
        try {
            Method findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            findLoadedClass.setAccessible(true);
            Object invoke = findLoadedClass.invoke(classLoader, targetClassName);
            findLoadedClass.setAccessible(false);
            if (invoke == null) {
                return true;
            }
            return invoke.hashCode() != targetClass.hashCode();
        } catch (Exception e) {
            FastChar.getLogger().error(FastClassLoader.class, e);
        }
        return false;
    }




    public boolean isClosed() {
        return closed;
    }

    public FastClassLoader setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public FastClassLoader setDesc(String desc) {
        this.desc = desc;
        return this;
    }
}
