package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

/**
 * FastChar核心接口，系统初始化接口
 */
public interface IFastWebRun extends IFastWeb{

    /**
     * web启动完成开始运行
     */
     void onRun(FastEngine engine) throws Exception;

}
