package com.fastchar.interfaces;

/**
 * 本地信息接口
 */
public interface IFastLocal {

    /**
     * 获取本地信息描述
     * @param key 信息的key
     * @param args 信息的参数
     * @return 本地信息
     */
    String getInfo(String key, Object... args);

}
