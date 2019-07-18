package com.fastchar.interfaces;

import com.fastchar.core.FastDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    void onInterceptor(HttpServletRequest request, HttpServletResponse response, FastDispatcher dispatcher) throws Exception;

}
