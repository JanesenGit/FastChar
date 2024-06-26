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

    private volatile CacheManager cacheManager;
    private String configCode;

    public FastEhCache2Provider(String configCode) {
        this.configCode = configCode;
    }

    public FastEhCache2Provider() {
    }

    private CacheManager getCacheManager() {
        if (cacheManager == null) {
            synchronized (this) {
                if (cacheManager == null) {
                    FastEhCache2Config ehCacheConfig = FastChar.getConfig(configCode, FastEhCache2Config.class);
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
            }
        }
        return cacheManager;
    }

    private Cache getCache(String tag) {
        Cache cache = getCacheManager().getCache(tag);
        if (cache == null) {
            synchronized (this) {
                getCacheManager().addCacheIfAbsent(tag);
                return getCacheManager().getCache(tag);
            }
        }
        return cache;
    }

    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return false;
        }
        Cache cache = getCache(tag);
        return cache.isKeyInCache(key);
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }
        String[] cacheNames = getCacheManager().getCacheNames();
        for (String cacheName : cacheNames) {
            if (FastStringUtils.matches(pattern, cacheName)) {
                tags.add(cacheName);
            }
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        Cache cache = getCache(tag);
        if (data == null) {
            cache.remove(key);
        } else {
            Element element = new Element(key, data);
            cache.put(element);
        }
    }

    @Override
    public <T> T get(String tag, String key) {
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return null;
        }
        Cache cache = getCache(tag);
        Element element = cache.get(key);
        if (element != null) {
            return (T) element.getObjectValue();
        }
        return null;
    }

    @Override
    public void delete(String tag) {
        if (FastStringUtils.isEmpty(tag)) {
            return;
        }
        getCacheManager().removeCache(tag);
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return;
        }
        Cache cache = getCache(tag);
        cache.remove(key);
    }


    public void onWebStop() {
        getCacheManager().shutdown();
    }

}
