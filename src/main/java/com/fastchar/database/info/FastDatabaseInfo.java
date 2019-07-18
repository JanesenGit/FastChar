package com.fastchar.database.info;

import com.fastchar.core.*;

import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.util.*;

public class FastDatabaseInfo extends FastBaseInfo {
    private static final long serialVersionUID = 7200502359495751886L;
    private transient LinkedHashMap<String, List<FastSqlInfo>> defaultData = new LinkedHashMap<>();

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

    private List<FastTableInfo<?>> tables = new ArrayList<>();

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

    public List<FastTableInfo<?>> getTables() {
        return tables;
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

    public boolean isMySql() {
        return type.equals("mysql");
    }

    public boolean isSqlServer() {
        return type.equals("sql_server");
    }


    public boolean isOracle() {
        return type.equals("oracle");
    }

    public DataSource getDataSource() {
        String databaseCode = FastChar.getSecurity().MD5_Encrypt("DataSource" + getName());
        return FastChar.getOverrides().singleInstance(databaseCode,
                IFastDataSource.class).getDataSource(this);
    }

    public IFastDatabaseOperate getOperate() {
        if (FastStringUtils.isEmpty(getType())) {
            return null;
        }
        return FastChar.getOverrides().singleInstance(IFastDatabaseOperate.class, getType());
    }




    public FastTableInfo getTableInfo(String name) {
        for (FastTableInfo table : this.tables) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
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
                    "&serverTimezone=GMT" +
                    "&useSSL=false";
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
        Collections.sort(tables);
        return tables;
    }


    /**
     * 合并
     */
    public FastDatabaseInfo merge(FastDatabaseInfo info) {
        for (String key : info.keySet()) {
            if (key.equals("tables")) {
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
            if (key.equals("tables")) {
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
        return fastDatabaseInfo;

    }
}
