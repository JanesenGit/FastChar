package com.fastchar.core;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

public final class FastConstant {

    //数据类型
    public static final String MYSQL = "mysql";
    public static final String SQL_SERVER = "sql_server";
    public static final String ORACLE = "oracle";

    public static final String FastCharVersion = "1.0.0";

    private String projectName;//项目名称
    private String encoding = "utf-8";//编码格式
    private boolean encryptDatabaseXml;//是否加密数据库的配置xml
    private String encryptPassword = "FAST_CHAR";//加密的密码
    private boolean readDatabaseXml;//是否已读取database.xml
    private boolean syncDatabaseXml=true;//是否同步xml到数据库中

    private boolean debug = true;//调试模式
    private int maxUseTotalLog = 30;//最大响应时间 单位秒 如果超时这控制打印时会标红提醒
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";//日期格式化统一，默认为 yyyy-MM-dd HH:mm:ss
    private boolean logRoute=false;//打印路由地址


    private String errorPage404;//404页面
    private String errorPage500;//500页面
    private String errorPage502;//502页面

    private boolean attachNameMD5;//自动保存的附件是否自动进行MD5加密处理名称
    private String attachDirectory;//附件保存的路径  默认WebRoot/attachments
    private int attachMaxPostSize;//附件最大上传大小，单位 字节(b) 默认(30M) 30*1024*1024



    public boolean isEncryptDatabaseXml() {
        return encryptDatabaseXml;
    }

    public FastConstant setEncryptDatabaseXml(boolean encryptDatabaseXml) {
        this.encryptDatabaseXml = encryptDatabaseXml;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

     FastConstant setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public FastConstant setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public FastConstant setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public int getMaxUseTotalLog() {
        return maxUseTotalLog;
    }

    public FastConstant setMaxUseTotalLog(int maxUseTotalLog) {
        this.maxUseTotalLog = maxUseTotalLog;
        return this;
    }

    public String getErrorPage404() {
        return errorPage404;
    }

    public FastConstant setErrorPage404(String errorPage404) {
        this.errorPage404 = errorPage404;
        return this;
    }

    public String getErrorPage500() {
        return errorPage500;
    }

    public FastConstant setErrorPage500(String errorPage500) {
        this.errorPage500 = errorPage500;
        return this;
    }

    public String getErrorPage502() {
        return errorPage502;
    }

    public FastConstant setErrorPage502(String errorPage502) {
        this.errorPage502 = errorPage502;
        return this;
    }

    public String getAttachDirectory() {
        return attachDirectory;
    }

    public FastConstant setAttachDirectory(String attachDirectory) {
        this.attachDirectory = attachDirectory;
        return this;
    }

    public int getAttachMaxPostSize() {
        return attachMaxPostSize;
    }

    public FastConstant setAttachMaxPostSize(int attachMaxPostSize) {
        this.attachMaxPostSize = attachMaxPostSize;
        return this;
    }

    public boolean isAttachNameMD5() {
        return attachNameMD5;
    }

    public FastConstant setAttachNameMD5(boolean attachNameMD5) {
        this.attachNameMD5 = attachNameMD5;
        return this;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public FastConstant setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public FastConstant setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
        return this;
    }

    public boolean isReadDatabaseXml() {
        return readDatabaseXml;
    }

    public FastConstant setReadDatabaseXml(boolean readDatabaseXml) {
        this.readDatabaseXml = readDatabaseXml;
        return this;
    }

    public boolean isLogRoute() {
        return logRoute;
    }

    public FastConstant setLogRoute(boolean logRoute) {
        this.logRoute = logRoute;
        return this;
    }

    public boolean isSyncDatabaseXml() {
        return syncDatabaseXml;
    }

    public FastConstant setSyncDatabaseXml(boolean syncDatabaseXml) {
        this.syncDatabaseXml = syncDatabaseXml;
        return this;
    }
}
