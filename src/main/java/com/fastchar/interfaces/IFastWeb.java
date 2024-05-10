package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

/**
 * FastChar核心接口，系统初始化接口
 */
public interface IFastWeb {
    /**
     * web启动初始化
     */
    default void onInit(FastEngine engine) throws Exception {
    }


    /**
     * 当注册当前IFastWeb后 触发
     */
    default void onRegister(FastEngine engine) throws Exception {

    }

    /**
     * web启动完成开始运行
     */
    default void onRun(FastEngine engine) throws Exception {

    }


    /**
     * web系统启动完成后触发，
     */
    default void onFinish(FastEngine engine) throws Exception {

    }


    /**
     * web停止
     */
    default void onDestroy(FastEngine engine) throws Exception {

    }


}
