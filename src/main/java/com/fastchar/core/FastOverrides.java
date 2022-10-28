package com.fastchar.core;

import com.fastchar.accepter.*;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastOverrideError;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.asm.FastMethodRead;
import com.fastchar.converters.*;
import com.fastchar.database.operate.FastMySqlDatabaseOperateProvider;
import com.fastchar.database.operate.FastOracleDatabaseOperateProvider;
import com.fastchar.database.operate.FastSqlServerDatabaseOperateProvider;
import com.fastchar.database.provider.FastColumnSecurity;
import com.fastchar.database.provider.FastSimpleDataSourceProvider;
import com.fastchar.database.sql.FastMySql;
import com.fastchar.database.sql.FastOracle;
import com.fastchar.database.sql.FastSqlServer;
import com.fastchar.exception.FastFindClassException;
import com.fastchar.exception.FastOverrideException;
import com.fastchar.extend.c3p0.FastC3p0DataSourceProvider;
import com.fastchar.extend.caffeine.FastCaffeineProvider;
import com.fastchar.extend.druid.FastDruidDataSourceProvider;
import com.fastchar.extend.ehcache.FastEhCache2Provider;
import com.fastchar.extend.ehcache.FastEhCache3Provider;
import com.fastchar.extend.fastjson.FastJsonProvider;
import com.fastchar.extend.gson.FastGsonProvider;
import com.fastchar.extend.jdbc.FastJdbcDataSourceProvider;
import com.fastchar.extend.redis.FastRedisClusterProvider;
import com.fastchar.extend.redis.FastRedisNormalProvider;
import com.fastchar.local.FastCharLocal;
import com.fastchar.local.FastCharLocal_CN;
import com.fastchar.provider.FastMemoryCacheProvider;
import com.fastchar.provider.FastSecurityProvider;
import com.fastchar.servlet.FastFileRenameProvider;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;
import com.fastchar.validators.FastNullValidator;
import com.fastchar.validators.FastRegularValidator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastChar核心类，所有类代理器，线程安全
 *
 * @author 沈建（Janesen）
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
public final class FastOverrides {
    private final List<ClassInfo> classes = new ArrayList<>(50);

    private final Map<Object, List<ClassInfo>> mapClasses = new HashMap<>(16);

    private final ConcurrentHashMap<String, Object> instanceMap = new ConcurrentHashMap<>(16);

    //排除的父类，支持匹配符
    private final String[] excludePackages = new String[]{"java.lang.Object"};

    FastOverrides() {
        add(FastMethodRead.class);

        add(FastMySql.class);
        add(FastSqlServer.class);
        add(FastOracle.class);

        add(FastSecurityProvider.class);
        add(FastColumnSecurity.class);
        add(FastFileRenameProvider.class);

        add(FastSimpleDataSourceProvider.class);
        add(FastJdbcDataSourceProvider.class);
        add(FastC3p0DataSourceProvider.class);
        add(FastDruidDataSourceProvider.class);

        add(FastMySqlDatabaseOperateProvider.class);
        add(FastOracleDatabaseOperateProvider.class);
        add(FastSqlServerDatabaseOperateProvider.class);

        add(FastGsonProvider.class);
        add(FastJsonProvider.class);

        add(FastEhCache3Provider.class);
        add(FastEhCache2Provider.class);
        add(FastRedisNormalProvider.class);
        add(FastRedisClusterProvider.class);

        add(FastMemoryCacheProvider.class);
        add(FastCaffeineProvider.class);

        add(FastCharLocal_CN.class);

        add(FastEntityParamConverter.class);
        add(FastStringParamConverter.class);
        add(FastDateParamConverter.class);
        add(FastFileParamConverter.class);
        add(FastNumberParamConverter.class);
        add(FastBooleanParamConverter.class);
        add(FastEnumParamConverter.class);
        add(FastNormalParamConverter.class);

        add(FastNullValidator.class);
        add(FastRegularValidator.class);

        add(FastOverrideScannerAccepter.class);
        add(FastDatabaseXmlScannerAccepter.class);
        add(FastWebXmlScannerAccepter.class);
        add(FastEntityScannerAccepter.class);
        add(FastActionScannerAccepter.class);
    }

    private ClassInfo getClassInfo(Class<?> targetClass) {
        for (ClassInfo aClass : classes) {
            if (aClass.targetClass == targetClass) {
                return aClass;
            }
        }
        return null;
    }

    /**
     * 添加实现类
     *
     * @param targetClass 类
     * @return 当前对象
     */
    public FastOverrides add(Class<?> targetClass) {
        int priority = 0;
        if (targetClass.isAnnotationPresent(AFastPriority.class)) {
            AFastPriority fastPriority = targetClass.getAnnotation(AFastPriority.class);
            priority = fastPriority.value();
        }
        return add(targetClass, priority);
    }

    /**
     * 添加实现类
     *
     * @param targetClass 类
     * @param priority    优先级
     * @return 当前对象
     */
    public FastOverrides add(Class<?> targetClass, int priority) {
        try {
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

            List<Class<?>> allSuperClasses = FastClassUtils.getAllSuperClasses(targetClass);
            for (Class<?> allSuperClass : allSuperClasses) {
                if (isExcludePackages(allSuperClass)) {
                    continue;
                }
                if (!mapClasses.containsKey(allSuperClass.hashCode())) {
                    mapClasses.put(allSuperClass.hashCode(), new ArrayList<>());
                }
                List<ClassInfo> classInfos = mapClasses.get(allSuperClass.hashCode());
                classInfos.add(0, classInfo);
            }
        } finally {
            sortClasses();
        }
        return this;
    }

    private boolean isExcludePackages(Class<?> targetClass) {
        for (String excludePackage : excludePackages) {
            if (FastStringUtils.matches(excludePackage, targetClass.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置类的优先级
     *
     * @param targetClass 类
     * @param priority    优先级
     * @return 当前对象
     */
    public FastOverrides setPriority(Class<?> targetClass, int priority) {
        try {
            ClassInfo classInfo = getClassInfo(targetClass);
            if (classInfo == null) {
                return this;
            }
            classInfo.priority = priority;
        } finally {
            sortClasses();
        }
        return this;
    }


    /**
     * 移除实现类
     *
     * @param targetClass 类
     * @return 当前对象
     */
    public FastOverrides removeClass(Class<?> targetClass) {
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, false) == null) {
                    return this;
                }
            }
        }

        Set<String> waitRemove = new HashSet<>();
        for (Map.Entry<String, Object> stringObjectEntry : instanceMap.entrySet()) {
            String key = stringObjectEntry.getKey();
            if (stringObjectEntry.getValue().getClass() == targetClass) {
                waitRemove.add(key);
            }
        }
        for (String key : waitRemove) {
            instanceMap.remove(key);
        }

        ClassInfo classInfo = getClassInfo(targetClass);
        if (classInfo != null) {
            classes.remove(classInfo);
        }
        for (Map.Entry<Object, List<ClassInfo>> stringListEntry : mapClasses.entrySet()) {
            List<ClassInfo> waitRemoves = new ArrayList<>();
            for (ClassInfo info : stringListEntry.getValue()) {
                if (info.targetClass == targetClass) {
                    waitRemoves.add(info);
                }
            }
            stringListEntry.getValue().removeAll(waitRemoves);
        }
        return this;
    }

    /**
     * 检查是否存在指定类的单例对象
     *
     * @param targetClass 目标类
     * @param <T>         目标类
     * @return 布尔值
     */
    public <T> boolean hasSingleInstance(Class<T> targetClass, Object... constructorParams) {
        return hasSingleInstance(null, targetClass, constructorParams);
    }

    /**
     * 检查是否存在指定类的单例对象
     *
     * @param targetClass 目标类
     * @param <T>         目标类
     * @return 布尔值
     */
    public <T> boolean hasSingleInstance(String onlyCode, Class<T> targetClass, Object... constructorParams) {
        if (FastStringUtils.isEmpty(onlyCode)) {
            onlyCode = FastMD5Utils.MD5To16(targetClass.getName() + Arrays.toString(constructorParams));
        }
        return instanceMap.containsKey(onlyCode);
    }


    /**
     * 获取目标类的单例对象，线程安全
     *
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T singleInstance(Class<T> targetClass, Object... constructorParams) {
        return singleInstance(true, targetClass, constructorParams);
    }

    /**
     * 获取目标类的单例对象，线程安全
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T singleInstance(boolean check, Class<T> targetClass, Object... constructorParams) {
        return singleInstance(check, null, targetClass, constructorParams);
    }


    /**
     * 获取目标类的单例对象，线程安全
     *
     * @param onlyCode          单例的唯一编号
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T singleInstance(String onlyCode, Class<T> targetClass, Object... constructorParams) {
        return singleInstance(true, onlyCode, targetClass, constructorParams);
    }

    /**
     * 获取目标类的单例对象，线程安全
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param onlyCode          单例的唯一编号
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T singleInstance(boolean check, String onlyCode, Class<T> targetClass, Object... constructorParams) {
        targetClass = findClass(targetClass, constructorParams);
        if (FastStringUtils.isEmpty(onlyCode)) {
            onlyCode = FastMD5Utils.MD5To16(targetClass.getName() + Arrays.toString(constructorParams));
        }
        Object instance = instanceMap.get(onlyCode);
        if (instance != null) {
            if (instance.getClass() != targetClass) {
                instanceMap.remove(onlyCode);
            } else {
                return (T) instance;
            }
        }
        T t = newInstance(false, check, targetClass, constructorParams);
        if (t == null) {
            return null;
        }
        T presentLockValue = (T) instanceMap.putIfAbsent(onlyCode, t);
        T finalValue = presentLockValue == null ? t : presentLockValue;

        if (finalValue.getClass().isAnnotationPresent(AFastObserver.class)) {
            FastChar.getObservable().addObserver(finalValue);
        }
        return finalValue;
    }

    /**
     * 获取目标类的多个实现类的单例对象，线程安全
     *
     * @param onlyCode          单例的唯一编号
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> singleInstances(String onlyCode, Class<T> targetClass, Object... constructorParams) {
        return singleInstances(true, onlyCode, targetClass, constructorParams);
    }

    /**
     * 获取目标类的多个实现类的单例对象，线程安全
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> singleInstances(boolean check, Class<T> targetClass, Object... constructorParams) {
        return singleInstances(check, null, targetClass, constructorParams);
    }

    /**
     * 获取目标类的多个实现类的单例对象，线程安全
     *
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> singleInstances(Class<T> targetClass, Object... constructorParams) {
        return singleInstances(true, null, targetClass, constructorParams);
    }

    /**
     * 获取目标类的多个实现类的单例对象，线程安全
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param onlyCode          单例的唯一编号
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> singleInstances(boolean check, String onlyCode, Class<T> targetClass, Object... constructorParams) {
        List<T> instances = new ArrayList<>(5);
        List<Class<T>> classes = findClasses(targetClass, constructorParams);
        for (Class<T> aClass : classes) {
            String instanceOnlyCode;
            if (FastStringUtils.isEmpty(onlyCode)) {
                instanceOnlyCode = FastMD5Utils.MD5(aClass.getName() + Arrays.toString(constructorParams));
            } else {
                instanceOnlyCode = FastMD5Utils.MD5(aClass.getName() + onlyCode);
            }
            Object instance = instanceMap.get(instanceOnlyCode);
            if (instance != null) {
                if (instance.getClass() != aClass) {
                    instanceMap.remove(instanceOnlyCode);
                } else {
                    instances.add((T) instance);
                    continue;
                }
            }
            T t = newInstance(false, false, aClass, constructorParams);
            if (t == null) {
                continue;
            }
            T presentLockValue = (T) instanceMap.putIfAbsent(instanceOnlyCode, t);
            T finalValue = presentLockValue == null ? t : presentLockValue;
            if (finalValue.getClass().isAnnotationPresent(AFastObserver.class)) {
                FastChar.getObservable().addObserver(finalValue);
            }
            instances.add(finalValue);
        }
        String errorInfo = "Could not initialize class " + targetClass.getName() + ":" + Arrays.toString(constructorParams);
        if (instances.size() == 0 && check) {
            throw new FastOverrideException(errorInfo);
        }
        return instances;
    }


    /**
     * 获取目标类的多个实现类新的对象
     *
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> newInstances(Class<T> targetClass, Object... constructorParams) {
        return newInstances(true, targetClass, constructorParams);
    }

    /**
     * 获取目标类的多个实现类新的对象
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> List<T> newInstances(boolean check, Class<T> targetClass, Object... constructorParams) {
        if (targetClass == null) {
            throw new NullPointerException();
        }
        List<T> instances = new ArrayList<>(5);
        String errorInfo = checkClassAnnotation(targetClass, constructorParams);

        if (Modifier.isFinal(targetClass.getModifiers())) {
            T instance = FastClassUtils.newInstance(targetClass, constructorParams);
            if (instance == null) {
                if (check) {
                    throw new FastOverrideException(errorInfo);
                }
                return instances;
            }
            return Collections.singletonList(instance);
        }
        List<Class<T>> classes = findClasses(targetClass, constructorParams);
        for (Class<T> aClass : classes) {
            T instance = newInstance(false, false, aClass, constructorParams);
            if (instance == null) {
                continue;
            }
            instances.add(instance);
        }
        if (instances.size() == 0 && check) {
            throw new FastOverrideException(errorInfo);
        }
        return instances;
    }


    /**
     * 获取目标类的单个新的对象，如果存在多个实现类，则按照优先级规则返回优先级最高的实现类对象
     *
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T newInstance(Class<T> targetClass, Object... constructorParams) {
        return newInstance(true, targetClass, constructorParams);
    }


    /**
     * 获取目标类的单个新的对象，如果存在多个实现类，则按照优先级规则返回优先级最高的实现类对象
     *
     * @param check             是否检测目标类对象，true则获取对象失败后抛出异常，默认true
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标类的对象
     * @return 当前对象
     */
    public <T> T newInstance(boolean check, Class<T> targetClass, Object... constructorParams) {
        return newInstance(true, check, targetClass, constructorParams);
    }

    private <T> T newInstance(boolean needFind, boolean check, Class<T> targetClass, Object... constructorParams) {
        if (targetClass == null) {
            throw new NullPointerException();
        }
        String superClass = targetClass.getName();
        String errorInfo = checkClassAnnotation(targetClass, constructorParams);

        if (needFind) {
            targetClass = findClass(targetClass, constructorParams);
        }
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

        if (!targetClass.getName().equals(superClass)) {
            if (FastChar.getConstant().isLogOverride()) {
                FastChar.getLog().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.OVERRIDE_ERROR2, superClass, targetClass.getName()));
            }
        }
        return instance;
    }


    private String checkClassAnnotation(Class<?> targetClass, Object... constructorParams) {
        String errorInfo = "Could not initialize class " + targetClass.getName() + ":" + Arrays.toString(constructorParams);
        if (targetClass.isAnnotationPresent(AFastOverrideError.class)) {
            AFastOverrideError overrideError = targetClass.getAnnotation(AFastOverrideError.class);
            errorInfo = overrideError.value();
        }
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            String[] url = fastFind.url();
            int length = fastFind.value().length;
            for (int i = 0; i < length; i++) {
                String className = fastFind.value()[i];
                if (FastClassUtils.getClass(className, false) == null) {
                    if (i < url.length) {
                        throw new FastFindClassException(FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR4, className, url[i]));
                    } else {
                        throw new FastFindClassException(FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR3, className));
                    }
                }
            }
        }
        return errorInfo;
    }


    /**
     * 检测目标类是否有实现类
     *
     * @param targetClass       目标类
     * @param constructorParams 构造函数参数
     * @param <T>               目标实现类的对象
     * @return 当前对象
     */
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
        if (Modifier.isFinal(targetClass.getModifiers())) {
            return targetClass;
        }
        List<ClassInfo> classInfos = mapClasses.get(targetClass.hashCode());
        if (classInfos == null) {
            return targetClass;
        }
        for (ClassInfo classInfo : classInfos) {
            Class<?> aClass = classInfo.targetClass;
            if (targetClass.isAssignableFrom(aClass) && targetClass != aClass) {
                List<Method> overrideMethods = classInfo.getOverrideMethods();
                boolean isOverrideClass = true;
                for (Method overrideMethod : overrideMethods) {
                    if (!Modifier.isStatic(overrideMethod.getModifiers())) {
                        continue;
                    }
                    List<Object> params = new ArrayList<>(5);
                    int paaramLength = overrideMethod.getParameterTypes().length;
                    for (int i = 0; i < paaramLength; i++) {
                        if (i < constructorParams.length) {
                            params.add(constructorParams[i]);
                        } else {
                            params.add(null);
                        }
                    }
                    try {
                        overrideMethod.setAccessible(true);
                        Object isOverride = overrideMethod.invoke(aClass, params.toArray());
                        if (!FastBooleanUtils.formatToBoolean(isOverride, true)) {
                            isOverrideClass = false;
                            break;
                        }
                    } catch (Exception e) {
                        throw new FastOverrideException(e);
                    }
                }
                if (!isOverrideClass) {
                    continue;
                }
                return (Class<T>) aClass;
            }
        }
        return targetClass;
    }

    private <T> List<Class<T>> findClasses(Class<T> targetClass, Object... constructorParams) {
        List<Class<T>> list = new ArrayList<>(5);
        if (Modifier.isFinal(targetClass.getModifiers())) {
            list.add(targetClass);
            return list;
        }

        List<ClassInfo> classInfos = mapClasses.get(targetClass.hashCode());
        if (classInfos == null) {
            return list;
        }

        for (ClassInfo classInfo : classInfos) {
            Class<?> aClass = classInfo.targetClass;
            if (targetClass.isAssignableFrom(aClass) && targetClass != aClass) {
                List<Method> overrideMethods = classInfo.getOverrideMethods();
                if (overrideMethods.size() > 0) {
                    Method method = overrideMethods.get(0);
                    List<Object> params = new ArrayList<>(5);
                    int length = method.getParameterTypes().length;
                    for (int i = 0; i < length; i++) {
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
                list.add((Class<T>) aClass);
            }
        }
        if (list.size() == 0) {
            list.add(targetClass);
        }
        return list;
    }

    private void sortClasses() {
        for (Map.Entry<Object, List<ClassInfo>> stringListEntry : mapClasses.entrySet()) {
            Collections.sort(stringListEntry.getValue(), new Comparator<ClassInfo>() {
                @Override
                public int compare(ClassInfo o1, ClassInfo o2) {
                    //FastOverrides 原则：后进先出
                    //此处由于FastOverrides 遍历classes 所以优先级由高到底排序，不可更改！
                    return Integer.compare(o2.priority, o1.priority);
                }
            });
        }
    }


    public void flush() {
        List<ClassInfo> waitRemove = new ArrayList<>(16);
        for (ClassInfo aClass : classes) {
            if (FastClassUtils.isRelease(aClass.targetClass)) {
                waitRemove.add(aClass);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastOverrides.class,
                            FastChar.getLocal().getInfo(FastCharLocal.OVERRIDE_ERROR3, aClass.targetClass));
                }
            }
        }
        classes.removeAll(waitRemove);

        List<String> waitRemoveInstance = new ArrayList<>(16);
        for (Map.Entry<String, Object> stringObjectEntry : instanceMap.entrySet()) {
            if (FastClassUtils.isRelease(stringObjectEntry.getValue())) {
                waitRemoveInstance.add(stringObjectEntry.getKey());
            }
        }
        for (String s : waitRemoveInstance) {
            instanceMap.remove(s);
        }
    }


    private static class ClassInfo {
        private int priority;
        private Class<?> targetClass;

        private List<Method> overrideMethods;

        public List<Method> getOverrideMethods() {
            if (overrideMethods == null) {
                overrideMethods = FastClassUtils.getMethod(targetClass, "isOverride");
            }
            return overrideMethods;
        }
    }

}
