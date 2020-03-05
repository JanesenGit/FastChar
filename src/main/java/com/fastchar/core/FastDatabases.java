package com.fastchar.core;

import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.exception.FastDatabaseInfoException;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库操作
 */
public final class FastDatabases {
    private static final ThreadLocal<String> LOCKER_DATABASE_NAME = new ThreadLocal<String>();

    private static final String MYSQL_REG = "jdbc:mysql://(.*):(\\d{2,4})/([^?&;=]*)";
    private static final String SQL_SERVER_REG = "jdbc:sqlserver://(.*):(\\d{2,4});databaseName=([^?&;=]*)";
    private static final String ORACLE_REG = "jdbc:oracle:thin:@[/]{0,2}(.*):(\\d{2,4})[:/]([^?&;=]*)";
    private List<FastDatabaseInfo> databaseInfos = new ArrayList<>();

    private List<String> modifyTicket;
    private boolean firstTicket;


    FastDatabases() {
    }

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


    public boolean hasDatabase() {
        return databaseInfos.size() > 0;
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




    /**
     * 刷新并同步数据库
     * @throws Exception 异常信息
     */
    public synchronized void flushDatabase() throws Exception {
        restoreTicket();
        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            IFastDatabaseOperate databaseOperate = databaseInfo.getOperate();
            if (databaseOperate == null) {
                continue;
            }

            //必要验证【重要】
            List<FastTableInfo<?>> tables = new ArrayList<>(databaseInfo.getTables());
            for (FastTableInfo<?> table : tables) {
                for (FastColumnInfo<?> column : table.getColumns()) {
                    if (column.isFromXml()) {
                        column.validate();
                    }
                }
                if (table.isFromXml()) {
                    table.validate();
                }
            }

            if (FastChar.getConstant().isSyncDatabaseXml()) {
                if (databaseInfo.getBoolean("enable", true) && databaseInfo.isFromXml()) {
                    databaseOperate.createDatabase(databaseInfo);

                    for (FastTableInfo<?> table : databaseInfo.getTables()) {
                        if (table.getBoolean("enable", true) && table.isFromXml()) {
                            if (!databaseOperate.checkTableExists(databaseInfo, table)) {
                                databaseOperate.createTable(databaseInfo, table);
                                removeTicket(databaseInfo.getName(), table.getName());
                            }
                            for (FastColumnInfo<?> column : table.getColumns()) {
                                if (column.getBoolean("enable", true) && table.isFromXml()) {
                                    if (databaseOperate.checkColumnExists(databaseInfo, table, column)) {
                                        if (checkIsModified(databaseInfo.getName(), table.getName(), column)) {
                                            databaseOperate.alterColumn(databaseInfo, table, column);
                                        }
                                        continue;
                                    }
                                    databaseOperate.addColumn(databaseInfo, table, column);
                                    checkIsModified(databaseInfo.getName(), table.getName(), column);
                                }
                            }
                        }
                    }
                }
            }
            databaseOperate.fetchDatabaseInfo(databaseInfo);
        }
        saveTicket();
        for (FastDatabaseInfo fastDatabaseInfo : FastChar.getDatabases().getAll()) {
            fastDatabaseInfo.tableToMap();
        }
        if (FastChar.getDatabases().getAll().size() > 0) {
            FastChar.getObservable().notifyObservers("onDatabaseFinish");
        }
    }


    private boolean checkIsModified(String databaseName, String tableName, FastColumnInfo<?> columnInfo) {
        if (modifyTicket == null) {
            return false;
        }
        String key = FastChar.getSecurity().MD5_Encrypt(databaseName)
                + "@" + FastChar.getSecurity().MD5_Encrypt(tableName)
                + "@" + FastChar.getSecurity().MD5_Encrypt(columnInfo.getName());

        String value = columnInfo.getModifyTick();
        boolean hasModified = true;
        boolean hasAdded = false;
        try {
            if (!firstTicket) {
                for (int i = 0; i < modifyTicket.size(); i++) {
                    String string = modifyTicket.get(i);
                    if (string.startsWith(key)) {
                        hasAdded = true;
                        if (string.equals(key + "@" + value)) {
                            hasModified = false;
                        } else {
                            hasModified = true;
                            modifyTicket.set(i, key + "@" + value);
                        }
                        break;
                    }
                }
            }

            if (!hasAdded) {
                modifyTicket.add(key + "@" + value);
            }
        } catch (Exception e) {
            return false;
        }
        return hasModified;
    }


    private void restoreTicket() {
        try {
            File file = new File(FastChar.getPath().getWebInfoPath(), ".fast_database");
            if (!file.exists()) {
                firstTicket = true;
                if (!file.createNewFile()) {
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLog().error(FastChar.getLocal().getInfo("File_Error9", file.getAbsolutePath()));
                    }
                }
            } else {
                firstTicket = false;
            }
            if (modifyTicket != null) {
                modifyTicket.clear();
            }
            modifyTicket = FastFileUtils.readLines(file);
        } catch (Exception ignored) {
        }
    }

    private void saveTicket() {
        try {
            if (modifyTicket != null) {
                if (firstTicket) {
                    modifyTicket.add(0, FastChar.getLocal().getInfo("Ticket_Error1"));
                }
                File file = new File(FastChar.getPath().getWebInfoPath(), ".fast_database");
                FastFileUtils.writeLines(file, modifyTicket);
                modifyTicket.clear();
                firstTicket = false;
            }
        } catch (Exception ignored) {
        }
    }


    private void removeTicket(String databaseName, String tableName) {
        String key = FastChar.getSecurity().MD5_Encrypt(databaseName)
                + "@" + FastChar.getSecurity().MD5_Encrypt(tableName);
        List<String> waitRemove = new ArrayList<>();
        for (String string : modifyTicket) {
            if (string.startsWith(key)) {
                waitRemove.add(string);
            }
        }
        modifyTicket.removeAll(waitRemove);
    }


}
