package com.fastchar.extend.caffeine;

import com.fastchar.interfaces.IFastConfig;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class FastCaffeineConfig implements IFastConfig {

    private final Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000);


    public Caffeine<Object, Object> getBuilder() {
        return builder;
    }
}
