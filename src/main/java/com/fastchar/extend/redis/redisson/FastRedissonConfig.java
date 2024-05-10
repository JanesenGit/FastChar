package com.fastchar.extend.redis.redisson;

import com.fastchar.extend.redis.FastRedisHostAndPort;
import com.fastchar.interfaces.IFastConfig;

public class FastRedissonConfig implements IFastConfig {

    private FastRedisHostAndPort masterServer;

    public FastRedisHostAndPort getMasterServer() {
        return masterServer;
    }

    public FastRedissonConfig setMasterServer(FastRedisHostAndPort masterServer) {
        this.masterServer = masterServer;
        return this;
    }
}
