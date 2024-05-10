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
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastObserver(priority = -9)
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastJedisClusterProvider extends FastRedisBaseProvider implements IFastCache, IFastMessagePubSub {
    public static boolean isOverride(String configCode) {
        FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
        if (redisConfig.getServers().isEmpty()) {
            return false;
        }
        return redisConfig.isCluster();
    }

    public FastJedisClusterProvider() {
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR3, "Jedis-Cluster"));
    }

    public FastJedisClusterProvider(String configCode) {
        this();
        this.configCode = configCode;
    }

    private String configCode;
    private volatile JedisCluster jedisCluster;

    /**
     * 此处是集群模式，无需手动close
     * @return  JedisCluster
     */
    private JedisCluster getJedis() {
        if (jedisCluster == null) {
            synchronized (this) {
                if (jedisCluster == null) {
                    FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
                    if (redisConfig.getServers().isEmpty()) {
                        throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                    }

                    Set<FastRedisHostAndPort> servers = redisConfig.getServers();
                    Set<HostAndPort> clusterServers = new HashSet<>();
                    for (FastRedisHostAndPort server : servers) {
                        clusterServers.add(new HostAndPort(server.getHost(), server.getPort()));
                    }

                    jedisCluster = new JedisCluster(clusterServers,
                            redisConfig.getTimeout(),
                            redisConfig.getSoTimeout(),
                            redisConfig.getMaxAttempts(),
                            redisConfig.getUsername(),
                            redisConfig.getPassword(),
                            redisConfig.getClientName(),
                            redisConfig.jedisConfig().getJedisClusterPoolConfig());
                }
            }
        }
        return jedisCluster;
    }

    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }
        JedisCluster jedis = getJedis();
        return jedis.exists(wrapKey(tag, key));
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }
        JedisCluster jedis = getJedis();
        Set<String> keys = jedis.keys(pattern);
        for (String key : keys) {
            tags.add(FastStringUtils.splitByWholeSeparator(key, "#")[0]);
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        JedisCluster jedis = getJedis();
        byte[] serialize = FastSerializeUtils.serialize(data);
        if (serialize == null) {
            jedis.del(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8));
        } else {
            jedis.set(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8), serialize);
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        JedisCluster jedis = getJedis();
        Object deserialize = FastSerializeUtils.deserialize(jedis.get(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8)));
        if (deserialize == null) {
            delete(tag, key);
            return null;
        }
        return (T) deserialize;
    }

    @Override
    public void delete(String tag) {
        if (FastStringUtils.isEmpty(tag)) {
            return;
        }
        JedisCluster jedis = getJedis();
        Set<String> keys = jedis.keys(this.wrapPattern(this.safeString(tag) + "#"));
        if (!keys.isEmpty()) {
            jedis.del(keys.toArray(new String[]{}));
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        JedisCluster jedis = getJedis();
        jedis.del(wrapKey(tag, key));
    }


    public void publish(String channel, String message) {
        if (FastStringUtils.isEmpty(channel) || FastStringUtils.isEmpty(message)) {
            return;
        }
        JedisCluster jedis = getJedis();
        jedis.publish(channel, message);
    }

    @Override
    public AutoCloseable subscribe(String channel, OnSubscribe onSubscribe) throws Exception {
        if (FastStringUtils.isEmpty(channel)) {
            return () -> {
            };
        }

        JedisCluster jedis = getJedis();
        jedis.close();
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
            if (jedisCluster != null) {
                jedisCluster.close();
            }
        } finally {
            jedisCluster = null;
        }
    }

}
