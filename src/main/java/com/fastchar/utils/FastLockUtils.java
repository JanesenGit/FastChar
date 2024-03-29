package com.fastchar.utils;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMemoryCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 沈建（Janesen）
 * @date 2020/8/3 16:37
 */
public class FastLockUtils {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<String, ReentrantLock>(16);


    /**
     * 根据key获取一个对象锁
     *
     * @param key 唯一key
     * @return ReentrantLock
     */
    public static ReentrantLock getLock(String key) {
        IFastMemoryCache iFastMemoryCache = FastChar.safeGetMemoryCache();
        if (iFastMemoryCache != null) {
            return iFastMemoryCache.get(key, new ReentrantLock());
        }

        ReentrantLock lock = LOCK_MAP.get(key);
        if (lock != null) {
            return lock;
        }
        lock = new ReentrantLock();
        ReentrantLock previousLock = LOCK_MAP.putIfAbsent(key, lock);
        return previousLock == null ? lock : previousLock;
    }

    /**
     * 删除一个对象锁
     *
     * @param key 唯一key
     */
    public static void removeLock(String key) {
        IFastMemoryCache iFastMemoryCache = FastChar.safeGetMemoryCache();
        if (iFastMemoryCache != null) {
            iFastMemoryCache.remove(key);
        }
        LOCK_MAP.remove(key);
    }

}
