package com.fastchar.extend.redis.lettuce;

import com.fastchar.interfaces.IFastConfig;

public class FastLettuceConfig implements IFastConfig {


    private String masterId;

    public String getMasterId() {
        return masterId;
    }

    public FastLettuceConfig setMasterId(String masterId) {
        this.masterId = masterId;
        return this;
    }
}
