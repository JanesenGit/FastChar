package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FastSqlInfo extends FastBaseInfo {

    private String type;//数据库类型
    private String sql;
    private boolean log = true;
    private List<Object> params = new ArrayList<>();
    private List<FastSqlInfo> children = new ArrayList<>();

    public Object[] toParams(){
        return params.toArray(new Object[]{});
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public FastSqlInfo setType(String type) {
        this.type = type;
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

    public String toStaticSql() {
        String newSql = sql;
        for (Object param : params) {
            newSql = newSql.replaceFirst("\\?", "'" + param + "'");
        }
        return newSql;
    }
}
