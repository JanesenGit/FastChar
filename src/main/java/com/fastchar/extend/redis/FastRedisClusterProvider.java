package com.fastchar.extend.redis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastRedisClusterProvider implements IFastCache {
    private static final Object LOCKER = new Object();

    public static boolean isOverride() {
        FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
        if (redisConfig.getServers().size() == 0) {
            return false;
        }
        return FastChar.getConfigs().getRedisConfig().isCluster();
    }

    private JedisCluster jedisCluster;

    private JedisCluster getJedis() {
        if (jedisCluster == null) {
            synchronized (LOCKER) {
                FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
                if (redisConfig.getServers().size() == 0) {
                    throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                }
                jedisCluster = new JedisCluster(redisConfig.getServers(),
                        redisConfig.getTimeout(),
                        redisConfig.getSoTimeout(),
                        redisConfig.getMaxAttempts(),
                        redisConfig.getPassword(),
                        redisConfig.getJedisPoolConfig());

            }
        }
        return jedisCluster;
    }

    private String wrapKey(String tag, String key) {
        return tag + "#" + key;
    }


    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }
        try (JedisCluster jedis = getJedis()) {
            return jedis.exists(wrapKey(tag, key));
        }
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }
        try (JedisCluster jedis = getJedis()) {
            Set<String> keys = jedis.keys(pattern);
            for (String key : keys) {
                tags.add(key.split("#")[0]);
            }
            return tags;
        }
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (JedisCluster jedis = getJedis()) {
            jedis.set(wrapKey(tag, key).getBytes(), FastSerializeUtils.serialize(data));
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        try (JedisCluster jedis = getJedis()) {
            Object deserialize = FastSerializeUtils.deserialize(jedis.get(wrapKey(tag, key).getBytes()));
            if (deserialize == null) {
                delete(tag, key);
                return null;
            }
            return (T) deserialize;
        }
    }

    @Override
    public void delete(String tag) {
        if (FastStringUtils.isEmpty(tag)) {
            return;
        }
        try (JedisCluster jedis = getJedis()) {
            Set<String> keys = jedis.keys(tag + "#*");
            if (keys.size() > 0) {
                jedis.del(keys.toArray(new String[]{}));
            }
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (JedisCluster jedis = getJedis()) {
            jedis.del(wrapKey(tag, key));
        }
    }
}
