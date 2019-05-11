package com.fastchar.extend.redis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.interfaces.IFastCacheProvider;
import com.fastchar.utils.FastSerializeUtils;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastRedisClusterProvider implements IFastCacheProvider {
    private static final Object locker = new Object();

    public static boolean isOverride() {
        return FastChar.getConfigs().getRedisConfig().isCluster();
    }

    private JedisCluster jedisCluster;

    private JedisCluster getJedis() {
        if (jedisCluster == null) {
            synchronized (locker) {
                FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
                if (redisConfig.getServers().size() == 0) {
                    throw new FastCacheException(FastChar.getLocal().getInfo("Redis_Error1"));
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
        try (JedisCluster jedis = getJedis()) {
            return jedis.exists(wrapKey(tag, key));
        }
    }

    @Override
    public Set<String> getTags(String pattern) {
        try (JedisCluster jedis = getJedis()) {
            Set<String> tags = new HashSet<>();
            Set<String> keys = jedis.keys(pattern);
            for (String key : keys) {
                tags.add(key.split("#")[0]);
            }
            return tags;
        }
    }

    @Override
    public void setCache(String tag, String key, Object data) throws Exception {
        try (JedisCluster jedis = getJedis()) {
            jedis.set(wrapKey(tag, key).getBytes(), FastSerializeUtils.serialize(data));
        }
    }

    @Override
    public <T> T getCache(String tag, String key) throws Exception {
        try (JedisCluster jedis = getJedis()) {
            Object deserialize = FastSerializeUtils.deserialize(jedis.get(wrapKey(tag, key).getBytes()));
            if (deserialize == null) {
                deleteCache(tag, key);
                return null;
            }
            return (T) deserialize;
        }
    }

    @Override
    public void deleteCache(String tag) {
        try (JedisCluster jedis = getJedis()) {
            Set<String> keys = jedis.keys(tag + "#*");
            jedis.del(keys.toArray(new String[]{}));
        }
    }

    @Override
    public void deleteCache(String tag, String key) {
        try (JedisCluster jedis = getJedis()) {
            jedis.del(wrapKey(tag, key));
        }
    }
}
