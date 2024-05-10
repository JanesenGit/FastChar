package com.fastchar.extend.rabbitmq;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastConfig;

/**
 * rabbitmq连接配置
 */
@AFastClassFind(value = "com.rabbitmq.client.Connection", url = "https://mvnrepository.com/artifact/com.rabbitmq/amqp-client")
public class FastRabbitMQConfig implements IFastConfig {

    private String host;

    private int port = 5672;

    private String username;

    private String password;

    private String virtualHost;

    private String defaultExchange;//默认交换机


    public String getHost() {
        return host;
    }

    public FastRabbitMQConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public FastRabbitMQConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public FastRabbitMQConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FastRabbitMQConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public FastRabbitMQConfig setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    public String getDefaultExchange() {
        return defaultExchange;
    }

    public FastRabbitMQConfig setDefaultExchange(String defaultExchange) {
        this.defaultExchange = defaultExchange;
        return this;
    }
}
