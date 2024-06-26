package com.fastchar.core;


import com.fastchar.enums.FastServerType;
import com.fastchar.enums.FastServletType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 系统全局配置
 *
 * @author 沈建（Janesen）
 */
public class FastConstant {
    /**
     * FastChar框架的版本
     */
    public static final String FAST_CHAR_VERSION = "2.2.0";

    /**
     * 数据库xml配置文件的前缀
     */
    public static String FAST_DATA_BASE_FILE_PREFIX = "fastchar-database";

    /**
     * 默认数据xml配置文件的前缀
     */
    public static String FAST_DATA_FILE_PREFIX = "fastchar-data";

    FastConstant() {
    }

    FastServletType servletType = FastServletType.None;//启动项目的servlet类型
    FastServerType serverType = FastServerType.None;//启动项目的容器类型
    private boolean debug = true;//调试模式
    private boolean markers = true;//是否在响应头中追加Power-By标记头

    private boolean proxyResource = false;//是否代理静态资源的获取和访问，如果配置为true，将跳过插件包的里解压功能。
    private boolean embedServer = false;//是否是内嵌运行的server服务器
    private String projectHost;//项目主地址
    private String projectName;//项目名称
    private long beginInitTime;//项目开始初始化时间戳
    private long endInitTime;//项目结束初始化时间戳
    private String charset = "utf-8";//编码格式
    private String encryptPassword = "FAST_CHAR";//加密的密码
    private boolean syncDatabaseXml = true;//是否同步xml到数据库中
    private boolean testEnvironment;//是否测试环境，一般在main方法中使用
    private boolean crossDomain = false;//是否允许跨域
    private final Set<String> crossHeaders = new HashSet<>();//允许跨域的头部配置名
    private final Set<String> crossAllowDomains = new HashSet<>();//允许跨域的域名配置

    private boolean ansi = true; //是否支持控制ansi字体颜色设置
    private int maxResponseTime = 30;//最大响应时间 单位秒 如果超时这控制打印时会标红提醒
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";//日期格式化统一，默认为 yyyy-MM-dd HH:mm:ss


    private boolean jdbcParseToTimestamp = true;//是否将jdbc查询的日期默认转为 Timestamp

    private boolean log = true;//允许打印日志
    private boolean logRoute = false;//打印路由地址
    private boolean logOverride = false;//打印覆盖器日志
    private boolean logHeaders = false;//打印请求的header信息
    private boolean logRemoteAddress = false;//打印远程请求的地址
    private boolean logInterceptorUseTotal = false;//打印拦截器的耗时时间
    private boolean logFilterResponseTime = false;//是否只打印 超时的路由日志
    private boolean logSql = true;//是否打印sql语句日志
    private boolean logExtract = false;//是否打印解压jar包的文件日志
    private boolean logExtractNewFile = false;//是否打印新版本的文件消息
    private boolean logActionResolver = false;//是否打印解析FastAction的类名

    private boolean systemOutPrint = true;//是否允许system.out输出

    private String errorPage404;//404页面
    private String errorPage500;//500页面
    private String errorPage502;//502页面

    private boolean attachNameMD5;//自动保存的附件是否自动进行MD5加密处理名称
    private boolean attachNameSuffix = true;//保留文件的后缀名
    private String attachDirectory;//附件保存的路径  默认WebRoot/attachments
    private int attachMaxPostSize = 100 * 1024 * 1024;//附件最大上传大小，单位 字节(b) 默认(100M) 100*1024*1024

    private String propertiesName = "config.properties";//默认properties文件名

    private String yamlName = "config.yml";//默认yml文件名

    boolean webServer;//是否是web项目
    boolean webStarted;//web服务器是否已启动
    boolean webStopped;//web服务器是否已停止

    private boolean decodeUploadFileName = true;//是否使用URLDecoder解码上传文件的名称
    private String decodeUploadFileNameEncoding = charset;//URLDecoder解码时的编码

    private int sessionMaxInterval = 30 * 60;//session失效时间，单位秒 默认30分钟

    public FastServletType getServletType() {
        return servletType;
    }


    /**
     * 获得项目名称
     *
     * @return 项目名
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * 设置项目名称
     *
     * @param projectName 项目名
     * @return 当前对象
     */
    FastConstant setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * 获得系统的编码格式
     *
     * @return 系统编号，默认：utf-8
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置系统的编码格式
     *
     * @param charset 编码 例如：utf-8
     * @return 当前对象
     */
    public FastConstant setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 是否开启调试模式
     *
     * @return boolean 默认：true
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
     * @return 最大附件大小，默认：100M
     */
    public int getAttachMaxPostSize() {
        return attachMaxPostSize;
    }

    /**
     * 设置上传附件的最大大小，默认：100M
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
     * @return boolean 默认：false
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
     * @return boolean
     */
    public boolean isLogRoute() {
        return logRoute;
    }

    /**
     * 设置在系统解析路由地址时，是否打印路由信息
     *
     * @param logRoute boolean
     * @return 当前对象
     */
    public FastConstant setLogRoute(boolean logRoute) {
        this.logRoute = logRoute;
        return this;
    }

    /**
     * 是否同步fast-database.xml数据库配置到数据库中
     *
     * @return boolean
     */
    public boolean isSyncDatabaseXml() {
        return syncDatabaseXml;
    }

    /**
     * 设置是否同步fast-database.xml数据库配置到数据库中
     *
     * @param syncDatabaseXml boolean
     * @return 当前对象
     */
    public FastConstant setSyncDatabaseXml(boolean syncDatabaseXml) {
        this.syncDatabaseXml = syncDatabaseXml;
        return this;
    }

    /**
     * 是否启用控制日志彩色打印
     *
     * @return boolean，默认：true
     */
    public boolean isAnsi() {
        return ansi;
    }

    /**
     * 设置启用控制日志彩色打印
     *
     * @param ansi boolean
     * @return 当前对象
     */
    public FastConstant setAnsi(boolean ansi) {
        this.ansi = ansi;
        return this;
    }


    /**
     * 是否允许跨域请求
     *
     * @return boolean，默认：false
     */
    public boolean isCrossDomain() {
        return crossDomain;
    }

    /**
     * 设置是否允许跨域请求
     *
     * @param crossDomain boolean
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
     * @return boolean
     */
    public boolean isLogOverride() {
        return logOverride;
    }

    /**
     * 设置是否打印类覆盖器里日志
     *
     * @param logOverride boolean
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
     * @return boolean
     */
    public boolean isTestEnvironment() {
        return testEnvironment;
    }

    /**
     * 设置是否为测试环境
     *
     * @param testEnvironment boolean
     * @return 当前对象
     */
    public FastConstant setTestEnvironment(boolean testEnvironment) {
        this.testEnvironment = testEnvironment;
        return this;
    }


    /**
     * 判断当前系统环境是否为linux系统
     *
     * @return boolean
     */
    public boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("linux");
    }

    /**
     * 是否保留附件的后缀名
     *
     * @return boolean
     */
    public boolean isAttachNameSuffix() {
        return attachNameSuffix;
    }

    /**
     * 设置是否保留附件的后缀名
     *
     * @param attachNameSuffix boolean
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
     * @return boolean
     */
    public boolean isWebStopped() {
        return webStopped;
    }

    /**
     * 是否打印请求的header日志
     *
     * @return boolean
     */
    public boolean isLogHeaders() {
        return logHeaders;
    }

    /**
     * 设置是否打印请求的header日志
     *
     * @param logHeaders boolean
     * @return 当前对象
     */
    public FastConstant setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
        return this;
    }

    /**
     * 是否打印拦截器的耗时时间
     *
     * @return boolean
     */
    public boolean isLogInterceptorUseTotal() {
        return logInterceptorUseTotal;
    }

    /**
     * 设置打印拦截器的耗时时间
     *
     * @param logInterceptorUseTotal boolean
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
     *
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
     *
     * @return Set&lt;String&gt;
     */
    public Set<String> getCrossAllowDomains() {
        return crossAllowDomains;
    }

    /**
     * 是否只打印请求响应时间超过配置的maxUseTotalLog时间日志
     *
     * @return boolean
     */
    public boolean isLogFilterResponseTime() {
        return logFilterResponseTime;
    }

    /**
     * 设置是否只打印请求响应时间超过配置的maxResponseTime时间日志
     *
     * @param logFilterResponseTime boolean
     * @return 当前对象
     */
    public FastConstant setLogFilterResponseTime(boolean logFilterResponseTime) {
        this.logFilterResponseTime = logFilterResponseTime;
        return this;
    }

    /**
     * 是否打印sql语句
     *
     * @return boolean
     */
    public boolean isLogSql() {
        return logSql;
    }

    /**
     * 设置是否打印sql语句
     *
     * @param logSql boolean
     * @return 当前对象
     */
    public FastConstant setLogSql(boolean logSql) {
        this.logSql = logSql;
        return this;
    }

    /**
     * 获取session失效时间，默认 30分钟
     *
     * @return 时间（秒）
     */
    public int getSessionMaxInterval() {
        return sessionMaxInterval;
    }

    /**
     * 设置session失效时间
     *
     * @param sessionMaxInterval 时间（秒）
     * @return 当前对象
     */
    public FastConstant setSessionMaxInterval(int sessionMaxInterval) {
        this.sessionMaxInterval = sessionMaxInterval;
        return this;
    }

    /**
     * 是否打印解压jar包的文件日志
     *
     * @return boolean
     */
    public boolean isLogExtract() {
        return logExtract;
    }

    /**
     * 设置是否打印解压jar包的文件日志
     *
     * @param logExtract boolean
     * @return 当前对象
     */
    public FastConstant setLogExtract(boolean logExtract) {
        this.logExtract = logExtract;
        return this;
    }

    /**
     * 是否打印远程请求的地址
     *
     * @return boolean
     */
    public boolean isLogRemoteAddress() {
        return logRemoteAddress;
    }

    /**
     * 设置是否打印远程请求接口的地址
     *
     * @param logRemoteAddress boolean
     * @return 当前对象
     */
    public FastConstant setLogRemoteAddress(boolean logRemoteAddress) {
        this.logRemoteAddress = logRemoteAddress;
        return this;
    }

    /**
     * 是否允许系统使用System.out输出打印
     *
     * @return boolean
     */
    public boolean isSystemOutPrint() {
        return systemOutPrint;
    }

    /**
     * 配置是否允许系统使用System.out输出打印
     *
     * @param systemOutPrint boolean
     * @return 当前对象
     */
    public FastConstant setSystemOutPrint(boolean systemOutPrint) {
        this.systemOutPrint = systemOutPrint;
        return this;
    }

    /**
     * 获取项目的Web服务器是否已启动
     *
     * @return boolean
     */
    public boolean isWebStarted() {
        return webStarted;
    }


    /**
     * 是否打印解析FastAction的类名
     *
     * @return boolean
     */
    public boolean isLogActionResolver() {
        return logActionResolver;
    }

    /**
     * 配置是否打印解析FastAction的类名
     *
     * @param logActionResolver boolean
     * @return 当前对象
     */
    public FastConstant setLogActionResolver(boolean logActionResolver) {
        this.logActionResolver = logActionResolver;
        return this;
    }


    /**
     * 是否使用URLDecoder解码上传文件的名称 默认：true
     *
     * @return boolean
     */
    public boolean isDecodeUploadFileName() {
        return decodeUploadFileName;
    }

    /**
     * 是否使用URLDecoder解码上传文件的名称
     *
     * @param decodeUploadFileName boolean
     * @return 当前对象
     */
    public FastConstant setDecodeUploadFileName(boolean decodeUploadFileName) {
        this.decodeUploadFileName = decodeUploadFileName;
        return this;
    }

    /**
     * URLDecoder解码时的编码
     *
     * @return 字符串
     */
    public String getDecodeUploadFileNameEncoding() {
        return decodeUploadFileNameEncoding;
    }

    /**
     * 设置URLDecoder解码时的编码
     *
     * @param decodeUploadFileNameEncoding 字符串
     * @return 当前对象
     */
    public FastConstant setDecodeUploadFileNameEncoding(String decodeUploadFileNameEncoding) {
        this.decodeUploadFileNameEncoding = decodeUploadFileNameEncoding;
        return this;
    }

    /**
     * 是否打印插件中新版本的文件消息
     *
     * @return boolean
     */
    public boolean isLogExtractNewFile() {
        return logExtractNewFile;
    }

    /**
     * 是否打印插件中新版本的文件消息
     *
     * @param logExtractNewFile boolean
     * @return 当前对象
     */
    public FastConstant setLogExtractNewFile(boolean logExtractNewFile) {
        this.logExtractNewFile = logExtractNewFile;
        return this;
    }

    /**
     * 是否允许打印日志
     * @return boolean
     */
    public boolean isLog() {
        return log;
    }

    /**
     * 设置是否打印日志
     * @param log boolean
     * @return 当前对象
     */
    public FastConstant setLog(boolean log) {
        this.log = log;
        return this;
    }


    /**
     * 是否将jdbc查询的日期数据默认转为 Timestamp，默认：true
     * @return boolean
     */
    public boolean isJdbcParseToTimestamp() {
        return jdbcParseToTimestamp;
    }

    /**
     * 设置是否将jdbc查询的日期数据默认转为 Timestamp
     * @param jdbcParseToTimestamp boolean
     * @return 当前对象
     */
    public FastConstant setJdbcParseToTimestamp(boolean jdbcParseToTimestamp) {
        this.jdbcParseToTimestamp = jdbcParseToTimestamp;
        return this;
    }


    /**
     * 获取项目开始初始化时间戳
     * @return 时间戳
     */
    public long getBeginInitTime() {
        return beginInitTime;
    }

    /**
     * 设置项目开始初始化时间戳
     *
     * @param beginInitTime 时间戳
     * @return 当前对象
     */
    public FastConstant setBeginInitTime(long beginInitTime) {
        this.beginInitTime = beginInitTime;
        return this;
    }

    /**
     * 获取项目结束初始化时间戳
     * @return 时间戳
     */
    public long getEndInitTime() {
        return endInitTime;
    }

    /**
     * 设置项目初始化结束时间戳
     *
     * @param endInitTime 时间戳
     * @return 当前对象
     */
    public FastConstant setEndInitTime(long endInitTime) {
        this.endInitTime = endInitTime;
        return this;
    }

    public String getYamlName() {
        return yamlName;
    }

    public FastConstant setYamlName(String yamlName) {
        this.yamlName = yamlName;
        return this;
    }

    public boolean isWebServer() {
        return webServer;
    }

    public boolean isMarkers() {
        return markers;
    }

    public FastConstant setMarkers(boolean markers) {
        this.markers = markers;
        return this;
    }


    public String getProjectHost() {
        return projectHost;
    }

    public FastConstant setProjectHost(String projectHost) {
        this.projectHost = projectHost;
        return this;
    }

    /**
     * 是否是内嵌运行的server服务器
     * @return boolean
     */
    public boolean isEmbedServer() {
        return embedServer;
    }

    public FastConstant setEmbedServer(boolean embedServer) {
        this.embedServer = embedServer;
        return this;
    }

    public FastServerType getServerType() {
        return serverType;
    }


    public boolean isProxyResource() {
        return proxyResource;
    }

    public FastConstant setProxyResource(boolean proxyResource) {
        this.proxyResource = proxyResource;
        return this;
    }

    @Override
    public String toString() {
        return "FastConstant{" +
                "projectName='" + projectName + '\'' +
                ", encoding='" + charset + '\'' +
                ", encryptPassword='" + encryptPassword + '\'' +
                ", syncDatabaseXml=" + syncDatabaseXml +
                ", testEnvironment=" + testEnvironment +
                ", crossDomain=" + crossDomain +
                ", crossHeaders=" + crossHeaders +
                ", crossAllowDomains=" + crossAllowDomains +
                ", debug=" + debug +
                ", ansi=" + ansi +
                ", maxResponseTime=" + maxResponseTime +
                ", dateFormat='" + dateFormat + '\'' +
                ", logRoute=" + logRoute +
                ", logOverride=" + logOverride +
                ", logHeaders=" + logHeaders +
                ", logRemoteAddress=" + logRemoteAddress +
                ", logInterceptorUseTotal=" + logInterceptorUseTotal +
                ", logFilterResponseTime=" + logFilterResponseTime +
                ", logSql=" + logSql +
                ", logExtract=" + logExtract +
                ", logExtractNewFile=" + logExtractNewFile +
                ", logActionResolver=" + logActionResolver +
                ", systemOutPrint=" + systemOutPrint +
                ", errorPage404='" + errorPage404 + '\'' +
                ", errorPage500='" + errorPage500 + '\'' +
                ", errorPage502='" + errorPage502 + '\'' +
                ", attachNameMD5=" + attachNameMD5 +
                ", attachNameSuffix=" + attachNameSuffix +
                ", attachDirectory='" + attachDirectory + '\'' +
                ", attachMaxPostSize=" + attachMaxPostSize +
                ", propertiesName='" + propertiesName + '\'' +
                ", webServer=" + webServer +
                ", webStarted=" + webStarted +
                ", webStopped=" + webStopped +
                ", decodeUploadFileName=" + decodeUploadFileName +
                ", decodeUploadFileNameEncoding='" + decodeUploadFileNameEncoding + '\'' +
                ", sessionMaxInterval=" + sessionMaxInterval +
                '}';
    }
}
