package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

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
