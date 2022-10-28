package com.fastchar.database.sql;


import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.exception.FastSqlException;
import com.fastchar.interfaces.IFastColumnSecurity;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.*;

public abstract class FastSql {

    public static FastSql getInstance(String type) {
        return FastChar.getOverrides().singleInstance(FastSql.class, type);
    }

    protected String type;

    public abstract FastSqlInfo buildInsertSql(FastEntity<?> entity, String... checks);


    public FastSqlInfo buildCopySql(FastEntity<?> entity) {
        if (entity == null) {
            return null;
        }

        FastTableInfo<?> table = entity.getTable();
        if (table == null) {
            return null;
        }
        Collection<FastColumnInfo<?>> tableAllColumns = table.getColumns();
        List<String> columns = new ArrayList<>(tableAllColumns.size());
        List<String> valueColumns = new ArrayList<>(tableAllColumns.size());

        for (FastColumnInfo<?> column : tableAllColumns) {
            if (column.isPrimary() || !column.isEnable()) {
                continue;
            }
            columns.add(column.getName());
            valueColumns.add(column.getName());
        }

        List<Object> values = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ").append(entity.getTableName()).append(" (").append(FastStringUtils.join(columns, ",")).append(") ").append(" select ").append(FastStringUtils.join(valueColumns, ",")).append(" from ").append(entity.getTableName()).append(" where ").append(" 1=1 ");

        for (FastColumnInfo<?> primary : entity.getPrimaries()) {
            if (entity.isEmpty(primary.getName())) {
                throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR4, "'" + primary.getName() + "'"));
            }
            sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
            values.add(getColumnValue(entity, primary));
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo buildDeleteSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        if (checks.length == 0) {
            List<String> primaryList = new ArrayList<>();
            Collection<FastColumnInfo<?>> primaries = entity.getPrimaries();
            if (primaries.isEmpty()) {
                throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO5, entity.getTableName()));
            }
            for (FastColumnInfo<?> primary : primaries) {
                if (entity.isEmpty(primary.getName())) {
                    throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR4, "'" + primary.getName() + "'"));
                }
                primaryList.add(primary.getName());
            }
            checks = primaryList.toArray(new String[]{});
        }
        return buildCheckSql("delete from " + entity.getTableName(), entity, null, checks);
    }

    public FastSqlInfo buildDeleteSqlByIds(FastEntity<?> entity, Object... ids) {
        if (entity == null) {
            return null;
        }
        if (ids.length == 0) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder("delete from " + entity.getTableName() + " where 1=1 ");
        int count = 0;
        Collection<FastColumnInfo<?>> primaries = entity.getPrimaries();
        if (primaries.isEmpty()) {
            throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO5, entity.getTableName()));
        }
        for (FastColumnInfo<?> primary : primaries) {
            sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
            count++;
            if (count >= ids.length) {
                break;
            }
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }

    public FastSqlInfo buildUpdateSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }

        entity.markSetDefaultValue("update");
        entity.setDefaultValue();
        entity.unmarkSetDefaultValue();

        int initialCapacity = entity.getModified().size();
        List<String> columns = new ArrayList<>(initialCapacity);
        List<Object> values = new ArrayList<>(initialCapacity);
        TreeSet<String> treeKeys = new TreeSet<>(entity.getModified());

        for (String key : treeKeys) {
            FastColumnInfo<?> columnInfo = entity.getColumn(key);
            if (columnInfo != null) {
                if (columnInfo.isPrimary()) {
                    continue;
                }
                columns.add(key + " = ? ");
                Object columnValue = getColumnValue(entity, columnInfo);
                values.add(columnValue);
            }
        }
        if (values.size() == 0) {
            return null;
        }


        if (checks.length == 0) {
            List<String> primaryList = new ArrayList<>();
            Collection<FastColumnInfo<?>> primaries = entity.getPrimaries();
            if (primaries.isEmpty()) {
                throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO5, entity.getTableName()));
            }
            for (FastColumnInfo<?> primary : primaries) {
                if (entity.isEmpty(primary.getName())) {
                    throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR4, "'" + primary.getName() + "'"));
                }
                primaryList.add(primary.getName());
            }
            checks = primaryList.toArray(new String[]{});
        }

        return buildCheckSql("update " + entity.getTableName()
                + " set " + FastStringUtils.join(columns, ","), entity, values, checks);

    }

    public FastSqlInfo buildUpdateSqlByIds(FastEntity<?> entity, Object... ids) {
        if (entity == null) {
            return null;
        }
        if (ids.length == 0) {
            return null;
        }

        entity.markSetDefaultValue("update");
        entity.setDefaultValue();
        entity.unmarkSetDefaultValue();

        int initialCapacity = entity.getModified().size();
        List<String> columns = new ArrayList<>(initialCapacity);
        List<Object> values = new ArrayList<>(initialCapacity);
        TreeSet<String> treeKeys = new TreeSet<>(entity.getModified());
        for (String key : treeKeys) {
            FastColumnInfo<?> columnInfo = entity.getColumn(key);
            if (columnInfo != null && !columnInfo.isPrimary()) {
                columns.add(key + " = ? ");
                values.add(getColumnValue(entity, columnInfo));
            }
        }
        if (values.size() == 0) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder("update " + entity.getTableName()
                + " set " + FastStringUtils.join(columns, ",")
                + " where 1=1 ");
        int count = 0;
        Collection<FastColumnInfo<?>> primaries = entity.getPrimaries();
        if (primaries.isEmpty()) {
            throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO5, entity.getTableName()));
        }
        for (FastColumnInfo<?> primary : primaries) {
            sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
            values.add(ids[count]);
            count++;
            if (count >= ids.length) {
                break;
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo buildSelectSqlByIds(FastEntity<?> entity, Object... ids) {
        StringBuilder sqlBuilder = new StringBuilder("select * from " + entity.getTableName() + " where 1=1 ");
        if (ids.length > 0) {
            int count = 0;
            Collection<FastColumnInfo<?>> primaries = entity.getPrimaries();
            if (primaries.isEmpty()) {
                throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_INFO5, entity.getTableName()));
            }
            for (FastColumnInfo<?> primary : primaries) {
                sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
                count++;
                if (count >= ids.length) {
                    break;
                }
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }

    public abstract String buildPageSql(String selectSql, int page, int pageSize);

    public FastSqlInfo buildCountSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        if (checks.length == 0) {
            checks = entity.allKeys().toArray(new String[]{});
        }
        return buildCheckSql("select count(1) as ct from " + entity.getTableName(), entity, null, checks);
    }

    public String getCountSql(String selectSql, String alias) {
        return "select count(1) as " + alias + " from (" + selectSql + ") as temp ";
    }

    protected FastSqlInfo buildCheckSql(String baseSql, FastEntity<?> entity, List<Object> values, String... checks) {
        StringBuilder sqlBuilder = new StringBuilder(baseSql + " where 1=1 ");

        if (values == null) {
            values = new ArrayList<>(checks.length);
        }
        for (String check : checks) {
            boolean exclude = check.charAt(0) == '!';
            if (exclude) {
                check = check.substring(1);
            }
            FastColumnInfo<?> column = entity.getColumn(check);
            if (column != null) {
                sqlBuilder.append(" and ").append(check).append(exclude ? " != " : " = ").append(" ? ");
                values.add(getColumnValue(entity, column));
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setListener(entity.getBoolean("sqlListener", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    protected FastSqlInfo newSqlInfo() {
        return FastChar.getOverrides().newInstance(FastSqlInfo.class).setType(this.type);
    }

    protected Object getAttrValue(FastEntity<?> entity, String attr) {
        Object value = entity.get(attr);
        if (value == null) {
            return null;
        }
        if ("<null>".equalsIgnoreCase(value.toString())) {
            return null;
        }
        return value;
    }

    public Object getColumnValue(FastEntity<?> entity, FastColumnInfo<?> columnInfo) {
        Object value = getAttrValue(entity, columnInfo.getName());
        if (value == null) {
            return null;
        }
        if (FastStringUtils.isNotEmpty(columnInfo.getEncrypt())) {
            value = FastChar.getOverrides()
                    .singleInstance(IFastColumnSecurity.class)
                    .encrypt(columnInfo, String.valueOf(value));
        }
        return value;
    }

    public String getType() {
        return type;
    }


}
