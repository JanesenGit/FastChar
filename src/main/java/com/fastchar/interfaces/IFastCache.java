package com.fastchar.interfaces;

import java.util.List;
import java.util.Set;

/**
 * 缓存处理接口
 */
public interface IFastCache {

    /**
     * 检测缓存是否已存在
     * @param tag 缓存的标签
     * @param key 缓存的key
     * @return 布尔值
     */
    boolean exists(String tag, String key);

    /**
     * 获取所有缓存的标签
     * @param pattern 标签匹配
     * @return Set&lt;String&gt;
     */
    Set<String> getTags(String pattern);

    /**
     * 设置缓存
     * @param tag 缓存的标签
     * @param key 缓存的key
     * @param data 缓存的数据
     * @throws Exception 异常
     */
    void set(String tag, String key, Object data) throws Exception;

    /**
     * 获取缓存
     * @param tag 缓存的标签
     * @param key 缓存的key
     * @param <T> 泛型
     * @return 泛型对象
     * @throws Exception 异常
     */
    <T> T get(String tag, String key) throws Exception;

    /**
     * 删除缓存
     * @param tag 缓存的标签
     */
    void delete(String tag);

    /**
     * 删除缓存
     * @param tag 缓存的标签
     * @param key 缓存的key
     */
    void delete(String tag, String key);

}
