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
import com.fastchar.interfaces.IFastMessageQueue;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.ChannelName;
import org.redisson.client.protocol.pubsub.PubSubMessage;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;


@SuppressWarnings("unchecked")
@AFastObserver(priority = -9)
@AFastClassFind("org.redisson.Redisson")
public class FastRedissonNormalProvider extends FastRedisBaseProvider implements IFastCache, IFastMessagePubSub, IFastLocker, IFastMessageQueue {

    public static boolean isOverride(String configCode) {
        FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
        if (redisConfig.getServers().isEmpty()) {
            return false;
        }
        return !redisConfig.isCluster();
    }


    private String configCode;
    public FastRedissonNormalProvider(String configCode) {
        this();
        this.configCode = configCode;
    }

    public FastRedissonNormalProvider() {
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR3, "Redisson"));
    }

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

                    if (redisConfig.getServers().size() > 1) {
                        MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers()
                                .setDatabase(redisConfig.getDatabase())
                                .setTimeout(redisConfig.getTimeout())
                                .setConnectTimeout(redisConfig.getSoTimeout())
                                .setRetryAttempts(redisConfig.getMaxAttempts())
                                .setUsername(redisConfig.getUsername())
                                .setPassword(redisConfig.getPassword())
                                .setMasterAddress("redis://" + redisConfig.redissonConfig().getMasterServer().toString())
                                .setClientName(redisConfig.getClientName());

                        for (FastRedisHostAndPort server : redisConfig.getServers()) {
                            masterSlaveServersConfig
                                    .addSlaveAddress("redis://" + server.toString());
                        }
                    }else{
                        config.useSingleServer()
                                .setDatabase(redisConfig.getDatabase())
                                .setTimeout(redisConfig.getTimeout())
                                .setConnectTimeout(redisConfig.getSoTimeout())
                                .setRetryAttempts(redisConfig.getMaxAttempts())
                                .setUsername(redisConfig.getUsername())
                                .setPassword(redisConfig.getPassword())
                                .setClientName(redisConfig.getClientName())
                                .setAddress("redis://" + redisConfig.getServer(0).toString());
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
        return getRedissonClient().getLock(key);
    }

    @Override
    public void removeLock(String key) {
        RLock lock = getRedissonClient().getLock(key);
        lock.unlock();
    }

    @Override
    public void send(String channel, String message) throws Exception {
        RQueue<Object> queue = getRedissonClient().getQueue(channel);
        queue.add(message);
        getRedissonClient().shutdown();
    }

    @Override
    public AutoCloseable receive(String channel, OnReceive onReceive) throws Exception {
        RQueue<Object> queue = getRedissonClient().getQueue(channel);

        final boolean[] subscribing = {true};
        Thread backThread = new Thread(() -> {
            while (subscribing[0]) {
                try {
                    Object poll = queue.poll();
                    if (poll != null) {
                        onReceive.onMessage(channel, poll.toString(), () -> {

                        });
                    }
                } catch (Exception e) {
                    FastChar.getLogger().error(this.getClass(), e);
                }
            }
        });
        backThread.start();
        backThread.join();
        return () -> {
            subscribing[0] = false;
            if (redissonClient != null) {
                redissonClient.shutdown();
            }
        };
    }
}
