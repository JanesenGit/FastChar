package com.fastchar.extend.redis.jedis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.extend.redis.FastRedisBaseProvider;
import com.fastchar.extend.redis.FastRedisConfig;
import com.fastchar.extend.redis.FastRedisHostAndPort;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastMessagePubSub;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastObserver(priority = -9)
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastJedisNormalProvider extends FastRedisBaseProvider implements IFastCache, IFastMessagePubSub {
    public static boolean isOverride(String configCode) {
        FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
        if (redisConfig.getServers().isEmpty()) {
            return false;
        }
        return !redisConfig.isCluster();
    }

    public FastJedisNormalProvider() {
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR3, "Jedis"));
    }

    public FastJedisNormalProvider(String configCode) {
        this();
        this.configCode = configCode;
    }

    private volatile Pool<Jedis> jedisPool;
    private String configCode;

    /**
     * 必须手动释放调用close
     *
     * @return Jedis
     */
    private Jedis getJedis() {
        if (jedisPool == null) {
            synchronized (this) {
                if (jedisPool == null) {
                    FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);

                    if (redisConfig.getServers().isEmpty()) {
                        throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                    }
                    if (redisConfig.getServers().size() > 1) {
                        if (FastStringUtils.isEmpty(redisConfig.jedisConfig().getMasterName())) {
                            throw new FastCacheException("[JedisConfig] masterName must be not null !");
                        }
                        //主从哨兵模式
                        jedisPool = new JedisSentinelPool(redisConfig.jedisConfig().getMasterName(),
                                redisConfig.toSentinels(),
                                redisConfig.jedisConfig().getJedisPoolConfig(),
                                redisConfig.getTimeout(),
                                redisConfig.getSoTimeout(),
                                redisConfig.getUsername(),
                                redisConfig.getPassword(),
                                redisConfig.getDatabase());
                    } else {
                        FastRedisHostAndPort server = redisConfig.getServer(0);
                        jedisPool = new JedisPool(
                                redisConfig.jedisConfig().getJedisPoolConfig(),
                                server.getHost(),
                                server.getPort(),
                                redisConfig.getTimeout(),
                                redisConfig.getSoTimeout(),
                                redisConfig.getUsername(),
                                redisConfig.getPassword(),
                                redisConfig.getDatabase(), null);
                    }
                }
            }
        }
        return jedisPool.getResource();
    }


    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }
        try (Jedis jedis = getJedis()) {
            return jedis.get(wrapKey(tag, key)) != null;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
            return false;
        }
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }
        try (Jedis jedis = getJedis()) {
            Set<String> keys = jedis.keys(pattern);
            for (String key : keys) {
                tags.add(FastStringUtils.splitByWholeSeparator(key, "#")[0]);
            }
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            byte[] serialize = FastSerializeUtils.serialize(data);
            if (serialize == null) {
                jedis.del(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8));
            } else {
                jedis.set(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8), serialize);
            }
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        try (Jedis jedis = getJedis()) {
            Object deserialize = FastSerializeUtils.deserialize(jedis.get(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8)));
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
        try (Jedis jedis = getJedis()) {
            Set<String> keys = jedis.keys(this.wrapPattern(this.safeString(tag) + "#"));
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[]{}));
            }
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            jedis.del(wrapKey(tag, key));
        }
    }


    public void publish(String channel, String message) {
        if (FastStringUtils.isEmpty(channel) || FastStringUtils.isEmpty(message)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            jedis.publish(channel, message);
        }
    }

    @Override
    public AutoCloseable subscribe(String channel, OnSubscribe onSubscribe) throws Exception {
        if (FastStringUtils.isEmpty(channel)) {
            return () -> {
            };
        }

        Jedis jedis = getJedis();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                super.onMessage(channel, message);
                onSubscribe.onMessage(channel, message);
            }
        }, channel);
        return jedis;
    }

    public synchronized void onWebStop() {
        try {
            if (jedisPool != null) {
                jedisPool.close();
            }
        } finally {
            jedisPool = null;
        }
    }


}
