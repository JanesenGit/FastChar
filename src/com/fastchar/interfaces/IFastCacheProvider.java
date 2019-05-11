package com.fastchar.interfaces;

import java.util.List;
import java.util.Set;

public interface IFastCacheProvider {

    boolean exists(String tag, String key);

    Set<String> getTags(String pattern);

    void setCache(String tag, String key, Object data) throws Exception;

    <T> T getCache(String tag, String key) throws Exception;

    void deleteCache(String tag);

    void deleteCache(String tag, String key);

}
