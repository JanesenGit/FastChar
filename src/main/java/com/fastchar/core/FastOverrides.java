package com.fastchar.core;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.database.operate.FastMySqlDatabaseOperateProvider;
import com.fastchar.database.operate.FastSqlServerDatabaseOperateProvider;
import com.fastchar.database.sql.FastMySql;
import com.fastchar.database.sql.FastOracle;
import com.fastchar.database.sql.FastSqlServer;
import com.fastchar.exception.FastOverrideException;
import com.fastchar.extend.c3p0.FastC3p0DataSourceProvider;
import com.fastchar.extend.druid.FastDruidDataSourceProvider;
import com.fastchar.extend.ehcache.FastEhCache2Provider;
import com.fastchar.extend.ehcache.FastEhCache3Provider;
import com.fastchar.extend.fastjson.FastJsonProvider;
import com.fastchar.extend.gson.FastGsonProvider;
import com.fastchar.extend.jdbc.FastJdbcDataSourceProvider;
import com.fastchar.extend.redis.FastRedisClusterProvider;
import com.fastchar.extend.redis.FastRedisNormalProvider;
import com.fastchar.interfaces.IFastLocal;
import com.fastchar.local.FastCharLocal_CN;
import com.fastchar.provider.FastColumnSecurity;
import com.fastchar.provider.FastFileRename;
import com.fastchar.provider.FastSecurity;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked", "UnusedReturnValue"})
public final class FastOverrides {
    private final List<ClassInfo> classes = new ArrayList<>();
    private final ConcurrentHashMap<String, List<String>> classMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> instanceMap = new ConcurrentHashMap<>();


    FastOverrides() {
        add(FastMySql.class);
        add(FastSqlServer.class);
        add(FastOracle.class);

        add(FastSecurity.class);
        add(FastColumnSecurity.class);
        add(FastFileRename.class);

        add(FastJdbcDataSourceProvider.class);
        add(FastC3p0DataSourceProvider.class);
        add(FastDruidDataSourceProvider.class);

        add(FastMySqlDatabaseOperateProvider.class);
        add(FastSqlServerDatabaseOperateProvider.class);

        add(FastGsonProvider.class);
        add(FastJsonProvider.class);

        add(FastEhCache3Provider.class);
        add(FastEhCache2Provider.class);
        add(FastRedisNormalProvider.class);
        add(FastRedisClusterProvider.class);

        add(FastCharLocal_CN.class);
    }

    private ClassInfo getClassInfo(Class<?> targetClass) {
        for (ClassInfo aClass : classes) {
            if (aClass.targetClass == targetClass) {
                return aClass;
            }
        }
        return null;
    }

    public FastOverrides add(Class<?> targetClass) {
        int priority = 0;
        if (targetClass.isAnnotationPresent(AFastPriority.class)) {
            AFastPriority fastPriority = targetClass.getAnnotation(AFastPriority.class);
            priority = fastPriority.value();
        }
        return add(targetClass, priority);
    }

    public FastOverrides add(Class<?> targetClass, int priority) {
        if (getClassInfo(targetClass) != null) {
            return this;
        }
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, false) == null) {
                    return this;
                }
            }
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.targetClass = targetClass;
        classInfo.priority = priority;
        classes.add(classInfo);
        sortClasses();
        return this;
    }

    public FastOverrides setPriority(Class<?> targetClass, int priority) {
        ClassInfo classInfo = getClassInfo(targetClass);
        if (classInfo == null) {
            return this;
        }
        classInfo.priority = priority;
        sortClasses();
        return this;
    }


    public FastOverrides removeClass(Class<?> targetClass) {
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, false) == null) {
                    return this;
                }
            }
        }
        List<String> strings = classMap.get(targetClass.getName());
        if (strings != null) {
            for (String string : strings) {
                instanceMap.remove(string);
            }
        }
        ClassInfo classInfo = getClassInfo(targetClass);
        if (classInfo != null) {
            classes.remove(classInfo);
        }
        return this;
    }


    public <T> T singleInstance(Class<T> targetClass, Object... constructorParams) {
        return singleInstance(true,targetClass,  constructorParams);
    }

    public <T> T singleInstance(boolean check, Class<T> targetClass, Object... constructorParams) {
        return singleInstance(check, null, targetClass, constructorParams);
    }


    public <T> T singleInstance(String onlyCode, Class<T> targetClass, Object... constructorParams) {
        return singleInstance(true, onlyCode, targetClass, constructorParams);
    }

    public <T> T singleInstance(boolean check, String onlyCode, Class<T> targetClass, Object... constructorParams) {
        targetClass = findClass(targetClass, constructorParams);
        if (!classMap.containsKey(targetClass.getName())) {
            classMap.put(targetClass.getName(), new ArrayList<String>());
        }
        if (FastStringUtils.isEmpty(onlyCode)) {
            onlyCode= FastMD5Utils.MD5(targetClass.getName() + Arrays.toString(constructorParams));
        }

        classMap.get(targetClass.getName()).add(onlyCode);
        if (instanceMap.containsKey(onlyCode)) {
            return (T) instanceMap.get(onlyCode);
        }
        T t = newInstance(check, targetClass, constructorParams);
        if (t == null) {
            return null;
        }
        if (t.getClass().isAnnotationPresent(AFastObserver.class)) {
            FastChar.getObservable().addObserver(t);
        }
        instanceMap.put(onlyCode, t);
        return t;
    }

    public <T> T newInstance(Class<T> targetClass, Object... constructorParams) {
        return newInstance(true, targetClass, constructorParams);
    }


    public <T> T newInstance(boolean check, Class<T> targetClass, Object... constructorParams) {
        if (targetClass == null) {
            throw new NullPointerException();
        }
        String superClass = targetClass.getName();
        String errorInfo = "can not get instance for " + targetClass.getName() + ":" + Arrays.toString(constructorParams);
        if (Modifier.isFinal(targetClass.getModifiers())) {
            T instance = FastClassUtils.newInstance(targetClass, constructorParams);
            if (instance == null) {
                if (check) {
                    throw new FastOverrideException(errorInfo);
                }
                return null;
            }
            return instance;
        }
        targetClass = findClass(targetClass, constructorParams);
        if (Modifier.isInterface(targetClass.getModifiers())) {
            if (check) {
                throw new FastOverrideException(errorInfo);
            }
            return null;
        }
        if (Modifier.isAbstract(targetClass.getModifiers())) {
            if (check) {
                throw new FastOverrideException(errorInfo);
            }
            return null;
        }
        if (Modifier.isPrivate(targetClass.getModifiers())) {
            if (check) {
                throw new FastOverrideException(errorInfo);
            }
            return null;
        }
        if (Modifier.isProtected(targetClass.getModifiers())) {
            if (check) {
                throw new FastOverrideException(errorInfo);
            }
            return null;
        }
        T instance = FastClassUtils.newInstance(targetClass, constructorParams);
        if (instance == null) {
            if (check) {
                throw new FastOverrideException(errorInfo);
            }
            return null;
        }

        if (!IFastLocal.class.isAssignableFrom(targetClass)) {
            if (!targetClass.getName().equals(superClass)) {
                if (FastChar.getConstant().isLogOverride()) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo("Override_Error2", superClass, targetClass.getName()));
                }
            }
        }
        return instance;
    }

    public <T> boolean check(Class<T> targetClass, Object... constructorParams) {
        targetClass = findClass(targetClass, constructorParams);
        if (Modifier.isInterface(targetClass.getModifiers())) {
            return false;
        }
        if (Modifier.isAbstract(targetClass.getModifiers())) {
            return false;
        }
        if (Modifier.isPrivate(targetClass.getModifiers())) {
            return false;
        }
        if (Modifier.isProtected(targetClass.getModifiers())) {
            return false;
        }
        return true;
    }


    private <T> Class<T> findClass(Class<T> targetClass, Object... constructorParams) {
        for (int r = classes.size() - 1; r >= 0; r--) {
            Class<?> aClass = classes.get(r).targetClass;
            if (targetClass.isAssignableFrom(aClass) && targetClass != aClass) {
                List<Method> overrideMethods = FastClassUtils.getDeclaredMethod(aClass, "isOverride");
                if (overrideMethods.size() > 0) {
                    Method method = overrideMethods.get(0);
                    List<Object> params = new ArrayList<>();
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        if (i < constructorParams.length) {
                            params.add(constructorParams[i]);
                        } else {
                            params.add(null);
                        }
                    }
                    try {
                        Object isOverride = method.invoke(aClass, params.toArray());
                        if (!FastBooleanUtils.formatToBoolean(isOverride, true)) {
                            continue;
                        }
                    } catch (Exception e) {
                        throw new FastOverrideException(e);
                    }
                }
                return (Class<T>) findClass(aClass, constructorParams);
            }
        }
        return targetClass;
    }

    private void sortClasses() {
        Collections.sort(classes, new Comparator<ClassInfo>() {
            @Override
            public int compare(ClassInfo o1, ClassInfo o2) {
                return Integer.compare(o1.priority, o2.priority);
            }
        });
    }


    private class ClassInfo {
        private int priority;
        private Class<?> targetClass;
    }

}
