package com.fastchar.extend.redis.lettuce;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastCacheException;
import com.fastchar.extend.redis.FastRedisBaseProvider;
import com.fastchar.extend.redis.FastRedisConfig;
import com.fastchar.extend.redis.FastRedisHostAndPort;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastMessagePubSub;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.internal.LettuceFactories;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;


@SuppressWarnings("unchecked")
@AFastObserver(priority = -9)
@AFastClassFind("io.lettuce.core.cluster.RedisClusterClient")
public class FastLettuceClusterProvider extends FastRedisBaseProvider implements IFastCache, IFastMessagePubSub {

    public static boolean isOverride(String configCode) {
        FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
        if (redisConfig.getServers().isEmpty()) {
            return false;
        }
        return redisConfig.isCluster();
    }
    public FastLettuceClusterProvider() {
        FastChar.getLogger().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR3, "Lettuce-Cluster"));
    }

    public FastLettuceClusterProvider(String configCode) {
        this();
        this.configCode = configCode;
    }

    private String configCode;

    private volatile RedisClusterClient redisClient;

    private RedisClusterClient getRedisClient() {
        if (redisClient == null) {
            synchronized (this) {
                if (redisClient == null) {
                    FastRedisConfig redisConfig = FastChar.getConfig(configCode, FastRedisConfig.class);
                    if (redisConfig.getServers().isEmpty()) {
                        throw new FastCacheException(FastChar.getLocal().getInfo(FastCharLocal.REDIS_ERROR1));
                    }

                    List<RedisURI> redisURIS = new ArrayList<>();
                    for (FastRedisHostAndPort server : redisConfig.getServers()) {
                        RedisURI build = RedisURI.Builder
                                .sentinel(server.getHost(), server.getPort())
                                .withAuthentication(redisConfig.getPassword(), redisConfig.getUsername())
                                .withClientName(redisConfig.getClientName())
                                .withTimeout(Duration.of(redisConfig.getTimeout(), ChronoUnit.SECONDS))
                                .withDatabase(redisConfig.getDatabase())
                                .build();
                        redisURIS.add(build);
                    }
                    redisClient = RedisClusterClient.create(redisURIS);
                }
            }
        }
        return redisClient;
    }

    private StatefulRedisClusterConnection<byte[], byte[]> getConnection() {
        RedisClusterClient redisClient = getRedisClient();
        return redisClient.connect(ByteArrayCodec.INSTANCE);
    }


    @Override
    public boolean exists(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return false;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            return connection.sync().exists(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8)) > 0;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
            return false;
        }
    }

    @Override
    public Set<String> getTags(String pattern) {
        Set<String> tags = new HashSet<>();
        if (FastStringUtils.isEmpty(pattern)) {
            return tags;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            List<byte[]> keys = connection.sync().keys(pattern.getBytes(StandardCharsets.UTF_8));
            for (byte[] key : keys) {
                tags.add(FastStringUtils.splitByWholeSeparator(new String(key), "#")[0]);
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return tags;
    }

    @Override
    public void set(String tag, String key, Object data) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> async = connection.async();
            byte[] serialize = FastSerializeUtils.serialize(data);
            if (serialize == null) {
                async.del(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8));
            } else {
                async.set(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8), serialize);
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    @Override
    public <T> T get(String tag, String key) throws Exception {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return null;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            RedisAdvancedClusterCommands<byte[], byte[]> sync = connection.sync();
            Object deserialize = FastSerializeUtils.deserialize(sync.get(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8)));
            if (deserialize == null) {
                delete(tag, key);
                return null;
            }
            return (T) deserialize;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return null;
    }

    @Override
    public void delete(String tag) {
        if (FastStringUtils.isEmpty(tag)) {
            return;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> async = connection.async();
            RedisFuture<List<byte[]>> keys = async.keys((this.wrapPattern(this.safeString(tag) + "#")).getBytes(StandardCharsets.UTF_8));
            keys.thenAccept(bytes -> {
                for (byte[] aByte : bytes) {
                    async.del(aByte);
                }
            });
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    @Override
    public void delete(String tag, String key) {
        if (FastStringUtils.isEmpty(tag) || FastStringUtils.isEmpty(key)) {
            return;
        }
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> async = connection.async();
            async.del(wrapKey(tag, key).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    @Override
    public void publish(String channel, String message) throws Exception {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = getConnection()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> async = connection.async();
            async.publish(channel.getBytes(StandardCharsets.UTF_8), message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    @Override
    public AutoCloseable subscribe(String channel, OnSubscribe onSubscribe) throws Exception {
        LinkedBlockingQueue<String> messages = LettuceFactories.newBlockingQueue(); // 阻塞队列，用来存储 channel 中收到的消息

        RedisClusterClient client = getRedisClient();
        StatefulRedisPubSubConnection<String, String> pubSubConnection = client.connectPubSub(); // 获取订阅、发布连接
        pubSubConnection.addListener(new RedisPubSubAdapter<String,String>(){
            @Override
            public void message(String channel, String message) {
                super.message(channel, message);
                try {
                    messages.put(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        pubSubConnection.sync().subscribe(channel);

        final boolean[] subscribing = {true};
        Thread backThread = new Thread(() -> {
            while (subscribing[0]) {
                try {
                    onSubscribe.onMessage(channel, messages.take());
                } catch (InterruptedException e) {
                    FastChar.getLogger().error(this.getClass(), e);
                }
            }
        });
        backThread.start();
        backThread.join();
        return () -> {
            subscribing[0] = false;
            pubSubConnection.close();
        };
    }


    public synchronized void onWebStop() {
        try {
            if (redisClient != null) {
                redisClient.close();
            }
        } finally {
            redisClient = null;
        }
    }
}
