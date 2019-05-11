package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.exception.FastSqlException;

import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastSql<T> {
    private String type;//数据库类型

    public static FastSql<?> newInstance(String type) {
        FastSql<?> fastSql = FastChar.getOverrides().newInstance(FastSql.class);
        return (FastSql<?>) fastSql.setType(type.toLowerCase());
    }

    protected FastSqlInfo newSqlInfo() {
        return new FastSqlInfo().setType(this.type);
    }


    public String getType() {
        return type;
    }

    public T setType(String type) {
        this.type = type;
        return (T) this;
    }

    /**
     * 获得统计的sql语句
     */
    public String getCountSql(String sqlStr, String alias) {
        int[] orderIndex = getTokenIndex("order by", sqlStr);
        if (orderIndex[0] > 0) {
            String orderSql = sqlStr.substring(orderIndex[0], orderIndex[1] + orderIndex[0]);
            sqlStr = sqlStr.replace(orderSql, "");
        }

        int startIndex = sqlStr.toLowerCase().indexOf("select") + 6;
        int endIndex = sqlStr.toLowerCase().indexOf("from");
        if (endIndex > 0) {
            String select = sqlStr.substring(startIndex, endIndex);
            return sqlStr.replace(select, " count(1) as " + alias + " ");
        }
        throw new FastSqlException(FastChar.getLocal().getInfo("Db_Sql_Error2"));
    }

    /**
     * 获得分页的sql
     */
    public String getPageSql(String sqlStr, int page, int pageSize) {
        if (page > 0) {
            if (type.toLowerCase().equals("mysql")) {
                sqlStr += " limit " + (page - 1) * pageSize + "," + pageSize;
            } else if (type.toLowerCase().equals("sql_server")) {//后续跟进

            } else if (type.toLowerCase().equals("oracle")) {//后续跟进

            }
        }
        return sqlStr;
    }

    protected Object getColumnValue(FastEntity<?> entity, FastColumnInfo columnInfo) {
        Object value = entity.get(columnInfo.getName());
        if (value == null) {
            return null;
        }
        if (value.toString().equalsIgnoreCase("<null>")) {
            return null;
        }
        if (FastStringUtils.isNotEmpty(columnInfo.getEncrypt())) {
            if (columnInfo.getEncrypt().equalsIgnoreCase("md5")) {
                value = FastChar.getSecurity().MD5_Encrypt(String.valueOf(value));
            } else if (columnInfo.getEncrypt().equalsIgnoreCase("true")) {
                value = FastChar.getSecurity().AES_Encrypt(String.valueOf(value));
            }
        }
        return value;
    }

    public FastSqlInfo toSelectSql(String sqlStr, FastEntity<?> entity) {
        return convertSqlName(sqlStr, entity);
    }

    public FastSqlInfo toSelectSql(FastEntity<?> entity) {
        return toSelectSql("select * from " + entity.getTableName(), entity);
    }

    public FastSqlInfo toSelectSql(FastEntity<?> entity, Object... ids) {
        if (ids.length == 0) {
            return null;
        }
        StringBuilder sqlStr = new StringBuilder("select * from " + entity.getTableName() + " where 1=1 ");
        int count = 0;
        for (FastColumnInfo primary : entity.getPrimaries()) {
            sqlStr.append(" and ").append(primary.getName()).append(" = ? ");
            count++;
            if (count >= ids.length) {
                break;
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlStr.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }

    public FastSqlInfo toInsertSql(FastEntity<?> entity) {
        if (entity == null) {
            return null;
        }
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        entity.setDefaultValue();

        TreeSet<String> treeKeys = new TreeSet<>(entity.allKeys());
        for (String key : treeKeys) {
            FastColumnInfo column = entity.getColumn(key);
            if (column != null) {
                Object columnValue = getColumnValue(entity, column);
                if (column.isNotNull() && columnValue == null) {
                    throw new FastSqlException(FastChar.getLocal().getInfo("Db_Sql_Error3", "'" + column.getName() + "'"));
                }
                columns.add(key);
                values.add(columnValue);
                placeholders.add("?");
            }
        }
        if (values.size() == 0) {
            return null;
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        String sqlStr = "insert into " + entity.getTableName()
                + " (" + FastStringUtils.join(columns, ",") + ") values" +
                " (" + FastStringUtils.join(placeholders, ",") + ") ";
        sqlInfo.setSql(sqlStr);
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }


    public FastSqlInfo toUpdateSql(FastEntity<?> entity) {
        if (entity == null) {
            return null;
        }
        entity.setDefaultValue();
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        TreeSet<String> treeKeys = new TreeSet<>(entity.getModified());
        for (String key : treeKeys) {
            FastColumnInfo columnInfo = entity.getColumn(key);
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
        entity.getModified().clear();
        StringBuilder sqlStr = new StringBuilder("update " + entity.getTableName()
                + " set " + FastStringUtils.join(columns, ",")
                + " where 1=1 ");

        for (FastColumnInfo primary : entity.getPrimaries()) {
            if (entity.isEmpty(primary.getName())) {
                throw new FastSqlException(FastChar.getLocal().getInfo("Db_Sql_Error4", "'" + primary.getName() + "'"));
            }
            sqlStr.append(" and ").append(primary.getName()).append(" = ? ");
            values.add(getColumnValue(entity, primary));
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlStr.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }

    public FastSqlInfo toUpdateSql(FastEntity<?> entity, Object... ids) {
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
            FastColumnInfo columnInfo = entity.getColumn(key);
            if (columnInfo != null && !columnInfo.isPrimary()) {
                columns.add(key + " = ? ");
                values.add(getColumnValue(entity, columnInfo));
            }
        }
        if (values.size() == 0) {
            return null;
        }
        entity.getModified().clear();
        StringBuilder sqlStr = new StringBuilder("update " + entity.getTableName()
                + " set " + FastStringUtils.join(columns, ",")
                + " where 1=1 ");
        int count = 0;
        for (FastColumnInfo primary : entity.getPrimaries()) {
            sqlStr.append(" and ").append(primary.getName()).append(" = ? ");
            values.add(ids[count]);
            count++;
            if (count >= ids.length) {
                break;
            }
        }

        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlStr.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }


    public FastSqlInfo toDeleteSql(FastEntity<?> entity) {
        if (entity == null) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        StringBuilder sqlStr = new StringBuilder("delete from " + entity.getTableName() + " where 1=1 ");
        for (FastColumnInfo primary : entity.getPrimaries()) {
            sqlStr.append(" and ").append(primary.getName()).append(" = ? ");
            if (entity.isEmpty(primary.getName())) {
                throw new FastSqlException(FastChar.getLocal().getInfo("Db_Sql_Error4", "'" + primary.getName() + "'"));
            }
            values.add(getColumnValue(entity, primary));
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlStr.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(values);
        return sqlInfo;
    }


    public FastSqlInfo toDeleteSql(FastEntity<?> entity, Object... ids) {
        if (entity == null) {
            return null;
        }
        if (ids.length == 0) {
            return null;
        }
        StringBuilder sqlStr = new StringBuilder("delete from " + entity.getTableName() + " where 1=1 ");
        int count = 0;
        for (FastColumnInfo primary : entity.getPrimaries()) {
            sqlStr.append(" and ").append(primary.getName()).append(" = ? ");
            count++;
            if (count >= ids.length) {
                break;
            }
        }
        FastSqlInfo sqlInfo = newSqlInfo();
        sqlInfo.setSql(sqlStr.toString());
        sqlInfo.setLog(entity.getBoolean("log", true));
        sqlInfo.setParams(Arrays.asList(ids));
        return sqlInfo;
    }


    /**
     * 条件属性 转为 sql语句
     * <p>
     * 条件属性格式：
     * 连接符号+属性名+比较符（例如：&name?% 翻译后为：and name like '值%' ）
     * <p>
     * 连接符号：&  翻译成sql  and
     * 连接符号：@  翻译成sql  and
     * 连接符号：|| 翻译成sql  or
     * 连接符号：?  翻译成sql  like
     * 连接符号：!? 翻译成sql  not like
     * 连接符号：#  翻译成sql  in
     * 连接符号：!# 翻译成sql  not in
     * <p>
     * 前缀符号：__ 翻译成sql  . (别名前缀，例如：a__name 翻译后为：a.name)
     * <p>
     * 以下特性被忽略：
     * 以^符号开头的属性 （例如：^test ）
     *
     * @param entity
     * @return
     */
    public FastSqlInfo convertSqlName(String sqlStr, FastEntity<?> entity) {
        sqlStr = sqlStr.trim();
        String regStr = "([0-9]+)?(&{1}|@{1}|\\|{2})?([_a-zA-Z0-9.]*)([?!#><=%]+)?([:sort]+)?";
        Pattern compile = Pattern.compile(regStr);

        FastSqlInfo sqlInfo = newSqlInfo();

        TreeSet<String> keys = new TreeSet<>(entity.keySet());
        LinkedHashMap<String, String> sorts = new LinkedHashMap<>();
        StringBuilder whereBuilder = new StringBuilder(" ");
        String alias = getAlias(sqlStr);
        if (FastStringUtils.isEmpty(alias)) {
            alias = "";
        } else {
            alias = alias + ".";
        }
        for (String whereAttr : keys) {
            if (whereAttr.startsWith("^")) {
                continue;
            }
            String where = "and";
            String attr = alias + whereAttr;
            Object value = entity.get(whereAttr);

            if (value == null || FastStringUtils.isEmpty(value.toString())) {
                continue;
            }

            String compare = "=";
            String placeholder = "?";
            Matcher matcher = compile.matcher(whereAttr);
            if (matcher.find()) {
                where = FastStringUtils.defaultValue(matcher.group(2), "and");
                attr = FastStringUtils.defaultValue(matcher.group(3), whereAttr);
                compare = FastStringUtils.defaultValue(matcher.group(4), "=");
                String rank = matcher.group(5);// :sort
                if (FastStringUtils.isNotEmpty(rank)) {
                    if (rank.equalsIgnoreCase(":sort")) {
                        sorts.put(attr, String.valueOf(value).toLowerCase());
                        continue;
                    }
                }
                if (FastStringUtils.isEmpty(compare)) {
                    continue;
                }

                attr = attr.replace("__", ".");
                FastColumnInfo column;
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

                if (FastStringUtils.isNotEmpty(where)) {
                    if (where.equals("||")) {
                        where = "or";
                    } else {
                        where = "and";
                    }
                } else {
                    where = "and";
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

            whereBuilder.append(where)
                    .append(" ")
                    .append(attr)
                    .append(" ")
                    .append(compare)
                    .append(" ")
                    .append(placeholder)
                    .append(" ");

            if (placeholder.equals("?")) {
                sqlInfo.getParams().add(value);
            }
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

    private String convertInPlaceholder(Object value, List<Object> values) {
        List<String> placeholders = new ArrayList<String>();
        if (value instanceof Object[]) {
            Object[] ps = (Object[]) value;
            for (Object p : ps) {
                values.add(p);
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



    private String getAlias(String sql) {
        int[] fromPosition = getTokenIndex("from", sql);
        String fromSql = sql.substring(fromPosition[0]);
        return getTokenValue("as", fromSql);
    }


    private int[] getTokenIndex(String token, String sql, String... endToken) {
        String[] tokens = token.split(" ");
        int tokenIndex = 0;
        int[] position = new int[]{-1, -1};
        StringBuilder stringBuilder = new StringBuilder();
        int beginGroupChar = 0, endGroupChar = 0;
        for (int i = 0; i < sql.length(); i++) {
            char chr = sql.charAt(i);
            if (chr == ' ') {
                if (!FastArrayUtils.contains(endToken, stringBuilder.toString().toLowerCase().trim())) {
                    stringBuilder.delete(0, stringBuilder.length());
                    continue;
                }
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
            if (tokenIndex < tokens.length) {
                if (stringBuilder.toString().equalsIgnoreCase(tokens[tokenIndex])) {
                    if (position[0] == -1) {
                        position[0] = i - tokens[tokenIndex].length();
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    tokenIndex++;
                }
            }
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

    private String getTokenValue(String token, String sql) {
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


//    public static void main(String[] args) {
////        String sql = "select 'a',avg(1) from test left join (select * from area where 1=1 group by test order by name asc )" +
////                " order by name asc";
////        String sql = " select *  from  test  as  tma   group by test order by name asc";
////
////        int[] fromPosition = new FastSql().getTokenIndex("from", sql);
////        String fromSql = sql.substring(fromPosition[0]);
////        System.out.println("as:" + new FastSql().getTokenValue("as", fromSql));
//
//
//        //
////        LinkedHashMap<String, Object> whereData = new LinkedHashMap<>();
////        whereData.put("a__name%?%", "janesen");
////        whereData.put("@city!#", "合肥,上海,huain");
////        whereData.put("||age!=", 12);
////        whereData.put("area", "包河");
////        whereData.put("area:sort", "desc");
//////
////        FastEntity<?> entity = new FastEntity<FastEntity>() {
////            @Override
////            public String getTableName() {
////                return "NONE";
////            }
////        };
////        entity.putAll(whereData);
////
////        FastSqlInfo sqlInfo = FastSql.newInstance("mysql").convertSqlName(sql, entity);
////        System.out.println(sqlInfo.getSql());
////        System.out.println(Arrays.toString(sqlInfo.toParams()));
//
//
////        String regStr = "([0-9]+)?(&{1}|@{1}|\\|{2})?([_a-zA-Z0-9]*)([?!#><=%]+)?([:sort]+)?";
////        Pattern compile = Pattern.compile(regStr);
////        Matcher matcher = compile.matcher("a__city:sort");
////        if (matcher.find()) {
////            for (int i = 0; i < matcher.groupCount(); i++) {
////                System.out.println("group_" + (i + 1) + ":" + matcher.group(i + 1));
////            }
////        }
//
//
////
////
////        int where = getTokenIndex("where", sql);
////        System.out.println(sql.substring(where));
//
//    }


}
