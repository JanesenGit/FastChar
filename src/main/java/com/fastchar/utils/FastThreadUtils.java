package com.fastchar.utils;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMemoryCache;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程工具类
 * @author 沈建（Janesen）
 * @date 2021/7/14 11:34
 */
public class FastThreadUtils {
    private static final ConcurrentHashMap<String, ThreadLocal<?>> threadLocalMap = new ConcurrentHashMap<>();

    /**
     * 获取指定key的线程
     * @param key 标识
     */
    @SuppressWarnings("unchecked")
    public static <T> ThreadLocal<T> getThreadLocal(String key) {
        IFastMemoryCache iFastMemoryCache = FastChar.safeGetMemoryCache();
        if (iFastMemoryCache != null) {
            return iFastMemoryCache.get(key, new ThreadLocal<T>());
        }

        ThreadLocal<T> threadLocal = (ThreadLocal<T>) threadLocalMap.get(key);
        if (threadLocal != null) {
            return threadLocal;
        }
        threadLocal = new ThreadLocal<>();
        ThreadLocal<T> lastThreadLocal = (ThreadLocal<T>) threadLocalMap.putIfAbsent(key, threadLocal);
        return lastThreadLocal != null ? lastThreadLocal : threadLocal;
    }


}
