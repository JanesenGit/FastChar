package com.fastchar.core;

import com.fastchar.extend.c3p0.FastC3p0Config;
import com.fastchar.extend.druid.FastDruidConfig;
import com.fastchar.extend.ehcache.FastEhCache2Config;
import com.fastchar.extend.ehcache.FastEhCache3Config;
import com.fastchar.extend.jdbc.FastJdbcConfig;
import com.fastchar.extend.redis.FastRedisConfig;
import com.fastchar.interfaces.IFastConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * FastChar默认提供的配置类集合，与FastChar.getConfig功能一致
 * @author 沈建（Janesen）
 */
public final class FastConfigs {

    FastConfigs() {
    }


    public FastDruidConfig getDruidConfig() {
        return FastChar.getConfig(FastDruidConfig.class);
    }

    public FastJdbcConfig getJdbcConfig() {
        return FastChar.getConfig(FastJdbcConfig.class);
    }


    public FastRedisConfig getRedisConfig() {
        return FastChar.getConfig(FastRedisConfig.class);
    }

    public FastEhCache3Config getEhCache3Config() {
        return  FastChar.getConfig(FastEhCache3Config.class);
    }

    public FastEhCache2Config getEhCache2Config() {
        return  FastChar.getConfig(FastEhCache2Config.class);
    }

    public FastC3p0Config getC3p0Config() {
        return FastChar.getConfig(FastC3p0Config.class);
    }

}
