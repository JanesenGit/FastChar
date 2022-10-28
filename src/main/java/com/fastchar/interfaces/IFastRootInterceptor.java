package com.fastchar.interfaces;

import com.fastchar.core.FastDispatcher;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;


/**
 * 系统根拦截器
 */
public interface IFastRootInterceptor {

    /**
     * 触发拦截器
     * @param request 请求对象
     * @param response 响应对象
     * @param dispatcher 路由分配器
     * @throws Exception 异常
     */
    void onInterceptor(FastHttpServletRequest request, FastHttpServletResponse response, FastDispatcher dispatcher) throws Exception;

}
