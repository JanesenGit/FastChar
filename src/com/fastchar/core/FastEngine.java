package com.fastchar.core;

import com.fastchar.accepter.*;
import com.fastchar.converters.*;
import com.fastchar.database.FastDb;
import com.fastchar.database.operate.FastMySqlDatabaseOperateProvider;
import com.fastchar.extend.ehcache.FastEhCacheProvider;
import com.fastchar.extend.fastjson.FastJsonProvider;
import com.fastchar.extend.gson.FastGsonProvider;
import com.fastchar.extend.jdbc.FastJdbcDataSourceProvider;
import com.fastchar.extend.redis.FastRedisClusterProvider;
import com.fastchar.extend.redis.FastRedisNormalProvider;
import com.fastchar.local.FastCharLocal_CN;
import com.fastchar.observer.FastDatabaseObserver;
import com.fastchar.extend.druid.FastDruidDataSourceProvider;
import com.fastchar.provider.*;
import com.fastchar.system.FastErrorPrintStream;
import com.fastchar.utils.FastStringUtils;
import com.fastchar.validators.FastNullValidator;
import com.fastchar.validators.FastRegularValidator;

import javax.servlet.ServletContext;

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
    private final FastConfigs caches = new FastConfigs();
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


    void init(ServletContext servletContext) {
        this.servletContext = servletContext;
        if (servletContext != null) {
            path.setWebRootPath(servletContext.getRealPath("/"));
            constant.setProjectName(servletContext.getContextPath().replace("/", ""))
                    .setAttachDirectory(FastStringUtils.stripEnd(path.getWebRootPath(), "/") + "/attachments")
                    .setAttachMaxPostSize(30 * 1024 * 1024);
        }
        //保持控制台打印不会错乱
        System.setErr(new FastErrorPrintStream(System.out,true));

        converters.add(FastEntityParamConverter.class);
        converters.add(FastStringParamConverter.class);
        converters.add(FastDateParamConverter.class);
        converters.add(FastFileParamConverter.class);
        converters.add(FastNumberParamConverter.class);
        converters.add(FastBooleanParamConverter.class);
        converters.add(FastEnumParamConverter.class);

        observable.addObserver(new FastDatabaseObserver());
        observable.addObserver(entities);

        validators.add(FastNullValidator.class);
        validators.add(FastRegularValidator.class);

        scanner.addAccepter(new FastOverrideScannerAccepter());
        scanner.addAccepter(new FastDatabaseXmlScannerAccepter());
        scanner.addAccepter(new FastWebXmlScannerAccepter());
        scanner.addAccepter(new FastEntityScannerAccepter());
        scanner.addAccepter(new FastActionScannerAccepter());
    }

    void startScanner() throws Exception {
        scanner.startScanner();
    }

    void run() throws Exception {
        scanner.coreScanner(this);
        webs.initWeb(this);
        startScanner();
        observable.notifyObservers("onWebStart",this);
        FastDispatcher.initDispatcher();
    }


    void destroy() throws Exception {
        observable.notifyObservers("onWebStop",this);
        getWebs().destroyWeb(this);
    }

    /**
     * 处理url
     *
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

    public FastDb getDb() {
        return getOverrides().newInstance(FastDb.class);
    }

    public FastPath getPath() {
        return path;
    }

    public FastConstant getConstant() {
        return constant;
    }

    public FastInterceptors getInterceptors() {
        return interceptors;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }


    public FastObservable getObservable() {
        return observable;
    }

    public FastScanner getScanner() {
        return scanner;
    }

    public FastActions getActions() {
        return actions;
    }

    public FastConverters getConverters() {
        return converters;
    }

    public FastEntities getEntities() {
        return entities;
    }

    public FastTemplates getTemplates() {
        return templates;
    }

    public FastConfigs getConfigs() {
        return caches;
    }

    public FastValues getValues() {
        return values;
    }

    public FastOverrides getOverrides() {
        return overrides;
    }

    public FastLog getLog() {
        return log;
    }

    public FastValidators getValidators() {
        return validators;
    }

    public FastDatabases getDatabases() {
        return dataSources;
    }

    public <T> T getConfig(Class<T> targetClass) {
        return FastChar.getOverrides().singleInstance(targetClass);
    }
}
