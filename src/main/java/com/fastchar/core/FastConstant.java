package com.fastchar.core;


import java.util.*;

/**
 * 系统全局配置
 */
public final class FastConstant {
    /**
     * FastChar框架的版本
     */
    public static final String FastCharVersion = "1.2.3";

    /**
     * mysql数据库类型
     */
    public static final String MYSQL = "mysql";
    /**
     * sql_server数据库类型
     */
    public static final String SQL_SERVER = "sql_server";
    /**
     * oracle数据库类型
     */
    public static final String ORACLE = "oracle";


    FastConstant() {
    }

    private String projectName;//项目名称
    private String encoding = "utf-8";//编码格式
    private boolean encryptDatabaseXml;//是否加密数据库的配置xml
    private String encryptPassword = "FAST_CHAR";//加密的密码
    private boolean syncDatabaseXml = true;//是否同步xml到数据库中
    private boolean testEnvironment;//是否测试环境，一般在main方法中使用
    private boolean crossDomain = false;//是否允许跨域
    private Set<String> crossHeaders = new HashSet<>();//允许跨域的头部配置名
    private Set<String> crossAllowDomains = new HashSet<>();//允许跨域的域名配置

    private boolean debug = true;//调试模式
    private boolean ansi = false;
    private int maxResponseTime = 30;//最大响应时间 单位秒 如果超时这控制打印时会标红提醒
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";//日期格式化统一，默认为 yyyy-MM-dd HH:mm:ss
    private boolean logRoute = false;//打印路由地址
    private boolean logOverride = false;//打印覆盖器日志
    private boolean logHeaders = false;//打印请求的header信息
    private boolean logRemoteAddress = false;//打印远程请求的地址
    private boolean logInterceptorUseTotal = false;//打印拦截器的耗时时间
    private boolean logFilterResponseTime =false;//是否只打印 超时的路由日志
    private boolean logSql = true;//是否打印sql语句日志
    private boolean logExtract = false;//是否打印解压jar包的文件日志

    private boolean systemOutPrint = true;//是否允许system.out输出

    private String errorPage404;//404页面
    private String errorPage500;//500页面
    private String errorPage502;//502页面

    private boolean attachNameMD5;//自动保存的附件是否自动进行MD5加密处理名称
    private boolean attachNameSuffix = true;//保留文件的后缀名
    private String attachDirectory;//附件保存的路径  默认WebRoot/attachments
    private int attachMaxPostSize;//附件最大上传大小，单位 字节(b) 默认(30M) 30*1024*1024

    private String propertiesName = "config.properties";

    private boolean webStopped;

    private int sessionMaxInterval = 30 * 60;//session失效时间，单位秒 默认30分钟

    /**
     * 是否加密fast-database.xml相关的数据库配置文件
     *
     * @return 布尔值 默认：false
     */
    public boolean isEncryptDatabaseXml() {
        return encryptDatabaseXml;
    }

    /**
     * 配置是否自动加密fast-database.xml相关的数据库配置文件
     *
     * @param encryptDatabaseXml 是否加密
     * @return 当前对象
     */
    public FastConstant setEncryptDatabaseXml(boolean encryptDatabaseXml) {
        this.encryptDatabaseXml = encryptDatabaseXml;
        return this;
    }

    /**
     * 获得项目名称
     *
     * @return 项目名
     */
    public String getProjectName() {
        return projectName;
    }

    FastConstant setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * 获得系统的编码格式
     *
     * @return 系统编号，默认：utf-8
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 设置系统的编码格式
     *
     * @param encoding 编码 例如：utf-8
     * @return 当前对象
     */
    public FastConstant setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * 是否开启调试模式
     *
     * @return 布尔值 默认：true
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置系统调试模式，默认为true
     *
     * @param debug 是否开启
     * @return 当前对象
     */
    public FastConstant setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 获得请求最大响应的时间，单位：秒，如果超时那么控制打印时会黄色字体提醒
     *
     * @return 最大响应时间，默认30秒
     */
    public int getMaxResponseTime() {
        return maxResponseTime;
    }

    /**
     * 设置请求最大响应的时间，单位：秒
     *
     * @param maxResponseTime 最大时间
     * @return 当前对象
     */
    public FastConstant setMaxResponseTime(int maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
        return this;
    }

    /**
     * 获得404界面路径
     *
     * @return 网页路径，默认使用FastChar404界面
     */
    public String getErrorPage404() {
        return errorPage404;
    }

    /**
     * 设置系统404界面路径
     *
     * @param errorPage404 界面路径
     * @return 当前对象
     */
    public FastConstant setErrorPage404(String errorPage404) {
        this.errorPage404 = errorPage404;
        return this;
    }

    /**
     * 获得500界面路径
     *
     * @return 网页路径，默认使用FastChar400界面
     */
    public String getErrorPage500() {
        return errorPage500;
    }

    /**
     * 设置系统500界面路径
     *
     * @param errorPage500 界面路径
     * @return 当前对象
     */
    public FastConstant setErrorPage500(String errorPage500) {
        this.errorPage500 = errorPage500;
        return this;
    }

    /**
     * 获得502界面路径
     *
     * @return 网页路径，默认使用FastChar502界面
     */
    public String getErrorPage502() {
        return errorPage502;
    }

    /**
     * 设置系统502界面路径
     *
     * @param errorPage502 界面路径
     * @return 当前对象
     */
    public FastConstant setErrorPage502(String errorPage502) {
        this.errorPage502 = errorPage502;
        return this;
    }

    /**
     * 获得上传的附件保存在本地的目录
     *
     * @return 目录地址
     */
    public String getAttachDirectory() {
        return attachDirectory;
    }

    /**
     * 设置上传的附件保存在本地的目录
     *
     * @param attachDirectory 附件目录地址
     * @return 当前对象
     */
    public FastConstant setAttachDirectory(String attachDirectory) {
        this.attachDirectory = attachDirectory;
        return this;
    }

    /**
     * 获得上传附件的最大大小
     *
     * @return 最大附件大小，默认30M
     */
    public int getAttachMaxPostSize() {
        return attachMaxPostSize;
    }

    /**
     * 设置上传附件的最大大小
     *
     * @param attachMaxPostSize 附件最大大小，单位：字节(b)
     * @return 当前对象
     */
    public FastConstant setAttachMaxPostSize(int attachMaxPostSize) {
        this.attachMaxPostSize = attachMaxPostSize;
        return this;
    }

    /**
     * 是否用MD5加密附件名称
     *
     * @return 布尔值 默认：false
     */
    public boolean isAttachNameMD5() {
        return attachNameMD5;
    }

    /**
     * 设置是否用MD5加密附件名称
     *
     * @param attachNameMD5 是否加密
     * @return 当前对象
     */
    public FastConstant setAttachNameMD5(boolean attachNameMD5) {
        this.attachNameMD5 = attachNameMD5;
        return this;
    }

    /**
     * 获得系统默认的日期格式
     *
     * @return 日期格式 默认：yyyy-MM-dd HH:mm:ss
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * 设置系统默认的日期格式
     *
     * @param dateFormat 日期格式，例如：yyyy-MM-dd HH:mm
     * @return 当前对象
     */
    public FastConstant setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * 获取加密fast-database.xml的加密密码
     *
     * @return 加密密码，默认：FAST_CHAR
     */
    public String getEncryptPassword() {
        return encryptPassword;
    }

    /**
     * 设置加密fast-database.xml的加密密码
     *
     * @param encryptPassword 加密密码
     * @return 当前对象
     */
    public FastConstant setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
        return this;
    }


    /**
     * 在系统解析路由地址时，是否打印路由信息
     *
     * @return 布尔值
     */
    public boolean isLogRoute() {
        return logRoute;
    }

    /**
     * 设置在系统解析路由地址时，是否打印路由信息
     *
     * @param logRoute 布尔值
     * @return 当前对象
     */
    public FastConstant setLogRoute(boolean logRoute) {
        this.logRoute = logRoute;
        return this;
    }

    /**
     * 是否同步fast-database.xml数据库配置到数据库中
     *
     * @return 布尔值
     */
    public boolean isSyncDatabaseXml() {
        return syncDatabaseXml;
    }

    /**
     * 设置是否同步fast-database.xml数据库配置到数据库中
     *
     * @param syncDatabaseXml 布尔值
     * @return 当前对象
     */
    public FastConstant setSyncDatabaseXml(boolean syncDatabaseXml) {
        this.syncDatabaseXml = syncDatabaseXml;
        return this;
    }

    /**
     * 是否启用控制日志彩色打印
     *
     * @return 布尔值，默认：true
     */
    public boolean isAnsi() {
        return ansi;
    }

    /**
     * 设置启用控制日志彩色打印
     *
     * @param ansi 布尔值
     * @return 当前对象
     */
    public FastConstant setAnsi(boolean ansi) {
        this.ansi = ansi;
        return this;
    }


    /**
     * 是否允许跨域请求
     *
     * @return 布尔值，默认：false
     */
    public boolean isCrossDomain() {
        return crossDomain;
    }

    /**
     * 设置是否允许跨域请求
     *
     * @param crossDomain 布尔值
     * @return 当前对象
     */
    public FastConstant setCrossDomain(boolean crossDomain) {
        this.crossDomain = crossDomain;
        if (crossDomain) {
            addCrossAllowDomain("*");
        }
        return this;
    }

    /**
     * 是否打印类覆盖器里日志
     *
     * @return 布尔值
     */
    public boolean isLogOverride() {
        return logOverride;
    }

    /**
     * 设置是否打印类覆盖器里日志
     *
     * @param logOverride 布尔值
     * @return 当前对象
     */
    public FastConstant setLogOverride(boolean logOverride) {
        this.logOverride = logOverride;
        return this;
    }

    /**
     * 获取默认的properties文件名
     *
     * @return String 默认为：config.properties
     */
    public String getPropertiesName() {
        return propertiesName;
    }


    /**
     * 设置默认的properties文件名
     *
     * @param propertiesName 文件名
     * @return 当前对象
     */
    public FastConstant setPropertiesName(String propertiesName) {
        this.propertiesName = propertiesName;
        return this;
    }

    /**
     * 是否为测试环境，一般在main方法中使用
     *
     * @return 布尔值
     */
    public boolean isTestEnvironment() {
        return testEnvironment;
    }

    /**
     * 设置是否为测试环境
     *
     * @param testEnvironment 布尔值
     * @return 当前对象
     */
    public FastConstant setTestEnvironment(boolean testEnvironment) {
        this.testEnvironment = testEnvironment;
        return this;
    }


    /**
     * 判断当前系统环境是否为linux系统
     *
     * @return 布尔值
     */
    public boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("linux");
    }

    /**
     * 是否保留附件的后缀名
     *
     * @return 布尔值
     */
    public boolean isAttachNameSuffix() {
        return attachNameSuffix;
    }

    /**
     * 设置是否保留附件的后缀名
     *
     * @param attachNameSuffix 布尔值
     * @return 当前对象
     */
    public FastConstant setAttachNameSuffix(boolean attachNameSuffix) {
        this.attachNameSuffix = attachNameSuffix;
        return this;
    }


    /**
     * 获得当前系统环境的语言
     *
     * @return 语言编号
     */
    public String getLanguage() {
        Locale l = Locale.getDefault();
        if (l != null) {
            return l.toString();
        }
        return "en";
    }

    /**
     * web服务是否已停止
     *
     * @return 布尔值
     */
    public boolean isWebStopped() {
        return webStopped;
    }

    /**
     * 设置web服务器是否已停止
     *
     * @param webStopped 布尔值
     * @return 当前对象
     */
    public FastConstant setWebStopped(boolean webStopped) {
        this.webStopped = webStopped;
        return this;
    }

    /**
     * 是否打印请求的header日志
     *
     * @return 布尔值
     */
    public boolean isLogHeaders() {
        return logHeaders;
    }

    /**
     * 设置是否打印请求的header日志
     *
     * @param logHeaders 布尔值
     * @return 当前对象
     */
    public FastConstant setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
        return this;
    }

    /**
     * 是否打印拦截器的耗时时间
     *
     * @return 布尔值
     */
    public boolean isLogInterceptorUseTotal() {
        return logInterceptorUseTotal;
    }

    /**
     * 设置打印拦截器的耗时时间
     *
     * @param logInterceptorUseTotal 布尔值
     * @return 当前对象
     */
    public FastConstant setLogInterceptorUseTotal(boolean logInterceptorUseTotal) {
        this.logInterceptorUseTotal = logInterceptorUseTotal;
        return this;
    }

    /**
     * 添加允许跨域请求的头部请求
     *
     * @param headers 请求头信息
     * @return 当前对象
     */
    public FastConstant addCrossHeaders(String... headers) {
        crossHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    /**
     * 添加允许跨域的域名
     * @param domains 域名地址 支持匹配符*
     * @return 当前对象
     */
    public FastConstant addCrossAllowDomain(String... domains) {
        crossAllowDomains.addAll(Arrays.asList(domains));
        return this;
    }


    /**
     * 获得允许跨域的头部信息
     *
     * @return Set&lt;String&gt;
     */
    public Set<String> getCrossHeaders() {
        return crossHeaders;
    }

    /**
     * 获取允许跨域的域名地址
     * @return Set&lt;String&gt;
     */
    public Set<String> getCrossAllowDomains() {
        return crossAllowDomains;
    }

    /**
     * 是否只打印请求响应时间超过配置的maxUseTotalLog时间日志
     * @return 布尔值
     */
    public boolean isLogFilterResponseTime() {
        return logFilterResponseTime;
    }

    /**
     * 设置是否只打印请求响应时间超过配置的maxResponseTime时间日志
     * @param logFilterResponseTime 布尔值
     * @return 当前对象
     */
    public FastConstant setLogFilterResponseTime(boolean logFilterResponseTime) {
        this.logFilterResponseTime = logFilterResponseTime;
        return this;
    }

    /**
     * 是否打印sql语句
     * @return 布尔值
     */
    public boolean isLogSql() {
        return logSql;
    }

    /**
     * 设置是否打印sql语句
     * @param logSql 布尔值
     * @return 当前对象
     */
    public FastConstant setLogSql(boolean logSql) {
        this.logSql = logSql;
        return this;
    }

    /**
     * 获取session失效时间，默认 30分钟
     * @return 时间（秒）
     */
    public int getSessionMaxInterval() {
        return sessionMaxInterval;
    }

    /**
     * 设置session失效时间
     * @param sessionMaxInterval 时间（秒）
     * @return 当前对象
     */
    public FastConstant setSessionMaxInterval(int sessionMaxInterval) {
        this.sessionMaxInterval = sessionMaxInterval;
        return this;
    }

    /**
     * 是否打印解压jar包的文件日志
     * @return 布尔值
     */
    public boolean isLogExtract() {
        return logExtract;
    }

    /**
     * 设置是否打印解压jar包的文件日志
     * @param logExtract 布尔值
     * @return 当前对象
     */
    public FastConstant setLogExtract(boolean logExtract) {
        this.logExtract = logExtract;
        return this;
    }

    /**
     * 是否打印远程请求的地址
     * @return 布尔值
     */
    public boolean isLogRemoteAddress() {
        return logRemoteAddress;
    }

    /**
     * 设置是否打印远程请求接口的地址
     * @param logRemoteAddress 布尔值
     * @return 当前对象
     */
    public FastConstant setLogRemoteAddress(boolean logRemoteAddress) {
        this.logRemoteAddress = logRemoteAddress;
        return this;
    }

    /**
     * 是否允许系统使用System.out输出打印
     * @return 布尔值
     */
    public boolean isSystemOutPrint() {
        return systemOutPrint;
    }

    /**
     * 配置是否允许系统使用System.out输出打印
     * @param systemOutPrint 布尔值
     * @return 当前对象
     */
    public FastConstant setSystemOutPrint(boolean systemOutPrint) {
        this.systemOutPrint = systemOutPrint;
        return this;
    }
}
