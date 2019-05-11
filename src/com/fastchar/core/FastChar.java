package com.fastchar.core;

import com.fastchar.database.FastDb;
import com.fastchar.interfaces.*;

import javax.servlet.ServletContext;

/**
 * 全局类
 */
public final class FastChar {
    public static final Object PrintLock = new Object();

    private FastChar() {
    }

    /**
     * 启动测试环境，一般在main方法中使用
     * @throws Exception
     */
    public static void startTest() throws Exception {
        FastEngine instance = FastEngine.instance();
        instance.init(null);
        instance.run();
        System.out.println(instance.getLog().lightStyle("======Test environment started successfully！======\n\n"));
    }

    public static ServletContext getServletContext() {
        return FastEngine.instance().getServletContext();
    }

    public static FastConverters getConverters() {
        return FastEngine.instance().getConverters();
    }

    public static FastTemplates getTemplates() {
        return FastEngine.instance().getTemplates();
    }

    public static IFastFileRenameProvider getFileRename() {
        return getOverrides().newInstance(IFastFileRenameProvider.class);
    }

    public static IFastSecurityProvider getSecurity() {
        return getOverrides().newInstance(IFastSecurityProvider.class);
    }

    public static FastDb getDb() {
        return FastEngine.instance().getDb();
    }

    public static FastPath getPath() {
        return FastEngine.instance().getPath();
    }

    public static FastObservable getObservable() {
        return FastEngine.instance().getObservable();
    }

    public static FastConstant getConstant() {
        return FastEngine.instance().getConstant();
    }

    public static FastValues getValues() {
        return FastEngine.instance().getValues();
    }

    public static FastEntities getEntities() {
        return FastEngine.instance().getEntities();
    }

    public static FastActions getActions() {
        return FastEngine.instance().getActions();
    }

    public static FastOverrides getOverrides() {
        return FastEngine.instance().getOverrides();
    }

    public static IFastLocalProvider getLocal() {
        return FastChar.getOverrides().singleInstance(IFastLocalProvider.class);
    }

    public static FastLog getLog() {
        return FastEngine.instance().getLog();
    }

    public static String wrapperUrl(String url) {
        return FastEngine.instance().wrapperUrl(url);
    }

    public static IFastJsonProvider getJson() {
        return FastChar.getOverrides().singleInstance(IFastJsonProvider.class);
    }


    public static FastConfigs getConfigs() {
        return FastEngine.instance().getConfigs();
    }

    public static FastDatabases getDatabases() {
        return FastEngine.instance().getDatabases();
    }


    public static IFastCacheProvider getCache() {
        return FastChar.getOverrides().singleInstance(IFastCacheProvider.class);
    }

    public static FastValidators getValidators() {
        return FastEngine.instance().getValidators();
    }


    public static <T> T getConfig(Class<T> targetClass) {
        return FastEngine.instance().getConfig(targetClass);
    }
}
