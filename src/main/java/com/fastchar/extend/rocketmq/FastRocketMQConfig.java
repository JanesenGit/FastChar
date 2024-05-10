package com.fastchar.extend.rocketmq;

import com.fastchar.interfaces.IFastConfig;

public class FastRocketMQConfig implements IFastConfig {


    /**
     * 采用阿里云的rocketmq时，需要授权码访问，【实例用户名】
     */
    private String accessKey;

    /**
     * 采用阿里云的rocketmq时，需要授权码访问【实例密码】
     */
    private String accessSecret;


    /**
     * 消息主题，手动创建
     */
    private String topic;

    /**
     * 消息组，手动创建
     */
    private String group;

    /**
     * rocketmq地址列表，多个以';'分号分割
     */
    private String endpoints;

    public String getTopic() {
        return topic;
    }

    public FastRocketMQConfig setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public FastRocketMQConfig setEndpoints(String endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public FastRocketMQConfig setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public FastRocketMQConfig setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public FastRocketMQConfig setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
        return this;
    }
}
