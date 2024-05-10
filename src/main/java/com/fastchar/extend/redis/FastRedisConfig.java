package com.fastchar.extend.redis;

import com.fastchar.core.FastChar;
import com.fastchar.extend.redis.jedis.FastJedisConfig;
import com.fastchar.extend.redis.lettuce.FastLettuceConfig;
import com.fastchar.extend.redis.redisson.FastRedissonConfig;
import com.fastchar.interfaces.IFastConfig;
import com.fastchar.local.FastCharLocal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class FastRedisConfig implements IFastConfig {
    private final Set<FastRedisHostAndPort> servers = new LinkedHashSet<>();
    private String username;
    private String password;
    private boolean cluster = false;
    private int timeout = 2000;
    private int database = 0;
    private int soTimeout = 2000;
    private int maxAttempts = 5;

    private String clientName;

    private volatile FastJedisConfig jedisConfig;

    private volatile FastLettuceConfig lettuceConfig;

    private volatile FastRedissonConfig redissonConfig;


    public FastJedisConfig jedisConfig() {
        if (jedisConfig == null) {
            synchronized (FastRedisConfig.class) {
                if (jedisConfig == null) {
                    jedisConfig = new FastJedisConfig();
                }
            }
        }
        return jedisConfig;
    }

    public FastLettuceConfig lettuceConfig() {
        if (lettuceConfig == null) {
            synchronized (FastLettuceConfig.class) {
                if (lettuceConfig == null) {
                    lettuceConfig = new FastLettuceConfig();
                }
            }
        }
        return lettuceConfig;
    }

    public FastRedissonConfig redissonConfig() {
        if (redissonConfig == null) {
            synchronized (FastRedissonConfig.class) {
                if (redissonConfig == null) {
                    redissonConfig = new FastRedissonConfig();
                }
            }
        }
        return redissonConfig;
    }


    public String getPassword() {
        return password;
    }

    public FastRedisConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public FastRedisConfig addServer(String host, int port) {
        servers.add(new FastRedisHostAndPort(host, port));
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR2) + host + ":" + port);
        return this;
    }

    public Set<FastRedisHostAndPort> getServers() {
        return servers;
    }

    public Set<String> toSentinels() {
        Set<String> sentinels = new HashSet<>();
        for (FastRedisHostAndPort server : servers) {
            sentinels.add(server.toString());
        }
        return sentinels;
    }

    public FastRedisHostAndPort getServer(int index) {
        if (servers.isEmpty()) {
            return null;
        }
        return servers.toArray(new FastRedisHostAndPort[]{})[index];
    }

    public boolean isCluster() {
        return cluster;
    }

    public FastRedisConfig setCluster(boolean cluster) {
        this.cluster = cluster;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public FastRedisConfig setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getDatabase() {
        return database;
    }

    public FastRedisConfig setDatabase(int database) {
        this.database = database;
        return this;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public FastRedisConfig setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public FastRedisConfig setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }


    public String getUsername() {
        return username;
    }

    public FastRedisConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getClientName() {
        return clientName;
    }

    public FastRedisConfig setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }


}
