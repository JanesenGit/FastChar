package com.fastchar.core;

import com.fastchar.out.FastOut;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FastAction插件
 * @author 沈建（Janesen）
 */
public final class FastActions {
    private Class<? extends FastOut<?>> defaultOut;
    private final List<String> excludeUrls = new ArrayList<>();//排除拦截url
    private boolean excludeServlet = true;//排除拦截servlet

    FastActions() {
    }
    public FastActions add(Class<? extends FastAction> actionClass) throws Exception {
        if (Modifier.isAbstract(actionClass.getModifiers())) {
            return this;
        }
        if (Modifier.isInterface(actionClass.getModifiers())) {
            return this;
        }
        if (!Modifier.isPublic(actionClass.getModifiers())) {
            return this;
        }
        FastDispatcher.actionResolver(actionClass);
        return this;
    }

    public Class<? extends FastOut<?>> getDefaultOut() {
        return defaultOut;
    }

    public FastActions setDefaultOut(Class<? extends FastOut<?>> defaultOut) {
        this.defaultOut = defaultOut;
        return this;
    }

    /**
     * 排除路径，例如：/druid/*,/user/servlet.action
     * @param urlPatterns url匹配值
     * @return 当前对象
     */
    public FastActions addExcludeUrls(String... urlPatterns) {
        this.excludeUrls.addAll(Arrays.asList(urlPatterns));
        return this;
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public boolean isExcludeServlet() {
        return excludeServlet;
    }

    public FastActions setExcludeServlet(boolean excludeServlet) {
        this.excludeServlet = excludeServlet;
        return this;
    }
}
