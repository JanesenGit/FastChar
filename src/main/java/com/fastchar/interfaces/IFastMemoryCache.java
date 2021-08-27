package com.fastchar.interfaces;

/**
 * Java内存缓存框架
 * @author 沈建（Janesen）
 * @date 2021/7/5 18:13
 */
public interface IFastMemoryCache {

    <T> T get(String key);

    <T> T get(String key, T defaultValue);

    void put(String key, Object value);

    void remove(String key);
}
