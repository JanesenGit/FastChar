package com.fastchar.interfaces;

import java.io.Serializable;

/**
 * 本地信息接口
 */
public interface IFastLocal extends Serializable {

    /**
     * 获取本地信息描述
     * @param key 信息的key
     * @param args 信息的参数
     * @return 本地信息
     */
    String getInfo(String key, Object... args);

}
