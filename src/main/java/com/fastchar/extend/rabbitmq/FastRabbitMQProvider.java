package com.fastchar.extend.rabbitmq;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastMessageQueue;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * rabbitmq 工具类
 */
@AFastClassFind("com.rabbitmq.client.Connection")
public class FastRabbitMQProvider implements IFastMessageQueue {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);
    private String configCode;

    public FastRabbitMQProvider() {
    }

    public FastRabbitMQProvider(String configCode) {
        this.configCode = configCode;
    }

    private volatile Channel channel;

    private volatile Connection connection;

    private String charset = FastChar.getConstant().getCharset();

    public String getCharset() {
        return charset;
    }

    public FastRabbitMQProvider setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    private FastRabbitMQConfig getConfig() {
        return FastChar.getConfig(this.configCode, FastRabbitMQConfig.class);
    }

    private Connection getConnection() throws Exception {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    connection = getConnectFactory().newConnection();
                }
            }
        }
        return connection;
    }

    private Channel getChannel() throws Exception {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    channel = getConnection().createChannel(ATOMIC_INTEGER.incrementAndGet());
                }
            }
        }
        return channel;
    }

    private ConnectionFactory getConnectFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        FastRabbitMQConfig config = getConfig();
        factory.setHost(config.getHost());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());
        factory.setPort(config.getPort());
        factory.setVirtualHost(config.getVirtualHost());
        return factory;
    }

    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FastRabbitMQProvider exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,
                                                Map<String, Object> arguments) throws Exception {
        getChannel().exchangeDeclare(exchange, type, durable, autoDelete, arguments);
        return this;
    }

    public FastRabbitMQProvider queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete,
                                             Map<String, Object> arguments) throws Exception {
        getChannel().queueDeclare(queue, durable, exclusive, autoDelete, arguments);
        return this;
    }

    public FastRabbitMQProvider queueBind(String exchange, String queue, String routingKey) throws Exception {
        getChannel().queueBind(exchange, queue, routingKey);
        return this;
    }

    public FastRabbitMQProvider queueBind(String exchange, String queue) throws Exception {
        getChannel().queueBind(queue, exchange, queue);
        return this;
    }


    /**
     * 快速构建常规"交换机exchange类型direct"和"队列queue"并绑定
     *
     * @param exchange 交换机
     * @param queue    队列
     */
    public FastRabbitMQProvider normal(String exchange, String queue) throws Exception {
        this.exchangeDeclare(exchange, "direct", true, false, null)
                .queueDeclare(queue, true, false, false, null)
                .queueBind(exchange, queue);
        return this;
    }


    public FastRabbitMQProvider sendMessage(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, String message) throws Exception {
        getChannel().basicPublish(exchange, routingKey, mandatory, props, message.getBytes(charset));
        return this;
    }

    public FastRabbitMQProvider sendMessage(String exchange, String routingKey, String message) throws Exception {
        getChannel().basicPublish(exchange, routingKey, null, message.getBytes(charset));
        return this;
    }


    public FastRabbitMQProvider watchMessage(String queue, Consumer consumer) throws Exception {
        return watchMessage(queue, true, consumer);
    }
    public FastRabbitMQProvider watchMessage(String queue,boolean autoAck, Consumer consumer) throws Exception {
        getChannel().basicConsume(queue, autoAck, consumer);
        return this;
    }
    @Override
    public void send(String channel, String message) throws Exception {
        this.queueDeclare(channel, true, false, false, null)
                .sendMessage(getConfig().getDefaultExchange(), channel, message)
                .close();
    }

    @Override
    public AutoCloseable receive(String channel, OnReceive onReceive) throws Exception {
        Channel rabbitChannel = getChannel();
        this.queueDeclare(channel, true, false, false, null)
                .watchMessage(channel, false, new DefaultConsumer(rabbitChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                        onReceive.onMessage(channel, new String(body), () -> {
                            try {
                                rabbitChannel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                });
        return rabbitChannel;
    }
}
