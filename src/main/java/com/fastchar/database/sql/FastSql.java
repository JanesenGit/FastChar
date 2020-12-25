package com.fastchar.database.sql;


import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.exception.FastSqlException;
import com.fastchar.interfaces.IFastColumnSecurity;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        List<String> columns = new ArrayList<>();
        List<String> valueColumns = new ArrayList<>();
        FastTableInfo<?> table = entity.getTable();
        if (table == null) {
            return null;
        }
        List<FastColumnInfo<?>> tableColumns = table.getColumns();
        for (FastColumnInfo<?> column : tableColumns) {
            if (column.isPrimary()) {
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
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo buildDeleteSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("delete from " + entity.getTableName() + " where 1=1 ");


        List<String> checkColumns = new ArrayList<>();
        for (String key : checks) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                checkColumns.add(key);
            }
        }

        if (checkColumns.size() == 0) {
            for (FastColumnInfo<?> primary : entity.getPrimaries()) {
                if (entity.isEmpty(primary.getName())) {
                    throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR4, "'" + primary.getName() + "'"));
                }
                sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
                values.add(getColumnValue(entity, primary));
            }
        } else {
            for (String check : checkColumns) {
                FastColumnInfo<?> column = entity.getColumn(check);
                if (column != null) {
                    sqlBuilder.append(" and ").append(check).append(" = ? ");
                    values.add(getColumnValue(entity, column));
                }
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
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
        for (FastColumnInfo<?> primary : entity.getPrimaries()) {
            sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
            count++;
            if (count >= ids.length) {
                break;
            }
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }

    public FastSqlInfo buildUpdateSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        entity.markSetDefaultValue("update");
        entity.setDefaultValue();
        entity.unmarkSetDefaultValue();

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
        StringBuilder sqlBuilder = new StringBuilder("update " + entity.getTableName()
                + " set " + FastStringUtils.join(columns, ",")
                + " where 1=1 ");


        List<String> checkColumns = new ArrayList<>();
        for (String key : checks) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                checkColumns.add(key);
            }
        }

        if (checkColumns.size() == 0) {
            for (FastColumnInfo<?> primary : entity.getPrimaries()) {
                if (entity.isEmpty(primary.getName())) {
                    throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR4, "'" + primary.getName() + "'"));
                }
                sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
                values.add(getColumnValue(entity, primary));
            }
        } else {
            for (String check : checkColumns) {
                FastColumnInfo<?> column = entity.getColumn(check);
                if (column != null) {
                    sqlBuilder.append(" and ").append(check).append(" = ? ");
                    values.add(getColumnValue(entity, column));
                }
            }
        }


        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo buildUpdateSqlByIds(FastEntity<?> entity, Object... ids) {
        if (entity == null) {
            return null;
        }
        if (ids.length == 0) {
            return null;
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
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
        for (FastColumnInfo<?> primary : entity.getPrimaries()) {
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
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo buildSelectSqlByIds(FastEntity<?> entity, Object... ids) {
        if (ids.length == 0) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder("select * from " + entity.getTableName() + " where 1=1 ");
        int count = 0;
        for (FastColumnInfo<?> primary : entity.getPrimaries()) {
            sqlBuilder.append(" and ").append(primary.getName()).append(" = ? ");
            count++;
            if (count >= ids.length) {
                break;
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }

    public FastSqlInfo buildSelectSql(String selectSql, FastEntity<?> entity) {
        return appendWhere(selectSql, entity);
    }

    public FastSqlInfo buildSelectSql(FastEntity<?> entity) {
        return buildSelectSql("select * from " + entity.getTableName(), entity);
    }

    public abstract String buildPageSql(String selectSql, int page, int pageSize);

    public FastSqlInfo buildCountSql(FastEntity<?> entity, String... checks) {
        if (entity == null) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("select count(1) as ct from " + entity.getTableName() + " where 1=1 ");

        List<String> checkColumns = new ArrayList<>();
        for (String key : checks) {
            FastColumnInfo<?> column = entity.getColumn(key);
            if (column != null) {
                checkColumns.add(key);
            }
        }


        if (checkColumns.size() > 0) {
            for (String check : checkColumns) {
                FastColumnInfo<?> column = entity.getColumn(check);
                if (column != null) {
                    sqlBuilder.append(" and ").append(check).append(" = ? ");
                    values.add(getColumnValue(entity, column));
                }
            }
        } else {
            for (String check : entity.allKeys()) {
                FastColumnInfo<?> column = entity.getColumn(check);
                if (column != null) {
                    sqlBuilder.append(" and ").append(check).append(" = ? ");
                    values.add(getColumnValue(entity, column));
                }
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlBuilder.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }


    public String getCountSql(String selectSql, String alias) {
        int[] orderIndex = getTokenIndex("order by", selectSql);
        if (orderIndex[0] > 0) {
            String orderSql = selectSql.substring(orderIndex[0], orderIndex[1] + orderIndex[0]);
            selectSql = selectSql.replace(orderSql, "");
        }

        int startIndex = selectSql.toLowerCase().indexOf("select") + 6;
        int endIndex = selectSql.toLowerCase().indexOf("from");
        if (endIndex > 0) {
            String select = selectSql.substring(startIndex, endIndex);
            return selectSql.replace(select, " count(1) as " + alias + " ");
        }
        throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.DB_SQL_ERROR2));
    }

    protected FastSqlInfo newSqlInfo() {
        return FastChar.getOverrides().newInstance(FastSqlInfo.class).setType(this.type);
    }

    protected Object getColumnValue(FastEntity<?> entity, FastColumnInfo<?> columnInfo) {
        Object value = entity.get(columnInfo.getName());
        if (value == null) {
            return null;
        }
        if ("<null>".equalsIgnoreCase(value.toString())) {
            return null;
        }
        if (FastStringUtils.isNotEmpty(columnInfo.getEncrypt())) {
            value = FastChar.getOverrides()
                    .singleInstance(IFastColumnSecurity.class)
                    .encrypt(columnInfo, String.valueOf(value));
        }
        return value;
    }

    protected String convertInPlaceholder(Object value, List<Object> values) {
        List<String> placeholders = new ArrayList<String>();
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                values.add(Array.get(value, i));
                placeholders.add("?");
            }
        } else if (value instanceof Collection<?>) {
            Collection<?> list = (Collection<?>) value;
            for (Object object : list) {
                values.add(object);
                placeholders.add("?");
            }
        } else if (value.toString().contains(",")) {
            String[] arrays = value.toString().split(",");
            for (String string : arrays) {
                values.add(string);
                placeholders.add("?");
            }
        } else {
            values.add(value);
            placeholders.add("?");
        }
        return FastStringUtils.join(placeholders, ",");
    }

    protected String getAlias(String sql) {
        int[] fromPosition = getTokenIndex("from", sql);
        if (fromPosition.length > 0 && fromPosition[0] > 0) {
            String fromSql = sql.substring(fromPosition[0]);
            return getTokenValue("as", fromSql);
        }
        return null;
    }

    protected int[] getTokenIndex(String token, String sql, String... endToken) {
        String[] tokens = token.split(" ");
        int tokenIndex = 0;
        int[] position = new int[]{-1, -1};
        StringBuilder stringBuilder = new StringBuilder();
        int beginGroupCharCount = 0, endGroupCharCount = 0;
        for (int i = 0; i < sql.length(); i++) {
            char chr = sql.charAt(i);
            if (chr == ' ') {
                if (tokenIndex < tokens.length) {
                    if (stringBuilder.toString().equalsIgnoreCase(tokens[tokenIndex])) {
                        if (position[0] == -1) {
                            position[0] = i - 1 - tokens[tokenIndex].length();
                        }
                        tokenIndex++;
                    }
                }
                if (!FastArrayUtils.contains(endToken, stringBuilder.toString().toLowerCase().trim())) {
                    stringBuilder.delete(0, stringBuilder.length());
                    continue;
                }
            }
            if (chr == '(') {
                beginGroupCharCount++;
            } else if (chr == ')') {
                endGroupCharCount++;
            }
            if (beginGroupCharCount != endGroupCharCount) {
                stringBuilder.delete(0, stringBuilder.length());
                continue;
            }
            stringBuilder.append(sql.charAt(i));

            if (FastArrayUtils.contains(endToken, stringBuilder.toString().toLowerCase())) {
                position[1] = i - position[0] - stringBuilder.length();
                break;
            }
        }
        if (position[1] == -1) {
            position[1] = sql.length() - position[0];
        }
        return position;
    }

    //从主sql中获取token的位置
    protected String getTokenValue(String token, String sql) {
        StringBuilder stringBuilder = new StringBuilder();
        int beginGroupChar = 0, endGroupChar = 0;

        boolean hasToken = false;
        for (int i = 0; i < sql.length(); i++) {
            char chr = sql.charAt(i);
            if (chr == ' ') {
                if (hasToken && stringBuilder.length() > 0) {
                    return stringBuilder.toString();
                }
                stringBuilder.delete(0, stringBuilder.length());
                continue;
            }
            if (chr == '(') {
                beginGroupChar++;
            } else if (chr == ')') {
                endGroupChar++;
            }
            if (beginGroupChar != endGroupChar) {
                stringBuilder.delete(0, stringBuilder.length());
                continue;
            }
            stringBuilder.append(sql.charAt(i));
            if (stringBuilder.toString().equalsIgnoreCase(token)) {
                stringBuilder.delete(0, stringBuilder.length());
                hasToken = true;
            }
        }
        return null;
    }


    /**
     * 条件属性 转为 sql语句
     * <p>
     * 条件属性格式：
     * <br/>
     * 分组符号+连接符号+属性名+比较符号（例如：&name?% 翻译后为：and name like '值%' ）
     * </p>
     * <p>
     * 分组符号 $[0-9]  翻译成sql ()
     * </p>
     * 连接符号：&  翻译成sql  and
     * <br/>
     * 连接符号：@  翻译成sql  and
     * <br/>
     * 连接符号：|| 翻译成sql  or
     * <br/>
     * 比较符号：?  翻译成sql  like
     * <br/>
     * 比较符号：!? 翻译成sql  not like
     * <br/>
     * 比较符号：#  翻译成sql  in
     * <br/>
     * 比较符号：!# 翻译成sql  not in
     * <br/>
     * 前缀符号：__ 翻译成sql  . (别名前缀，例如：a__name 翻译后为：a.name)
     * </p>
     * <p>
     * 以下特性被忽略转换：
     * 以^符号开头的属性 （例如：^test ）
     * </p>
     */
    public FastSqlInfo appendWhere(String sqlStr, FastEntity<?> entity) {
        sqlStr = sqlStr.trim();
        String regStr = "([$]?[0-9]+)?(&|@|\\|{2})?([_a-zA-Z0-9.]*)([?!#><=%]+)?([:sort]+)?";
        Pattern compile = Pattern.compile(regStr);

        FastSqlInfo sqlInfo = newSqlInfo();
        TreeSet<String> keys = new TreeSet<>(entity.allKeys());
        LinkedHashMap<String, String> sorts = new LinkedHashMap<>();
        StringBuilder whereBuilder = new StringBuilder(" ");
        String alias = getAlias(sqlStr);
        if (FastStringUtils.isEmpty(alias)) {
            alias = "";
        } else {
            alias = alias + ".";
        }

        String lastGroupKey = "";
        for (String whereAttr : keys) {
            if (whereAttr.startsWith("^")) {
                continue;
            }
            String link = "and";
            String attr = alias + whereAttr;
            Object value = entity.get(whereAttr);

            if (value == null || FastStringUtils.isEmpty(value.toString())) {
                continue;
            }

            String before_1 = "";
            String before_2 = "";
            String compare = "=";
            String placeholder = "?";
            Matcher matcher = compile.matcher(whereAttr);
            if (matcher.find()) {
                String groupKey = FastStringUtils.defaultValue(matcher.group(1), "");
                link = FastStringUtils.defaultValue(matcher.group(2), "and");
                attr = FastStringUtils.defaultValue(matcher.group(3), whereAttr);
                compare = FastStringUtils.defaultValue(matcher.group(4), "=");
                String rank = matcher.group(5);// :sort
                if (FastStringUtils.isNotEmpty(rank)) {
                    if (":sort".equalsIgnoreCase(rank)) {
                        sorts.put(attr, String.valueOf(value).toLowerCase());
                        continue;
                    }
                }
                if (FastStringUtils.isEmpty(compare)) {
                    continue;
                }

                attr = attr.replace("__", ".");
                FastColumnInfo<?> column;
                if (attr.contains(".")) {
                    column = entity.getColumn(attr.split("\\.")[1]);
                } else {
                    column = entity.getColumn(attr);
                    attr = alias + attr;
                }
                if (column != null) {
                    Object convertValue = getColumnValue(entity, column);
                    if (convertValue != null) {
                        value = convertValue;
                    }
                }

                if (FastStringUtils.isNotEmpty(link)) {
                    if ("||".equals(link)) {
                        link = "or";
                    } else {
                        link = "and";
                    }
                } else {
                    link = "and";
                }

                if (groupKey.startsWith("$")) {
                    if (FastStringUtils.isEmpty(lastGroupKey)) {
                        if (FastStringUtils.isNotEmpty(groupKey)) {
                            before_2 = " ( ";
                            lastGroupKey = groupKey;
                        }
                    } else if (!lastGroupKey.equalsIgnoreCase(groupKey)) {
                        before_1 = " ) ";
                        if (FastStringUtils.isNotEmpty(groupKey)) {
                            before_2 = " ( ";
                            lastGroupKey = groupKey;
                        } else {
                            lastGroupKey = "";
                        }
                    }
                }

                switch (compare) {
                    case "?":
                        compare = "like";
                        break;
                    case "?%":
                        compare = "like";
                        value = value + "%";
                        break;
                    case "%?":
                        compare = "like";
                        value = "%" + value;
                        break;
                    case "%?%":
                        compare = "like";
                        value = "%" + value + "%";
                        break;
                    case "!?":
                        compare = "not like";
                        break;
                    case "!?%":
                        compare = "not like";
                        value = value + "%";
                        break;
                    case "%!?":
                        compare = "not like";
                        value = "%" + value;
                        break;
                    case "%!?%":
                        compare = "not like";
                        value = "%" + value + "%";
                        break;
                    case "#":
                        compare = "in";
                        placeholder = "(" + convertInPlaceholder(value, sqlInfo.getParams()) + ")";
                        break;
                    case "!#":
                        compare = "not in";
                        placeholder = "(" + convertInPlaceholder(value, sqlInfo.getParams()) + ")";
                        break;
                }
            }

            whereBuilder
                    .append(before_1)
                    .append(" ")
                    .append(link)
                    .append(" ")
                    .append(before_2)
                    .append(" ")
                    .append(attr)
                    .append(" ")
                    .append(compare)
                    .append(" ")
                    .append(placeholder)
                    .append(" ");

            if ("?".equals(placeholder)) {
                sqlInfo.getParams().add(value);
            }
        }
        if (FastStringUtils.isNotEmpty(lastGroupKey)) {
            whereBuilder.append(" ) ");
        }

        List<String> sortBuilder = new ArrayList<>();
        for (String s : sorts.keySet()) {
            sortBuilder.add(s + " " + sorts.get(s));
        }

        int[] wherePosition = getTokenIndex("where", sqlStr, "group", "order", "having", "union");
        if (wherePosition[0] == -1) {
            whereBuilder.insert(0, " where 1=1 ");
        }
        sqlStr = FastStringUtils.insertString(sqlStr,
                wherePosition[0] + wherePosition[1],
                whereBuilder.toString());

        if (sortBuilder.size() > 0) {
            int[] orderPosition = getTokenIndex("order by", sqlStr);
            if (orderPosition[0] == -1) {
                sqlStr += " order by " + FastStringUtils.join(sortBuilder, ",");
            } else {
                sqlStr = FastStringUtils.insertString(sqlStr,
                        orderPosition[0] + orderPosition[1],
                        "," + FastStringUtils.join(sortBuilder, ","));
            }
        }
        sqlInfo.setSql(sqlStr);
        return sqlInfo;
    }

    public String getType() {
        return type;
    }

}
