package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastDatabaseException;

import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;

public class FastTableInfo<T> extends FastBaseInfo {

    public static FastTableInfo<?> newInstance() {
        return FastChar.getOverrides().newInstance(FastTableInfo.class);
    }

    private String databaseName;
    private String name;
    private String comment = "";
    private List<FastColumnInfo<?>> columns = new ArrayList<>();

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

    public List<FastColumnInfo<?>> getColumns() {
        return columns;
    }

    public T setColumns(List<FastColumnInfo<?>> columns) {
        this.columns = columns;
        return (T) this;
    }

    public FastColumnInfo<?> getColumnInfo(String name) {
        for (FastColumnInfo<?> column : this.columns) {
            if (column.getName().equals(name)) {
                return column;
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

    /**
     * 获得主键
     * @return
     */
    public List<FastColumnInfo<?>> getPrimaries() {
        List<FastColumnInfo<?>> primaries = new ArrayList<>();
        for (FastColumnInfo<?> column : this.columns) {
            if (column.isPrimary()) {
                primaries.add(column);
            }
        }
        return primaries;
    }


    /**
     * 检测列是否存在
     *
     * @param name
     * @return
     */
    public boolean checkColumn(String name) {
        for (FastColumnInfo column : this.columns) {
            if (column.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 校验必须属性值配置
     */
    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(name)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo("Db_Table_Error1")
                    + "\n\tat " + getStackTrace("name"));
        }
    }


    /**
     * 合并
     *
     * @param info
     */
    public FastTableInfo merge(FastTableInfo<?> info) {
        for (String key : info.keySet()) {
            if (key.equals("columns")) {
                continue;
            }
            this.set(key, info.get(key));
        }
        for (FastColumnInfo column : info.getColumns()) {
            FastColumnInfo existColumn = this.getColumnInfo(column.getName());
            if (existColumn != null) {
                existColumn.merge(column);
            } else {
                this.getColumns().add(column);
            }
        }
        return this;
    }

    public FastTableInfo copy() {
        FastTableInfo<?> fastTableInfo = newInstance();
        for (String key : keySet()) {
            if (key.equals("columns")) {
                continue;
            }
            fastTableInfo.set(key, get(key));
        }
        for (FastColumnInfo column : getColumns()) {
            fastTableInfo.getColumns().add(column.copy());
        }
        return fastTableInfo;
    }
}
