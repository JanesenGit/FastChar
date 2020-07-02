package com.fastchar.extend.redis;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class FastRedisConfig implements IFastConfig {
    private Set<HostAndPort> servers = new LinkedHashSet<>();
    private String masterName;
    private String password;
    private boolean cluster = false;
    private int timeout = 2000;
    private int database = 0;
    private int soTimeout = 2000;
    private int maxAttempts = 5;
    private JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

    public FastRedisConfig() {
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(1800000);
    }

    public String getPassword() {
        return password;
    }

    public FastRedisConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public FastRedisConfig addServer(String host, int port) {
        servers.add(new HostAndPort(host, port));
        if (FastChar.getConstant().isDebug()) {
            FastChar.getLog().info(FastChar.getLocal().getInfo("Redis_Error3", "Druid") + host + ":" + port);
        }
        return this;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public FastRedisConfig setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
        return this;
    }

    public Set<HostAndPort> getServers() {
        return servers;
    }

    public Set<String> toSentinels() {
        Set<String> sentinels = new HashSet<>();
        for (HostAndPort server : servers) {
            sentinels.add(server.toString());
        }
        return sentinels;
    }

    public HostAndPort getServer(int index) {
        if (servers.size() == 0) {
            return null;
        }
        return servers.toArray(new HostAndPort[]{})[index];
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


    public String getMasterName() {
        return masterName;
    }

    public FastRedisConfig setMasterName(String masterName) {
        this.masterName = masterName;
        return this;
    }
}
