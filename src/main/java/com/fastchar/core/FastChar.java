package com.fastchar.core;

import com.fastchar.database.FastDB;
import com.fastchar.database.FastDatabaseTransaction;
import com.fastchar.database.FastDatabaseXml;
import com.fastchar.database.FastDatabases;
import com.fastchar.interfaces.*;
import com.fastchar.servlet.FastServletContext;

import java.io.File;

/**
 * FastChar全局工具类，涵盖了所有FastChar提供功能
 *
 * @author 沈建（Janesen）
 * @see <a href="https://www.fastchar.com">FastChar</a>
 */
public final class FastChar {
    private static final ThreadLocal<FastAction> THREAD_LOCAL_ACTION = new ThreadLocal<FastAction>();
    private static boolean TEST_STARTED = false;

    private FastChar() {
    }

    /**
     * 启动测试环境，一般在main方法中使用
     */
    public static boolean startTest() {
        try {
            FastEngine instance = FastEngine.instance();
            if (instance.getConstant().isWebStarted()) {
                FastChar.getLog().info(FastChar.class, "The current web server is running! ");
                return true;
            }
            if (TEST_STARTED) {
                FastChar.getLog().info(FastChar.class, "Test environment started!  ");
                return true;
            }
            instance.getConstant().setSyncDatabaseXml(false);
            instance.getConstant().setTestEnvironment(true);
            instance.init(null);
            instance.run();
            TEST_STARTED = true;
            FastChar.getLog().info(FastChar.class, "Test environment started successfully！");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static FastAction getThreadLocalAction() {
        return THREAD_LOCAL_ACTION.get();
    }

    public static void setThreadLocalAction(FastAction threadLocalAction) {
        THREAD_LOCAL_ACTION.set(threadLocalAction);
    }

    public static void removeThreadLocalAction() {
        THREAD_LOCAL_ACTION.remove();
    }


    public static FastServletContext getServletContext() {
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


    public static FastScanner getScanner() {
        return FastEngine.instance().getScanner();
    }

    public static FastModules getModules() {
        return FastEngine.instance().getModules();
    }

    public static FastDB getDB() {
        return FastEngine.instance().getDB();
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

    public static FastLogger getLogger() {
        return FastEngine.instance().getLogger();
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

    public static FastDatabaseXml getDatabaseXml() {
        return FastEngine.instance().getDatabaseXml();
    }

    public static IFastCache getCache() {
        return FastChar.getOverrides().singleInstance(IFastCache.class);
    }

    public static IFastCache safeGetCache() {
        return FastChar.getOverrides().singleInstance(false, IFastCache.class);
    }

    public static IFastMemoryCache getMemoryCache() {
        return FastChar.getOverrides().singleInstance(IFastMemoryCache.class);
    }

    public static IFastMemoryCache safeGetMemoryCache() {
        return FastChar.getOverrides().singleInstance(false, IFastMemoryCache.class);
    }

    public static FastValidators getValidators() {
        return FastEngine.instance().getValidators();
    }


    public static <T extends IFastConfig> T getConfig(Class<T> targetClass) {
        return FastEngine.instance().getConfig(targetClass);
    }

    public static <T extends IFastConfig> T getConfig(String onlyCode, Class<T> targetClass) {
        return FastEngine.instance().getConfig(onlyCode, targetClass);
    }

    public static FastProperties getProperties() {
        return FastEngine.instance().getProperties();
    }

    public static FastProperties getProperties(String fileName) {
        return FastEngine.instance().getProperties(fileName);
    }

    public static FastProperties getProperties(File properties) {
        return FastEngine.instance().getProperties(properties);
    }


    public static FastFindClass getFindClass() {
        return FastEngine.instance().getFindClass();
    }

    public static boolean isMain() {
        return FastEngine.instance().isMain();
    }


    /**
     * 判断当前线程是否开启了数据库事务
     *
     * @return 布尔值
     */
    public static boolean isThreadTransaction() {
        return FastDatabaseTransaction.isThreadTransaction();
    }


    /**
     * 开启当前线程的数据库事务
     */
    public synchronized static void beginDatabaseThreadTransaction() {
        FastDatabaseTransaction.beginThreadTransaction();
    }

    /**
     * 结束当前线程的数据库事务
     */
    public synchronized static void endDatabaseThreadTransaction()  {
        FastDatabaseTransaction.endThreadTransaction();
    }

    /**
     * 回滚当前线程的数据库事务
     */
    public synchronized static void rollbackDatabaseThreadTransaction()  {
        FastDatabaseTransaction.rollbackThreadTransaction();
    }

    /**
     * 获取当前线程的数据库事务
     */
    public static FastDatabaseTransaction getDatabaseThreadTransaction() {
        return FastDatabaseTransaction.getThreadTransaction();
    }


}
