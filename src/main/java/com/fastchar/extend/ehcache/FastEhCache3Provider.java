package com.fastchar.extend.ehcache;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.utils.FastStringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastObserver
@AFastClassFind("org.ehcache.Cache")
public class FastEhCache3Provider implements IFastCache {
    private static final Object LOCKER = new Object();

    private volatile CacheManager cacheManager;

    private synchronized CacheManager getCacheManager() {
       if(cacheManager==null){
           synchronized (FastEhCache3Provider.class) {
               if (cacheManager == null) {
                   FastEhCache3Config ehCacheConfig = FastChar.getConfigs().getEhCache3Config();
                   if (ehCacheConfig != null) {
                       if (ehCacheConfig.getConfiguration() != null) {
                           cacheManager = CacheManagerBuilder.newCacheManager(ehCacheConfig.getConfiguration());
                       } else if (ehCacheConfig.getConfigurationURL() != null) {
                           cacheManager = CacheManagerBuilder.newCacheManager(new XmlConfiguration(ehCacheConfig.getConfigurationURL()));
                       } else if (ehCacheConfig.getConfigurationFileName() != null) {
                           cacheManager = CacheManagerBuilder.newCacheManager(new XmlConfiguration(Objects.requireNonNull(getClass().getResource(ehCacheConfig.getConfigurationFileName()))));
                       } else {
                           cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
                       }
                   } else {
                       cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
                   }
                   cacheManager.init();
               }
           }
       }
        return cacheManager;
    }

    private Cache<String,Object> getCache(String tag) {
        Cache<String,Object> cache = getCacheManager().getCache(tag, String.class, Object.class);
        if (cache == null) {
            synchronized (LOCKER) {
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
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return false;
        }
        Cache<String,Object> cache = getCache(tag);
        return cache.containsKey(key);
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }

        Map<String, CacheConfiguration<?, ?>> cacheConfigurations = getCacheManager().getRuntimeConfiguration().getCacheConfigurations();
        for (Map.Entry<String, CacheConfiguration<?, ?>> stringCacheConfigurationEntry : cacheConfigurations.entrySet()) {
            if (FastStringUtils.matches(pattern, stringCacheConfigurationEntry.getKey())) {
                tags.add(stringCacheConfigurationEntry.getKey());
            }
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        Cache<String,Object> cache = getCache(tag);
        if (data == null) {
            cache.remove(key);
        } else {
            cache.put(key, data);
        }
    }

    @Override
    public <T> T get(String tag, String key) {
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return null;
        }
        Cache<String,Object> cache = getCache(tag);
        return (T) cache.get(key);
    }

    @Override
    public void delete(String tag) {
        getCacheManager().removeCache(tag);
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag)||FastStringUtils.isEmpty(key)) {
            return;
        }
        Cache<String,Object> cache = getCache(tag);
        cache.remove(key);
    }

    public void onWebStop() {
        if (cacheManager != null) {
            cacheManager.close();
        }
    }


}
