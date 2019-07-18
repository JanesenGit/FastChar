package com.fastchar.extend.ehcache;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.utils.FastStringUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastObserver
@AFastClassFind("net.sf.ehcache.Cache")
public class FastEhCache2Provider implements IFastCache {
    private static final Object locker = new Object();

    private CacheManager cacheManager;

    private CacheManager getCacheManager() {
        if (cacheManager == null) {
            FastEhCache2Config ehCacheConfig = FastChar.getConfigs().getEhCache2Config();
            if (ehCacheConfig != null) {
                if (ehCacheConfig.getConfiguration() != null) {
                    cacheManager = CacheManager.create(ehCacheConfig.getConfiguration());
                } else if (FastStringUtils.isNotEmpty(ehCacheConfig.getConfigurationFileName())) {
                    cacheManager = CacheManager.create(ehCacheConfig.getConfigurationFileName());
                } else if (ehCacheConfig.getConfigurationURL() != null) {
                    cacheManager = CacheManager.create(ehCacheConfig.getConfigurationURL());
                } else if (ehCacheConfig.getConfigurationInputStream() != null) {
                    cacheManager = CacheManager.create(ehCacheConfig.getConfigurationInputStream());
                } else {
                    cacheManager = CacheManager.create();
                }
            } else {
                cacheManager = CacheManager.create();
            }
        }
        return cacheManager;
    }

    private Cache getCache(String tag) {
        Cache cache = getCacheManager().getCache(tag);
        if (cache == null) {
            synchronized (locker) {
                getCacheManager().addCacheIfAbsent(tag);
                return getCacheManager().getCache(tag);
            }
        }
        return cache;
    }

    @Override
    public boolean exists(String tag, String key) {
        Cache cache = getCache(tag);
        return cache.isKeyInCache(key);
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> strings = new HashSet<>();
        String[] cacheNames = getCacheManager().getCacheNames();
        for (String cacheName : cacheNames) {
            if (FastStringUtils.matches(pattern, cacheName)) {
                strings.add(cacheName);
            }
        }
        return strings;
    }

    @Override
    public void set(String tag, String key, Object data) {
        Cache cache = getCache(tag);
        Element element = new Element(key, data);
        cache.put(element);
    }

    @Override
    public <T> T get(String tag, String key) {
        Cache cache = getCache(tag);
        Element element = cache.get(key);
        if (element != null) {
            return (T) element.getObjectValue();
        }
        return null;
    }

    @Override
    public void delete(String tag) {
        getCacheManager().removeCache(tag);
    }

    @Override
    public void delete(String tag, String key) {
        Cache cache = getCache(tag);
        cache.remove(key);
    }


    public void onWebStop() {
        getCacheManager().shutdown();
    }

}
