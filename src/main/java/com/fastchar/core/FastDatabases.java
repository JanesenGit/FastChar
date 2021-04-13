package com.fastchar.core;

import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.enums.FastObservableEvent;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.exception.FastDatabaseInfoException;
import com.fastchar.interfaces.IFastDatabaseListener;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库操作
 *
 * @author 沈建（Janesen）
 */
public final class FastDatabases {
    private static final ThreadLocal<String> LOCKER_DATABASE_NAME = new ThreadLocal<String>();

    private static final String MYSQL_REG = "jdbc:mysql://(.*):(\\d{2,4})/([^?&;=]*)";
    private static final String SQL_SERVER_REG = "jdbc:sqlserver://(.*):(\\d{2,4});databaseName=([^?&;=]*)";
    private static final String ORACLE_REG = "jdbc:oracle:thin:@[/]{0,2}(.*):(\\d{2,4})[:/]([^?&;=]*)";
    private final List<FastDatabaseInfo> databaseInfos = new ArrayList<>();

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
        for (FastDatabaseInfo info : databaseInfos) {
            if (info.getName().equals(databaseInfo.getName())) {
                throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR1, databaseInfo.getName()));
            }
        }
        databaseInfo.fromProperty();
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
            throw new FastDatabaseInfoException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR3));
        }
        if (FastStringUtils.isEmpty(databaseName)) {
            return databaseInfos.get(0);
        }

        for (FastDatabaseInfo databaseInfo : databaseInfos) {
            if (databaseInfo.getName().equals(databaseName)) {
                return databaseInfo;
            }
        }
        throw new FastDatabaseInfoException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR2, databaseName));
    }


    public boolean hasDatabase() {
        return databaseInfos.size() > 0;
    }


    private String getHost(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
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
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
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
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
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
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
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
     *
     * @throws Exception 异常信息
     */
    public synchronized void flushDatabase() throws Exception {
        restoreTicket();

        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            databaseInfo.validate();

            IFastDatabaseOperate databaseOperate = databaseInfo.getOperate();
            if (databaseOperate == null) {
                FastChar.getLog().error(FastDatabases.class, FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO3, databaseInfo.getName()));
                continue;
            }

            if (FastChar.getConstant().isSyncDatabaseXml()) {
                if (databaseInfo.getBoolean("enable", true) && databaseInfo.isFromXml()) {
                    if (notifyListener(0, databaseInfo, null, null)) {
                        databaseOperate.createDatabase(databaseInfo);
                    }

                    for (FastTableInfo<?> table : databaseInfo.getTables()) {
                        if (table.getName().contains("*")) {
                            continue;
                        }
                        if (table.getBoolean("enable", true) && table.isFromXml()) {
                            if (!databaseOperate.checkTableExists(databaseInfo, table)) {
                                if (notifyListener(1, databaseInfo, table, null)) {
                                    databaseOperate.createTable(databaseInfo, table);
                                }
                                removeTicket(databaseInfo.getName(), table.getName());
                            }
                            for (FastColumnInfo<?> column : table.getColumns()) {
                                if (column.getBoolean("enable", true) && table.isFromXml()) {
                                    if (databaseOperate.checkColumnExists(databaseInfo, table, column)) {
                                        if (checkIsModified(databaseInfo.getName(), table.getName(), column)) {
                                            if (notifyListener(3, databaseInfo, table, column)) {
                                                databaseOperate.alterColumn(databaseInfo, table, column);
                                            }
                                        }
                                        continue;
                                    }
                                    if (notifyListener(2, databaseInfo, table, column)) {
                                        databaseOperate.addColumn(databaseInfo, table, column);
                                    }
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
            FastChar.getObservable().notifyObservers(FastObservableEvent.onDatabaseFinish.name());
        }
    }


    private boolean notifyListener(int type, FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) {
        try {
            List<IFastDatabaseListener> iFastDatabaseListeners = FastChar.getOverrides().singleInstances(false, IFastDatabaseListener.class);
            for (IFastDatabaseListener iFastDatabaseListener : iFastDatabaseListeners) {
                if (iFastDatabaseListener == null) {
                    continue;
                }
                if (type == 0) {//创建数据库
                    if (!iFastDatabaseListener.onCreateDatabase(databaseInfo)) {
                        return false;
                    }
                } else if (type == 1) {//创建表格
                    if (!iFastDatabaseListener.onCreateTable(databaseInfo, tableInfo)) {
                        return false;
                    }
                } else if (type == 2) {//添加表格列
                    if (!iFastDatabaseListener.onAddTableColumn(databaseInfo, tableInfo, columnInfo)) {
                        return false;
                    }
                } else if (type == 3) {//修改表格列
                    if (!iFastDatabaseListener.onAlterTableColumn(databaseInfo, tableInfo, columnInfo)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
                        FastChar.getLog().error(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR9, file.getAbsolutePath()));
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
                    modifyTicket.add(0, FastChar.getLocal().getInfo(FastCharLocal.TICKET_ERROR1));
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
