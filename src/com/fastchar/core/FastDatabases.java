package com.fastchar.core;

import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.exception.FastDatabaseInfoException;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FastDatabases {
    private static final ThreadLocal<String> LOCKER_DATABASE_NAME = new ThreadLocal<String>();

    private static final String MYSQL_REG = "jdbc:mysql://(.*):(\\d{2,4})/([^?&;=]*)";
    private static final String SQL_SERVER_REG = "jdbc:sqlserver://(.*):(\\d{2,4});databaseName=([^?&;=]*)";
    private static final String ORACLE_REG = "jdbc:oracle:thin:@[/]{0,2}(.*):(\\d{2,4})[:/]([^?&;=]*)";
    private List<FastDatabaseInfo> databaseInfos = new ArrayList<>();

    public synchronized FastDatabases add(String user, String password, String url) throws Exception {
        return add(user, password, url, false);
    }

    public synchronized FastDatabases add(String user, String password, String url, boolean cache) throws Exception {
        return add(new FastDatabaseInfo()
                .setUser(user)
                .setPassword(password)
                .setCache(cache)
                .setUrl(url)
                .setHost(getHost(url))
                .setType(getType(url))
                .setPort(getPort(url))
                .setName(getName(url)));
    }

    public synchronized FastDatabases add(FastDatabaseInfo databaseInfo) throws Exception {
        databaseInfo.fromProperty();
        for (FastDatabaseInfo info : databaseInfos) {
            if (info.getName().equals(databaseInfo.getName())) {
                throw new FastDatabaseException(FastChar.getLocal().getInfo("Db_Error1", databaseInfo.getName()));
            }
        }
        databaseInfos.add(databaseInfo);
        return this;
    }

    public List<FastDatabaseInfo> getAll() {
        return databaseInfos;
    }


    public void lock(String databaseName) {
        LOCKER_DATABASE_NAME.set(databaseName);
    }

    public void unlock() {
        LOCKER_DATABASE_NAME.remove();
    }

    public FastDatabaseInfo get() {
        return get(null);
    }

    public FastDatabaseInfo get(String databaseName) {
        String lockDatabaseName = LOCKER_DATABASE_NAME.get();
        if (FastStringUtils.isNotEmpty(lockDatabaseName)) {
            databaseName = lockDatabaseName;
        }
        if (databaseInfos.size() == 0) {
            throw new FastDatabaseInfoException(FastChar.getLocal().getInfo("Db_Error3"));
        }
        if (FastStringUtils.isEmpty(databaseName)) {
            return databaseInfos.get(0);
        }

        for (FastDatabaseInfo databaseInfo : databaseInfos) {
            if (databaseInfo.getName().equals(databaseName)) {
                return databaseInfo;
            }
        }
        throw new FastDatabaseInfoException(FastChar.getLocal().getInfo("Db_Error2", databaseName));
    }


    private String getHost(String url) {
        Matcher matcher = Pattern.compile(MYSQL_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = Pattern.compile(SQL_SERVER_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = Pattern.compile(ORACLE_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getPort(String url) {
        Matcher matcher = Pattern.compile(MYSQL_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        matcher = Pattern.compile(SQL_SERVER_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        matcher = Pattern.compile(ORACLE_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private String getName(String url) {
        Matcher matcher = Pattern.compile(MYSQL_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        matcher = Pattern.compile(SQL_SERVER_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        matcher = Pattern.compile(ORACLE_REG).matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }

    private String getType(String url) {
        Matcher matcher = Pattern.compile(MYSQL_REG).matcher(url);
        if (matcher.find()) {
            return "mysql";
        }
        matcher = Pattern.compile(SQL_SERVER_REG).matcher(url);
        if (matcher.find()) {
            return "sql_server";
        }
        matcher = Pattern.compile(ORACLE_REG).matcher(url);
        if (matcher.find()) {
            return "oracle";
        }
        return null;
    }


}
