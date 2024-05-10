package com.fastchar.extend.rocketmq;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMessageQueue;
import com.fastchar.utils.FastStringUtils;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.Producer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@AFastClassFind("org.apache.rocketmq.client.apis.ClientServiceProvider")
public class FastRocketMQProvider implements IFastMessageQueue {

    private String configCode;

    private volatile ClientServiceProvider provider;

    public FastRocketMQProvider() {
    }

    public FastRocketMQProvider(String configCode) {
        this.configCode = configCode;
    }

    private FastRocketMQConfig getConfig() {
        return FastChar.getConfig(this.configCode, FastRocketMQConfig.class);
    }


    private ClientConfiguration getClientConfiguration() {
        if (FastStringUtils.isNotEmpty(getConfig().getAccessKey())
                && FastStringUtils.isNotEmpty(getConfig().getAccessSecret())) {
            StaticSessionCredentialsProvider staticSessionCredentialsProvider =
                    new StaticSessionCredentialsProvider(getConfig().getAccessKey(), getConfig().getAccessSecret());
            return ClientConfiguration
                    .newBuilder()
                    .setEndpoints(getConfig().getEndpoints())
                    .setCredentialProvider(staticSessionCredentialsProvider)
                    .build();
        }

        return ClientConfiguration
                .newBuilder()
                .setEndpoints(getConfig().getEndpoints())
                .build();
    }

    private Producer getProducer() throws Exception {
        FastRocketMQConfig config = getConfig();
        return getProvider()
                .newProducerBuilder()
                .setTopics(config.getTopic())
                .setClientConfiguration(getClientConfiguration())
                .build();
    }

    private ClientServiceProvider getProvider() {
        if (provider == null) {
            synchronized (this) {
                if (provider == null) {
                    provider = ClientServiceProvider.loadService();
                }
            }
        }
        return provider;
    }

    @Override
    public void send(String channel, String message) throws Exception {
        try (Producer producer = getProducer()) {
            Message messageInfo = getProvider().newMessageBuilder()
                    .setTopic(getConfig().getTopic())
                    .setKeys(message)
                    .setTag(channel)
                    .setBody(message.getBytes(StandardCharsets.UTF_8))
                    .build();
            producer.send(messageInfo);
        }

    }

    @Override
    public AutoCloseable receive(String channel, OnReceive onReceive) throws Exception {
        FilterExpression filterExpression = new FilterExpression(channel, FilterExpressionType.TAG);

        SimpleConsumer simpleConsumer = getProvider().newSimpleConsumerBuilder()
                .setClientConfiguration(getClientConfiguration())
                .setConsumerGroup(getConfig().getGroup())
                .setAwaitDuration(Duration.ofSeconds(60))
                .setSubscriptionExpressions(Collections.singletonMap(getConfig().getTopic(), filterExpression))
                .build();

        final boolean[] subscribing = {true};
        Thread backThread = new Thread(() -> {
            while (subscribing[0]) {
                try {
                    List<MessageView> listCompletableFuture = simpleConsumer.receive(1, Duration.ofSeconds(30));
                    for (MessageView messageView : listCompletableFuture) {
                        onReceive.onMessage(messageView.getTag().orElse(channel), StandardCharsets.UTF_8.decode(messageView.getBody()).toString(), () -> {
                            try {
                                simpleConsumer.ack(messageView);
                            } catch (ClientException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (ClientException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        backThread.start();
        backThread.join();
        return () -> {
            subscribing[0] = false;
            simpleConsumer.close();
        };
    }
}
