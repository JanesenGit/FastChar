package com.fastchar.database.operate;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.FastDB;
import com.fastchar.database.FastResultSet;
import com.fastchar.database.FastScriptRunner;
import com.fastchar.database.FastType;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.enums.FastDatabaseType;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

/**
 * Oracle数据库同步操作，未进行完整测试
 */
public class FastOracleDatabaseOperateProvider implements IFastDatabaseOperate {

    public static boolean isOverride(Object data) {
        if (data == null) {
            return false;
        }
        return FastDatabaseType.ORACLE.name().equalsIgnoreCase(data.toString());
    }

    private Set<String> tables = null;
    private final Map<String, Set<String>> tableColumns = new HashMap<>(16);
    private final FastDB fastDB = new FastDB().setLog(false).setIgnoreCase(true).setUseCache(false);

    @Override
    public String getConnectionDriverClass(FastDatabaseInfo databaseInfo) throws Exception {
        return "oracle.jdbc.driver.OracleDriver";
    }

    @Override
    public int getConnectionPort(FastDatabaseInfo databaseInfo) throws Exception {
        return 1433;
    }

    @Override
    public String getConnectionUrl(FastDatabaseInfo databaseInfo) throws Exception {
        return "jdbc:oracle:thin:@" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + ":" + databaseInfo.getName();
    }


    @Override
    public void fetchDatabaseInfo(FastDatabaseInfo databaseInfo) throws Exception {
        Connection connection = fastDB.setDatabase(databaseInfo.getCode()).getConnection();
        if (connection == null) {
            return;
        }
        ResultSet resultSet = null;
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            String databaseProductName = dmd.getDatabaseProductName();
            databaseInfo.setProduct(databaseProductName);
            databaseInfo.setVersion(dmd.getDatabaseProductVersion());
            databaseInfo.setType(FastDatabaseType.ORACLE.name().toLowerCase());
            databaseInfo.setUrl(dmd.getURL());

            resultSet = dmd.getTables(databaseInfo.getName(), null, null, new String[]{"table", "TABLE"});
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).setIgnoreCase(true).getListResult();
            for (FastEntity<?> fastEntity : listResult) {
                String table_name = fastEntity.getString("table_name");
                FastTableInfo<?> tableInfo = FastTableInfo.newInstance();
                tableInfo.setName(table_name);
                tableInfo.setComment(fastEntity.getString("remarks"));
                tableInfo.setExist(true);
                tableInfo.setFromXml(false);

                //检索主键
                ResultSet keyRs = dmd.getPrimaryKeys(null, null, tableInfo.getName());
                List<FastEntity<?>> primaryKeys = new FastResultSet(keyRs).setIgnoreCase(true).getListResult();
                for (FastEntity<?> primaryKey : primaryKeys) {
                    String column_name = primaryKey.getString("column_name");
                    FastColumnInfo<?> columnInfo = tableInfo.getColumnInfo(column_name);
                    if (columnInfo == null) {
                        columnInfo = FastColumnInfo.newInstance();
                        columnInfo.setName(column_name);
                    }
                    columnInfo.setExist(true);
                    columnInfo.setPrimary("true");
                    tableInfo.addColumn(columnInfo);
                }
                keyRs.close();

                //检索列
                String sqlStr = String.format("select * from %s where 1=0 ", tableInfo.getName());
                PreparedStatement statement = null;
                ResultSet columnsRs = null;
                try {
                    statement = connection.prepareStatement(sqlStr);
                    columnsRs = statement.executeQuery();
                    ResultSetMetaData data = columnsRs.getMetaData();
                    for (int i = 1; i < data.getColumnCount() + 1; i++) {

                        String columnName = data.getColumnName(i);
                        String type = data.getColumnTypeName(i).toLowerCase();
                        int displaySize = data.getColumnDisplaySize(i);
                        int nullable = data.isNullable(i);
                        boolean isAutoIncrement = data.isAutoIncrement(i);
                        String columnLabel = data.getColumnLabel(i);
//                        int precision = data.getPrecision(i);
//                        int scale = data.getScale(i);

                        FastColumnInfo<?> columnInfo = tableInfo.getColumnInfo(columnName);
                        if (columnInfo == null) {
                            columnInfo = FastColumnInfo.newInstance();
                            columnInfo.setName(columnName);
                            columnInfo.setType(type);

                            columnInfo.setAutoincrement(String.valueOf(isAutoIncrement));
                            if (nullable == ResultSetMetaData.columnNoNulls) {
                                columnInfo.setNullable("not null");
                            } else {
                                columnInfo.setNullable("null");
                            }
                            String index = getColumnIndex(databaseInfo, tableInfo.getName(), columnName);
                            columnInfo.setIndex(formatIndex(index));

                        }
                        columnInfo.setExist(true);
                        columnInfo.setDisplaySize(displaySize);
                        if (FastStringUtils.isEmpty(columnInfo.getType())) {
                            columnInfo.setType(type);
                        }
                        if (FastStringUtils.isEmpty(columnInfo.getComment())) {
                            columnInfo.setComment(columnLabel);
                        }
                        tableInfo.addColumn(columnInfo);
                    }
                } finally {
                    fastDB.close(statement, columnsRs);
                    databaseInfo.addTable(tableInfo);
                }
            }
        } finally {
            fastDB.close(connection, resultSet);
        }
    }

    @Override
    public void createDatabase(FastDatabaseInfo databaseInfo) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            String driverClassName = databaseInfo.getDriver();
            Class.forName(driverClassName);

            connection = DriverManager.getConnection("jdbc:mysql://" + databaseInfo.getHost()
                            + ":" + databaseInfo.getPort() + "/mysql", databaseInfo.getUser(),
                    databaseInfo.getPassword());
            statement = connection.createStatement();
            String sqlStr = String.format("create database if not exists %s default character set utf8 collate utf8_general_ci", databaseInfo.getName());
            statement.executeUpdate(sqlStr);
        } finally {
            fastDB.close(connection, statement);
        }
    }

    @Override
    public boolean checkTableExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception {
        Connection connection = fastDB.setDatabase(databaseInfo.getCode()).getConnection();
        if (connection == null) {
            return true;
        }
        ResultSet resultSet = null;
        try {
            if (tables == null) {
                tables = new HashSet<>();
                DatabaseMetaData dmd = connection.getMetaData();
                String databaseProductName = dmd.getDatabaseProductName();
                databaseInfo.setProduct(databaseProductName);
                databaseInfo.setVersion(dmd.getDatabaseProductVersion());
                resultSet = dmd.getTables(databaseInfo.getName(), null, null, new String[]{"TABLE"});
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (databaseInfo.isIgnoreCase()) {
                        tables.add(tableName.toLowerCase());
                    } else {
                        tables.add(tableName);
                    }
                }
            }
        } finally {
            fastDB.close(connection, resultSet);
        }
        if (databaseInfo.isIgnoreCase()) {
            return tables.contains(tableInfo.getName().toLowerCase());
        }
        return tables.contains(tableInfo.getName());
    }

    @Override
    public void createTable(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception {
        try {
            List<String> columnSql = new ArrayList<>(16);
            List<String> primaryKey = new ArrayList<>(5);
            List<FastColumnInfo<?>> columns = new ArrayList<>(tableInfo.getColumns());
            columns.sort(Comparator.comparingInt(FastColumnInfo::getSortIndex));

            for (FastColumnInfo<?> column : columns) {
                columnSql.add(buildColumnSql(column));
                if (column.isPrimary()) {
                    primaryKey.add(column.getName());
                }
            }
            if (!primaryKey.isEmpty()) {
                columnSql.add(" primary key (" + FastStringUtils.join(primaryKey, ",") + ")");
            }
            String sql = String.format(" create table if not exists %s ( %s ) comment = '%s' ;", tableInfo.getName(), FastStringUtils.join(columnSql, ","), tableInfo.getComment());

            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(sql);

            for (FastColumnInfo<?> column : columns) {
                alterColumnIndex(databaseInfo, tableInfo.getName(), column);
            }

            if (databaseInfo.getDefaultData().containsKey(tableInfo.getName())) {
                for (FastSqlInfo sqlInfo : databaseInfo.getDefaultData().get(tableInfo.getName())) {
                    fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(sqlInfo.getSql(), sqlInfo.toParams());
                }
            }

            if (FastStringUtils.isNotEmpty(tableInfo.getData())) {
                File file = new File(tableInfo.getData());
                if (file.exists() && file.getName().toLowerCase().endsWith(".sql")) {
                    FastScriptRunner scriptRunner = new FastScriptRunner(fastDB.setLog(true).setDatabase(databaseInfo.getCode()).getConnection());
                    scriptRunner.runScript(new FileReader(file));
                    scriptRunner.closeConnection();
                }
            }

        } finally {
            FastChar.getLogger().info(IFastDatabaseOperate.class, FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO1,
                    databaseInfo.getCode(), tableInfo.getName()));
        }
    }

    @Override
    public boolean checkColumnExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        Connection connection = fastDB.setDatabase(databaseInfo.getCode()).getConnection();
        if (connection == null) {
            return true;
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String realTableName = tableInfo.getName();
        if (databaseInfo.isIgnoreCase()) {
            realTableName = realTableName.toLowerCase();
        }
        try {
            if (!tableColumns.containsKey(realTableName)) {
                try {
                    String sqlStr = String.format("select * from %s where 1=0 ", tableInfo.getName());
                    statement = connection.prepareStatement(sqlStr);
                    Set<String> columns = new HashSet<>();
                    resultSet = statement.executeQuery();
                    ResultSetMetaData data = resultSet.getMetaData();
                    for (int i = 1; i < data.getColumnCount() + 1; i++) {
                        String columnName = data.getColumnName(i);
                        if (databaseInfo.isIgnoreCase()) {
                            columns.add(columnName.toLowerCase());
                        } else {
                            columns.add(columnName);
                        }
                    }
                    tableColumns.put(realTableName, columns);
                } catch (Exception e) {
                    FastChar.getLogger().error(this.getClass(), e);
                }
            }
            if (databaseInfo.isIgnoreCase()) {
                return tableColumns.get(realTableName).contains(columnInfo.getName().toLowerCase());
            }
            return tableColumns.get(realTableName).contains(columnInfo.getName());
        } finally {
            fastDB.close(connection, statement, resultSet);
        }
    }

    @Override
    public void addColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        try {
            String sql = String.format("alter table %s add %s", tableInfo.getName(), buildColumnSql(columnInfo));
            if (columnInfo.isPrimary()) {
                List<String> keys = getKeys(databaseInfo, tableInfo.getName());
                if (!keys.contains(columnInfo.getName())) {
                    sql += ",";
                    if (!keys.isEmpty()) {
                        sql += "drop primary key,";
                    }
                    keys.add(columnInfo.getName());
                    sql += "add primary key (" + FastStringUtils.join(keys, ",") + ") using btree;";
                }
            }
            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(sql);
            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLogger().info(FastMySqlDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO2, databaseInfo.getCode(), tableInfo.getName(), columnInfo.getName()));
        }
    }

    @Override
    public void alterColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        try {
            String sql = String.format("alter table %s modify %s ", tableInfo.getName(), buildColumnSql(columnInfo));
            if (columnInfo.isPrimary()) {
                List<String> keys = getKeys(databaseInfo, tableInfo.getName());
                if (!keys.contains(columnInfo.getName())) {
                    sql += ",";
                    if (!keys.isEmpty()) {
                        sql += "drop primary key,";
                    }
                    keys.add(columnInfo.getName());
                    sql += "add primary key (" + FastStringUtils.join(keys, ",") + ") using btree;";
                }
            }
            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(sql);
            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLogger().info(FastMySqlDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO3, databaseInfo.getCode(),
                            tableInfo.getName(), columnInfo.getName()));
        }
    }


    private void alterColumnIndex(FastDatabaseInfo databaseInfo, String tableName, FastColumnInfo<?> columnInfo) throws Exception {
        String convertIndex = convertIndex(columnInfo);
        if (!"none".equalsIgnoreCase(convertIndex)) {
            String columnName = columnInfo.getName();

            String indexName = String.format("%s_OF_%s", columnName, convertIndex.toUpperCase());

            String oldIndexName = getColumnIndex(databaseInfo, tableName, columnName);
            //如果数据库存在了此列索引，则跳过
            if (FastStringUtils.isNotEmpty(oldIndexName)) {
                return;
            }

            if (StringUtils.isNotEmpty(oldIndexName)) {
                String deleteIndexStr = "drop index " + oldIndexName + " on " + tableName + ";";
                fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(deleteIndexStr);
            }
            String createIndexSql = String.format("alter table %s add %s index %s (%s%s)", tableName, convertIndex,
                    indexName, columnName, getIndexMaxLength(getLength(columnInfo), getType(columnInfo)));
            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).update(createIndexSql);
            FastChar.getLogger().info(FastMySqlDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO4, databaseInfo.getCode(), tableName, columnInfo.getName(), indexName));
        }
    }


    private String getColumnIndex(FastDatabaseInfo databaseInfo, String tableName, String columnName) {
        try {
            String checkIndexSql = String.format("select index_name  from information_schema.statistics where table_name = '%s'" +
                    "  and column_name='%s' and table_schema='%s' ", tableName, columnName, databaseInfo.getName());

            FastEntity<?> fastEntity = fastDB.setLog(false)
                    .setDatabase(databaseInfo.getCode())
                    .selectFirst(checkIndexSql);
            if (fastEntity != null) {
                return fastEntity.getString("index_name", "");
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private List<String> getKeys(FastDatabaseInfo databaseInfo, String tableName) {
        List<String> keys = new ArrayList<>(5);
        try {
            String checkKeysSql = String.format("select column_name  from information_schema.key_column_usage where table_name = '%s'" +
                    "  and table_schema='%s'", tableName, databaseInfo.getName());
            List<FastEntity<?>> select = fastDB.setLog(false)
                    .setDatabase(databaseInfo.getCode())
                    .select(checkKeysSql);
            for (FastEntity<?> fastEntity : select) {
                String column_name = fastEntity.getString("column_name", "");
                if (FastStringUtils.isNotEmpty(column_name)) {
                    keys.add(column_name);
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return keys;
    }


    private String convertIndex(FastColumnInfo<?> columnInfo) {
        String index = columnInfo.getIndex();
        if (FastStringUtils.isNotEmpty(index)) {
            String[] indexArray = new String[]{"normal", "fulltext", "spatial", "unique"};
            if ("true".equalsIgnoreCase(index) || "normal".equalsIgnoreCase(index)) {
                return "";
            }
            for (String s : indexArray) {
                if (s.equalsIgnoreCase(index)) {
                    return index;
                }
            }
        }
        return "none";
    }

    private String formatIndex(String indexName) {
        if (FastStringUtils.isEmpty(indexName)) {
            return "false";
        }
        if (indexName.endsWith("_OF_")) {
            return "true";
        }
        String[] of_s = FastStringUtils.splitByWholeSeparator(indexName, "_OF_");
        if (of_s.length > 1) {
            indexName = of_s[1];
        }
        if (FastStringUtils.isEmpty(indexName)) {
            return "true";
        }
        return indexName;
    }

    private String buildColumnSql(FastColumnInfo<?> columnInfo) {
        StringBuilder stringBuilder = new StringBuilder(columnInfo.getName());
        stringBuilder.append(" ");
        stringBuilder.append(getType(columnInfo));

        String length = getLength(columnInfo);
        if (FastStringUtils.isNotEmpty(length)) {
            stringBuilder.append(" (").append(length).append(") ");
        }
        if (FastType.isStringType(getType(columnInfo))) {
            stringBuilder.append(" character set ").append(FastStringUtils.defaultValue(columnInfo.getCharset(),
                    "utf8mb4"));
        }

        if (columnInfo.isAutoincrement()) {
            stringBuilder.append(" auto_increment ");
        }

        stringBuilder.append(" ");
        stringBuilder.append(getNullable(columnInfo));

        if (FastStringUtils.isNotEmpty(columnInfo.getValue())) {
            if (FastType.isNumberType(getType(columnInfo))) {
                stringBuilder.append(" default ");
                if (FastNumberUtils.isNumber(columnInfo.getValue())) {
                    stringBuilder.append(columnInfo.getValue());
                }
            } else if (FastType.isStringType(getType(columnInfo))) {
                stringBuilder.append(" default ");
                stringBuilder.append("'").append(columnInfo.getValue()).append("'");
            }
        }

        stringBuilder.append(" comment '");
        stringBuilder.append(columnInfo.getComment());
        stringBuilder.append("'");

        return stringBuilder.toString();
    }


    private String getLength(FastColumnInfo<?> columnInfo) {
        String type = getType(columnInfo);
        String length = columnInfo.getLength();
        if (FastType.isIntType(type)) {
            if (FastStringUtils.isEmpty(length)) {
                return "11";
            }
        } else if (FastType.isFloatType(type) || FastType.isDoubleType(type)) {
            if (FastStringUtils.isEmpty(length)) {
                return "11,2";
            }
        } else if (FastType.isBigStringType(type)) {
            return null;
        } else if (FastType.isStringType(type)) {
            if (FastStringUtils.isEmpty(length)) {
                if (columnInfo.isPrimary()) {
                    return "188";
                }
                return "500";
            }
        } else if (FastType.isSqlDateType(type) || FastType.isSqlTimeType(type) || FastType.isTimestampType(type)) {
            return null;
        }
        return length;
    }

    private String getType(FastColumnInfo<?> columnInfo) {
        return FastType.convertType(FastDatabaseType.ORACLE.name().toLowerCase(), columnInfo.getType());
    }

    public String getIndexMaxLength(String length, String type) {
        if ("fulltext".equalsIgnoreCase(type)) {
            return "";
        }

        if (FastType.isSqlDateType(type)
                || FastType.isSqlTimeType(type)
                || FastType.isTimestampType(type)
                || FastType.isNumberType(type)
                || FastType.isByteArrayType(type)) {
            return "";
        }
        int numberLength = FastNumberUtils.formatToInt(length);
        if (numberLength == 0) {
            numberLength = 50;
        }
        return "(" + Math.min(numberLength, 155) + ")";
    }


    private String getNullable(FastColumnInfo<?> columnInfo) {
        if (columnInfo.isNotNull()) {
            return " not null ";
        }
        return " null ";
    }
}
