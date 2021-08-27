package com.fastchar.provider;

import com.fastchar.interfaces.IFastMemoryCache;

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
    private final ConcurrentHashMap<String, Cache> cacheDataMap = new ConcurrentHashMap<>();
    public static long CACHE_TIMEOUT = 3 * 60 * 1000L;//单位 毫秒
    public static long CACHE_IDLE = 5000;//单位 毫秒

    public FastMemoryCacheProvider() {
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.schedule(new Runnable() {
            @Override
            public void run() {
                clearValidity();
            }
        }, CACHE_IDLE, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        if (value == null) {
            return (T) put(key, defaultValue, CACHE_TIMEOUT);
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
        cache.setKey(key);
        cache.setValue(value);
        if (timeout != null) {
            cache.setTimeOut(timeout + System.currentTimeMillis());
        }
        cache.setValue(value);
        Cache previousLock = cacheDataMap.putIfAbsent(key, cache);
        return previousLock == null ? cache.getValue() : previousLock.getValue();
    }


    public <T> T get(String key) {
        Cache cache = cacheDataMap.get(key);
        if (cache != null) {
            cache.setTimeOut(CACHE_TIMEOUT + System.currentTimeMillis());
            return (T) cache.getValue();
        }
        return null;
    }

    public void clearValidity() {
        for (String key : cacheDataMap.keySet()) {
            Cache cache = cacheDataMap.get(key);
            Long timeOut = cache.getTimeOut();
            if (timeOut == null) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime > timeOut) {
                cacheDataMap.remove(key);
            }
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
