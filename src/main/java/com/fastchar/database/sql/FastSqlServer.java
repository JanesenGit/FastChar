package com.fastchar.database.sql;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.exception.FastSqlException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FastSqlServer extends FastSql {

    public static boolean isOverride(String type) {
        if (FastStringUtils.isNotEmpty(type)) {
            return "sql_server".equalsIgnoreCase(type);
        }
        return false;
    }


    public FastSqlServer() {
        this.type = "sql_server";
    }

    @Override
    public FastSqlInfo buildInsertSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        entity.markSetDefaultValue("insert");
        entity.setDefaultValue();
        entity.unmarkSetDefaultValue();

        TreeSet<String> treeKeys = new TreeSet<>(entity.allKeys());
        for (String key : treeKeys) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                Object columnValue = getColumnValue(entity, column);
                if (column.isNotNull() && columnValue == null) {
                    throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR3, "'" + column.getName() + "'"));
                }
                columns.add(key);
                values.add(columnValue);
                placeholders.add("?");
            }
        }
        if (values.size() == 0) {
            return null;
        }
        List<Object> checkValues = new ArrayList<>();
        for (String key : checks) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                Object columnValue = getColumnValue(entity, column);
                checkValues.add(columnValue);
            }
        }
        values.addAll(0, checkValues);

        FastSqlInfo sqlInfo = newSqlInfo();
        if (checks.length == 0) {
            String sqlStr = "insert into " + entity.getTableName()
                    + " (" + FastStringUtils.join(columns, ",") + ") values" +
                    " (" + FastStringUtils.join(placeholders, ",") + ") ";
            sqlInfo.setSql(sqlStr);
        } else {
            StringBuilder builder = new StringBuilder("if not exits (select * from " + entity.getTableName()
                    + " where 1=1 ");
            for (String check : checks) {
                builder.append(" and ").append(check).append(" = ? ");
            }
            builder.append(") begin ");

            String insertSql = "insert into " + entity.getTableName()
                    + " (" + FastStringUtils.join(columns, ",") + ") values" +
                    " (" + FastStringUtils.join(placeholders, ",") + ") ";

            builder.append(insertSql).append(" end");

            sqlInfo.setSql(builder.toString());
        }
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        return sqlInfo;
    }


    @Override
    public String buildPageSql(String selectSql, int page, int pageSize) {
        if (page > 0) {
            selectSql = selectSql.replaceFirst("select", "select top " + page * pageSize + " _temp=0,");
            selectSql = "select * from (select row_number() over(order by _temp ) as row,*" +
                    " from (" + selectSql + ") as me ) as my where row > " + (page - 1) * pageSize;
        }
        return selectSql;
    }
}
