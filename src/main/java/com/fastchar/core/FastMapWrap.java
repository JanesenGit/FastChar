package com.fastchar.core;

import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastEnumUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Timestamp;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class FastMapWrap {

    public static FastMapWrap newInstance(Map map) {
        return FastChar.getOverrides().newInstance(FastMapWrap.class).setMap(map);
    }

    private Map map;
    protected FastMapWrap() {

    }
    public Map getMap() {
        return map;
    }

    public FastMapWrap setMap(Map map) {
        this.map = map;
        return this;
    }


    public Object get(String attr) {
        return map.get(attr);
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
        return String.valueOf(value).equalsIgnoreCase("<null>");
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


}
