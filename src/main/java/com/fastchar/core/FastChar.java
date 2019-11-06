package com.fastchar.core;

import com.fastchar.database.FastDb;
import com.fastchar.interfaces.*;

import javax.servlet.ServletContext;

/**
 * FastChar全局工具类，涵盖了所有FastChar提供功能
 */
public final class FastChar {

    private FastChar() {
    }

    /**
     * 启动测试环境，一般在main方法中使用
     */
    public static boolean startTest() {
        try {
            FastEngine instance = FastEngine.instance();
            instance.getConstant().setSyncDatabaseXml(false);
            instance.getConstant().setTestEnvironment(true);
            instance.init(null);
            instance.run();
            FastChar.getLog().info(FastChar.class, "Test environment started successfully！");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public static IFastFileRename getFileRename() {
        return getOverrides().singleInstance(IFastFileRename.class);
    }

    public static IFastSecurity getSecurity() {
        return FastEngine.instance().getSecurity();
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

    public static IFastLocal getLocal() {
        return FastChar.getOverrides().singleInstance(IFastLocal.class);
    }

    public static FastLog getLog() {
        return FastEngine.instance().getLog();
    }

    public static String wrapperUrl(String url) {
        return FastEngine.instance().wrapperUrl(url);
    }

    public static IFastJson getJson() {
        return FastChar.getOverrides().singleInstance(IFastJson.class);
    }


    public static FastConfigs getConfigs() {
        return FastEngine.instance().getConfigs();
    }

    public static FastDatabases getDatabases() {
        return FastEngine.instance().getDatabases();
    }


    public static IFastCache getCache() {
        return FastChar.getOverrides().singleInstance(IFastCache.class);
    }

    public static FastValidators getValidators() {
        return FastEngine.instance().getValidators();
    }


    public static <T extends IFastConfig> T getConfig(Class<T> targetClass) {
        return FastEngine.instance().getConfig(targetClass);
    }

    public static <T extends IFastConfig> T getConfig(String onlyCode,Class<T> targetClass) {
        return FastEngine.instance().getConfig(onlyCode, targetClass);
    }

    public static FastProperties getProperties() {
        return FastEngine.instance().getProperties();
    }

    public static FastProperties getProperties(String fileName) {
        return FastEngine.instance().getProperties(fileName);
    }
}
