package com.fastchar.database.info;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.enums.FastDatabaseType;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastSqlInfo extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1881604396324620713L;

    private static final Pattern SQL_PATTERN = Pattern.compile("([ ,()\"']+\\?)");

    protected transient FastMapWrap mapWrap;

    public static FastSqlInfo newInstance() {
        return FastChar.getOverrides().newInstance(FastSqlInfo.class);
    }

    public FastSqlInfo() {
        super(16);
        mapWrap = FastMapWrap.newInstance(this);
    }

    public Object[] toParams() {
        return getParams().toArray(new Object[]{});
    }

    public String getType() {
        return mapWrap.getString("type");
    }

    public FastSqlInfo setType(String type) {
        put("type", type);
        return this;
    }

    public String getSql() {
        String sql = mapWrap.getString("sql");
        if (FastStringUtils.isNotEmpty(getType())
                && getType().equalsIgnoreCase(FastDatabaseType.ORACLE.name())
                && FastStringUtils.isNotEmpty(sql)) {
            sql = sql.replace(" as ", " ");
        }
        return sql;
    }

    public FastSqlInfo setSql(String sql) {
        put("sql", sql);
        return this;
    }

    public List<Object> getParams() {
        List<Object> params = mapWrap.getObject("params");
        if (params == null) {
            params = new ArrayList<>();
            put("params", params);
        }
        return params;
    }

    public FastSqlInfo setParams(List<Object> params) {
        put("params", params);
        return this;
    }

    public FastSqlInfo setParams(Object... params) {
        setParams(Arrays.asList(params));
        return this;
    }


    public List<FastSqlInfo> getChildren() {
        List<FastSqlInfo> children = mapWrap.getObject("children");
        if (children == null) {
            children = new ArrayList<>();
            put("children", children);
        }
        return children;
    }

    public FastSqlInfo setChildren(List<FastSqlInfo> children) {
        put("children", children);
        return this;
    }

    public boolean isLog() {
        return mapWrap.getBoolean("log");
    }

    public FastSqlInfo setLog(boolean log) {
        put("log", log);
        return this;
    }

    public boolean isListener() {
        return mapWrap.getBoolean("listener");
    }

    public FastSqlInfo setListener(boolean listener) {
        put("listener", listener);
        return this;
    }

    public String toStaticSql() {
        String newSql = getSql();
        Matcher matcher = SQL_PATTERN.matcher(newSql);
        while (matcher.find()) {
            newSql = newSql.replace(matcher.group(1), matcher.group(1).replace("?", "{{?}}"));
        }
        for (Object param : getParams()) {
            newSql = newSql.replaceFirst("\\{\\{\\?}}", "'" + param + "'");
        }
        return newSql;
    }

    public FastSqlInfo copy() {
        FastSqlInfo fastSqlInfo = FastSqlInfo.newInstance();
        fastSqlInfo.putAll(this);
        fastSqlInfo.setParams(new ArrayList<>(getParams()));
        fastSqlInfo.setChildren(new ArrayList<>(getChildren()));
        return fastSqlInfo;
    }

//    public static void main(String[] args) {
//        String sql = "select * from dd?ddsdf whre 1 like ? and name in (?,?,?) ?";
//        String regStr = "([ ,()\"']+\\?)";
//        Pattern compile = Pattern.compile(regStr);
//        Matcher matcher = compile.matcher(sql);
//        while (matcher.find()) {
//            sql = sql.replace(matcher.group(1),  matcher.group(1).replace("?","{{?}}"));
//        }
//        System.out.println(sql);
//    }
}
