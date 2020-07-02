package com.fastchar.database.operate;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.FastDb;
import com.fastchar.database.FastResultSet;
import com.fastchar.database.FastScriptRunner;
import com.fastchar.database.FastType;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySql数据库操作
 */
public class FastSqlServerDatabaseOperateProvider implements IFastDatabaseOperate {

    public static boolean isOverride(Object data) {
        if (data == null) {
            return false;
        }
        return data.toString().equalsIgnoreCase("sql_server")||data.toString().equalsIgnoreCase("sqlserver");
    }

    private Set<String> tables = null;
    private Map<String, Set<String>> tableColumns = new HashMap<>();
    private FastDb fastDb = new FastDb().setLog(false).setIgnoreCase(true).setUseCache(false);

    @Override
    public void fetchDatabaseInfo(FastDatabaseInfo databaseInfo) throws Exception {
        Connection connection = fastDb.setDatabase(databaseInfo.getName()).getConnection();
        if (connection == null) return;
        ResultSet resultSet = null;
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            String databaseProductName = dmd.getDatabaseProductName();
            databaseInfo.setProduct(databaseProductName);
            databaseInfo.setVersion(dmd.getDatabaseProductVersion());
            databaseInfo.setType("sql_server");
            databaseInfo.setUrl(dmd.getURL());

            resultSet = dmd.getTables(null, null, null, new String[]{"table","TABLE"});
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).setIgnoreCase(true).getListResult();
            for (FastEntity<?> fastEntity : listResult) {
                String table_name = fastEntity.getString("table_name");
                FastTableInfo<?> tableInfo = databaseInfo.getTableInfo(table_name);
                if (tableInfo == null) {
                    tableInfo = FastTableInfo.newInstance();
                    tableInfo.setName(table_name);
                    databaseInfo.getTables().add(tableInfo);
                }

                //检索主键
                ResultSet keyRs = dmd.getPrimaryKeys(null, null, tableInfo.getName());
                List<FastEntity<?>> primaryKeys = new FastResultSet(keyRs).setIgnoreCase(true).getListResult();
                for (FastEntity<?> primaryKey : primaryKeys) {
                    String column_name = primaryKey.getString("column_name");
                    FastColumnInfo<?> columnInfo = tableInfo.getColumnInfo(column_name);
                    if (columnInfo == null) {
                        columnInfo = FastColumnInfo.newInstance();
                        columnInfo.setName(column_name);
                        tableInfo.getColumns().add(columnInfo);
                    }
                    columnInfo.setPrimary("true");
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
//                        int precision = data.getPrecision(i);
//                        int scale = data.getScale(i);

                        if (displaySize >= 715827882) {
                            type = "ntext";
                        } else if (displaySize >= 21845) {
                            type = "text";
                        }

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
                            String indexName = String.format("%s_%s_Index", tableInfo.getName(), columnName);
                            boolean index = checkColumnIndex(databaseInfo.getName(), indexName);
                            columnInfo.setIndex(String.valueOf(index));
                            columnInfo.fromProperty();

                            tableInfo.getColumns().add(columnInfo);
                        }
                    }
                } finally {
                    fastDb.close(statement, columnsRs);
                }

                tableInfo.fromProperty();
            }
            databaseInfo.fromProperty();
        } finally {
            fastDb.close(connection, resultSet);
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
            fastDb.close(connection, statement);
        }
    }

    @Override
    public boolean checkTableExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception {
        Connection connection = fastDb.setDatabase(databaseInfo.getName()).getConnection();
        if (connection == null) return true;
        ResultSet resultSet = null;
        try {
            if (tables == null) {
                tables = new HashSet<>();
                DatabaseMetaData dmd = connection.getMetaData();
                String schemaPattern = null;
                String databaseProductName = dmd.getDatabaseProductName();
                if (databaseProductName.toLowerCase().replace(" ", "").equals("sqlserver")) {
                    schemaPattern = "dbo";
                }
                databaseInfo.setProduct(databaseProductName);
                databaseInfo.setVersion(dmd.getDatabaseProductVersion());
                resultSet = dmd.getTables(null, schemaPattern, null, new String[]{"TABLE"});
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }
        } finally {
            fastDb.close(connection, resultSet);
        }

        return tables.contains(tableInfo.getName());
    }

    @Override
    public void createTable(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception {
        try {
            List<String> columnSql = new ArrayList<>();
            List<String> primaryKey = new ArrayList<>();
            for (FastColumnInfo<?> column : tableInfo.getColumns()) {
                columnSql.add(buildColumnSql(column));
                if (column.isPrimary()) {
                    primaryKey.add(column.getName());
                }
            }
            if (primaryKey.size() > 0) {
                columnSql.add(" primary key (" + FastStringUtils.join(primaryKey, ",") + ")");
            }
            String sql = String.format(" if not exists (select * from sysobjects where name = '%s' ) create table  %s ( %s )  comment = '%s' ;", tableInfo.getName(), tableInfo.getName(), FastStringUtils.join(columnSql, ","),tableInfo.getComment());

            fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql);
            if (databaseInfo.getDefaultData().containsKey(tableInfo.getName())) {
                for (FastSqlInfo sqlInfo : databaseInfo.getDefaultData().get(tableInfo.getName())) {
                    fastDb.setLog(true).setDatabase(databaseInfo.getName()).update(sqlInfo.getSql(), sqlInfo.toParams());
                }
            }
            if (FastStringUtils.isNotEmpty(tableInfo.getData())) {
                File file = new File(tableInfo.getData());
                if (file.exists() && file.getName().toLowerCase().endsWith(".sql")) {
                    FastScriptRunner scriptRunner = new FastScriptRunner(fastDb.setLog(true).setDatabase(databaseInfo.getName()).getConnection());
                    scriptRunner.setLogWriter(null);
                    scriptRunner.setSendFullScript(true);
                    scriptRunner.runScript(new FileReader(file));
                    scriptRunner.closeConnection();
                }
            }

        } finally {
            FastChar.getLog().info(IFastDatabaseOperate.class, FastChar.getLocal().getInfo("Db_Table_Info1", databaseInfo.getName(), tableInfo.getName()));
        }
    }

    @Override
    public boolean checkColumnExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        Connection connection = fastDb.setDatabase(databaseInfo.getName()).getConnection();
        if (connection == null) {
            return true;
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (!tableColumns.containsKey(tableInfo.getName())) {
                try {
                    String sqlStr = String.format("select * from %s where 1=0 ", tableInfo.getName());
                    statement = connection.prepareStatement(sqlStr);
                    Set<String> columns = new HashSet<>();
                    resultSet = statement.executeQuery();
                    ResultSetMetaData data = resultSet.getMetaData();
                    for (int i = 1; i < data.getColumnCount() + 1; i++) {
                        String columnName = data.getColumnName(i);
                        columns.add(columnName);
                    }
                    tableColumns.put(tableInfo.getName(), columns);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return tableColumns.get(tableInfo.getName()).contains(columnInfo.getName());
        } finally {
            fastDb.close(connection, statement, resultSet);
        }
    }

    @Override
    public void addColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        try {
            String sql = String.format("alter table %s add %s", tableInfo.getName(), buildColumnSql(columnInfo));
            fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql);
            if (columnInfo.isPrimary()) {
                String constraintName = null;
                List<FastEntity<?>> keys = getKeys(databaseInfo.getName(), tableInfo.getName());
                if (keys != null) {
                    List<String> keyColumns = new ArrayList<>();
                    for (FastEntity<?> key : keys) {
                        String columnName = key.getString("column_name");
                        constraintName = key.getString("constraint_name");
                        if (columnName.equals(columnInfo.getName())) {
                            return;
                        }
                        keyColumns.add(columnName);
                    }

                    if (FastStringUtils.isNotEmpty(constraintName)) {
                        if (keys.size() > 0) {
                            String sql2 = "alter table " + tableInfo.getName() + " drop constraint " + constraintName;
                            fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql2);
                        }
                        String sql3 = "alter table add constraint " + constraintName + " primary key (" + FastStringUtils.join(keyColumns, ",") + ") ";
                        fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql3);
                    }
                }
            }

            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLog().info(FastSqlServerDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo("Db_Table_Info2", databaseInfo.getName(), tableInfo.getName(), columnInfo.getName()));
        }
    }

    @Override
    public void alterColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception {
        try {
            String sql = String.format("alter table %s alter column %s", tableInfo.getName(), buildColumnSql(columnInfo,true));
            fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql);
            if (columnInfo.isPrimary()) {
                String constraintName = null;
                List<FastEntity<?>> keys = getKeys(databaseInfo.getName(), tableInfo.getName());
                if (keys != null) {
                    List<String> keyColumns = new ArrayList<>();
                    for (FastEntity<?> key : keys) {
                        String columnName = key.getString("column_name");
                        constraintName = key.getString("constraint_name");
                        if (columnName.equals(columnInfo.getName())) {
                            return;
                        }
                        keyColumns.add(columnName);
                    }

                    if (FastStringUtils.isNotEmpty(constraintName)) {
                        if (keys.size() > 0) {
                            String sql2 = "alter table " + tableInfo.getName() + " drop constraint " + constraintName;
                            fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql2);
                        }
                        String sql3 = "alter table add constraint " + constraintName + " primary key (" + FastStringUtils.join(keyColumns, ",") + ") ";
                        fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(sql3);
                    }
                }
            }

            alterColumnIndex(databaseInfo, tableInfo.getName(), columnInfo);
        } finally {
            FastChar.getLog().info(FastSqlServerDatabaseOperateProvider.class,
                    FastChar.getLocal().getInfo("Db_Table_Info3", databaseInfo.getName(),
                            tableInfo.getName(), columnInfo.getName()));
        }
    }

    private void alterColumnIndex(FastDatabaseInfo databaseInfo, String tableName, FastColumnInfo<?> columnInfo) throws Exception {
        String convertIndex = convertIndex(columnInfo);
        if (!convertIndex.equalsIgnoreCase("none")) {
            String columnName = columnInfo.getName();
            String indexName = String.format("%s_%s_Index", tableName, columnName);
            if (!checkColumnIndex(databaseInfo.getName(), indexName)) {
                String createIndexSql = String.format("create %s index %s on %s(%s) ", convertIndex, indexName, tableName, columnName);
                fastDb.setLog(true).setDatabase(databaseInfo.getName()).run(createIndexSql);
                FastChar.getLog().info(FastSqlServerDatabaseOperateProvider.class,
                        FastChar.getLocal().getInfo("Db_Table_Info4", databaseInfo.getName(), tableName, columnInfo.getName(), indexName));
            }
        }
    }


    private boolean checkColumnIndex(String databaseName, String indexName) {
        try {
            String checkIndexSql = String.format("select count(1) as countIndex  from sysindexes where name = '%s'", indexName);
            FastEntity<?> fastEntity = fastDb.setLog(false).setDatabase(databaseName).selectFirst(checkIndexSql);
            if (fastEntity != null) {
                return fastEntity.getInt("countIndex") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<FastEntity<?>> getKeys(String databaseName, String tableName) {
        try {
            String checkKeysSql = String.format("select column_name,constraint_name  from information_schema.key_column_usage where table_name = '%s'" +
                    "  and table_catalog='%s'", tableName, databaseName);
            return fastDb.setLog(false).setDatabase(databaseName)
                    .setIgnoreCase(true)
                    .select(checkKeysSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String convertIndex(FastColumnInfo<?> columnInfo) {
        String index = columnInfo.getIndex();
        if (FastStringUtils.isNotEmpty(index)) {
            String[] indexArray = new String[]{"nonclustered", "clustered"};
            if (index.equalsIgnoreCase("true") || index.equalsIgnoreCase("normal")) {
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

    private String buildColumnSql(FastColumnInfo<?> columnInfo,boolean isModify) {
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
            stringBuilder.append(FastStringUtils.defaultValue(columnInfo.getNullable(), " null "));
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
        } else if (type.equalsIgnoreCase("varchar")) {
            if (FastStringUtils.isEmpty(length)) {
                return "500";
            }
            if (columnInfo.isPrimary()) {
                return "250";
            }
        } else if (FastType.isSqlDateType(type)
                || FastType.isSqlTimeType(type)
                || FastType.isTimestampType(type)) {
            return null;
        }
        return length;
    }

    private String getType(FastColumnInfo<?> columnInfo) {
        return FastType.convertType("sql_server", columnInfo.getType());
    }


}
