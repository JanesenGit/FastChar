package com.fastchar.extend.caffeine;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastMemoryCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine内存缓存 https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
 *
 * @author 沈建（Janesen）
 * @date 2021/7/5 18:17
 */
@AFastClassFind("com.github.benmanes.caffeine.cache.Caffeine")
public class FastCaffeineProvider implements IFastMemoryCache {
    private static final Cache<String, Object> MANUAL_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) MANUAL_CACHE.getIfPresent(key);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        T result = get(key);
        if (result == null) {
            put(key, defaultValue);
            return defaultValue;
        }
        return result;
    }

    @Override
    public void put(String key, Object value) {
        MANUAL_CACHE.put(key, value);
    }

    @Override
    public void remove(String key) {
        MANUAL_CACHE.invalidate(key);
    }
}
