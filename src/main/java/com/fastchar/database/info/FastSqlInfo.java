package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;
import com.fastchar.core.FastChar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class FastSqlInfo extends FastBaseInfo {

    private static final long serialVersionUID = 1881604396324620713L;

    public static FastSqlInfo newInstance() {
        return FastChar.getOverrides().newInstance(FastSqlInfo.class);
    }

    protected FastSqlInfo() {
    }

    private String type;//数据库类型
    private String sql;
    private boolean log = true;
    private boolean listener = true;
    private List<Object> params = new ArrayList<>();
    private List<FastSqlInfo> children = new ArrayList<>();

    public Object[] toParams(){
        return params.toArray(new Object[]{});
    }

    public String getType() {
        return type;
    }

    public FastSqlInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public FastSqlInfo setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public List<Object> getParams() {
        return params;
    }

    public FastSqlInfo setParams(List<Object> params) {
        this.params = params;
        return this;
    }

    public FastSqlInfo setParams(Object... params) {
        this.params = Arrays.asList(params);
        return this;
    }


    public List<FastSqlInfo> getChildren() {
        return children;
    }

    public FastSqlInfo setChildren(List<FastSqlInfo> children) {
        this.children = children;
        return this;
    }

    public boolean isLog() {
        return log;
    }

    public FastSqlInfo setLog(boolean log) {
        this.log = log;
        return this;
    }

    public boolean isListener() {
        return listener;
    }

    public FastSqlInfo setListener(boolean listener) {
        this.listener = listener;
        return this;
    }

    public String toStaticSql() {
        String newSql = sql;
        for (Object param : params) {
            newSql = newSql.replaceFirst("\\?", "'" + param + "'");
        }
        return newSql;
    }
}
