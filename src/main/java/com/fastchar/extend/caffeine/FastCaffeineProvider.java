package com.fastchar.extend.caffeine;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMemoryCache;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * Caffeine内存缓存 <a href="https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine">https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine</a>
 *
 * @author 沈建（Janesen）
 * @date 2021/7/5 18:17
 */
@AFastClassFind("com.github.benmanes.caffeine.cache.Caffeine")
public class FastCaffeineProvider implements IFastMemoryCache {
    private volatile Cache<String, Object> manualCache;

    public Cache<String, Object> getManualCache() {
        if (manualCache == null) {
            synchronized (this) {
                if (manualCache == null) {
                    manualCache = FastChar.getConfig(FastCaffeineConfig.class).getBuilder().build();
                }
            }
        }
        return manualCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) getManualCache().getIfPresent(key);
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
        getManualCache().put(key, value);
    }

    @Override
    public void remove(String key) {
        getManualCache().invalidate(key);
    }
}
