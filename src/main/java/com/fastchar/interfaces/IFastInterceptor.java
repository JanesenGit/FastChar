package com.fastchar.interfaces;

import com.fastchar.core.FastAction;

/**
 * 拦截器接口
 */
public interface IFastInterceptor {

    /**
     * 触发拦截器
     * @param fastAction FastAction对象
     * @throws Exception 异常
     */
    void onInterceptor(FastAction fastAction) throws Exception;

}
