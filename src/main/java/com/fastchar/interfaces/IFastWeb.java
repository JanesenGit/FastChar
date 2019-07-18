package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

/**
 * FastChar核心接口，系统初始化接口
 */
public interface IFastWeb {

    /**
     * web启动初始化
     */
     void onInit(FastEngine engine) throws Exception;

    /**
     * web停止
     */
    void onDestroy(FastEngine engine) throws Exception;
}
