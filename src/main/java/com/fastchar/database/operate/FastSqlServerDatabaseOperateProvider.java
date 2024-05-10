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

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

/**
 * MySql数据库操作
 */
public class FastSqlServerDatabaseOperateProvider implements IFastDatabaseOperate {

    public static boolean isOverride(Object data) {
        if (data == null) {
            return false;
        }
        return FastDatabaseType.SQL_SERVER.name().equalsIgnoreCase(data.toString()) || "sqlserver".equalsIgnoreCase(data.toString());
    }

    private Set<String> tables = null;
    private final Map<String, Set<String>> tableColumns = new HashMap<>(16);
    private final FastDB fastDB = new FastDB().setLog(false).setIgnoreCase(true).setUseCache(false);

    @Override
    public String getConnectionDriverClass(FastDatabaseInfo databaseInfo) throws Exception {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public int getConnectionPort(FastDatabaseInfo databaseInfo) throws Exception {
        return 1521;
    }

    @Override
    public String getConnectionUrl(FastDatabaseInfo databaseInfo) throws Exception {
        return "jdbc:sqlserver://" + databaseInfo.getHost() + ":" + databaseInfo.getPort() + ";databaseName=" + databaseInfo.getName();
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
            databaseInfo.setType(FastDatabaseType.SQL_SERVER.name().toLowerCase());
            databaseInfo.setUrl(dmd.getURL());

            resultSet = dmd.getTables(databaseInfo.getName(), null, null, new String[]{"table", "TABLE"});
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).setIgnoreCase(true).getListResult();
            for (FastEntity<?> fastEntity : listResult) {
                String table_name = fastEntity.getString("table_name");
                FastTableInfo<?> tableInfo = FastTableInfo.newInstance();
                tableInfo.setName(table_name);
                // 备注取消从数据中同步
                tableInfo.setComment(fastEntity.getString("remarks"));
                tableInfo.setExist(true);
                tableInfo.setFromXml(false);

                //检索主键
                ResultSet keyRs = dmd.getPrimaryKeys(null, null, tableInfo.getName());
                List<FastEntity<?>> primaryKeys = new FastResultSet(keyRs).setIgnoreCase(true).getListResult();
                for (FastEntity<?> primaryKey : primaryKeys) {
                    String column_name = primaryKey.getString("column_name");
                    FastColumnInfo<?> columnInfo = FastColumnInfo.newInstance();
                    columnInfo.setName(column_name);
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
                    int totalCount = data.getColumnCount() + 1;
                    for (int i = 1; i < totalCount; i++) {

                        String columnName = data.getColumnName(i);
                        String type = data.getColumnTypeName(i).toLowerCase();
                        int displaySize = data.getColumnDisplaySize(i);
                        int nullable = data.isNullable(i);
                        boolean isAutoIncrement = data.isAutoIncrement(i);
                        String columnLabel = data.getColumnLabel(i);

                        FastColumnInfo<?> columnInfo = FastColumnInfo.newInstance();
                        columnInfo.setName(columnName);
                        columnInfo.setType(type);
                        columnInfo.setAutoincrement(String.valueOf(isAutoIncrement));
                        if (nullable == ResultSetMetaData.columnNoNulls) {
                            columnInfo.setNullable("not null");
                        } else {
                            columnInfo.setNullable("null");
                        }
                        String indexName = String.format("%s_%s_Index", tableInfo.getName(), columnName);
                        boolean index = checkColumnIndex(databaseInfo, indexName);
                        columnInfo.setIndex(String.valueOf(index));

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

            connection = DriverManager.getConnection("jdbc:sqlserver://" + databaseInfo.getHost() + ":" + databaseInfo.getPort(), databaseInfo.getUser(),
                    databaseInfo.getPassword());
            statement = connection.createStatement();
            String sqlStr = String.format("if not exists (select * from master.dbo.sysdatabases where name='%s')   CREATE DATABASE %s ", databaseInfo.getName(), databaseInfo.getName());
            statement.execute(sqlStr);
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
                String schemaPattern = null;
                String databaseProductName = dmd.getDatabaseProductName();
                if ("sqlserver".equals(databaseProductName.toLowerCase().replace(" ", ""))) {
                    schemaPattern = "dbo";
                }
                databaseInfo.setProduct(databaseProductName);
                databaseInfo.setVersion(dmd.getDatabaseProductVersion());
                resultSet = dmd.getTables(databaseInfo.getName(), schemaPattern, null, new String[]{"TABLE"});
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
            String sql = String.format(" if not exists (select * from sysobjects where name = '%s' ) create table  %s ( %s )  comment = '%s' ;", tableInfo.getName(), tableInfo.getName(), FastStringUtils.join(columnSql, ","), tableInfo.getComment());

            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql);

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
                    scriptRunner.setSendFullScript(true);
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
            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql);
            if (columnInfo.isPrimary()) {
                String constraintName = null;
                List<FastEntity<?>> keys = getKeys(databaseInfo, tableInfo.getName());
                if (keys != null) {
                    List<String> keyColumns = new ArrayList<>(5);
                    for (FastEntity<?> key : keys) {
                        String columnName = key.getString("column_name");
                        constraintName = key.getString("constraint_name");
                        if (columnName.equals(columnInfo.getName())) {
                            return;
                        }
                        keyColumns.add(columnName);
                    }

                    if (FastStringUtils.isNotEmpty(constraintName)) {
                        if (!keys.isEmpty()) {
                            String sql2 = "alter table " + tableInfo.getName() + " drop constraint " + constraintName;
                            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql2);
                        }
                        String sql3 = "alter table add constraint " + constraintName + " primary key (" + FastStringUtils.join(keyColumns, ",") + ") ";
                        fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql3);
                    }
                }
            }

            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLogger().info(FastSqlServerDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO2, databaseInfo.getCode(), tableInfo.getName(), columnInfo.getName()));
        }
    }

    @Override
    public void alterColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        try {
            String sql = String.format("alter table %s alter column %s", tableInfo.getName(), buildColumnSql(columnInfo, true));
            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql);
            if (columnInfo.isPrimary()) {
                String constraintName = null;
                List<FastEntity<?>> keys = getKeys(databaseInfo, tableInfo.getName());
                if (keys != null) {
                    List<String> keyColumns = new ArrayList<>(5);
                    for (FastEntity<?> key : keys) {
                        String columnName = key.getString("column_name");
                        constraintName = key.getString("constraint_name");
                        if (columnName.equals(columnInfo.getName())) {
                            return;
                        }
                        keyColumns.add(columnName);
                    }

                    if (FastStringUtils.isNotEmpty(constraintName)) {
                        if (!keys.isEmpty()) {
                            String sql2 = "alter table " + tableInfo.getName() + " drop constraint " + constraintName;
                            fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql2);
                        }
                        String sql3 = "alter table add constraint " + constraintName + " primary key (" + FastStringUtils.join(keyColumns, ",") + ") ";
                        fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(sql3);
                    }
                }
            }

            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLogger().info(FastSqlServerDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO3, databaseInfo.getCode(),
                            tableInfo.getName(), columnInfo.getName()));
        }
    }

    private void alterColumnIndex(FastDatabaseInfo databaseInfo, String tableName, FastColumnInfo<?> columnInfo) throws Exception {
        String convertIndex = convertIndex(columnInfo);
        if (!"none".equalsIgnoreCase(convertIndex)) {
            String columnName = columnInfo.getName();
            String indexName = String.format("%s_%s_Index", tableName, columnName);
            if (!checkColumnIndex(databaseInfo, indexName)) {
                String createIndexSql = String.format("create %s index %s on %s(%s) ", convertIndex, indexName, tableName, columnName);
                fastDB.setLog(true).setDatabase(databaseInfo.getCode()).run(createIndexSql);
                FastChar.getLogger().info(FastSqlServerDatabaseOperateProvider.class,
                        FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO4, databaseInfo.getCode(), tableName, columnInfo.getName(), indexName));
            }
        }
    }


    private boolean checkColumnIndex(FastDatabaseInfo databaseInfo, String indexName) {
        try {
            String checkIndexSql = String.format("select count(1) as countIndex  from sysindexes where name = '%s'", indexName);
            FastEntity<?> fastEntity = fastDB.setLog(false).setDatabase(databaseInfo.getCode()).selectFirst(checkIndexSql);
            if (fastEntity != null) {
                return fastEntity.getInt("countIndex") > 0;
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return false;
    }

    private List<FastEntity<?>> getKeys(FastDatabaseInfo databaseInfo, String tableName) {
        try {
            String checkKeysSql = String.format("select column_name,constraint_name  from information_schema.key_column_usage where table_name = '%s'" +
                    "  and table_catalog='%s'", tableName, databaseInfo.getName());
            return fastDB.setLog(false)
                    .setDatabase(databaseInfo.getCode())
                    .setIgnoreCase(true)
                    .select(checkKeysSql);
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return null;
    }


    private String convertIndex(FastColumnInfo<?> columnInfo) {
        String index = columnInfo.getIndex();
        if (FastStringUtils.isNotEmpty(index)) {
            String[] indexArray = new String[]{"nonclustered", "clustered"};
            if ("true".equalsIgnoreCase(index) || "normal".equalsIgnoreCase(index)) {
                return "nonclustered";
            }
            for (String s : indexArray) {
                if (s.equalsIgnoreCase(index)) {
                    return index;
                }
            }
        }
        return "none";
    }

    private String buildColumnSql(FastColumnInfo<?> columnInfo) {
        return buildColumnSql(columnInfo, false);
    }

    private String buildColumnSql(FastColumnInfo<?> columnInfo, boolean isModify) {
        StringBuilder stringBuilder = new StringBuilder(columnInfo.getName());
        stringBuilder.append(" ");
        stringBuilder.append(getType(columnInfo));

        String length = getLength(columnInfo);
        if (FastStringUtils.isNotEmpty(length)) {
            stringBuilder.append(" (").append(length).append(") ");
        }

        if (!isModify) {
            if (columnInfo.isAutoincrement()) {
                stringBuilder.append(" identity(1,1) not null ");
            }
        }

        if (!columnInfo.isAutoincrement()) {
            stringBuilder.append(" ");
            stringBuilder.append(getNullable(columnInfo));
        }

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

        return stringBuilder.toString();
    }


    private String getLength(FastColumnInfo<?> columnInfo) {
        String type = getType(columnInfo);
        String length = columnInfo.getLength();
        if (FastType.isNumberType(type)) {
            return null;
        } else if ("varchar".equalsIgnoreCase(type)) {
            if (FastStringUtils.isEmpty(length)) {
                if (columnInfo.isPrimary()) {
                    return "188";
                }
                return "500";
            }
        } else if (FastType.isSqlDateType(type)
                || FastType.isSqlTimeType(type)
                || FastType.isTimestampType(type)) {
            return null;
        }
        return length;
    }

    private String getType(FastColumnInfo<?> columnInfo) {
        return FastType.convertType(FastDatabaseType.SQL_SERVER.name().toLowerCase(), columnInfo.getType());
    }


    private String getNullable(FastColumnInfo<?> columnInfo) {
        if (columnInfo.isNotNull()) {
            return " not null ";
        }
        return " null ";
    }
}
