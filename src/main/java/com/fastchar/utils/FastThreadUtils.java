package com.fastchar.utils;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMemoryCache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程工具类
 * @author 沈建（Janesen）
 * @date 2021/7/14 11:34
 */
public class FastThreadUtils {
    private static final ConcurrentHashMap<String, ThreadLocal<?>> threadLocalMap = new ConcurrentHashMap<>(16);

    private static int getNanosOfMilli(final Duration duration) {
        return duration.getNano() % 1_000_000;
    }

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


    @SuppressWarnings("BusyWait")
    public static void sleep(final Duration duration) throws InterruptedException {
        // Using this method avoids depending on the vagaries of the precision and accuracy of system timers and schedulers.
        final Instant finishInstant = Instant.now().plus(duration);
        Duration remainingDuration = duration;
        do {
            Thread.sleep(remainingDuration.toMillis(), getNanosOfMilli(remainingDuration));
            remainingDuration = Duration.between(Instant.now(), finishInstant);
        } while (!remainingDuration.isNegative());
    }


}
