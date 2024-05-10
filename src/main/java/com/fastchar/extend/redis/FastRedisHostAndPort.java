package com.fastchar.extend.redis;

public class FastRedisHostAndPort {
    private final String host;
    private final int port;

    public FastRedisHostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}