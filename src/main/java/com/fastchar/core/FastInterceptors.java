package com.fastchar.core;

import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * FastChar核心类，拦截器
 * @author 沈建（Janesen）
 */
public final class FastInterceptors {
    private final List<InterceptorInfo<IFastRootInterceptor>> rootInterceptors = new ArrayList<>();
    private final List<InterceptorInfo<IFastInterceptor>> beforeInterceptors = new ArrayList<>();
    private final List<InterceptorInfo<IFastInterceptor>> afterInterceptors = new ArrayList<>();


    FastInterceptors() {
    }
    public FastInterceptors addRoot(Class<? extends IFastRootInterceptor> interceptor,
                                    String... urlPattern) {
        return addRoot(interceptor, 0, urlPattern);
    }
    public FastInterceptors addRoot(Class<? extends IFastRootInterceptor> interceptor,
                                    int priority,
                                    String... urlPattern) {
        if (!FastClassUtils.checkNewInstance(interceptor)) {
            return this;
        }
        for (String url : urlPattern) {
            InterceptorInfo<IFastRootInterceptor> info = new InterceptorInfo<>();
            info.interceptor = interceptor;
            info.url = url;
            info.priority = priority;
            if (isRootInterceptor(interceptor.getName(), url)) {
                continue;
            }
            rootInterceptors.add(info);
        }
        return this;
    }


    private boolean isRootInterceptor(String className, String url) {
        for (InterceptorInfo<?> interceptorInfo : rootInterceptors) {
            if (interceptorInfo.interceptor.getName().equals(className)
                    && interceptorInfo.url.equals(url)) {
                return true;
            }
        }
        return false;
    }


    public FastInterceptors addBefore(Class<? extends IFastInterceptor> interceptor,
                                      String... urlPattern) {
        return addBefore(interceptor, 0,urlPattern);
    }

    public FastInterceptors addBefore(Class<? extends IFastInterceptor> interceptor,
                                      int priority,
                                      String... urlPattern) {
        if (!FastClassUtils.checkNewInstance(interceptor)) {
            return this;
        }
        for (String url : urlPattern) {
            InterceptorInfo<IFastInterceptor> info = new InterceptorInfo<>();
            info.interceptor = interceptor;
            info.url = url;
            info.priority = priority;
            beforeInterceptors.add(info);
        }
        return this;
    }


    public FastInterceptors addAfter(Class<? extends IFastInterceptor> interceptor,
                                     String... urlPattern) {
        return addAfter(interceptor,0, urlPattern);
    }

    public FastInterceptors addAfter(Class<? extends IFastInterceptor> interceptor,
                                     int priority,
                                     String... urlPattern) {
        if (!FastClassUtils.checkNewInstance(interceptor)) {
            return this;
        }
        for (String url : urlPattern) {
            InterceptorInfo<IFastInterceptor> info = new InterceptorInfo<>();
            info.interceptor = interceptor;
            info.url = url;
            info.priority = priority;
            afterInterceptors.add(info);
        }
        return this;
    }

    void sortRootInterceptor() {
        Comparator<InterceptorInfo<?>> comparator = new Comparator<InterceptorInfo<?>>() {
            @Override
            public int compare(InterceptorInfo o1, InterceptorInfo o2) {
                if (o1.priority > o2.priority) {
                    return -1;
                }
                if (o1.priority < o2.priority) {
                    return 1;
                }
                return 0;
            }
        };
        Collections.sort(rootInterceptors, comparator);
    }


    List<Class<? extends IFastRootInterceptor>> getRootInterceptors(String contentPath) {
        ArrayList<Class<? extends IFastRootInterceptor>> interceptors = new ArrayList<>();
        for (InterceptorInfo<IFastRootInterceptor> rootInterceptor : rootInterceptors) {
            if (FastStringUtils.matches(rootInterceptor.getUrl(), contentPath)) {
                interceptors.add(rootInterceptor.getInterceptor());
            }
        }
        return interceptors;
    }

    List<InterceptorInfo<IFastInterceptor>> getBeforeInterceptors(String url) {
        List<InterceptorInfo<IFastInterceptor>> interceptorInfos = new ArrayList<>();
        for (InterceptorInfo<IFastInterceptor> beforeInterceptor : beforeInterceptors) {
            if (FastStringUtils.matches(beforeInterceptor.getUrl(), url)) {
                interceptorInfos.add(beforeInterceptor);
            }
        }
        return interceptorInfos;
    }

    List<InterceptorInfo<IFastInterceptor>> getAfterInterceptors(String url) {
        List<InterceptorInfo<IFastInterceptor>> interceptorInfos = new ArrayList<>();
        for (InterceptorInfo<IFastInterceptor> afterInterceptor : afterInterceptors) {
            if (FastStringUtils.matches(afterInterceptor.getUrl(), url)) {
                interceptorInfos.add(afterInterceptor);
            }
        }
        return interceptorInfos;
    }


    public void flush() {
        List<InterceptorInfo<IFastRootInterceptor>> waitRemoveA = new ArrayList<>();
        for (InterceptorInfo<IFastRootInterceptor> rootInterceptor : rootInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveA.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3,rootInterceptor.interceptor));
                }
            }
        }

        List<InterceptorInfo<IFastInterceptor>> waitRemoveB = new ArrayList<>();
        for (InterceptorInfo<IFastInterceptor> rootInterceptor : beforeInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveB.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3,rootInterceptor.interceptor));
                }
            }
        }
        for (InterceptorInfo<IFastInterceptor> rootInterceptor : afterInterceptors) {
            if (FastClassUtils.isRelease(rootInterceptor.interceptor)) {
                waitRemoveB.add(rootInterceptor);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastInterceptors.class,
                            FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR3,rootInterceptor.interceptor));
                }
            }
        }
        rootInterceptors.removeAll(waitRemoveA);
        beforeInterceptors.removeAll(waitRemoveB);
        afterInterceptors.removeAll(waitRemoveB);
    }


    public static class InterceptorInfo<T> {
        private String url;
        private Class<? extends T> interceptor;
        private int priority;

        public String getUrl() {
            return url;
        }

        public InterceptorInfo<T> setUrl(String url) {
            this.url = url;
            return this;
        }

        public Class<? extends T> getInterceptor() {
            return interceptor;
        }

        public InterceptorInfo<T> setInterceptor(Class<? extends T> interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public int getPriority() {
            return priority;
        }

        public InterceptorInfo<T> setPriority(int priority) {
            this.priority = priority;
            return this;
        }
    }

}
