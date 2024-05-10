package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FastChar核心类，拦截器
 *
 * @author 沈建（Janesen）
 */
public final class FastInterceptors {
    private final List<FastInterceptorInfo<IFastRootInterceptor>> rootInterceptors = new ArrayList<>(16);
    private final List<FastInterceptorInfo<IFastInterceptor>> beforeInterceptors = new ArrayList<>(16);
    private final List<FastInterceptorInfo<IFastInterceptor>> afterInterceptors = new ArrayList<>(16);


    FastInterceptors() {
    }

    public FastInterceptors addRoot(Class<? extends IFastRootInterceptor> interceptor,
                                    String... urlPattern) {
        int priority = 0;
        if (interceptor.isAnnotationPresent(AFastPriority.class)) {
            AFastPriority annotation = interceptor.getAnnotation(AFastPriority.class);
            priority = annotation.value();
        }
        return addRoot(interceptor, priority, urlPattern);
    }

    public FastInterceptors addRoot(Class<? extends IFastRootInterceptor> interceptor,
                                    int priority,
                                    String... urlPattern) {
        try {
            if (!FastClassUtils.checkNewInstance(interceptor)) {
                return this;
            }
            for (String url : urlPattern) {
                FastInterceptorInfo<IFastRootInterceptor> info = new FastInterceptorInfo<>();
                info.interceptor = interceptor;
                info.url = url;
                info.priority = priority;
                if (isRootInterceptor(interceptor.getName(), url)) {
                    continue;
                }
                rootInterceptors.add(info);
            }
            return this;
        } finally {
            sortRootInterceptor();
        }
    }


    private boolean isRootInterceptor(String className, String url) {
        for (FastInterceptorInfo<?> interceptorInfo : rootInterceptors) {
            if (interceptorInfo.interceptor.getName().equals(className)
                    && interceptorInfo.url.equals(url)) {
                return true;
            }
        }
        return false;
    }


    public FastInterceptors addBefore(Class<? extends IFastInterceptor> interceptor,
                                      String... urlPattern) {
        int priority = 0;
        if (interceptor.isAnnotationPresent(AFastPriority.class)) {
            AFastPriority annotation = interceptor.getAnnotation(AFastPriority.class);
            priority = annotation.value();
        }
        return addBefore(interceptor, priority, urlPattern);
    }

    public FastInterceptors addBefore(Class<? extends IFastInterceptor> interceptor,
                                      int priority,
                                      String... urlPattern) {
        if (!FastClassUtils.checkNewInstance(interceptor)) {
            return this;
        }
        for (String url : urlPattern) {
            FastInterceptorInfo<IFastInterceptor> info = new FastInterceptorInfo<>();
            info.interceptor = interceptor;
            info.url = url;
            info.priority = priority;
            beforeInterceptors.add(info);
        }
        return this;
    }


    public FastInterceptors addAfter(Class<? extends IFastInterceptor> interceptor,
                                     String... urlPattern) {
        int priority = 0;
        if (interceptor.isAnnotationPresent(AFastPriority.class)) {
            AFastPriority annotation = interceptor.getAnnotation(AFastPriority.class);
            priority = annotation.value();
        }
        return addAfter(interceptor, priority, urlPattern);
    }

    public FastInterceptors addAfter(Class<? extends IFastInterceptor> interceptor,
                                     int priority,
                                     String... urlPattern) {
        if (!FastClassUtils.checkNewInstance(interceptor)) {
            return this;
        }
        for (String url : urlPattern) {
            FastInterceptorInfo<IFastInterceptor> info = new FastInterceptorInfo<>();
            info.interceptor = interceptor;
            info.url = url;
            info.priority = priority;
            afterInterceptors.add(info);
        }
        return this;
    }

    void sortRootInterceptor() {
        Comparator<FastInterceptorInfo<?>> comparator = (o1, o2) -> Integer.compare(o2.priority, o1.priority);
        rootInterceptors.sort(comparator);
    }


    List<FastInterceptorInfo<IFastRootInterceptor>> getRootInterceptors(String contentPath) {
        ArrayList<FastInterceptorInfo<IFastRootInterceptor>> interceptors = new ArrayList<>(16);
        for (FastInterceptorInfo<IFastRootInterceptor> rootInterceptor : rootInterceptors) {
            if (FastStringUtils.matches(rootInterceptor.url, contentPath)) {
                interceptors.add(rootInterceptor);
            }
        }
        return interceptors;
    }

    List<FastInterceptorInfo<IFastInterceptor>> getBeforeInterceptors(String url) {
        List<FastInterceptorInfo<IFastInterceptor>> interceptorInfos = new ArrayList<>(16);
        for (FastInterceptorInfo<IFastInterceptor> beforeInterceptor : beforeInterceptors) {
            if (FastStringUtils.matches(beforeInterceptor.url, url)) {
                interceptorInfos.add(beforeInterceptor);
            }
        }
        return interceptorInfos;
    }

    List<FastInterceptorInfo<IFastInterceptor>> getAfterInterceptors(String url) {
        List<FastInterceptorInfo<IFastInterceptor>> interceptorInfos = new ArrayList<>(16);
        for (FastInterceptorInfo<IFastInterceptor> afterInterceptor : afterInterceptors) {
            if (FastStringUtils.matches(afterInterceptor.url, url)) {
                interceptorInfos.add(afterInterceptor);
            }
        }
        return interceptorInfos;
    }


    public void flush() {
        List<FastInterceptorInfo<IFastRootInterceptor>> waitRemoveA = new ArrayList<>(16);
        for (FastInterceptorInfo<IFastRootInterceptor> rootInterceptor : rootInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveA.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3, rootInterceptor.interceptor));
                }
            }
        }

        List<FastInterceptorInfo<IFastInterceptor>> waitRemoveB = new ArrayList<>(16);
        for (FastInterceptorInfo<IFastInterceptor> rootInterceptor : beforeInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveB.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3, rootInterceptor.interceptor));
                }
            }
        }
        for (FastInterceptorInfo<IFastInterceptor> rootInterceptor : afterInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveB.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3, rootInterceptor.interceptor));
                }
            }
        }
        rootInterceptors.removeAll(waitRemoveA);
        beforeInterceptors.removeAll(waitRemoveB);
        afterInterceptors.removeAll(waitRemoveB);
    }

}
