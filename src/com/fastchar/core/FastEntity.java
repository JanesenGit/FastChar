package com.fastchar.core;

import com.fastchar.database.FastData;
import com.fastchar.database.FastPage;
import com.fastchar.database.FastSql;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastJsonProvider;
import com.fastchar.utils.*;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public abstract class FastEntity<E extends FastEntity> extends ConcurrentHashMap<String, Object> {
    private String database;

    public abstract String getTableName();

    public String getTableDetails() {
        return null;
    }

    public void setDefaultValue() {
    }

    protected transient FastData<E> fastData = FastChar.getOverrides().newInstance(FastData.class, this);
    protected transient List<String> modified = new ArrayList<>();
    private String error = "";


    private boolean isDefaultMethodUse() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 4) {
            String useMethod = stackTrace[3].getMethodName();
            String useClassName = stackTrace[3].getClassName();
            return useMethod.equals("setDefaultValue") && useClassName.equals(this.getClass().getName());
        }
        return false;
    }

    public String getDatabase() {
        return database;
    }

    public E setDatabase(String database) {
        this.database = database;
        return (E) this;
    }

    /**
     * 设置属性值
     * @param attr
     * @param value
     */
    public E set(String attr, Object value) {
        if (value == null || attr == null) {
            return (E) this;
        }
        boolean defaultMethodUse = isDefaultMethodUse();
        if (defaultMethodUse && isNotEmpty(attr) && !isNull(attr)) {
            return (E) this;
        }
        if (isColumn(attr) && !defaultMethodUse && !modified.contains(attr)) {
            modified.add(attr);
        }
        super.put(attr, value);
        return (E) this;
    }

    /**
     * 设置属性值
     */
    public E setAll(Map<String, Object> source) {
        if (source == null) {
            return (E) this;
        }
        for (String s : source.keySet()) {
            set(s, source.get(s));
        }
        return (E) this;
    }


    @Override
    public Object put(String key, Object value) {
        if (value == null || key == null) {
            return null;
        }
        if (isDefaultMethodUse() && isNotEmpty(key)) {
            return null;
        }
        return super.put(key, value);
    }

    public String getError() {
        return error;
    }

    public E setError(String error) {
        this.error = error;
        return (E) this;
    }

    /**
     * 获取第一个主键的int值
     * @return
     */
    public int getId() {
        List<FastColumnInfo<?>> primaries = getPrimaries();
        if (primaries != null) {
            return getInt(primaries.get(0).getName(), -1);
        }
        return -1;
    }


    public E selectById(Object... ids) {
        return fastData.selectById(ids);
    }

    public E selectFirst(String sqlStr, Object... params) {
        return fastData.selectFirst(sqlStr, params);
    }

    public E selectFirst() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.selectFirst(sqlInfo.getSql(), sqlInfo.toParams());
    }

    public E selectLast(String sqlStr, Object... params) {
        return fastData.selectLast(sqlStr, params);
    }

    public E selectLast() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.selectLast(sqlInfo.getSql(), sqlInfo.toParams());
    }


    /**
     * 执行sql语句
     *
     * @param sqlStr
     * @param params
     * @return 返回数据集合
     */
    public List<E> select(String sqlStr, Object... params) {
        return fastData.select(sqlStr, params);
    }


    /**
     * 查询数据，根据配置的条件属性生成sql语句
     * @return 返回数据集合
     */
    public List<E> select() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.select(sqlInfo.getSql(), sqlInfo.toParams());
    }

    /**
     * 执行sql语句
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @return 分页数据
     */
    public FastPage<E> select(int page, int pageSize) {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.select(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
    }


    /**
     * 执行sql语句
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @param sqlStr   sql语句
     * @param params   参数
     * @return 分页数据
     */
    public FastPage<E> select(int page, int pageSize, String sqlStr, Object... params) {
        return fastData.select(page, pageSize, sqlStr, params);
    }


    /**
     * 删除当前数据
     *
     * @return
     */
    public boolean delete() {
        return fastData.delete();
    }

    /**
     * 根据Id删除数据
     */
    public boolean deleteById(Object... ids) {
        return fastData.deleteById(ids);
    }


    /**
     * 保存到数据库中
     */
    public boolean save() {
        return fastData.save();
    }

    /**
     * 更新到数据库中
     */
    public boolean update() {
        return fastData.update();
    }

    /**
     * 更新到数据库中
     *
     * @return
     */
    public boolean updateById(Object... ids) {
        return fastData.updateById(ids);
    }

    /**
     * 执行sql 语句
     *
     * @param sql
     * @param params
     * @return
     */
    public int update(String sql, Object... params) {
        return fastData.update(sql, params);
    }


    /**
     * 获得被修改的属性
     *
     * @return List<String>
     */
    public List<String> getModified() {
        return modified;
    }


    public <E> E getObject(String attr) {
        return (E) get(attr);
    }

    public String getString(String attr) {
        return FastStringUtils.defaultValue(get(attr), null);
    }

    public String getString(String attr, String defaultValue) {
        return FastStringUtils.defaultValue(get(attr), defaultValue);
    }

    public long getLong(String attr) {
        return FastNumberUtils.formatToLong(get(attr));
    }

    public long getLong(String attr, long defaultValue) {
        return FastNumberUtils.formatToLong(get(attr), defaultValue);
    }

    /**
     * 获得int值
     *
     * @param attr
     * @return
     */
    public int getInt(String attr) {
        return FastNumberUtils.formatToInt(get(attr));
    }

    /**
     * 获得int值
     *
     * @param attr
     * @param defaultValue 默认值
     * @return
     */
    public int getInt(String attr, int defaultValue) {
        return FastNumberUtils.formatToInt(get(attr), defaultValue);
    }


    public short getShort(String attr) {
        return FastNumberUtils.formatToShort(get(attr));
    }

    public short getShort(String attr, short defaultValue) {
        return FastNumberUtils.formatToShort(get(attr), defaultValue);
    }

    public boolean getBoolean(String attr) {
        return FastBooleanUtils.formatToBoolean(get(attr), false);
    }

    public boolean getBoolean(String attr, boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(get(attr), defaultValue);
    }


    /**
     * 获得float值
     *
     * @param attr
     * @return
     */
    public float getFloat(String attr) {
        return FastNumberUtils.formatToFloat(get(attr));
    }

    /**
     * 获得float值
     *
     * @param attr
     * @param defaultValue 默认值
     * @return
     */
    public float getFloat(String attr, float defaultValue) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue);
    }

    /**
     * 获得float值
     *
     * @param attr
     * @param digit 精度
     * @return
     */
    public float getFloat(String attr, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), digit);
    }

    public float getFloat(String attr, float defaultValue, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue, digit);
    }

    /**
     * 获得double值
     *
     * @param attr
     * @return
     */
    public double getDouble(String attr) {
        return FastNumberUtils.formatToDouble(get(attr));
    }

    /**
     * 获得double值
     *
     * @param attr
     * @param defaultValue 默认值
     * @return
     */
    public double getDouble(String attr, double defaultValue) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue);
    }

    /**
     * 获得double值
     *
     * @param attr
     * @param digit 精度
     * @return
     */
    public double getDouble(String attr, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), digit);
    }

    public double getDouble(String attr, double defaultValue, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue, digit);
    }


    public Date getDate(String attr) {
        return getTimestamp(attr);
    }

    public <T extends Enum> T getEnum(String attr, Class<? extends Enum> targetClass) {
        return getEnum(attr, targetClass, null);
    }

    public <T extends Enum> T getEnum(String attr, Class<? extends Enum> targetClass, Enum defaultEnum) {
        return FastEnumUtils.formatToEnum(targetClass, getString(attr), defaultEnum);
    }

    public Blob getBlob(String attr) {
        return (Blob) get(attr);
    }

    public Timestamp getTimestamp(String attr) {
        return getTimestamp(attr, null);
    }

    public Timestamp getTimestamp(String attr, Timestamp defaultValue) {
        if (isEmpty(attr)) {
            return defaultValue;
        }
        return (Timestamp) get(attr);
    }

    public BigDecimal getBigDecimal(String attr) {
        return getBigDecimal(attr, null);
    }

    public BigDecimal getBigDecimal(String attr, BigDecimal defaultValue) {
        if (isEmpty(attr)) {
            return defaultValue;
        }
        return (BigDecimal) get(attr);
    }

    public boolean isColumn(String attr) {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.checkColumn(attr);
        }
        return false;
    }

    public FastColumnInfo<?> getColumn(String attr) {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.getColumnInfo(attr);
        }
        return null;
    }

    public FastTableInfo<?> getTable() {
        return FastChar.getDatabases().get(database).getTableInfo(getTableName());
    }


    public List<FastColumnInfo<?>> getPrimaries() {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.getPrimaries();
        }
        return null;
    }

    public boolean isEmpty(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return FastStringUtils.isEmpty(String.valueOf(value));
    }


    public boolean isNotEmpty(String attr) {
        Object value = get(attr);
        if (value == null) {
            return false;
        }
        return FastStringUtils.isNotEmpty(String.valueOf(value));
    }


    public boolean isBlank(String attr) {
        Object value = get(attr);
        if (value == null) {
            return false;
        }
        return FastStringUtils.isBlank(String.valueOf(value));
    }

    public boolean isNotBlank(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return FastStringUtils.isNotBlank(String.valueOf(value));
    }

    public boolean isNull(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return value.toString().equalsIgnoreCase("<null>");
    }

    public boolean isTimestamp(String attr) {
        return get(attr) instanceof Timestamp;
    }

    public boolean isBigDecimal(String attr) {
        return get(attr) instanceof BigDecimal;
    }

    public Set<String> getExistsColumn() {
        Set<String> columns = new HashSet<>();
        Enumeration<String> keys = this.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (isColumn(key)) {
                columns.add(key);
            }
        }
        return columns;
    }

    public Set<String> allKeys() {
        Set<String> allKeys = new HashSet<>();
        Enumeration<String> keys = this.keys();
        while (keys.hasMoreElements()) {
            allKeys.add(keys.nextElement());
        }
        return allKeys;
    }


    public FastSqlInfo toSelectSql() {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toSelectSql(this);
    }

    public FastSqlInfo toSelectSql(String sqlStr) {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toSelectSql(sqlStr, this);
    }

    public FastSqlInfo toSelectSql(Object... ids) {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toSelectSql(this, ids);
    }

    public FastSqlInfo toInsertSql() {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toInsertSql(this);
    }

    public FastSqlInfo toUpdateSql() {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toUpdateSql(this);
    }

    public FastSqlInfo toUpdateSql(Object... ids) {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toUpdateSql(this, ids);
    }

    public FastSqlInfo toDeleteSql() {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toDeleteSql(this);
    }

    public FastSqlInfo toDeleteSql(Object... ids) {
        return FastSql.newInstance(FastChar.getDatabases().get(database).getType()).toDeleteSql(this, ids);
    }


    public <T extends FastEntity> T toEntity(Class<T> targetClass) {
        return toEntity(targetClass, true);
    }

    public <T extends FastEntity> T toEntity(Class<T> targetClass, boolean pluckAttr) {
        if (targetClass == null) {
            return null;
        }
        FastEntity fastEntity = FastClassUtils.newInstance(targetClass);
        if (fastEntity == null) {
            return null;
        }
        for (String allKey : allKeys()) {
            if (fastEntity.isColumn(allKey)) {
                fastEntity.put(allKey, get(allKey));
                if (pluckAttr && !isColumn(allKey)) {
                    remove(allKey);
                }
            }
        }
        return (T) fastEntity;
    }

    public String toJson() {
        return FastChar.getOverrides().newInstance(IFastJsonProvider.class).toJson(this);
    }


    public E formatDate(String attr, String pattern) {
        if (isTimestamp(attr)) {
            String format = FastDateUtils.format(getTimestamp(attr), pattern);
            put(attr, format);
        }
        return (E) this;
    }


    public E cache(String key) {
        return (E) this;
    }


}
