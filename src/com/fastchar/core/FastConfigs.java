package com.fastchar.core;

import com.fastchar.extend.druid.FastDruidConfig;
import com.fastchar.extend.ehcache.FastEhCacheConfig;
import com.fastchar.extend.jdbc.FastJdbcConfig;
import com.fastchar.extend.redis.FastRedisConfig;

public final class FastConfigs {
    private FastEhCacheConfig ehCacheConfig;
    private FastRedisConfig redisConfig;

    private FastJdbcConfig jdbcConfig;
    private FastDruidConfig druidConfig;


    public FastDruidConfig getDruidConfig() {
        if (druidConfig == null) {
            druidConfig = FastChar.getOverrides().singleInstance(FastDruidConfig.class);
        }
        return druidConfig;
    }


    public FastJdbcConfig getJdbcConfig() {
        if (jdbcConfig == null) {
            jdbcConfig = FastChar.getOverrides().singleInstance(FastJdbcConfig.class);
        }
        return jdbcConfig;
    }


    public FastRedisConfig getRedisConfig() {
        if (redisConfig == null) {
            redisConfig = FastChar.getOverrides().singleInstance(FastRedisConfig.class);
        }
        return redisConfig;
    }

    public FastEhCacheConfig getEhCacheConfig() {
        if (ehCacheConfig == null) {
            ehCacheConfig = FastChar.getOverrides().singleInstance(FastEhCacheConfig.class);
        }
        return ehCacheConfig;
    }

}
