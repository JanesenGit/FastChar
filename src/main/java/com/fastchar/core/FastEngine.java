package com.fastchar.core;

import com.fastchar.database.FastDB;
import com.fastchar.database.FastDatabaseObserver;
import com.fastchar.database.FastDatabaseXml;
import com.fastchar.database.FastDatabases;
import com.fastchar.enums.FastObservableEvent;
import com.fastchar.enums.FastServerType;
import com.fastchar.enums.FastServletType;
import com.fastchar.extend.yml.FastYaml;
import com.fastchar.interfaces.IFastConfig;
import com.fastchar.interfaces.IFastSecurity;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.FastHttpHeaders;
import com.fastchar.servlet.FastServletContext;
import com.fastchar.servlet.FastServletRegistration;
import com.fastchar.system.FastErrorPrintStream;
import com.fastchar.system.FastOutPrintStream;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * FastChar核心框架引擎
 *
 * @author 沈建（Janesen）
 * @see <a href="https://www.fastchar.com">FastChar</a>
 */
@SuppressWarnings("all")
public final class FastEngine {

    private FastEngine() {
    }

    private static class FastEngineHolder {
        final static FastEngine ENGINE_HOLDER = new FastEngine();
    }

    static FastEngine instance() {
        return FastEngineHolder.ENGINE_HOLDER;
    }

    private FastServletContext servletContext = null;
    private boolean catchExceptionDestory;
    private final FastWebs webs = new FastWebs();
    private final FastEntities entities = new FastEntities();
    private final FastActions actions = new FastActions();
    private final FastLogger logger = new FastLogger();
    private final FastOverrides overrides = new FastOverrides();
    private final FastTemplates templates = new FastTemplates();
    private final FastDatabases dataSources = new FastDatabases();
    private final FastJarResources jarResources = new FastJarResources();
    private final FastWebResources webResources = new FastWebResources();
    private final FastScanner scanner = new FastScanner();
    private final FastPath path = new FastPath();
    private final FastModules modules = new FastModules();
    private final FastConstant constant = new FastConstant();
    private final FastInterceptors interceptors = new FastInterceptors();
    private final FastObservable observable = new FastObservable();
    private final FastConverters converters = new FastConverters();
    private final FastValues values = new FastValues();
    private final FastValidators validators = new FastValidators();
    private final FastFindClass findClass = new FastFindClass();
    private final FastDatabaseXml databaseXml = new FastDatabaseXml();
    private final Manifest projectManifest = new Manifest();


    void createWebServer(Class<?> fromClass, FastServletContext servletContext, String web) throws Exception {
        try {
            long time = System.currentTimeMillis();
            FastEngine engine = FastEngine.instance();
            engine.getConstant().webServer = true;
            if (engine.getConstant().isWebStarted()) {
                return;
            }

            engine.getConstant().setBeginInitTime(time);
            engine.init(servletContext);
            engine.getLogger().info(FastEngine.class, FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR2,
                    FastChar.getConstant().getProjectName()));

            Class<?> aClass = FastClassUtils.getClass(web);
            if (aClass != null) {
                if (!IFastWeb.class.isAssignableFrom(aClass)) {
                    FastChar.getLogger().error(FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR1, aClass.getSimpleName(),
                            IFastWeb.class.getSimpleName()) +
                            "\n\tat " + new StackTraceElement(aClass.getName(), aClass.getSimpleName(), aClass.getSimpleName() + ".java", 1));
                } else {
                    engine.getWebs().putFastWeb((Class<? extends IFastWeb>) aClass);
                }
            }
            engine.run();
            long endTime = System.currentTimeMillis();
            engine.getLogger().info(FastEngine.class, FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR1,
                    FastChar.getConstant().getProjectName(), (endTime - time) / 1000.0));
            engine.getConstant().webStarted = true;
            engine.getConstant().setEndInitTime(endTime);

            engine.finish();
        } catch (Exception e) {
            if (catchExceptionDestory) {
                try {
                    FastEngine.instance().destroy();
                } catch (Exception ignored) {
                }
            }
            throw e;
        }
    }

    void init(FastServletContext servletContext) {
        this.servletContext = servletContext;
        if (servletContext != null) {
            path.webRootPath = servletContext.getRealPath("/");
            constant.setProjectName(FastStringUtils.strip(servletContext.getContextPath(), "/"))
                    .setAttachDirectory(new File(path.getWebRootPath(), "attachments").getAbsolutePath())
                    .setAttachMaxPostSize(500 * 1024 * 1024);

            String serverInfo = servletContext.getServerInfo().toLowerCase();
            if (serverInfo.contains(FastServerType.Tomcat.name().toLowerCase())) {
                constant.serverType = FastServerType.Tomcat;
            } else if (serverInfo.contains(FastServerType.Jetty.name().toLowerCase())) {
                constant.serverType = FastServerType.Jetty;
            } else if (serverInfo.contains(FastServerType.Undertow.name().toLowerCase())) {
                constant.serverType = FastServerType.Undertow;
            }
        }

        initTomcatConfig();

        System.setOut(new FastOutPrintStream(new FileOutputStream(FileDescriptor.out), true));
        System.setErr(new FastErrorPrintStream(System.out, true));

        observable.addObserver(FastDatabaseObserver.class);

        constant.addCrossHeaders(FastHttpHeaders.CONTENT_TYPE,
                FastHttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                FastHttpHeaders.AUTHORIZATION,
                FastHttpHeaders.X_REQUESTED_WITH,
                FastHttpHeaders.TOKEN,
                FastHttpHeaders.SESSION_ID);
    }

    void initTomcatConfig() {
        if (servletContext == null) {
            return;
        }
        //注册tomcat静态资源解析的servlet，并传入解析文件的编码格式
        if (FastChar.getConstant().getServerType() == FastServerType.Tomcat) {
            FastServletRegistration catalinaDefaultServlet = servletContext.addServlet("default", "org.apache.catalina.servlets.DefaultServlet");
            if (catalinaDefaultServlet.getTarget() != null) {
                //有可能注册失败
                catalinaDefaultServlet.setInitParameter("fileEncoding", FastChar.getConstant().getCharset());
                catalinaDefaultServlet.setInitParameter("listings", "false");
                catalinaDefaultServlet.setInitParameter("precompressed", "true");
                catalinaDefaultServlet.setInitParameter("gzip", "true");
            }
        }
    }


    void run() throws Exception {
        //优先扫描当前项目
        scanner.startScannerProject();

        //优先注册Override的类
        scanner.registerOverrider();

        //优先注册本地项目的web类
        scanner.registerWeb();

        //触发onRegister方法，执行本项目的IFastWeb类
        webs.onRegisterWeb(this);

        //触发onInit方法，执行本项目的IFastWeb类
        webs.onInitWeb(this);

        //接着扫描项目引用的其他jar包
        scanner.startScannerOther();

        //优先注册override的类
        scanner.registerOverrider();

        //注册扫描jar包里的web类
        scanner.registerWeb();

        //触发onRegister方法，执行Jar包中的IFastWeb类
        webs.onRegisterWeb(this);

        //触发onInit方法，执行Jar包中的IFastWeb类
        webs.onInitWeb(this);

        observable.notifyObservers(FastObservableEvent.onWebStart.name(), this);
        scanner.notifyAcceptor();
        observable.notifyObservers(FastObservableEvent.onScannerFinish.name());

        catchExceptionDestory = true;

        if (constant.getServletType() != FastServletType.None) {
            FastDispatcher.initDispatcher();
        }

        observable.notifyObservers(FastObservableEvent.onWebReady.name(), this);
        if (constant.getServletType() != FastServletType.None) {
            webs.onRunWeb(this);
            observable.notifyObservers(FastObservableEvent.onWebRun.name(), this);
            FastDispatcher.initMethodInterceptors();
        }
    }

    void finish() throws Exception {
        //最终触发完成事件
        getWebs().onFinishWeb(this);
    }


    void destroy() throws Exception {
        constant.webStopped = true;
        observable.notifyObservers(FastObservableEvent.onWebStop.name(), this);
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

    synchronized void setProjectManifest(Manifest manifest) {
        if (manifest == null) {
            return;
        }
        this.projectManifest.getMainAttributes().putAll(manifest.getMainAttributes());
        this.projectManifest.getEntries().putAll(manifest.getEntries());
    }

    /**
     * 获取jar包是主项目的Manifest配置
     *
     * @return
     */
    public Manifest getProjectManifest() {
        return projectManifest;
    }

    /**
     * 获取数据库操作类
     *
     * @return FastDb
     */
    public FastDB getDB() {
        return getOverrides().newInstance(FastDB.class);
    }

    /**
     * 获取系统路径工具类
     *
     * @return FastPath
     */
    public FastPath getPath() {
        return path;
    }

    /**
     * 获取系统常量配置
     *
     * @return FastConstant
     */
    public FastConstant getConstant() {
        return constant;
    }

    /**
     * 获取拦截器配置
     *
     * @return FastInterceptors
     */
    public FastInterceptors getInterceptors() {
        return interceptors;
    }

    /**
     * 获取ServletContext
     *
     * @return ServletContext
     */
    public FastServletContext getServletContext() {
        return servletContext;
    }


    /**
     * 获取观察者配置
     *
     * @return FastObservable
     */
    public FastObservable getObservable() {
        return observable;
    }

    /**
     * 获取扫描器
     *
     * @return FastScanner
     */
    public FastScanner getScanner() {
        return scanner;
    }

    /**
     * 获取FastAction配置
     *
     * @return FastActions
     */
    public FastActions getActions() {
        return actions;
    }

    /**
     * 获取参数转换器
     *
     * @return FastConverters
     */
    public FastConverters getConverters() {
        return converters;
    }

    /**
     * 获取FastEntity配置
     *
     * @return FastEntities
     */
    public FastEntities getEntities() {
        return entities;
    }

    /**
     * 获取模板配置
     *
     * @return FastTemplates
     */
    public FastTemplates getTemplates() {
        return templates;
    }

    /**
     * 获取系统全局数据存储器
     *
     * @return
     */
    public FastValues getValues() {
        return values;
    }

    /**
     * 获取类代理器
     *
     * @return FastOverrides
     */
    public FastOverrides getOverrides() {
        return overrides;
    }

    /**
     * 获取日志插件的工具类
     *
     * @return FastLog
     */
    public FastLogger getLogger() {
        return logger;
    }

    /**
     * 获取参数验证器
     *
     * @return FastValidators
     */
    public FastValidators getValidators() {
        return validators;
    }

    /**
     * 获取系统配置的数据库
     *
     * @return FastDatabases
     */
    public FastDatabases getDatabases() {
        return dataSources;
    }

    /**
     * 快速获取插件的配置类
     *
     * @param targetClass 插件配置类
     * @param <T>         继承IFastConfig的泛型类
     * @return &lt;T extends IFastConfig&gt;
     */
    public <T extends IFastConfig> T getConfig(Class<T> targetClass) {
        return FastChar.getOverrides().singleInstance(targetClass);
    }

    /**
     * 快速获取插件的配置类
     *
     * @param onlyCode    配置的唯一编号
     * @param targetClass 插件配置类
     * @param <T>         继承IFastConfig的泛型类
     * @return &lt;T extends IFastConfig&gt;
     */
    public <T extends IFastConfig> T getConfig(String onlyCode, Class<T> targetClass) {
        return FastChar.getOverrides().singleInstance(onlyCode, targetClass);
    }

    /**
     * 获取数据加密工具类
     *
     * @return IFastSecurity
     */
    public IFastSecurity getSecurity() {
        return getOverrides().singleInstance(IFastSecurity.class);
    }

    /**
     * 获取Properties配置工具类
     *
     * @return FastProperties
     */
    public FastProperties getProperties() {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(constant.getPropertiesName() + FastProperties.class.getName()),
                FastProperties.class).setFile(new FastResource(path.getClassRootPath(), constant.getPropertiesName()));
    }

    /**
     * 获取Properties配置工具类
     *
     * @param fileName 位于src目录下properties文件名
     * @return
     */
    public FastProperties getProperties(String fileName) {
        if (!fileName.endsWith(".properties")) {
            fileName = fileName + ".properties";
        }
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(fileName + FastProperties.class.getName()),
                FastProperties.class).setFile(new FastResource(path.getClassRootPath(), fileName));
    }

    /**
     * 获取Properties配置工具类
     *
     * @param propertiesFile 配置文件
     * @return
     */
    public FastProperties getProperties(File propertiesFile) {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(propertiesFile.getAbsolutePath() + FastProperties.class.getName()),
                FastProperties.class).setFile(new FastResource(propertiesFile));
    }

    /**
     * 获取Properties配置工具类
     *
     * @param propertiesFile 配置文件
     * @return
     */
    public FastProperties getProperties(FastResource propertiesFile) {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(propertiesFile.getURL() + FastProperties.class.getName()),
                FastProperties.class).setFile(propertiesFile);
    }

    /**
     * 获取yml配置工具类
     *
     * @return FastProperties
     */
    public FastYaml getYaml() {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(constant.getYamlName() + FastYaml.class.getName()),
                FastYaml.class).setFile(new FastResource(path.getClassRootPath(), constant.getYamlName()));
    }


    /**
     * 获取Yml配置工具类
     *
     * @param fileName 位于src目录下yml文件名
     * @return
     */
    public FastYaml getYaml(String fileName) {
        if (!fileName.endsWith(".yml")) {
            fileName = fileName + ".yml";
        }
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(fileName + FastYaml.class.getName()),
                FastYaml.class).setFile(new FastResource(path.getClassRootPath(), fileName));
    }

    /**
     * 获取Yml配置工具类
     *
     * @param ymlFile 配置文件
     * @return
     */
    public FastYaml getYaml(File ymlFile) {
        return FastChar.getOverrides().singleInstance(getSecurity().MD5_Encrypt(ymlFile.getAbsolutePath() + FastYaml.class.getName()),
                FastYaml.class).setFile(new FastResource(ymlFile));
    }


    /**
     * 获取类加载预检测器，可用于第三方jar包引用的检测
     *
     * @return FastFindClass
     */
    public FastFindClass getFindClass() {
        return findClass;
    }


    /**
     * 获取fast-database.xml操作工具类
     *
     * @return
     */
    public FastDatabaseXml getDatabaseXml() {
        return databaseXml;
    }


    /**
     * 获取系统模块加载器
     *
     * @return
     */
    public FastModules getModules() {
        return modules;
    }

    /**
     * 获取被扫描器扫描到的所有jar资源地址
     *
     * @return
     */
    public FastJarResources getJarResources() {
        return jarResources;
    }

    /**
     * 获取Web资源管理器
     *
     * @return
     */
    public FastWebResources getWebResources() {
        return webResources;
    }


    /**
     * 打印FastChar框架初始的核心信息
     */
    public void logFastCharInfo() {
        FastPrinter printer = new FastPrinter();

        List<String> infos = new ArrayList<>();
        Map<String, Object> printMap = new LinkedHashMap<>();

        printMap.put("JDK-Version", System.getProperty("java.version"));
        printMap.put("Server-Type", FastChar.getConstant().getServerType());
        printMap.put("Server-Embed", FastChar.getConstant().isEmbedServer());
        printMap.put("Servlet-Type", FastChar.getConstant().getServletType());
        printMap.put("Project-Name", FastChar.getConstant().getProjectName());
        if (FastStringUtils.isNotEmpty(FastChar.getConstant().getProjectHost())) {
            printMap.put("Project-Host", FastChar.getConstant().getProjectHost());
        }
        printMap.put("Class-Root-Path", FastChar.getPath().getClassRootPath());
        printMap.put("Web-Root-Path", FastChar.getPath().getWebRootPath());
        printMap.put("Project-Jar", FastChar.getPath().isProjectJar());
        if (FastChar.getPath().isProjectJar()) {
            printMap.put("Project-Jar-Path", FastChar.getPath().getProjectJarFilePath());
        }
        printMap.put("Proxy-Resource", FastChar.getConstant().isProxyResource());

        int maxKeyLength = 0;
        for (Map.Entry<String, Object> stringObjectEntry : printMap.entrySet()) {
            maxKeyLength = Math.max(stringObjectEntry.getKey().length(), maxKeyLength);
        }

        List<String> infoList = new ArrayList<>();
        for (Map.Entry<String, Object> stringObjectEntry : printMap.entrySet()) {
            infoList.add(FastStringUtils.rightPad(stringObjectEntry.getKey(), maxKeyLength, " ") + " : " + stringObjectEntry.getValue());
        }
        printer.info(this.getClass(), "\n" + FastStringUtils.join(infoList, "\n"));
    }


    /**
     * 刷新已被释放的类或对象
     */
    public synchronized void flush() {
        FastDispatcher.flush();
        getEntities().flush();
        getObservable().flush();
        getOverrides().flush();
        getInterceptors().flush();
        getWebs().flush();
    }

}
