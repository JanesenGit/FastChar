package com.fastchar.response;

public class FastResponseCacheConfig {

    private boolean cache;
    private String cacheKey;
    private String cacheTag;
    private long timeout;

    public String getCacheKey() {
        return cacheKey;
    }

    public FastResponseCacheConfig setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    public String getCacheTag() {
        return cacheTag;
    }

    public FastResponseCacheConfig setCacheTag(String cacheTag) {
        this.cacheTag = cacheTag;
        return this;
    }

    public boolean isCache() {
        return cache;
    }

    public FastResponseCacheConfig setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public FastResponseCacheConfig setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }
}

