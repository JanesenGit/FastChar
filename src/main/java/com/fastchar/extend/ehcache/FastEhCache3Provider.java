package com.fastchar.extend.ehcache;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.utils.FastStringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastObserver
@AFastClassFind("org.ehcache.Cache")
public class FastEhCache3Provider implements IFastCache {
    private static final Object locker = new Object();

    private CacheManager cacheManager;

    private CacheManager getCacheManager() {
        if (cacheManager == null) {
            FastEhCache3Config ehCacheConfig = FastChar.getConfigs().getEhCache3Config();
            if (ehCacheConfig != null) {
                if (ehCacheConfig.getConfiguration() != null) {
                    cacheManager = CacheManagerBuilder.newCacheManager(ehCacheConfig.getConfiguration());
                } else if (ehCacheConfig.getConfigurationURL() != null) {
                    cacheManager = CacheManagerBuilder.newCacheManager(new XmlConfiguration(ehCacheConfig.getConfigurationURL()));
                } else if (ehCacheConfig.getConfigurationFileName() != null) {
                    cacheManager = CacheManagerBuilder.newCacheManager(new XmlConfiguration(getClass().getResource(ehCacheConfig.getConfigurationFileName())));
                } else {
                    cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
                }
            } else {
                cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
            }
            cacheManager.init();
        }
        return cacheManager;
    }

    private Cache getCache(String tag) {
        Cache cache = getCacheManager().getCache(tag, String.class, Object.class);
        if (cache == null) {
            synchronized (locker) {
                ResourcePoolsBuilder disk = ResourcePoolsBuilder.heap(10);

                CacheConfigurationBuilder<String, Object> stringObjectCacheConfigurationBuilder
                        = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class, disk);

                return getCacheManager().createCache(tag, stringObjectCacheConfigurationBuilder);
            }
        }
        return cache;
    }

    @Override
    public boolean exists(String tag, String key) {
        Cache cache = getCache(tag);
        return cache.containsKey(key);
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> strings = new HashSet<>();
        Set<String> cacheNames = getCacheManager().getRuntimeConfiguration().getCacheConfigurations().keySet();
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
        if (data == null) {
            cache.remove(key);
        } else {
            cache.put(key, data);
        }
    }

    @Override
    public <T> T get(String tag, String key) {
        Cache cache = getCache(tag);
        return (T) cache.get(key);
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
        if (cacheManager != null) {
            cacheManager.close();
        }
    }


}
