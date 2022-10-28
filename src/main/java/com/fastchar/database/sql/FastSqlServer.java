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

public class FastSqlServer extends FastSql {

    public static boolean isOverride(String type) {
        if (FastStringUtils.isNotEmpty(type)) {
            return FastDatabaseType.SQL_SERVER.name().equalsIgnoreCase(type);
        }
        return false;
    }


    public FastSqlServer() {
        this.type = FastDatabaseType.SQL_SERVER.name().toLowerCase();
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
        if (values.size() == 0) {
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
            StringBuilder builder = new StringBuilder("if not exits (select * from " + entity.getTableName()
                    + " where 1=1 ");
            for (String check : checks) {
                boolean exclude = check.charAt(0) == '!';
                if (exclude) {
                    check = check.substring(1);
                }
                builder.append(" and ").append(check).append(exclude ? " != " : " = ").append(" ? ");
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
