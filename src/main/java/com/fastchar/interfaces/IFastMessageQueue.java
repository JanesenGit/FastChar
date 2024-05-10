package com.fastchar.interfaces;


/**
 * 消息队列
 */
public interface IFastMessageQueue {

    void send(String channel, String message) throws Exception;


    AutoCloseable receive(String channel, OnReceive onReceive) throws Exception;


    interface OnReceive {
        void onMessage(String channel, String message, AckHandler ackHandler);
    }

    interface AckHandler {
        void ack();
    }

}
