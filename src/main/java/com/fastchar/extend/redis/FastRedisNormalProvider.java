package com.fastchar.extend.redis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@AFastClassFind("redis.clients.jedis.Jedis")
public class FastRedisNormalProvider implements IFastCache {
    public static boolean isOverride() {
        FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
        if (redisConfig.getServers().size() == 0) {
            return false;
        }
        return !FastChar.getConfigs().getRedisConfig().isCluster();
    }

    private volatile Pool<Jedis> jedisPool;

    private Jedis getJedis() {
        if (jedisPool == null) {
            synchronized (FastRedisNormalProvider.class) {
                if (jedisPool == null) {
                    FastRedisConfig redisConfig = FastChar.getConfigs().getRedisConfig();
                    if (redisConfig.getServers().size() == 0) {
                        throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                    }
                    if (redisConfig.getServers().size() > 1) {
                        if (FastStringUtils.isEmpty(redisConfig.getMasterName())) {
                            throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR2));
                        }
                        jedisPool = new JedisSentinelPool(redisConfig.getMasterName(),
                                redisConfig.toSentinels(),
                                redisConfig.getJedisPoolConfig(),
                                redisConfig.getTimeout(),
                                redisConfig.getSoTimeout(),
                                redisConfig.getUsername(),
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
                                redisConfig.getUsername(),
                                redisConfig.getPassword(),
                                redisConfig.getDatabase(), null);
                    }
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
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }
        try (Jedis jedis = getJedis()) {
            return jedis.get(wrapKey(tag, key)) != null;
        } catch (Exception e) {
            e.printStackTrace();
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
                tags.add(FastStringUtils.splitByWholeSeparator(key,"#")[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            if (data == null) {
                jedis.del(wrapKey(tag, key).getBytes());
            } else {
                jedis.set(wrapKey(tag, key).getBytes(), FastSerializeUtils.serialize(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        try (Jedis jedis = getJedis()) {
            Object deserialize = FastSerializeUtils.deserialize(jedis.get(wrapKey(tag, key).getBytes()));
            if (deserialize == null) {
                delete(tag, key);
                return null;
            }
            return (T) deserialize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(String tag) {
        if (FastStringUtils.isEmpty(tag)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            Set<String> keys = jedis.keys(tag + "#*");
            if (keys.size() > 0) {
                jedis.del(keys.toArray(new String[]{}));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            jedis.del(wrapKey(tag, key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
