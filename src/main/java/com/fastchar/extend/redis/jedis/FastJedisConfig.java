package com.fastchar.extend.redis.jedis;

import com.fastchar.interfaces.IFastConfig;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class FastJedisConfig implements IFastConfig {

    private String masterName;

    private JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

    private ConnectionPoolConfig jedisClusterPoolConfig = new ConnectionPoolConfig();

    public FastJedisConfig() {

        jedisPoolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(60000));
        jedisPoolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000));
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMaxWait(Duration.ofMillis(30000));

        jedisClusterPoolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(60000));
        jedisClusterPoolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000));
        jedisClusterPoolConfig.setTestWhileIdle(true);
        jedisClusterPoolConfig.setMaxWait(Duration.ofMillis(30000));

    }


    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public FastJedisConfig setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
        return this;
    }

    public ConnectionPoolConfig getJedisClusterPoolConfig() {
        return jedisClusterPoolConfig;
    }

    public FastJedisConfig setJedisClusterPoolConfig(ConnectionPoolConfig jedisClusterPoolConfig) {
        this.jedisClusterPoolConfig = jedisClusterPoolConfig;
        return this;
    }

    public String getMasterName() {
        return masterName;
    }

    public FastJedisConfig setMasterName(String masterName) {
        this.masterName = masterName;
        return this;
    }
}
