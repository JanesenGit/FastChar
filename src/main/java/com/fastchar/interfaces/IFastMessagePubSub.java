package com.fastchar.interfaces;


/**
 * 消息订阅功能，消息无持久化
 */
public interface IFastMessagePubSub  {


    void publish(String channel, String message) throws Exception;


    AutoCloseable subscribe(String channel, OnSubscribe onSubscribe) throws Exception;

    interface OnSubscribe {

        void onMessage(String channel, String message);

    }

}
