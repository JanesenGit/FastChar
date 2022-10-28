package com.fastchar.database.info;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.*;

@SuppressWarnings("unchecked")
public class FastTableInfo<T> extends LinkedHashMap<String, Object> {

    public static FastTableInfo<?> newInstance() {
        return FastChar.getOverrides().newInstance(FastTableInfo.class);
    }

    protected transient FastMapWrap mapWrap;

    protected FastTableInfo() {
        super(16);
        put("tagName", "table");
        mapWrap = FastMapWrap.newInstance(this);
    }


    public FastMapWrap getMapWrap() {
        return mapWrap;
    }

    public boolean isFromXml() {
        return mapWrap.getBoolean("fromXml", false);
    }

    public void setFromXml(boolean fromXml) {
        put("fromXml", fromXml);
    }

    public int getLineNumber() {
        return mapWrap.getInt("lineNumber");
    }

    public void setLineNumber(int lineNumber) {
        put("lineNumber", lineNumber);
    }

    public String getFileName() {
        return mapWrap.getString("fileName");
    }

    public void setFileName(String fileName) {
        put("fileName", fileName);
    }

    public String getTagName() {
        return mapWrap.getString("tagName");
    }


    public boolean isEnable() {
        return mapWrap.getBoolean("enable", true);
    }


    public String getName() {
        return mapWrap.getString("name");
    }

    public T setName(String name) {
        put("name", name);
        return (T) this;
    }

    public String getComment() {
        return mapWrap.getString("comment");
    }

    public T setComment(String comment) {
        put("comment", comment);
        return (T) this;
    }

    public <E extends FastColumnInfo<?>> Collection<E> getColumns() {
        return (Collection<E>) getMapColumns().values();
    }

    public <E extends FastColumnInfo<?>> Collection<E> getPrimaries() {
        return (Collection<E>) getMapPrimaries().values();
    }

    private Map<String, FastColumnInfo<?>> getMapColumns() {
        //必须使用map存储，避免大数量处理较慢
        Map<String, FastColumnInfo<?>> columns = mapWrap.getObject("columns");
        if (columns == null) {
            columns = new LinkedHashMap<>(16);
            put("columns", columns);
        }
        return columns;
    }


    private Map<String, FastColumnInfo<?>> getMapPrimaries() {
        Map<String, FastColumnInfo<?>> primaries = mapWrap.getObject("primaries");
        if (primaries == null) {
            primaries = new LinkedHashMap<>(16);
            put("primaries", primaries);
        }
        return primaries;
    }


    public FastColumnInfo<?> addColumn(FastColumnInfo<?> columnInfo) {
        if (columnInfo == null) {
            return null;
        }
        String key = columnInfo.getName().toLowerCase();
        FastColumnInfo<?> fastColumnInfo = getMapColumns().get(key);
        if (fastColumnInfo != null) {
            fastColumnInfo.merge(columnInfo);
        } else {
            getMapColumns().put(key, columnInfo);
            fastColumnInfo = columnInfo;
        }

        if (fastColumnInfo.isPrimary()) {
            FastColumnInfo<?> fastPrimaryColumnInfo = getMapPrimaries().get(key);
            if (fastPrimaryColumnInfo != null) {
                fastPrimaryColumnInfo.merge(fastColumnInfo);
            } else {
                getMapPrimaries().put(key, fastColumnInfo);
            }
        }
        return fastColumnInfo;
    }


    public <E extends FastColumnInfo<?>> E getColumnInfo(String name) {
        if (FastStringUtils.isEmpty(name)) {
            return null;
        }
        FastColumnInfo<?> fastColumnInfo = getMapColumns().get(name.toLowerCase());
        if (fastColumnInfo == null) {
            return null;
        }
        if (!fastColumnInfo.isEnable()) {
            return null;
        }
        return (E) fastColumnInfo;
    }

    public <E extends FastColumnInfo<?>> E getPrimaryColumnInfo(String name) {
        if (FastStringUtils.isEmpty(name)) {
            return null;
        }
        FastColumnInfo<?> fastColumnInfo = getMapPrimaries().get(name.toLowerCase());
        if (fastColumnInfo == null) {
            return null;
        }
        if (!fastColumnInfo.isEnable()) {
            return null;
        }
        return (E) fastColumnInfo;
    }

    public String getDatabase() {
        return mapWrap.getString("database");
    }

    public T setDatabase(String database) {
        put("database", database);
        return (T) this;
    }

    public String getData() {
        return mapWrap.getString("data");
    }

    public T setData(String data) {
        put("data", data);
        return (T) this;
    }

    public T setIgnoreCase(boolean ignoreCase) {
        put("ignoreCase", ignoreCase);
        return (T) this;
    }

    public boolean isLock() {
        return mapWrap.getBoolean("lock");
    }

    public T setLock(boolean lock) {
        put("lock", lock);
        return (T) this;
    }


    public boolean isColumn(String name) {
        if (FastStringUtils.isEmpty(name)) {
            return false;
        }
        return getColumnInfo(name) != null;
    }


    public boolean isExist() {
        return mapWrap.getBoolean("exist");
    }

    public T setExist(boolean exist) {
        put("exist", exist);
        return (T) this;
    }

    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(getName())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_TABLE_ERROR1)
                    + "\n\tat " + getStackTrace("name"));
        }
        put("validated", true);
    }


    public FastTableInfo<?> merge(FastTableInfo<?> info) {
        return merge(info, false);
    }

    public FastTableInfo<?> merge(FastTableInfo<?> info, boolean onlyColumns) {
        if (!onlyColumns) {
            for (Map.Entry<String, Object> stringObjectEntry : info.entrySet()) {
                if ("columns".equals(stringObjectEntry.getKey())) {
                    continue;
                }
                this.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
            if (FastStringUtils.isNotEmpty(info.getFileName())) {
                setFileName(info.getFileName());
            }
            if (info.getLineNumber() != 0) {
                setLineNumber(info.getLineNumber());
            }
        }
        for (Map.Entry<String, FastColumnInfo<?>> columnInfoEntry : info.getMapColumns().entrySet()) {
            this.addColumn(columnInfoEntry.getValue());
        }
        return this;
    }


    public FastTableInfo<?> copy() {
        FastTableInfo<?> fastTableInfo = newInstance();
        for (Map.Entry<String, Object> stringObjectEntry : this.entrySet()) {
            if ("columns".equals(stringObjectEntry.getKey())) {
                continue;
            }
            fastTableInfo.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        fastTableInfo.setFileName(this.getFileName());
        fastTableInfo.setLineNumber(this.getLineNumber());
        for (Map.Entry<String, FastColumnInfo<?>> columnInfoEntry : getMapColumns().entrySet()) {
            fastTableInfo.addColumn(columnInfoEntry.getValue().copy());
        }
        return fastTableInfo;
    }

    public StackTraceElement getStackTrace(String attrName) {
        if (FastStringUtils.isEmpty(getFileName())) {
            return null;
        }
        return new StackTraceElement(
                getFileName() + "." + getTagName(),
                attrName,
                getFileName(),
                getLineNumber());
    }


    public String toSimpleInfo() {
        return getClass().getSimpleName() + " { name = '" + getName() + "' , database = " + FastChar.getDatabases().get(getDatabase()).toSimpleInfo() + " } ";
    }

}
