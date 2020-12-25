package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.exception.FastDatabaseException;

import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class FastTableInfo<T> extends FastBaseInfo {

    public static FastTableInfo<?> newInstance() {
        return FastChar.getOverrides().newInstance(FastTableInfo.class);
    }

    protected FastTableInfo() {
    }

    private String databaseName;
    private String name;
    private String comment = "";
    private String data;
    private boolean ignoreCase = true;

    private List<FastColumnInfo<?>> columns = new ArrayList<>();
    private FastMapWrap mapColumn;
    private FastMapWrap mapPrimary;

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getComment() {
        return comment;
    }

    public T setComment(String comment) {
        this.comment = comment;
        return (T) this;
    }

    public <E extends FastColumnInfo<?>> List<E> getColumns() {
        return (List<E>) columns;
    }

    public T setColumns(List<FastColumnInfo<?>> columns) {
        this.columns = columns;
        return (T) this;
    }

    public <E extends FastColumnInfo<?>> E getColumnInfo(String name) {
        if (FastStringUtils.isEmpty(name)) {
            return null;
        }
        if (mapColumn != null) {
            return mapColumn.getObject(name);
        }
        for (FastColumnInfo<?> column : this.columns) {
            if (isIgnoreCase()) {
                if (column.getName().equalsIgnoreCase(name)) {
                    return (E) column;
                }
            }
            if (column.getName().equals(name)) {
                return (E) column;
            }
        }
        return null;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public FastTableInfo<T> setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getData() {
        return data;
    }

    public FastTableInfo<T> setData(String data) {
        this.data = data;
        return this;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public FastTableInfo<T> setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    public <E extends FastColumnInfo<?>> List<E> getPrimaries() {
        if (mapPrimary != null) {
            return new ArrayList<E>((Collection<? extends E>) mapPrimary.getMap().values());
        }

        List<FastColumnInfo<?>> primaries = new ArrayList<>();
        for (FastColumnInfo<?> column : this.columns) {
            if (column.isPrimary()) {
                primaries.add(column);
            }
        }
        return (List<E>) primaries;
    }


    public boolean checkColumn(String name) {
        if (mapColumn != null) {
            return mapColumn.containsAttr(name);
        }
        for (FastColumnInfo<?> column : this.columns) {
            if (isIgnoreCase()) {
                if (column.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            if (column.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(name)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_ERROR1)
                    + "\n\tat " + getStackTrace("name"));
        }
    }


    public FastTableInfo<?> merge(FastTableInfo<?> info) {
        for (String key : info.keySet()) {
            if ("columns".equals(String.valueOf(key))) {
                continue;
            }
            this.set(key, info.get(key));
        }
        if (FastStringUtils.isNotEmpty(info.getFileName())) {
            setFileName(info.getFileName());
        }
        if (info.getLineNumber() != 0) {
            setLineNumber(info.getLineNumber());
        }
        for (FastColumnInfo<?> column : info.getColumns()) {
            FastColumnInfo<?> existColumn = this.getColumnInfo(column.getName());
            if (existColumn != null) {
                existColumn.merge(column);
            } else {
                this.getColumns().add(column);
            }
        }
        return this;
    }


    public FastTableInfo<?> copy() {
        FastTableInfo<?> fastTableInfo = newInstance();
        for (String key : keySet()) {
            if ("columns".equals(String.valueOf(key))) {
                continue;
            }
            fastTableInfo.set(key, get(key));
        }
        fastTableInfo.setFileName(this.getFileName());
        fastTableInfo.setLineNumber(this.getLineNumber());
        fastTableInfo.setTagName(this.getTagName());
        for (FastColumnInfo<?> column : getColumns()) {
            fastTableInfo.getColumns().add(column.copy());
        }
        fastTableInfo.fromProperty();
        return fastTableInfo;
    }


    public void columnToMap() {
        mapColumn = FastMapWrap.newInstance(new ConcurrentHashMap<>(16));
        mapColumn.setIgnoreCase(isIgnoreCase());
        mapPrimary = FastMapWrap.newInstance(new ConcurrentHashMap<>(16));
        mapPrimary.setIgnoreCase(isIgnoreCase());
        for (FastColumnInfo<?> column : this.columns) {
            mapColumn.set(column.getName(), column);
            if (column.isPrimary()) {
                mapPrimary.set(column.getName(), column);
            }
        }
    }

}
