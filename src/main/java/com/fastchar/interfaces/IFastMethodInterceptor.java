package com.fastchar.interfaces;

import java.lang.reflect.Method;

public interface IFastMethodInterceptor {

    boolean intercept(Object o, Method method, Object[] objects);
}
