package com.fastchar.interfaces;

import com.fastchar.core.FastDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IFastRootInterceptor {

    void onInterceptor(HttpServletRequest request, HttpServletResponse response, FastDispatcher dispatcher) throws Exception;

}
