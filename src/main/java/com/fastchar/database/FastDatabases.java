package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastTicket;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.enums.FastDatabaseType;
import com.fastchar.enums.FastObservableEvent;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.exception.FastDatabaseInfoException;
import com.fastchar.interfaces.IFastDatabaseListener;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库操作
 *
 * @author 沈建（Janesen）
 */
public final class FastDatabases {
    private static final ThreadLocal<String> LOCKER_DATABASE = new ThreadLocal<>();


    private static final ConcurrentHashMap<String, String> LOCKER_PACKAGE_DATABASE_NAME = new ConcurrentHashMap<>(16);

    private static final Pattern MYSQL_PATTERN = Pattern.compile("jdbc:mysql://(.*):(\\d{2,4})/([^?&;=]*)");
    private static final Pattern SQL_SERVER_PATTERN = Pattern.compile("jdbc:sqlserver://(.*):(\\d{2,4});databaseName=([^?&;=]*)");
    private static final Pattern ORACLE_PATTERN = Pattern.compile("jdbc:oracle:thin:@[/]{0,2}(.*):(\\d{2,4})[:/]([^?&;=]*)");
    private static final String TICKET_FILE_NAME = ".fastchar_database";



    private final Map<String, FastDatabaseInfo> databaseInfos = new LinkedHashMap<>(16);
    private String firstDatabase;
    private final FastTicket fastTicket = new FastTicket(TICKET_FILE_NAME);

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
        if (databaseInfos.containsKey(databaseInfo.getCode())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR1, databaseInfo.getCode()));
        }
        databaseInfos.put(databaseInfo.getCode(), databaseInfo);
        if (FastStringUtils.isEmpty(firstDatabase)) {
            firstDatabase = databaseInfo.getCode();
        }
        return this;
    }

    public List<FastDatabaseInfo> getAll() {
        return new ArrayList<>(databaseInfos.values());
    }

    public String getLockDatabase() {
        return LOCKER_DATABASE.get();
    }

    public String getLockDatabase(String packageName) {
        return LOCKER_PACKAGE_DATABASE_NAME.get(packageName);
    }

    public void lock(String database) {
        LOCKER_DATABASE.set(database);
    }

    public void unlock() {
        LOCKER_DATABASE.remove();
    }


    public void lock(String database, String packageName) {
        LOCKER_PACKAGE_DATABASE_NAME.put(packageName, database);
    }

    public void unlock(String packageName) {
        LOCKER_PACKAGE_DATABASE_NAME.remove(packageName);
    }


    public FastDatabaseInfo get() {
        return get(null);
    }

    /**
     * 获取数据信息，将同步检查被锁定的数据库
     *
     * @param database 数据库名称
     * @return FastDatabaseInfo
     */
    public FastDatabaseInfo get(String database) {
        String lockDatabaseName = LOCKER_DATABASE.get();
        if (FastStringUtils.isNotEmpty(lockDatabaseName)) {
            database = lockDatabaseName;
        }
        if (databaseInfos.size() == 0) {
            throw new FastDatabaseInfoException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR3));
        }
        if (FastStringUtils.isEmpty(database)) {
            return databaseInfos.get(firstDatabase);
        }

        FastDatabaseInfo fastDatabaseInfo = databaseInfos.get(database);
        if (fastDatabaseInfo != null) {
            return fastDatabaseInfo;
        }
        throw new FastDatabaseInfoException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR2, database));
    }

    public boolean hasDatabase() {
        return databaseInfos.size() > 0;
    }

    public boolean existDataBase(String database) {
        return databaseInfos.containsKey(database);
    }

    private String getHost(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        Matcher matcher = MYSQL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = SQL_SERVER_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = ORACLE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getPort(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        Matcher matcher = MYSQL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        matcher = SQL_SERVER_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        matcher = ORACLE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private String getName(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        Matcher matcher = MYSQL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        matcher = SQL_SERVER_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        matcher = ORACLE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }

    private String getType(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        Matcher matcher = MYSQL_PATTERN.matcher(url);
        if (matcher.find()) {
            return FastDatabaseType.MYSQL.name().toLowerCase();
        }
        matcher = SQL_SERVER_PATTERN.matcher(url);
        if (matcher.find()) {
            return FastDatabaseType.SQL_SERVER.name().toLowerCase();
        }
        matcher = ORACLE_PATTERN.matcher(url);
        if (matcher.find()) {
            return FastDatabaseType.ORACLE.name().toLowerCase();
        }
        return null;
    }


    /**
     * 刷新并同步数据库
     *
     * @throws Exception 异常信息
     */
    public synchronized void flushDatabase() throws Exception {
        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            databaseInfo.validate();

            IFastDatabaseOperate databaseOperate = databaseInfo.getOperate();
            if (databaseOperate == null) {
                FastChar.getLog().error(FastDatabases.class, FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO3, databaseInfo.toSimpleInfo()));
                continue;
            }

            if (FastChar.getConstant().isSyncDatabaseXml()) {
                if (databaseInfo.isEnable() && databaseInfo.isFromXml()
                        && databaseInfo.isSyncDatabaseXml()) {

                    if (notifyListener(0, false, databaseInfo, null, null)) {
                        databaseOperate.createDatabase(databaseInfo);
                        notifyListener(0, true, databaseInfo, null, null);
                    }

                    for (FastTableInfo<?> table : databaseInfo.getTables()) {
                        if (table.getName().contains("*")) {
                            continue;
                        }
                        String columnKeyPrefix = FastChar.getSecurity().MD5_Encrypt(databaseInfo.getCode()) + "@" + FastChar.getSecurity().MD5_Encrypt(table.getName());
                        if (table.isEnable() && table.isFromXml()) {
                            boolean doCreateTable = false;
                            if (!databaseOperate.checkTableExists(databaseInfo, table)) {
                                if (notifyListener(1, false, databaseInfo, table, null)) {
                                    databaseOperate.createTable(databaseInfo, table);
                                    doCreateTable = true;
                                    notifyListener(1, true, databaseInfo, table, null);
                                }
                                fastTicket.removeTicketWithPrefix(columnKeyPrefix);

                            }
                            Collection<FastColumnInfo<?>> columns = table.getColumns();
                            for (FastColumnInfo<?> column : columns) {
                                if (column.isEnable() && table.isFromXml()) {
                                    boolean doAddColumn = false;
                                    if (!databaseOperate.checkColumnExists(databaseInfo, table, column)) {
                                        if (notifyListener(2, false, databaseInfo, table, column)) {
                                            databaseOperate.addColumn(databaseInfo, table, column);
                                            doAddColumn = true;
                                            notifyListener(2, true, databaseInfo, table, column);
                                        }
                                    }
                                    String columnKey = columnKeyPrefix + "@" + FastChar.getSecurity().MD5_Encrypt(column.getName());
                                    if (fastTicket.pushTicket(columnKey, column.getModifyTicket()) && !doAddColumn && !doCreateTable) {
                                        if (notifyListener(3, false, databaseInfo, table, column)) {
                                            databaseOperate.alterColumn(databaseInfo, table, column);
                                            notifyListener(3, true, databaseInfo, table, column);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (databaseInfo.isFetchDatabaseInfo()) {
                databaseOperate.fetchDatabaseInfo(databaseInfo);
            }
        }
        fastTicket.saveTicket();

        if (FastChar.getDatabases().getAll().size() > 0) {
            FastChar.getObservable().notifyObservers(FastObservableEvent.onDatabaseFinish.name());
        }
    }


    private boolean notifyListener(int type, boolean after, FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) {
        try {
            List<IFastDatabaseListener> iFastDatabaseListeners = FastChar.getOverrides().singleInstances(false, IFastDatabaseListener.class);
            for (IFastDatabaseListener iFastDatabaseListener : iFastDatabaseListeners) {
                if (iFastDatabaseListener == null) {
                    continue;
                }
                if (type == 0) {//创建数据库
                    if (after) {
                        iFastDatabaseListener.onAfterCreateDatabase(databaseInfo);
                    } else {
                        Boolean onBeforeCreateDatabase = iFastDatabaseListener.onBeforeCreateDatabase(databaseInfo);
                        if (onBeforeCreateDatabase == null) {
                            return true;
                        }
                        if (!onBeforeCreateDatabase) {
                            return false;
                        }
                    }
                } else if (type == 1) {//创建表格
                    if (after) {
                        iFastDatabaseListener.onAfterCreateTable(databaseInfo, tableInfo);
                    } else {
                        Boolean onBeforeCreateTable = iFastDatabaseListener.onBeforeCreateTable(databaseInfo, tableInfo);
                        if (onBeforeCreateTable == null) {
                            return true;
                        }
                        if (!onBeforeCreateTable) {
                            return false;
                        }
                    }
                } else if (type == 2) {//添加表格列
                    if (after) {
                        iFastDatabaseListener.onAfterAddTableColumn(databaseInfo, tableInfo, columnInfo);
                    } else {
                        Boolean onBeforeAddTableColumn = iFastDatabaseListener.onBeforeAddTableColumn(databaseInfo, tableInfo, columnInfo);
                        if (onBeforeAddTableColumn == null) {
                            return true;
                        }
                        if (!onBeforeAddTableColumn) {
                            return false;
                        }
                    }

                } else if (type == 3) {//修改表格列
                    if (after) {
                        iFastDatabaseListener.onAfterAlterTableColumn(databaseInfo, tableInfo, columnInfo);
                    } else {
                        Boolean onBeforeAlterTableColumn = iFastDatabaseListener.onBeforeAlterTableColumn(databaseInfo, tableInfo, columnInfo);
                        if (onBeforeAlterTableColumn == null) {
                            return true;
                        }
                        if (!onBeforeAlterTableColumn) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
