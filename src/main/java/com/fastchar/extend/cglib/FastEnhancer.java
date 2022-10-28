package com.fastchar.extend.cglib;

import com.fastchar.interfaces.IFastMethodInterceptor;
import com.fastchar.utils.FastClassUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * cglib动态代理操作工具类
 */
@SuppressWarnings("unchecked")
public class FastEnhancer<T> {

    public static <T> FastEnhancer<T> get(Class<T> targetClass) {
        FastEnhancer<T> tFastEnhancer = new FastEnhancer<>();
        tFastEnhancer.targetClass = targetClass;
        tFastEnhancer.safe = false;
        return tFastEnhancer;
    }

    public static <T> FastEnhancer<T> getBySafe(Class<T> targetClass) {
        FastEnhancer<T> tFastEnhancer = new FastEnhancer<>();
        tFastEnhancer.targetClass = targetClass;
        tFastEnhancer.safe = true;
        return tFastEnhancer;
    }


    private FastEnhancer() {
    }

    private Class<T> targetClass;
    private boolean safe;
    private final List<IFastMethodInterceptor> beforeInterceptors = new ArrayList<>(5);
    private final List<IFastMethodInterceptor> afterInterceptors = new ArrayList<>(5);
    private final MethodInterceptor interceptor = new MethodInterceptor() {
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

            for (IFastMethodInterceptor beforeInterceptor : beforeInterceptors) {
                if (beforeInterceptor.intercept(o, method, objects)) {
                    return null;
                }
            }
            Object methodReturn = methodProxy.invokeSuper(o, objects);
            for (IFastMethodInterceptor beforeInterceptor : afterInterceptors) {
                if (beforeInterceptor.intercept(o, method, objects)) {
                    return null;
                }
            }
            return methodReturn;
        }
    };


    public T create() {
        if (safe) {
            Class<?> aClass = FastClassUtils.getClass("net.sf.cglib.proxy.Enhancer", false);
            if (aClass == null) {
                return FastClassUtils.newInstance(targetClass);
            }
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

    public T create(Class<T>[] argumentTypes, Object[] arguments) {
        if (safe) {
            Class<?> aClass = FastClassUtils.getClass("net.sf.cglib.proxy.Enhancer", false);
            if (aClass == null) {
                return FastClassUtils.newInstance(targetClass, arguments);
            }
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(interceptor);
        return (T) enhancer.create(argumentTypes, arguments);
    }

    public Class<T> createClass() {
        if (safe) {
            Class<?> aClass = FastClassUtils.getClass("net.sf.cglib.proxy.Enhancer", false);
            if (aClass == null) {
                return targetClass;
            }
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(interceptor);
        return enhancer.createClass();
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }


    public FastEnhancer<T> addBeforeInterceptor(IFastMethodInterceptor interceptor) {
        beforeInterceptors.add(interceptor);
        return this;
    }

    public FastEnhancer<T> addBeforeInterceptor(Class<? extends IFastMethodInterceptor> interceptor) {
        beforeInterceptors.add(FastClassUtils.newInstance(interceptor));
        return this;
    }

    public FastEnhancer<T> addAfterInterceptor(IFastMethodInterceptor interceptor) {
        afterInterceptors.add(interceptor);
        return this;
    }

    public FastEnhancer<T> addAfterInterceptor(Class<? extends IFastMethodInterceptor> interceptor) {
        afterInterceptors.add(FastClassUtils.newInstance(interceptor));
        return this;
    }
}
