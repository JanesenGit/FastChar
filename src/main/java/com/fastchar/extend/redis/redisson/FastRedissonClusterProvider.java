package com.fastchar.extend.redis.redisson;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.extend.redis.FastRedisBaseProvider;
import com.fastchar.extend.redis.FastRedisConfig;
import com.fastchar.extend.redis.FastRedisHostAndPort;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastLocker;
import com.fastchar.interfaces.IFastMessagePubSub;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.ChannelName;
import org.redisson.client.protocol.pubsub.PubSubMessage;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;


@SuppressWarnings("unchecked")
@AFastObserver(priority = -9)
@AFastClassFind("org.redisson.Redisson")
public class FastRedissonClusterProvider  extends FastRedisBaseProvider implements IFastCache, IFastMessagePubSub , IFastLocker {

    public static boolean isOverride(String configCode) {
        FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
        if (redisConfig.getServers().isEmpty()) {
            return false;
        }
        return redisConfig.isCluster();
    }

    public FastRedissonClusterProvider() {
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR3, "Redisson"));
    }

    public FastRedissonClusterProvider(String configCode) {
        this();
        this.configCode = configCode;
    }

    private String configCode;
    private volatile RedissonClient redissonClient;

    public synchronized void onWebStop() {
        try {
            if (redissonClient != null) {
                redissonClient.shutdown();
            }
        } finally {
            redissonClient = null;
        }
    }


    public RedissonClient getRedissonClient() {
        if (redissonClient == null) {
            synchronized (this) {
                if (redissonClient == null) {
                    Config config = new Config();

                    FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);

                    if (redisConfig.getServers().isEmpty()) {
                        throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                    }

                    ClusterServersConfig masterSlaveServersConfig = config.useClusterServers()
                            .setTimeout(redisConfig.getTimeout())
                            .setConnectTimeout(redisConfig.getSoTimeout())
                            .setRetryAttempts(redisConfig.getMaxAttempts())
                            .setUsername(redisConfig.getUsername())
                            .setPassword(redisConfig.getPassword())
                            .setClientName(redisConfig.getClientName());

                    for (FastRedisHostAndPort server : redisConfig.getServers()) {
                        masterSlaveServersConfig
                                .addNodeAddress("redis://" + server.toString());
                    }

                    redissonClient = Redisson.create(config);
                }
            }
        }
        return redissonClient;
    }

    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }

        RedissonClient redissonClient = getRedissonClient();
        RMap<Object, Object> objectRMap = redissonClient.getMap(IFastCache.class.getSimpleName());
        return objectRMap.containsKey(wrapKey(tag, key));
    }


    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }

        RedissonClient redissonClient = getRedissonClient();
        Set<Object> objects = redissonClient.getMap(IFastCache.class.getSimpleName()).keySet(pattern);
        for (Object object : objects) {
            tags.add(FastStringUtils.splitByWholeSeparator(object.toString(),"#")[0]);
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        RedissonClient redissonClient = getRedissonClient();
        RMap<Object, Object> objectRMap = redissonClient.getMap(IFastCache.class.getSimpleName());
        byte[] serialize = FastSerializeUtils.serialize(data);
        if (serialize == null) {
            objectRMap.remove(wrapKey(tag, key));
        }else{
            objectRMap.put(wrapKey(tag, key), serialize);
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        RedissonClient redissonClient = getRedissonClient();
        RMap<Object, Object> objectRMap = redissonClient.getMap(IFastCache.class.getSimpleName());

        Object deserialize = FastSerializeUtils.deserialize((byte[]) objectRMap.get(wrapKey(tag, key)));
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
        RedissonClient redissonClient = getRedissonClient();
        RMap<Object, Object> objectRMap = redissonClient.getMap(IFastCache.class.getSimpleName());

        Set<Object> keys = objectRMap.keySet(this.wrapPattern(this.safeString(tag) + "#"));
        if (!keys.isEmpty()) {
            objectRMap.fastRemove(keys.toArray());
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        RedissonClient redissonClient = getRedissonClient();
        RMap<Object, Object> objectRMap = redissonClient.getMap(IFastCache.class.getSimpleName());
        objectRMap.remove(wrapKey(tag, key));
    }



    @Override
    public void publish(String channel, String message) throws Exception {
        RedissonClient redissonClient = getRedissonClient();
        RTopic clientTopic = redissonClient.getTopic(channel);
        clientTopic.publish(new PubSubMessage(new ChannelName(channel), message));
        redissonClient.shutdown();
    }

    @Override
    public AutoCloseable subscribe(String channel, OnSubscribe onSubscribe) throws Exception {

        RedissonClient redissonClient = getRedissonClient();
        RTopic clientTopic = redissonClient.getTopic(channel);
        clientTopic.addListener(PubSubMessage.class, new MessageListener<PubSubMessage>() {
            @Override
            public void onMessage(CharSequence channel, PubSubMessage msg) {
                onSubscribe.onMessage(channel.toString(), msg.getValue().toString());
            }
        });

        return redissonClient::shutdown;
    }

    @Override
    public Lock getLock(String key) {
        return redissonClient.getLock(key);
    }

    @Override
    public void removeLock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
    }
}
