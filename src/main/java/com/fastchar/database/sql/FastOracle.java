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

public class FastOracle extends FastSql {

    public static boolean isOverride(String type) {
        if (FastStringUtils.isNotEmpty(type)) {
            return "oracle".equalsIgnoreCase(type);
        }
        return false;
    }

    public FastOracle() {
        this.type = "oracle";
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
        for (String key : checks) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                Object columnValue = getColumnValue(entity, column);
                values.add(columnValue);
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        if (checks.length == 0) {
            String sqlStr = "insert into " + entity.getTableName()
                    + " (" + FastStringUtils.join(columns, ",") + ") values" +
                    " (" + FastStringUtils.join(placeholders, ",") + ") ";
            sqlInfo.setSql(sqlStr);
        } else {
            StringBuilder sqlStr = new StringBuilder("insert into " + entity.getTableName()
                    + " (" + FastStringUtils.join(columns, ",") + ") select " +
                    FastStringUtils.join(placeholders, ",") + " from dual where not exists " +
                    " (select " + FastStringUtils.join(checks, ",") + " from " + entity.getTableName() + " where 1=1 ");

            for (String check : checks) {
                sqlStr.append(" and ").append(check).append(" = ? ");
            }
            sqlStr.append(")");
            sqlInfo.setSql(sqlStr.toString());
        }
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }


    @Override
    public String buildPageSql(String selectSql, int page, int pageSize) {
        if (page > 0) {
            selectSql = "select * from (select rownum AS _rownum,* from (" + selectSql + ") where _rownum <=" + page * pageSize + ") as me where me._rownum>" + (page - 1) * pageSize;
        }
        return selectSql;
    }
}
