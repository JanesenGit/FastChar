package com.fastchar.core;

import com.fastchar.accepter.*;
import com.fastchar.converters.*;
import com.fastchar.database.FastDb;
import com.fastchar.interfaces.IFastConfig;
import com.fastchar.interfaces.IFastSecurity;
import com.fastchar.observer.FastDatabaseObserver;
import com.fastchar.system.FastErrorPrintStream;
import com.fastchar.utils.FastStringUtils;
import com.fastchar.validators.FastNullValidator;
import com.fastchar.validators.FastRegularValidator;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * FastChar核心框架引擎
 */
@SuppressWarnings("all")
public final class FastEngine {

    private FastEngine() {
    }

    private static class FastContainerHolder {
        final static FastEngine CONTAINER = new FastEngine();
    }

    static FastEngine instance() {
        return FastContainerHolder.CONTAINER;
    }

    private ServletContext servletContext = null;
    private final FastWebs webs = new FastWebs();
    private final FastEntities entities = new FastEntities();
    private final FastActions actions = new FastActions();
    private final FastOverrides overrides = new FastOverrides();
    private final FastTemplates templates = new FastTemplates();
    private final FastConfigs configs = new FastConfigs();
    private final FastDatabases dataSources = new FastDatabases();
    private final FastScanner scanner = new FastScanner();
    private final FastPath path = new FastPath();
    private final FastConstant constant = new FastConstant();
    private final FastInterceptors interceptors = new FastInterceptors();
    private final FastObservable observable = new FastObservable();
    private final FastConverters converters = new FastConverters();
    private final FastValues values = new FastValues();
    private final FastLog log = new FastLog();
    private final FastValidators validators = new FastValidators();
    private final FastFindClass findClass = new FastFindClass();


    void init(ServletContext servletContext) {
        this.servletContext = servletContext;
        if (servletContext != null) {
            path.setWebRootPath(servletContext.getRealPath("/"));
            constant.setProjectName(FastStringUtils.strip(servletContext.getContextPath(), "/"))
                    .setAttachDirectory(FastStringUtils.stripEnd(path.getWebRootPath(), "/") + "/attachments")
                    .setAttachMaxPostSize(30 * 1024 * 1024);

        }
        System.setErr(new FastErrorPrintStream(System.out, true));

        converters.add(FastEntityParamConverter.class);
        converters.add(FastStringParamConverter.class);
        converters.add(FastDateParamConverter.class);
        converters.add(FastFileParamConverter.class);
        converters.add(FastNumberParamConverter.class);
        converters.add(FastBooleanParamConverter.class);
        converters.add(FastEnumParamConverter.class);

        observable.addObserver(FastDatabaseObserver.class);
        observable.addObserver(entities);

        validators.add(FastNullValidator.class);
        validators.add(FastRegularValidator.class);

        scanner.addAccepter(new FastOverrideScannerAccepter());
        scanner.addAccepter(new FastDatabaseXmlScannerAccepter());
        scanner.addAccepter(new FastWebXmlScannerAccepter());
        scanner.addAccepter(new FastEntityScannerAccepter());
        scanner.addAccepter(new FastActionScannerAccepter());

        constant.addCrossHeaders("Content-Type", "Access-Control-Allow-Headers", "Authorization", "X-Requested-With", "token");
    }


    void run() throws Exception {
        scanner.startScanner();
        webs.initWeb(this);
        observable.notifyObservers("onWebStart", this);
        scanner.notifyAccepter();
        observable.notifyObservers("onScannerFinish");
        FastDispatcher.initDispatcher();
    }


    void destroy() throws Exception {
        constant.setWebStopped(true);
        observable.notifyObservers("onWebStop", this);
        getWebs().destroyWeb(this);
    }

    /**
     * 处理url
     * @param url 如果以'/'开头 则相对项目下的路径 否则相对请求的路径
     */
    public String wrapperUrl(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        String projectHead = "/" + FastChar.getConstant().getProjectName();
        if (url.startsWith("/")) {
            if (url.startsWith(projectHead)) {
                return url;
            }
            return projectHead + url;
        }
        return url;
    }


    FastWebs getWebs() {
        return webs;
    }

    /**
     * 获取数据库操作类
     * @return FastDb
     */
    public FastDb getDb() {
        return getOverrides().newInstance(FastDb.class);
    }

    /**
     * 获取系统路径工具类
     * @return FastPath
     */
    public FastPath getPath() {
        return path;
    }

    /**
     * 获取系统常量配置
     * @return FastConstant
     */
    public FastConstant getConstant() {
        return constant;
    }

    /**
     * 获取拦截器配置
     * @return FastInterceptors
     */
    public FastInterceptors getInterceptors() {
        return interceptors;
    }

    /**
     * 获取ServletContext
     * @return ServletContext
     */
    public ServletContext getServletContext() {
        return servletContext;
    }


    /**
     * 获取观察者配置
     * @return FastObservable
     */
    public FastObservable getObservable() {
        return observable;
    }

    /**
     * 获取扫描器
     * @return FastScanner
     */
    public FastScanner getScanner() {
        return scanner;
    }

    /**
     * 获取FastAction配置
     * @return FastActions
     */
    public FastActions getActions() {
        return actions;
    }

    /**
     * 获取参数转换器
     * @return FastConverters
     */
    public FastConverters getConverters() {
        return converters;
    }

    /**
     * 获取FastEntity配置
     * @return FastEntities
     */
    public FastEntities getEntities() {
        return entities;
    }

    /**
     * 获取模板配置
     * @return FastTemplates
     */
    public FastTemplates getTemplates() {
        return templates;
    }

    /**
     * 获取插件配置器
     * @return FastConfigs
     */
    public FastConfigs getConfigs() {
        return configs;
    }

    /**
     * 获取系统全局数据存储器
     * @return
     */
    public FastValues getValues() {
        return values;
    }

    /**
     * 获取类代理器
     * @return FastOverrides
     */
    public FastOverrides getOverrides() {
        return overrides;
    }

    /**
     * 获取系统日志工具类
     * @return FastLog
     */
    public FastLog getLog() {
        return log;
    }

    /**
     * 获取参数验证器
     * @return FastValidators
     */
    public FastValidators getValidators() {
        return validators;
    }

    /**
     * 获取系统配置的数据库
     * @return FastDatabases
     */
    public FastDatabases getDatabases() {
        return dataSources;
    }

    /**
     * 快速获取插件的配置类
     * @param targetClass 插件配置类
     * @param <T> 继承IFastConfig的泛型类
     * @return &lt;T extends IFastConfig&gt;
     */
    public <T extends IFastConfig> T getConfig(Class<T> targetClass) {
        return FastChar.getOverrides().singleInstance(targetClass);
    }


    /**
     * 获取数据加密工具类
     * @return IFastSecurity
     */
    public IFastSecurity getSecurity() {
        return getOverrides().singleInstance(IFastSecurity.class);
    }

    /**
     * 获取Properties配置工具类
     * @return FastProperties
     */
    public FastProperties getProperties() {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(constant.getPropertiesName() + FastProperties.class.getName()),
                FastProperties.class).setFile(new File(path.getClassRootPath(), constant.getPropertiesName()));
    }

    /**
     *  获取Properties配置工具类
     * @param fileName 位于src目录下properties文件名
     * @return
     */
    public FastProperties getProperties(String fileName) {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(fileName + FastProperties.class.getName()),
                FastProperties.class).setFile(new File(path.getClassRootPath(), fileName));
    }

    /**
     * 获取类加载预检测器，可用于第三方jar包引用的检测
     * @return FastFindClass
     */
    public FastFindClass getFindClass() {
        return findClass;
    }
}
