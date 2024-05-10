package com.fastchar.database.info;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.database.sql.FastSql;
import com.fastchar.enums.FastDatabaseType;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class FastDatabaseInfo extends LinkedHashMap<String, Object> {

    protected transient FastMapWrap mapWrap;

    public FastDatabaseInfo() {
        super(16);
        put("tagName", "database");
        mapWrap = FastMapWrap.newInstance(this);
    }

    public FastMapWrap getMapWrap() {
        return mapWrap;
    }


    public <E extends FastTableInfo<?>> Collection<E> getTables() {
        return (Collection<E>) getMapTables().values();
    }

    private Map<String, FastTableInfo<?>> getMapTables() {
        Map<String, FastTableInfo<?>> tables = mapWrap.getObject("tables");
        if (tables == null) {
            tables = new LinkedHashMap<>(16);
            put("tables", tables);
        }
        return tables;
    }

    public FastTableInfo<?> getTableInfo(String name) {
        if (FastStringUtils.isEmpty(name)) {
            return null;
        }
        FastTableInfo<?> tableInfo = getMapTables().get(name.toLowerCase());
        if (tableInfo == null) {
            return null;
        }
        if (!tableInfo.isEnable()) {
            return null;
        }
        return tableInfo;
    }

    public boolean existTable(String name) {
        return getTableInfo(name) != null;
    }


    public boolean isFromXml() {
        return mapWrap.getBoolean("fromXml", false);
    }

    public void setFromXml(boolean fromXml) {
        put("fromXml", fromXml);
    }


    public int getLineNumber() {
        return mapWrap.getInt("lineNumber");
    }

    public void setLineNumber(int lineNumber) {
        put("lineNumber", lineNumber);
    }

    public String getFileName() {
        return mapWrap.getString("fileName");
    }

    public void setFileName(String fileName) {
        put("fileName", fileName);
    }

    public String getTagName() {
        return mapWrap.getString("tagName");
    }


    public boolean isEnable() {
        return mapWrap.getBoolean("enable", true);
    }

    public long getLastModified() {
        return mapWrap.getLong("lastModified");
    }

    public FastDatabaseInfo setLastModified(long lastModified) {
        put("lastModified", lastModified);
        return this;
    }

    public String getName() {
        return mapWrap.getString("name");
    }

    public FastDatabaseInfo setName(String name) {
        put("name", name);
        return this;
    }

    public String getHost() {
        return mapWrap.getString("host");
    }

    public FastDatabaseInfo setHost(String host) {
        put("host", host);
        return this;
    }

    public String getPort() {
        String port = mapWrap.getString("port");
        if (FastStringUtils.isEmpty(port)) {
            try {
                port = String.valueOf(getOperate().getConnectionPort(this));
                setPort(port);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return port;
    }

    public FastDatabaseInfo setPort(String port) {
        put("port", port);
        return this;
    }

    public String getUser() {
        return mapWrap.getString("user");
    }

    public FastDatabaseInfo setUser(String user) {
        put("user", user);
        return this;
    }

    public String getPassword() {
        return mapWrap.getString("password");
    }

    public FastDatabaseInfo setPassword(String password) {
        put("password", password);
        return this;
    }

    public String getDriver() {
        String driver = mapWrap.getString("driver");
        if (FastStringUtils.isEmpty(driver)) {
            try {
                driver = getOperate().getConnectionDriverClass(this);
                setDriver(driver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return driver;
    }

    public FastDatabaseInfo setDriver(String driver) {
        put("driver", driver);
        return this;
    }

    public String getType() {
        return mapWrap.getString("type");
    }

    public FastDatabaseInfo setType(String type) {
        put("type", type);
        return this;
    }

    public FastDatabaseInfo setType(FastDatabaseType type) {
        setType(type.name().toLowerCase());
        return this;
    }


    public String getVersion() {
        return mapWrap.getString("version");
    }

    public FastDatabaseInfo setVersion(String version) {
        put("version", version);
        return this;
    }

    public String getProduct() {
        return mapWrap.getString("product");
    }

    public FastDatabaseInfo setProduct(String product) {
        put("product", product);
        return this;
    }

    public String getUrl() {
        return mapWrap.getString("url");
    }

    public FastDatabaseInfo setUrl(String url) {
        put("url", url);
        return this;
    }

    public boolean isCache() {
        return mapWrap.getBoolean("cache", false);
    }

    public FastDatabaseInfo setCache(boolean cache) {
        put("cache", cache);
        return this;
    }

    public boolean isIgnoreCase() {
        return mapWrap.getBoolean("ignoreCase", true);
    }

    public FastDatabaseInfo setIgnoreCase(boolean ignoreCase) {
        put("ignoreCase", ignoreCase);
        mapWrap.setIgnoreCase(ignoreCase);
        return this;
    }

    public String getCode() {
        String code = mapWrap.getString("code");
        if (FastStringUtils.isEmpty(code)) {
            code = getName();
            setCode(code);
        }
        return code;
    }

    public FastDatabaseInfo setCode(String code) {
        put("code", code);
        return this;
    }

    public boolean isMySql() {
        return FastDatabaseType.MYSQL.name().equalsIgnoreCase(getType());
    }

    public boolean isSqlServer() {
        return FastDatabaseType.SQL_SERVER.name().equalsIgnoreCase(getType());
    }


    public boolean isOracle() {
        return FastDatabaseType.ORACLE.name().equalsIgnoreCase(getType());
    }

    public DataSource getDataSource() {
        String databaseCode = FastMD5Utils.MD5To16("FastChar-DataSource-" + getCode());
        if (FastStringUtils.isEmpty(getCode())) {
            databaseCode = FastMD5Utils.MD5To16("FastChar-DataSource");
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
        return mapWrap.getBoolean("syncDatabaseXml", true);
    }

    public FastDatabaseInfo setSyncDatabaseXml(boolean syncDatabaseXml) {
        put("syncDatabaseXml", syncDatabaseXml);
        return this;
    }

    public boolean isFetchDatabaseInfo() {
        return mapWrap.getBoolean("fetchDatabaseInfo", true);
    }

    public FastDatabaseInfo setFetchDatabaseInfo(boolean fetchDatabaseInfo) {
        put("fetchDatabaseInfo", fetchDatabaseInfo);
        return this;
    }


    public LinkedHashMap<String, List<FastSqlInfo>> getDefaultData() {
        LinkedHashMap<String, List<FastSqlInfo>> defaultData = mapWrap.getObject("defaultData");
        if (defaultData == null) {
            defaultData = new LinkedHashMap<>();
            put("defaultData", defaultData);
        }
        return defaultData;
    }


    public String toUrl() {
        String url = getUrl();
        if (FastStringUtils.isNotEmpty(url)) {
            return url;
        }
        try {
            url = getOperate().getConnectionUrl(this);
            setUrl(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }


    public List<String> getAllTables(String sqlStr) {
        List<String> tables = new ArrayList<>();
        for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : this.getMapTables().entrySet()) {
            if (sqlStr.contains(" " + tableInfoEntry.getKey() + " ")) {
                tables.add(tableInfoEntry.getKey());
            }
        }
        return tables;
    }

    public FastTableInfo<?> addTable(FastTableInfo<?> tableInfo) {
        if (tableInfo == null) {
            return null;
        }
        FastTableInfo<?> fastTableInfo = getMapTables().get(tableInfo.getName().toLowerCase());
        if (fastTableInfo != null) {
            fastTableInfo.merge(tableInfo);
        } else {
            getMapTables().put(tableInfo.getName().toLowerCase(), tableInfo);
        }
        return tableInfo;
    }

    public boolean isValidate() {
        return mapWrap.getBoolean("validate", true);
    }

    public FastDatabaseInfo setValidate(boolean validate) {
        put("validate", validate);
        return this;
    }

    public <T extends FastTableInfo<?>> List<T> findTables(String key) {
        List<T> findTales = new ArrayList<>();
        for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : getMapTables().entrySet()) {
            if (tableInfoEntry.getKey().contains(key)) {
                findTales.add((T) tableInfoEntry.getValue());
            }
        }
        return findTales;
    }

    /**
     * 合并
     */
    public FastDatabaseInfo merge(FastDatabaseInfo info) {
        for (Map.Entry<String, Object> stringObjectEntry : info.entrySet()) {
            if ("tables".equals(stringObjectEntry.getKey())
                    || "name".equals(stringObjectEntry.getKey())
                    || "code".equals(stringObjectEntry.getKey())) {
                continue;
            }
            put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        if (FastStringUtils.isNotEmpty(info.getFileName())) {
            setFileName(info.getFileName());
        }
        if (info.getLineNumber() != 0) {
            setLineNumber(info.getLineNumber());
        }
        for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : info.getMapTables().entrySet()) {
            this.addTable(tableInfoEntry.getValue());
        }

        return this;
    }

    public FastDatabaseInfo copy() {
        FastDatabaseInfo fastDatabaseInfo = FastChar.getOverrides().newInstance(FastDatabaseInfo.class);
        for (Map.Entry<String, Object> stringObjectEntry : this.entrySet()) {
            if ("tables".equals(stringObjectEntry.getKey())) {
                continue;
            }
            fastDatabaseInfo.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        fastDatabaseInfo.setFileName(this.getFileName());
        fastDatabaseInfo.setLineNumber(this.getLineNumber());
        for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : getMapTables().entrySet()) {
            fastDatabaseInfo.addTable(tableInfoEntry.getValue().copy());
        }
        return fastDatabaseInfo;

    }

    public FastSql getFastSql() {
        return FastSql.getInstance(getType());
    }


    public void validate() throws FastDatabaseException {

        if (FastStringUtils.isEmpty(getName())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR4, "name", this.toSimpleInfo()));
        }

        if (FastStringUtils.isEmpty(getHost())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR4, "host", this.toSimpleInfo()));
        }

        if (FastStringUtils.isEmpty(getUser())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR4, "user", this.toSimpleInfo()));
        }

        if (FastStringUtils.isEmpty(getPassword())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_ERROR4, "password", this.toSimpleInfo()));
        }

        //合并包含 * 号匹配的表格

        List<FastTableInfo<?>> matchesTableList = findTables("*");
        for (FastTableInfo<?> matchesTable : matchesTableList) {
            for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : getMapTables().entrySet()) {
                if (tableInfoEntry.getValue().isLock()) {
                    continue;
                }
                if (FastStringUtils.matches(matchesTable.getName(), tableInfoEntry.getKey())) {
                    tableInfoEntry.getValue().merge(matchesTable, true);
                }
            }
        }

        for (Map.Entry<String, FastTableInfo<?>> tableInfoEntry : getMapTables().entrySet()) {
            FastTableInfo<?> table = tableInfoEntry.getValue();
            table.setDatabase(getCode());
            Collection<FastColumnInfo<?>> columns = table.getColumns();
            for (FastColumnInfo<?> column : columns) {
                column.setDatabase(table.getDatabase());
                if (column.isFromXml()) {
                    column.validate();
                }
            }
            if (table.isFromXml()) {
                table.validate();
            }
        }
    }

    public String toSimpleInfo() {
        return getClass().getSimpleName()
                + " { name = '" + getName()
                + "' , host = '" + getHost()
                + "' , user = '" + getUser()
                + "' , password = '" + getPassword()
                + "' , port = '" + getPort()
                + "' , code = '" + getCode()
                + "' , type = '" + getType()
                + "' , driver = '" + getDriver()
                + "' } ";
    }

}
