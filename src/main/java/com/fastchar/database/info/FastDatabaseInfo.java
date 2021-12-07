package com.fastchar.database.info;

import com.fastchar.core.*;

import com.fastchar.database.sql.FastSql;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastStringUtils;
import com.sun.org.apache.xpath.internal.functions.FuncTrue;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FastDatabaseInfo extends FastBaseInfo {
    private final transient LinkedHashMap<String, List<FastSqlInfo>> defaultData = new LinkedHashMap<>();

    private String name;
    private String host;
    private String port;
    private String user;
    private String password;
    private String driver;
    private String type;//mysql、sql_server、oracle
    private String version;
    private String product;
    private String url;
    private String cache;
    private long lastModified;
    private boolean ignoreCase = true;
    private boolean syncDatabaseXml = true;//是否允许将fast-database-*.xml表格同步到数据库中
    private boolean fetchDatabaseInfo = true;//是否从数据库中抓取表格信息

    private List<FastTableInfo<?>> tables;
    private FastMapWrap mapTable;

    public FastDatabaseInfo() {
        tagName = "database";
    }

    public long getLastModified() {
        return lastModified;
    }

    public FastDatabaseInfo setLastModified(long lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getName() {
        return name;
    }

    public FastDatabaseInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        if (FastStringUtils.isEmpty(host)) {
            host = "localhost";
        }
        return host;
    }

    public FastDatabaseInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        if (FastStringUtils.isEmpty(port)) {
            if (isMySql()) {
                port = "3306";
            }
            if (isSqlServer()) {
                port = "1433";
            }
            if (isOracle()) {
                port = "1521";
            }
        }
        return port;
    }

    public FastDatabaseInfo setPort(String port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public FastDatabaseInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FastDatabaseInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDriver() {
        if (FastStringUtils.isEmpty(driver)) {
            if (isMySql()) {
                driver = "com.mysql.jdbc.Driver";
                if (FastChar.getFindClass().test("com.mysql.cj.jdbc.Driver")) {
                    driver = "com.mysql.cj.jdbc.Driver";
                }
            }
            if (isSqlServer()) {
                driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            }
            if (isOracle()) {
                driver = "oracle.jdbc.driver.OracleDriver";
            }
        }
        return driver;
    }

    public FastDatabaseInfo setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getType() {
        return type;
    }

    public FastDatabaseInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public FastDatabaseInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getProduct() {
        return product;
    }

    public FastDatabaseInfo setProduct(String product) {
        this.product = product;
        return this;
    }

    public <T extends FastTableInfo<?>> List<T> getTables() {
        if (tables == null) {
            tables = new ArrayList<>();
        }
        return (List<T>) tables;
    }

    public <T extends FastTableInfo<?>> List<T> findTables(String name) {
        List<T> findTales = new ArrayList<>();
        for (FastTableInfo<?> table : getTables()) {
            if (table.getName().contains(name)) {
                findTales.add((T) table);
            }
        }
        return findTales;
    }


    public FastDatabaseInfo setTables(List<FastTableInfo<?>> tables) {
        this.tables = tables;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public FastDatabaseInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isCache() {
        return FastBooleanUtils.formatToBoolean(cache, false);
    }

    public FastDatabaseInfo setCache(boolean cache) {
        this.cache = String.valueOf(cache);
        return this;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public FastDatabaseInfo setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    public boolean isMySql() {
        return "mysql".equals(type);
    }

    public boolean isSqlServer() {
        return "sql_server".equals(type);
    }


    public boolean isOracle() {
        return "oracle".equals(type);
    }

    public DataSource getDataSource() {
        String databaseCode = FastChar.getSecurity().MD5_Encrypt("FastChar-DataSource" + getName());
        if (FastStringUtils.isEmpty(getName())) {
            databaseCode = FastChar.getSecurity().MD5_Encrypt("FastChar-DataSource");
        }
        return FastChar.getOverrides().singleInstance(databaseCode, IFastDataSource.class).getDataSource(this);
    }

    public IFastDatabaseOperate getOperate() {
        if (FastStringUtils.isEmpty(getType())) {
            return null;
        }
        return FastChar.getOverrides().newInstance(IFastDatabaseOperate.class, getType());
    }

    public boolean isSyncDatabaseXml() {
        return syncDatabaseXml;
    }

    public FastDatabaseInfo setSyncDatabaseXml(boolean syncDatabaseXml) {
        this.syncDatabaseXml = syncDatabaseXml;
        return this;
    }

    public boolean isFetchDatabaseInfo() {
        return fetchDatabaseInfo;
    }

    public FastDatabaseInfo setFetchDatabaseInfo(boolean fetchDatabaseInfo) {
        this.fetchDatabaseInfo = fetchDatabaseInfo;
        return this;
    }

    public FastTableInfo<?> getTableInfo(String name) {
        if (mapTable != null) {
            return mapTable.getObject(name);
        }
        for (FastTableInfo<?> table : this.getTables()) {
            if (isIgnoreCase()) {
                if (table.getName().equalsIgnoreCase(name)) {
                    return table;
                }
            }
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    public boolean existTable(String name) {
        return getTableInfo(name) != null;
    }

    public LinkedHashMap<String, List<FastSqlInfo>> getDefaultData() {
        return defaultData;
    }


    public String toUrl() {
        if (FastStringUtils.isNotEmpty(getUrl())) {
            return url;
        }
        String url = null;
        if (isMySql()) {
            url = "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getName() +
                    "?rewriteBatchedStatements=true" +
                    "&useUnicode=true" +
                    "&characterEncoding=utf-8" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=GMT" +
                    "&useSSL=false" +
                    "&useInformationSchema=true";
        } else if (isSqlServer()) {
            url = "jdbc:sqlserver://" + getHost() + ":" + getPort() + ";databaseName=" + getName();
        } else if (isOracle()) {
            url = "jdbc:oracle:thin:@" + getHost() + ":" + getPort() + ":" + getName();
        }
        return url;
    }


    public List<String> getAllTables(String sqlStr) {
        List<String> tables = new ArrayList<>();
        for (FastTableInfo<?> table : this.getTables()) {
            if (sqlStr.contains(" " + table.getName() + " ")) {
                tables.add(table.getName());
            }
        }
        return tables;
    }

    public FastDatabaseInfo addTable(FastTableInfo<?> tableInfo) {
        if (existTable(tableInfo.getName())) {
            return this;
        }
        getTables().add(tableInfo);
        return this;
    }


    /**
     * 合并
     */
    public FastDatabaseInfo merge(FastDatabaseInfo info) {
        for (String key : info.keySet()) {
            if ("tables".equals(String.valueOf(key))) {
                continue;
            }
            this.set(key, info.get(key));
        }
        if (FastStringUtils.isNotEmpty(info.getFileName())) {
            setFileName(info.getFileName());
        }
        if (info.getLineNumber() != 0) {
            setLineNumber(info.getLineNumber());
        }

        for (FastTableInfo<?> table : info.getTables()) {
            FastTableInfo<?> existTable = this.getTableInfo(table.getName());
            if (existTable != null) {
                existTable.merge(table);
            } else {
                this.getTables().add(table);
            }
        }
        return this;
    }

    public FastDatabaseInfo copy() {
        FastDatabaseInfo fastDatabaseInfo = FastChar.getOverrides().newInstance(FastDatabaseInfo.class);
        for (String key : keySet()) {
            if ("tables".equals(String.valueOf(key))) {
                continue;
            }
            fastDatabaseInfo.set(key, get(key));
        }
        fastDatabaseInfo.setFileName(this.getFileName());
        fastDatabaseInfo.setLineNumber(this.getLineNumber());
        fastDatabaseInfo.setTagName(this.getTagName());
        for (FastTableInfo<?> table : getTables()) {
            fastDatabaseInfo.getTables().add(table.copy());
        }
        fastDatabaseInfo.fromProperty();
        return fastDatabaseInfo;

    }


    public void releaseTableMap() {
        if (mapTable != null) {
            mapTable.clear();
            mapTable = null;
        }
        for (FastTableInfo<?> table : this.getTables()) {
            table.releaseColumnMap();
        }
    }

    public void tableToMap() {
        mapTable = FastMapWrap.newInstance(new ConcurrentHashMap<>(16));
        mapTable.setIgnoreCase(isIgnoreCase());
        for (FastTableInfo<?> table : this.getTables()) {
            table.setDatabaseName(this.getName());
            table.setIgnoreCase(isIgnoreCase());
            table.fromProperty();
            table.columnToMap();
            this.mapTable.set(table.getName(), table);
        }
        this.fromProperty();
    }


    public FastSql getFastSql() {
        return FastSql.getInstance(type);
    }


    public void validate() throws FastDatabaseException {
        //合并 * 号匹配的表格

        List<FastTableInfo<?>> matchesTableList = findTables("*");
        for (FastTableInfo<?> matchesTable : matchesTableList) {
            for (FastTableInfo<?> table : this.getTables()) {
                if (table.isLock()) {
                    continue;
                }
                if (FastStringUtils.matches(matchesTable.getName(), table.getName())) {
                    table.merge(matchesTable, true);
                }
            }
        }

        List<FastTableInfo<?>> tables = new ArrayList<>(getTables());
        for (FastTableInfo<?> table : tables) {
            table.setDatabaseName(getName());
            table.fromProperty();
            for (FastColumnInfo<?> column : table.getColumns()) {
                column.setDatabaseName(table.getDatabaseName());
                column.fromProperty();
                if (column.isFromXml()) {
                    column.validate();
                }
            }
            if (table.isFromXml()) {
                table.validate();
            }
        }
    }

}
