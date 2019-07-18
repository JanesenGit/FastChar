package com.fastchar.extend.redis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastRedisNormalProvider implements IFastCache {
    private static final Object locker = new Object();

    public static boolean isOverride() {
        FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
        if (redisConfig.getServers().size() == 0) {
            return false;
        }
        return !FastChar.getConfigs().getRedisConfig().isCluster();
    }

    private JedisPoolAbstract jedisPool;

    private Jedis getJedis() {
        if (jedisPool == null) {
            synchronized (locker) {
                FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
                if (redisConfig.getServers().size() == 0) {
                    throw new FastCacheException(FastChar.getLocal().getInfo("Redis_Error1"));
                }
                if (redisConfig.getServers().size() > 1) {
                    if (FastStringUtils.isEmpty(redisConfig.getMasterName())) {
                        throw new FastCacheException(FastChar.getLocal().getInfo("Redis_Error2"));
                    }
                    jedisPool = new JedisSentinelPool(redisConfig.getMasterName(),
                            redisConfig.toSentinels(),
                            redisConfig.getJedisPoolConfig(),
                            redisConfig.getTimeout(),
                            redisConfig.getSoTimeout(),
                            redisConfig.getPassword(),
                            redisConfig.getDatabase());
                } else {
                    HostAndPort server = redisConfig.getServer(0);
                    jedisPool = new JedisPool(
                            redisConfig.getJedisPoolConfig(),
                            server.getHost(),
                            server.getPort(),
                            redisConfig.getTimeout(),
                            redisConfig.getSoTimeout(),
                            redisConfig.getPassword(),
                            redisConfig.getDatabase(), null);
                }
            }
        }
        return jedisPool.getResource();
    }

    private String wrapKey(String tag, String key) {
        return tag + "#" + key;
    }


    @Override
    public boolean exists(String tag, String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(wrapKey(tag, key));
        }
    }

    @Override
    public Set<String> getTags(String pattern) {
        try (Jedis jedis = getJedis()) {
            Set<String> tags = new HashSet<>();
            Set<String> keys = jedis.keys(pattern);
            for (String key : keys) {
                tags.add(key.split("#")[0]);
            }
            return tags;
        }
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        try (Jedis jedis = getJedis()) {
            jedis.set(wrapKey(tag, key).getBytes(), FastSerializeUtils.serialize(data));
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        try (Jedis jedis = getJedis()) {
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
        try (Jedis jedis = getJedis()) {
            Set<String> keys = jedis.keys(tag + "#*");
            jedis.del(keys.toArray(new String[]{}));
        }
    }

    @Override
    public void delete(String tag, String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(wrapKey(tag, key));
        }
    }

}
