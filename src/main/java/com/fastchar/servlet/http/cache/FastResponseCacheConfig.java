package com.fastchar.servlet.http.cache;

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

    public FastResponseCacheConfig copy() {
        FastResponseCacheConfig fastResponseCacheConfig = new FastResponseCacheConfig();
        fastResponseCacheConfig.setCache(this.cache);
        fastResponseCacheConfig.setCacheKey(this.cacheKey);
        fastResponseCacheConfig.setCacheTag(this.cacheTag);
        fastResponseCacheConfig.setTimeout(this.timeout);
        return fastResponseCacheConfig;
    }
}

