package com.fastchar.database.sql;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.enums.FastDatabaseType;
import com.fastchar.exception.FastSqlException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.*;

public class FastMySql extends FastSql {

    public static boolean isOverride(String type) {
        if (FastStringUtils.isNotEmpty(type)) {
            return FastDatabaseType.MYSQL.name().equalsIgnoreCase(type);
        }
        return false;
    }

    public FastMySql() {
        this.type = FastDatabaseType.MYSQL.name().toLowerCase();
    }

    @Override
    public FastSqlInfo buildInsertSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        entity.markSetDefaultValue("insert");
        entity.setDefaultValue();
        entity.unmarkSetDefaultValue();

        Set<String> allKeys = entity.allKeys();
        List<String> columns = new ArrayList<>(allKeys.size());
        List<String> placeholders = new ArrayList<>(allKeys.size());
        List<Object> values = new ArrayList<>(allKeys.size());

        TreeSet<String> treeKeys = new TreeSet<>(allKeys);

        Map<String, Object> keyValue = new LinkedHashMap<>();

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
                keyValue.put(key, columnValue);
            }
        }

        if (values.isEmpty()) {
            return null;
        }

        for (String key : checks) {
            values.add(keyValue.get(key));
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
                boolean exclude = check.charAt(0) == '!';
                if (exclude) {
                    check = check.substring(1);
                }
                sqlStr.append(" and ").append(check).append(exclude ? " != " : " = ").append(" ? ");
            }
            sqlStr.append(")");
            sqlInfo.setSql(sqlStr.toString());
        }
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        return sqlInfo;
    }


    @Override
    public String buildPageSql(String selectSql, int page, int pageSize) {
        if (page > 0) {
            selectSql += " limit " + (page - 1) * pageSize + "," + pageSize;
        }
        return selectSql;
    }

}

