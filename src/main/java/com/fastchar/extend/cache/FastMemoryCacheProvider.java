package com.fastchar.extend.cache;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMemoryCache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 简易版内存缓存控件，如果系统流量较大，建议使用Caffeine插件
 *
 * @author 沈建（Janesen）
 * @date 2021/7/5 18:58
 */
public class FastMemoryCacheProvider implements IFastMemoryCache {
    private final ConcurrentHashMap<String, Cache> cacheDataMap = new ConcurrentHashMap<>(16);

    
    public FastMemoryCacheProvider() {
        FastMemoryCacheConfig memoryCacheConfig = FastChar.getConfig(FastMemoryCacheConfig.class);
        ScheduledExecutorService scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
        scheduledThreadPool.scheduleAtFixedRate(this::clearValidity, memoryCacheConfig.getCacheIdle(), memoryCacheConfig.getCacheTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        if (value == null) {
            return (T) put(key, defaultValue, FastChar.getConfig(FastMemoryCacheConfig.class).getCacheTimeout());
        }
        return value;
    }

    public void put(String key, Object value) {
        put(key, value, null);
    }

    @Override
    public void remove(String key) {
        cacheDataMap.remove(key);
    }

    public Object put(String key, Object value, Long timeout) {
        if (value == null) {
            return null;
        }
        Cache cache = new Cache();
        Cache previousLock = cacheDataMap.putIfAbsent(key, cache);
        if (previousLock == null) {
            previousLock = cache;
        }
        previousLock.setKey(key);
        previousLock.setValue(value);
        if (timeout != null) {
            previousLock.setTimeOut(timeout + System.currentTimeMillis());
        }
        return previousLock.getValue();
    }


    public <T> T get(String key) {
        Cache cache = cacheDataMap.get(key);
        if (cache != null) {
            cache.setTimeOut(FastChar.getConfig(FastMemoryCacheConfig.class).getCacheTimeout() + System.currentTimeMillis());
            return (T) cache.getValue();
        }
        return null;
    }

    public void clearValidity() {
        Set<String> waitRemove = new HashSet<>();
        for (Map.Entry<String, Cache> stringCacheEntry : cacheDataMap.entrySet()) {
            Cache cache = stringCacheEntry.getValue();
            Long timeOut = cache.getTimeOut();
            if (timeOut == null) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime > timeOut) {
                waitRemove.add(stringCacheEntry.getKey());
            }
        }
        for (String key : waitRemove) {
            cacheDataMap.remove(key);
        }
    }

    public static class Cache {
        public Cache() {

        }

        public Cache(String key, Object value, Long timeOut) {
            super();
            this.key = key;
            this.value = value;
            this.timeOut = timeOut;
        }

        private String key;
        private Object value;
        private Long timeOut;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Long getTimeOut() {
            return timeOut;
        }

        public void setTimeOut(Long timeOut) {
            this.timeOut = timeOut;
        }

        @Override
        public String toString() {
            return "Cache{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    ", timeOut=" + timeOut +
                    '}';
        }

    }

}
