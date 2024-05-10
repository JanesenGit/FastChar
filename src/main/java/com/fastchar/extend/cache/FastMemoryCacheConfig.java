package com.fastchar.extend.cache;

import com.fastchar.interfaces.IFastConfig;

public class FastMemoryCacheConfig implements IFastConfig {


    /**
     * 超时时间，未使用的缓存数据 单位毫秒
     */
    private long cacheTimeout = 3 * 60 * 1000L;

    /**
     * 缓存检测心跳时间 单位毫秒
     */
    private long cacheIdle = 5000L;


    public long getCacheTimeout() {
        return cacheTimeout;
    }

    public FastMemoryCacheConfig setCacheTimeout(long cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
        return this;
    }

    public long getCacheIdle() {
        return cacheIdle;
    }

    public FastMemoryCacheConfig setCacheIdle(long cacheIdle) {
        this.cacheIdle = cacheIdle;
        return this;
    }
}
