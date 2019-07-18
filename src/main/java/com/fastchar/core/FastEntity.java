package com.fastchar.core;

import com.fastchar.database.FastData;
import com.fastchar.database.FastPage;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.database.sql.FastSql;
import com.fastchar.interfaces.IFastJson;
import com.fastchar.utils.*;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据载体类FastEntity，FastChar的核心类
 *
 * @param <E>
 */
@SuppressWarnings("unchecked")
public abstract class FastEntity<E extends FastEntity> extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = 4002535971197915410L;
    private String database;

    /**
     * 获得实体对应的表格名称
     *
     * @return
     */
    public abstract String getTableName();

    /**
     * 获得表格的描述
     *
     * @return
     */
    public String getTableDetails() {
        return null;
    }

    /**
     * 转换属性值，在查询实体数据时，会调用此方法
     */
    public void convertValue() {

    }

    /**
     * 设置属性默认值，在调用save和update方法时会触发
     */
    public void setDefaultValue() {
    }

    protected transient FastData<E> fastData = FastChar.getOverrides().newInstance(FastData.class, this);
    protected transient List<String> modified = new ArrayList<>();
    private String error = "";


    private boolean isDefaultMethodUse() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 5) {
            String useMethod = stackTrace[4].getMethodName();
            String useClassName = stackTrace[4].getClassName();
            Class<?> aClass = FastClassUtils.getClass(useClassName, false);
            if (aClass != null && FastEntity.class.isAssignableFrom(aClass)) {
                return useMethod.equals("setDefaultValue");
            }
        }
        return false;
    }

    /**
     * 是否是MySql数据库
     *
     * @return 布尔值
     */
    public boolean isMySql() {
        FastDatabaseInfo fastDatabaseInfo = FastChar.getDatabases().get(getDatabase());
        if (fastDatabaseInfo != null) {
            return fastDatabaseInfo.isMySql();
        }
        return false;
    }

    /**
     * 是否是SqlServe数据库
     *
     * @return 布尔值
     */
    public boolean isSqlServer() {
        FastDatabaseInfo fastDatabaseInfo = FastChar.getDatabases().get(getDatabase());
        if (fastDatabaseInfo != null) {
            return fastDatabaseInfo.isSqlServer();
        }
        return false;
    }

    /**
     * 是否是Oracle数据库
     *
     * @return 布尔值
     */
    public boolean isOracle() {
        FastDatabaseInfo fastDatabaseInfo = FastChar.getDatabases().get(getDatabase());
        if (fastDatabaseInfo != null) {
            return fastDatabaseInfo.isOracle();
        }
        return false;
    }


    /**
     * 获得当前entity绑定的数据库名称
     *
     * @return String
     */
    public String getDatabase() {
        return database;
    }

    /**
     * 获取当前entity绑定数据库的类型
     *
     * @return
     */
    public String getDatabaseType() {
        return FastChar.getDatabases().get(database).getType();
    }

    /**
     * 设置数据库名称
     *
     * @param database 数据库名称
     * @return 当前对象
     */
    public E setDatabase(String database) {
        this.database = database;
        return (E) this;
    }

    /**
     * 设置属性值，会标识当前属性被修改
     *
     * @param attr  属性名
     * @param value 属性值
     * @return 当前对象
     */
    public E set(String attr, Object value) {
        return set(attr, value, false);
    }

    /**
     * 设置属性值
     *
     * @param attr  属性名称
     * @param value 属性值
     * @param must  是否必须 说明：为true时，跳过[setDefaultValue]方法的检测，标记为：被修改的属性。在执行update方法时有效。
     * @return 当前对象
     */
    public E set(String attr, Object value, boolean must) {
        if (value == null || attr == null) {
            return (E) this;
        }
        boolean defaultMethodUse = isDefaultMethodUse();
        if (defaultMethodUse && isNotEmpty(attr) && !isNull(attr)) {
            return (E) this;
        }
        if (must) {
            defaultMethodUse = false;
        }
        if (isColumn(attr) && !defaultMethodUse && !modified.contains(attr)) {
            modified.add(attr);
        }
        super.put(attr, value);
        return (E) this;
    }

    /**
     * 设置属性值
     *
     * @param source map对象
     * @return 当前对象
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


    /**
     * 添加属性
     *
     * @param key   属性
     * @param value 值
     * @return Object
     */
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

    /**
     * 获取错误信息
     *
     * @return String
     */
    public String getError() {
        return error;
    }

    /**
     * 设置错误信息
     *
     * @param error 错误信息
     * @return 当前对象
     */
    public E setError(String error) {
        this.error = error;
        return (E) this;
    }

    /**
     * 获取第一个主键的int值
     *
     * @return int
     */
    public int getId() {
        List<FastColumnInfo<?>> primaries = getPrimaries();
        if (primaries != null) {
            return getInt(primaries.get(0).getName(), -1);
        }
        return -1;
    }


    /**
     * 根据主键查询数据
     *
     * @param ids 主键数组
     * @return 当前类的新对象
     */
    public E selectById(Object... ids) {
        return fastData.selectById(ids);
    }

    /**
     * 查询sql语句返回的第一条数据
     *
     * @param sqlStr sql语句
     * @param params sql参数
     * @return 当前类的新对象
     */
    public E selectFirstBySql(String sqlStr, Object... params) {
        return fastData.selectFirstBySql(sqlStr, params);
    }

    /**
     * 查询表格的第一条数据
     *
     * @return 当前类的新对象
     */
    public E selectFirst() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.selectFirstBySql(sqlInfo.getSql(), sqlInfo.toParams());
    }

    /**
     * 查询sql语句返回的最后一条数据
     *
     * @param sqlStr sql语句
     * @param params sql参数
     * @return 当前类的新对象
     */
    public E selectLastBySql(String sqlStr, Object... params) {
        return fastData.selectLastBySql(sqlStr, params);
    }

    /**
     * 查询表格的最后一条数据
     *
     * @return 当前类的新对象
     */
    public E selectLast() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.selectLastBySql(sqlInfo.getSql(), sqlInfo.toParams());
    }


    /**
     * 执行sql语句
     *
     * @param sqlStr sql语句
     * @param params sql参数
     * @return 返回数据集合
     */
    public List<E> selectBySql(String sqlStr, Object... params) {
        return fastData.selectBySql(sqlStr, params);
    }


    /**
     * 查询数据，根据配置的条件属性生成sql语句
     *
     * @return 返回数据集合
     */
    public List<E> select() {
        FastSqlInfo sqlInfo = this.toSelectSql();
        return fastData.selectBySql(sqlInfo.getSql(), sqlInfo.toParams());
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
        return fastData.selectBySql(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
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
    public FastPage<E> selectBySql(int page, int pageSize, String sqlStr, Object... params) {
        return fastData.selectBySql(page, pageSize, sqlStr, params);
    }


    /**
     * 根据主键值删除数据
     * @return 布尔值
     */
    public boolean delete() {
        return fastData.delete();
    }

    /**
     * 根据传入的检测属性名删除数据
     *
     * @param checks 检测属性名，会用作where条件值
     * @return 布尔值
     */
    public boolean delete(String... checks) {
        return fastData.delete(checks);
    }

    /**
     * 根据指定主键值删除数据
     * @param ids 主键值，如果是复合主键，那么必须与主键顺序匹配，主键的顺序已表格中的列顺序为准
     * @return 布尔值
     */
    public boolean deleteById(Object... ids) {
        return fastData.deleteById(ids);
    }


    /**
     * 删除关联表格的数据
     *
     * @return
     */
    public boolean deleteByLink(String... tables) {

        return false;
    }


    /**
     * 保存数据到数据库中，如果存在自增长的主键，则在插入数据成功后会自动赋值到当前对象中
     *
     * @return 布尔值
     */
    public boolean save() {
        return fastData.save();
    }

    /**
     * 保存到数据库中
     *
     * @param checks 检测属性名，用作where判断，判断是否已存在，如果存在则不插入
     * @return 布尔值
     */
    public boolean save(String... checks) {
        return fastData.save(checks);
    }


    /**
     * 复制一条数据并保存
     * @return 布尔值
     */
    public boolean copySave() {
        return fastData.copySave();
    }


    /**
     * 统计指定检测属性名的数量
     * @param checks  检测属性名，用作where判断
     * @return 统计的数字
     */
    public int count(String... checks) {
        return fastData.count(checks);
    }

    /**
     * 设置数据到数据库中，根据指定的检测属性，如果不存在则添加，存在则修改
     * @param checks  检测属性名，用作where判断
     * @return 布尔值
     */
    public boolean push(String... checks) {
        return fastData.push(checks);
    }

    /**
     * 更新数据
     * @return 布尔值
     */
    public boolean update() {
        return fastData.update();
    }

    /**
     * 更新到数据库中
     * @param checks  检测属性名，用作where判断
     * @return 布尔值
     */
    public boolean update(String... checks) {
        return fastData.update(checks);
    }


    /**
     * 更新到数据库中
     * @param ids 主键值，如果是复合主键，那么必须与主键顺序匹配，主键的顺序已表格中的列顺序为准
     * @return
     */
    public boolean updateById(Object... ids) {
        return fastData.updateById(ids);
    }

    /**
     * 执行sql语句
     * @param sql sql语句
     * @param params sql参数
     * @return 受影响的行数
     */
    public int updateBySql(String sql, Object... params) {
        return fastData.updateBySql(sql, params);
    }


    /**
     * 获得被修改的属性
     * @return List&lt;String&gt;
     */
    public List<String> getModified() {
        return modified;
    }


    /**
     * 获取任意对象值
     * @param attr 属性名称
     * @param <E> 任意类
     * @return 任意类
     */
    public <E> E getObject(String attr) {
        return (E) get(attr);
    }

    /**
     * 获取字符串类值
     * @param attr 属性名
     * @return 字符串
     */
    public String getString(String attr) {
        return FastStringUtils.defaultValue(get(attr), null);
    }

    /**
     * 获取字符串类值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return 字符串
     */
    public String getString(String attr, String defaultValue) {
        return FastStringUtils.defaultValue(get(attr), defaultValue);
    }

    /**
     * 获取Long类值
     * @param attr 属性名
     * @return Long
     */
    public long getLong(String attr) {
        return FastNumberUtils.formatToLong(get(attr));
    }

    /**
     * 获取Long值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return Long
     */
    public long getLong(String attr, long defaultValue) {
        return FastNumberUtils.formatToLong(get(attr), defaultValue);
    }

    /**
     * 获取int值
     * @param attr 属性名
     * @return int
     */
    public int getInt(String attr) {
        return FastNumberUtils.formatToInt(get(attr));
    }

    /**
     * 获得int值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return int
     */
    public int getInt(String attr, int defaultValue) {
        return FastNumberUtils.formatToInt(get(attr), defaultValue);
    }


    /**
     * 获得short值
     * @param attr 属性名
     * @return short
     */
    public short getShort(String attr) {
        return FastNumberUtils.formatToShort(get(attr));
    }

    /**
     * 获得short值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return short
     */
    public short getShort(String attr, short defaultValue) {
        return FastNumberUtils.formatToShort(get(attr), defaultValue);
    }

    /**
     * 获得Boolean值
     * @param attr 属性名
     * @return Boolean
     */
    public boolean getBoolean(String attr) {
        return FastBooleanUtils.formatToBoolean(get(attr), false);
    }

    /**
     * 获得Boolean值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return Boolean
     */
    public boolean getBoolean(String attr, boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(get(attr), defaultValue);
    }


    /**
     * 获得Float值
     * @param attr 属性名
     * @return Float
     */
    public float getFloat(String attr) {
        return FastNumberUtils.formatToFloat(get(attr));
    }

    /**
     * 获得Float值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return Float
     */
    public float getFloat(String attr, float defaultValue) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue);
    }

    /**
     * 获得Float值
     * @param attr 属性名
     * @param digit 精度
     * @return Float
     */
    public float getFloat(String attr, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), digit);
    }

    /**
     * 获得Float值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @param digit 精度
     * @return Float
     */
    public float getFloat(String attr, float defaultValue, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue, digit);
    }

    /**
     * 获得Double值
     * @param attr 属性名
     * @return Double
     */
    public double getDouble(String attr) {
        return FastNumberUtils.formatToDouble(get(attr));
    }

    /**
     * 获得Double值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return Double
     */
    public double getDouble(String attr, double defaultValue) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue);
    }

    /**
     * 获得Double值
     * @param attr 属性名
     * @param digit 精度
     * @return Double
     */
    public double getDouble(String attr, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), digit);
    }

    /**
     * 获得Double值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @param digit 精度
     * @return Double
     */
    public double getDouble(String attr, double defaultValue, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue, digit);
    }


    /**
     * 获得Date值
     * @param attr 属性名
     * @return Date
     */
    public Date getDate(String attr) {
        return getTimestamp(attr);
    }

    /**
     * 获取格式化后的Date字符串值，默认格式： FastChar.getConstant().getDateFormat()
     * @param attr 属性名
     * @return String
     */
    public String getDateString(String attr) {
        return getDateString(attr, FastChar.getConstant().getDateFormat());
    }

    /**
     * 获取格式化后的Date字符串值
     * @param attr 属性名
     * @param pattern 指定日期格式
     * @return String
     */
    public String getDateString(String attr, String pattern) {
        return FastDateUtils.format(getDate(attr), pattern);
    }

    /**
     * 获取枚举值
     * @param attr 属性名
     * @param targetClass 枚举类型
     * @param <T> 继承Enum的泛型类
     * @return 枚举值
     */
    public <T extends Enum> T getEnum(String attr, Class<T> targetClass) {
        return getEnum(attr, targetClass, null);
    }

    /**
     * 获取枚举值
     * @param attr 属性名
     * @param targetClass 枚举类型
     * @param defaultEnum 默认枚举值
     * @param <T> 继承Enum的泛型类
     * @return 枚举值
     */
    public <T extends Enum> T getEnum(String attr, Class<T> targetClass, Enum defaultEnum) {
        return FastEnumUtils.formatToEnum(targetClass, getString(attr), defaultEnum);
    }

    /**
     * 获取Blob值
     * @param attr 属性名
     * @return Blob
     */
    public Blob getBlob(String attr) {
        return (Blob) get(attr);
    }

    /**
     * 获取Timestamp值
     * @param attr 属性名
     * @return Timestamp
     */
    public Timestamp getTimestamp(String attr) {
        return getTimestamp(attr, null);
    }

    /**
     * 获取Timestamp值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return Timestamp
     */
    public Timestamp getTimestamp(String attr, Timestamp defaultValue) {
        if (isEmpty(attr)) {
            return defaultValue;
        }
        Object value = get(attr);
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        return Timestamp.valueOf(value.toString());
    }

    /**
     * 获取BigDecimal值
     * @param attr 属性名
     * @return BigDecimal
     */
    public BigDecimal getBigDecimal(String attr) {
        return getBigDecimal(attr, null);
    }

    /**
     * 获取BigDecimal值
     * @param attr 属性名
     * @param defaultValue 默认值
     * @return BigDecimal
     */
    public BigDecimal getBigDecimal(String attr, BigDecimal defaultValue) {
        if (isEmpty(attr)) {
            return defaultValue;
        }
        return (BigDecimal) get(attr);
    }

    /**
     * 判断属性是否为表格中的列
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isColumn(String attr) {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.checkColumn(attr);
        }
        return false;
    }

    /**
     * 获取表格列对象
     * @param attr 属性名
     * @return FastColumnInfo
     */
    public FastColumnInfo<?> getColumn(String attr) {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.getColumnInfo(attr);
        }
        return null;
    }

    /**
     * 获取绑定的表格对象
     * @return FastTableInfo
     */
    public FastTableInfo<?> getTable() {
        return FastChar.getDatabases().get(database).getTableInfo(getTableName());
    }


    /**
     * 获取主键列集合
     * @return List&lt;FastColumnInfo&lt;?&gt;&gt;
     */
    public List<FastColumnInfo<?>> getPrimaries() {
        FastTableInfo tableInfo = FastChar.getDatabases().get(database).getTableInfo(getTableName());
        if (tableInfo != null) {
            return tableInfo.getPrimaries();
        }
        return null;
    }

    /**
     * 是否为空
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isEmpty(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return FastStringUtils.isEmpty(String.valueOf(value));
    }


    /**
     * 是否不为空
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNotEmpty(String attr) {
        Object value = get(attr);
        if (value == null) {
            return false;
        }
        return FastStringUtils.isNotEmpty(String.valueOf(value));
    }


    /**
     * 是否为空白字符
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isBlank(String attr) {
        Object value = get(attr);
        if (value == null) {
            return false;
        }
        return FastStringUtils.isBlank(String.valueOf(value));
    }

    /**
     * 是否不为空白字符
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNotBlank(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return FastStringUtils.isNotBlank(String.valueOf(value));
    }

    /**
     * 是否为null
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNull(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return value.toString().equalsIgnoreCase("<null>");
    }

    /**
     * 是否为Timestamp类型
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isTimestamp(String attr) {
        return get(attr) instanceof Timestamp;
    }

    /**
     * 是否为BigDecimal类型
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isBigDecimal(String attr) {
        return get(attr) instanceof BigDecimal;
    }

    /**
     * 获取存在于表格列中的所有属性
     * @return Set&lt;String&gt;
     */
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

    /**
     * 获取所有属性名
     * @return Set&lt;String&gt;
     */
    public Set<String> allKeys() {
        Set<String> allKeys = new HashSet<>();
        Enumeration<String> keys = this.keys();
        while (keys.hasMoreElements()) {
            allKeys.add(keys.nextElement());
        }
        return allKeys;
    }


    /**
     * 获取sql操作对象
     * @return
     */
    public FastSql getSql() {
        return FastSql.getInstance(getDatabaseType());
    }

    /**
     * 将当前对象转换成select语句对象
     * @return FastSqlInfo
     */
    public FastSqlInfo toSelectSql() {
        return FastSql.getInstance(getDatabaseType()).buildSelectSql(this);
    }

    /**
     * 将当前对象转换成select语句对象
     * @param sqlStr 指定sql语句
     * @return FastSqlInfo
     */
    public FastSqlInfo toSelectSql(String sqlStr) {
        return FastSql.getInstance(getDatabaseType()).buildSelectSql(sqlStr, this);
    }

    /**
     * 将当前对象转换成select语句对象
     * @param ids 指定主键值 如果是复合主键，那么必须与主键顺序匹配，主键的顺序已表格中的列顺序为准
     * @return FastSqlInfo
     */
    public FastSqlInfo toSelectSql(Object... ids) {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildSelectSqlByIds(this, ids);
    }

    /**
     * 将当前对象转换成insert语句对象
     * @return FastSqlInfo
     */
    public FastSqlInfo toInsertSql() {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildInsertSql(this);
    }

    /**
     * 将当前对象转换成update语句对象
     * @return FastSqlInfo
     */
    public FastSqlInfo toUpdateSql() {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildUpdateSql(this);
    }

    /**
     * 将当前对象转换成update语句对象
     * @param ids 指定主键值 如果是复合主键，那么必须与主键顺序匹配，主键的顺序已表格中的列顺序为准
     * @return FastSqlInfo
     */
    public FastSqlInfo toUpdateSql(Object... ids) {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildUpdateSqlByIds(this, ids);
    }

    /**
     * 将当前对象转换成delete语句对象
     * @return FastSqlInfo
     */
    public FastSqlInfo toDeleteSql() {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildDeleteSql(this);
    }

    /**
     * 将当前对象转换成delete语句对象
     * @param ids 指定主键值 如果是复合主键，那么必须与主键顺序匹配，主键的顺序已表格中的列顺序为准
     * @return FastSqlInfo
     */
    public FastSqlInfo toDeleteSql(Object... ids) {
        return FastSql.getInstance(FastChar.getDatabases().get(database).getType()).buildDeleteSqlByIds(this, ids);
    }


    /**
     * 将当前对象里的属性转换成指定的实体类中，判断依据：该属性存在于目标实体类的表格列中
     * @param targetClass 目标实体类
     * @param <T> 继承FastEntity的泛型类
     * @return &lt;T extends FastEntity&gt;
     */
    public <T extends FastEntity> T toEntity(Class<T> targetClass) {
        return toEntity(null, targetClass, true);
    }

    /**
     * 将当前对象里的属性转换成指定的实体类中，判断依据：该属性存在于目标实体类的表格列中
     * @param targetClass 目标实体类
     * @param pluckAttr 是否分离符合条件的属性，true则移除当前对象里的离符合条件的属性值
     * @param <T> 继承FastEntity的泛型类
     * @return &lt;T extends FastEntity&gt;
     */
    public <T extends FastEntity> T toEntity(Class<T> targetClass, boolean pluckAttr) {
        return toEntity(null, targetClass, pluckAttr);
    }

    /**
     * 将当前对象里的属性转换成指定的实体类中，判断依据：该属性存在于目标实体类的表格列中
     * @param alias 属性别名，符合：alias__attr 格式
     * @param targetClass 目标实体类
     * @param <T> 继承FastEntity的泛型类
     * @return &lt;T extends FastEntity&gt;
     */
    public <T extends FastEntity> T toEntity(String alias, Class<T> targetClass) {
        return toEntity(alias, targetClass, true);
    }

    /**
     * 将当前对象里的属性转换成指定的实体类中，判断依据：该属性存在于目标实体类的表格列中
     * @param alias 属性别名，符合：alias__attr 格式
     * @param targetClass 目标实体类
     * @param pluckAttr 是否分离符合条件的属性，true则移除当前对象里的离符合条件的属性值
     * @param <T> 继承FastEntity的泛型类
     * @return &lt;T extends FastEntity&gt;
     */
    public <T extends FastEntity> T toEntity(String alias, Class<T> targetClass, boolean pluckAttr) {
        if (targetClass == null) {
            return null;
        }
        FastEntity fastEntity = FastClassUtils.newInstance(targetClass);
        if (fastEntity == null) {
            return null;
        }
        if (FastStringUtils.isEmpty(alias)) {
            alias = "";
        }
        for (String key : allKeys()) {
            if (key.startsWith(alias)) {
                String truthKey = key.replace(alias + "__", "");
                boolean checkColumn = false;
                if (FastStringUtils.isEmpty(alias)) {
                    checkColumn = true;
                }
                if (checkColumn && !fastEntity.isColumn(truthKey)) {
                    continue;
                }
                fastEntity.put(truthKey, get(key));
                if (pluckAttr && !isColumn(key)) {
                    remove(key);
                }
            }
        }
        if (fastEntity.size() == 0) {
            return null;
        }
        fastEntity.convertValue();
        return (T) fastEntity;
    }

    /**
     * 将当前对象转成json字符串
     * @return 字符串
     */
    public String toJson() {
        return FastChar.getOverrides().newInstance(IFastJson.class).toJson(this);
    }


    /**
     * 格式化Date类型的属性值
     * @param attr 属性名
     * @param pattern 日期格式
     * @return 当前对象
     */
    public E formatDate(String attr, String pattern) {
        if (isTimestamp(attr)) {
            String format = FastDateUtils.format(getTimestamp(attr), pattern);
            put(attr, format);
        }
        return (E) this;
    }

    /**
     * 构造缓存代码运行
     * @param run 内部代码运行
     * @return 缓存对象
     */
    public Cache buildCache(Cache.Run run) {
        return new Cache().setRun(run);
    }

    /**
     * 删除指定tag的缓存数据
     * @param tag 缓存标签
     * @return 当前对象
     */
    public E deleteCache(String tag) {
        FastChar.getCache().delete(tag);
        return (E) this;
    }

    /**
     * 删除指定tag和key的缓存数据
     * @param tag 缓存标签
     * @param key 缓存的key
     * @return 当前对象
     */
    public E deleteCache(String tag, String key) {
        FastChar.getCache().delete(tag, key);
        return (E) this;
    }

    /**
     * 构造查询指定列名的列名集合
     * @param alias 指定列名的前缀
     * @param excludes 排序指定属性名
     * @return List&lt;String&gt;
     */
    public List<String> toSelectColumns(String alias, String... excludes) {
        if (FastStringUtils.isEmpty(alias)) {
            alias = "";
        } else {
            if (!alias.endsWith(".")) {
                alias = alias + ".";
            }
        }

        List<String> columns = new ArrayList<>();
        for (FastColumnInfo<?> column : getTable().getColumns()) {
            if (FastArrayUtils.contains(excludes, column.getName())) {
                continue;
            }
            String selectColumn = alias + column.getName();
            if (FastStringUtils.isNotEmpty(alias)) {
                selectColumn = selectColumn + " as " + FastStringUtils.strip(alias, ".") + "__" + column.getName();
            }
            columns.add(selectColumn);
        }
        return columns;
    }





    /**
     * FastEntity内部数据缓存类
     */
    public static class Cache {
        private String tag = "FAST_CHAR_DEFAULT_TAG";
        private String key = "FAST_CHAR_DEFAULT_KEY";
        private Run run;

        public String getTag() {
            return tag;
        }

        public Cache setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public String getKey() {
            return key;
        }

        public Cache setKey(String key) {
            this.key = key;
            return this;
        }

        public Cache setRun(Run run) {
            this.run = run;
            return this;
        }

        /**
         * 运行，如果检测到缓存则直接返回缓存的数据，否则运行代码块并返回
         * @param <T>
         * @return
         */
        public <T> T run() {
            try {
                if (FastChar.getCache().exists(tag, key)) {
                    return FastChar.getCache().get(tag, key);
                }
            } catch (Exception ignored) {
            }
            if (run != null) {
                return (T) run.invoke();
            }
            return null;
        }


        public interface Run {
            Object invoke();
        }
    }
}
