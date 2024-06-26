package com.fastchar.core;

import com.fastchar.object.FastObjectExecute;
import com.fastchar.utils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Blob;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FastMapWrap {

    public static FastMapWrap newInstance(Map<?, ?> map) {
        return FastChar.getOverrides().newInstance(FastMapWrap.class).setMap(map);
    }

    protected Map<?, ?> map;

    protected transient Map<String, Object> ignoreMap = new HashMap<>(16);
    protected boolean ignoreCase;

    //强制使用${}格式获取属性值
    protected boolean forceAttr;

    protected FastMapWrap() {

    }


    public Map<?, ?> getMap() {
        if (map == null) {
            map = new HashMap<>(16);
        }
        return map;
    }

    private String convertAttr(String attr) {
        attr = FastStringUtils.strip(attr);
        if (forceAttr) {
            if (attr.startsWith("${") && attr.endsWith("}")) {
                return attr;
            }
            return "${" + attr + "}";
        }
        return attr;
    }

    public FastMapWrap setMap(Map<?, ?> map) {
        this.map = map;
        return this;
    }

    private Object refreshIgnoreCaseMap(Object attr) {
        if (ignoreCase) {
            Object catchKey = null;
            for (Map.Entry<?, ?> entry : getMap().entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    String lowerCase = ((String) key).toLowerCase();
                    ignoreMap.put(lowerCase, key);
                    if (attr != null && lowerCase.equalsIgnoreCase((String) attr)) {
                        catchKey = key;
                    }
                }
            }
            if (catchKey != null) {
                return catchKey;
            }
        }
        return attr;
    }


    private Object getRealAttr(Object attr) {
        if (attr == null) {
            return null;
        }
        if (getMap().containsKey(attr.toString())) {
            return attr;
        }
        if (ignoreCase && attr instanceof String) {
            Object realKey = ignoreMap.get(((String) attr).toLowerCase());
            if (realKey != null) {
                return realKey;
            }
            return refreshIgnoreCaseMap(attr);
        }
        return attr;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * 设置忽略大小写
     *
     * @param ignoreCase 布尔值
     * @return 当前对象
     */
    public FastMapWrap setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    /**
     * 获取Object对象值
     *
     * @param attr 属性名称
     * @return Object对象
     */
    public Object get(Object attr) {
        return getMap().get(getRealAttr(attr));
    }

    /**
     * 获取Object对象值
     *
     * @param attr 属性名称 支持类似 ${user.userNickName} 表达式，可获取深层次的对象属性值，并且此对象必须继承map
     * @return Object对象
     */
    public Object get(String attr) {
        attr = convertAttr(attr);
        //尽量不使用正则表达式
        if (attr.startsWith("${") && attr.endsWith("}")) {
            return new FastObjectExecute(getMap()).execute(attr);
        }
        return getMap().get(getRealAttr(attr));
    }


    /**
     * 是否为空
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isEmpty(String attr) {
        if (isNull(attr)) {
            return true;
        }
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return FastStringUtils.isEmpty(String.valueOf(value));
    }


    /**
     * 是否不为空
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNotEmpty(String attr) {
        return !isEmpty(attr);
    }


    /**
     * 是否为空白字符
     *
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
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNotBlank(String attr) {
        return !isBlank(attr);
    }

    /**
     * 是否为null
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNull(String attr) {
        Object value = get(attr);
        if (value == null) {
            return true;
        }
        return "<null>".equalsIgnoreCase(String.valueOf(value));
    }

    /**
     * 是否不为null
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isNotNull(String attr) {
        return !isNull(attr);
    }

    /**
     * 是否为Timestamp类型
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isTimestamp(String attr) {
        return get(attr) instanceof Timestamp;
    }

    /**
     * 是否为BigDecimal类型
     *
     * @param attr 属性名
     * @return 布尔值
     */
    public boolean isBigDecimal(String attr) {
        return get(attr) instanceof BigDecimal;
    }


    /**
     * 获取任意对象值
     *
     * @param attr 属性名称
     * @param <E>  任意类
     * @return 任意类
     */
    public <E> E getObject(String attr) {
        return (E) get(attr);
    }


    /**
     * 获取任意对象值
     *
     * @param attr 属性名称
     * @param <E>  任意类
     * @return 任意类
     */
    public <E> E getObject(String attr,Class<E> targetClass) {
        Object value = get(attr);
        if (value == null) {
            return null;
        }
        return FastChar.getJson().fromJson(FastChar.getJson().toJson(value), targetClass);
    }




    /**
     * 获取字符串类值
     *
     * @param attr 属性名
     * @return 字符串
     */
    public String getString(String attr) {
        return FastStringUtils.defaultValue(get(attr), null);
    }

    /**
     * 获取字符串类值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return 字符串
     */
    public String getString(String attr, String defaultValue) {
        return FastStringUtils.defaultValue(get(attr), defaultValue);
    }

    /**
     * 获取Long类值
     *
     * @param attr 属性名
     * @return Long
     */
    public long getLong(String attr) {
        return FastNumberUtils.formatToLong(get(attr));
    }

    /**
     * 获取Long值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return Long
     */
    public long getLong(String attr, long defaultValue) {
        return FastNumberUtils.formatToLong(get(attr), defaultValue);
    }

    /**
     * 获取int值
     *
     * @param attr 属性名
     * @return int
     */
    public int getInt(String attr) {
        return FastNumberUtils.formatToInt(get(attr));
    }

    /**
     * 获得int值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return int
     */
    public int getInt(String attr, int defaultValue) {
        return FastNumberUtils.formatToInt(get(attr), defaultValue);
    }


    /**
     * 获得short值
     *
     * @param attr 属性名
     * @return short
     */
    public short getShort(String attr) {
        return FastNumberUtils.formatToShort(get(attr));
    }

    /**
     * 获得short值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return short
     */
    public short getShort(String attr, short defaultValue) {
        return FastNumberUtils.formatToShort(get(attr), defaultValue);
    }

    /**
     * 获得Boolean值
     *
     * @param attr 属性名
     * @return Boolean
     */
    public boolean getBoolean(String attr) {
        return FastBooleanUtils.formatToBoolean(get(attr), false);
    }

    /**
     * 获得Boolean值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return Boolean
     */
    public boolean getBoolean(String attr, boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(get(attr), defaultValue);
    }


    /**
     * 获得Float值
     *
     * @param attr 属性名
     * @return Float
     */
    public float getFloat(String attr) {
        return FastNumberUtils.formatToFloat(get(attr));
    }

    /**
     * 获得Float值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return Float
     */
    public float getFloat(String attr, float defaultValue) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue);
    }

    /**
     * 获得Float值
     *
     * @param attr  属性名
     * @param digit 精度
     * @return Float
     */
    public float getFloat(String attr, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), digit);
    }

    /**
     * 获得Float值
     *
     * @param attr         属性名
     * @param digit        精度
     * @param roundingMode 四舍五入的方式
     * @return Float
     */
    public float getFloat(String attr, int digit, RoundingMode roundingMode) {
        return FastNumberUtils.formatToFloat(get(attr), digit, roundingMode);
    }

    /**
     * 获得Float值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @param digit        精度
     * @return Float
     */
    public float getFloat(String attr, float defaultValue, int digit) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue, digit);
    }

    /**
     * 获得Float值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @param digit        精度
     * @param roundingMode 四舍五入的方式
     * @return Float
     */
    public float getFloat(String attr, float defaultValue, int digit, RoundingMode roundingMode) {
        return FastNumberUtils.formatToFloat(get(attr), defaultValue, digit, roundingMode);
    }

    /**
     * 获得Double值
     *
     * @param attr 属性名
     * @return Double
     */
    public double getDouble(String attr) {
        return FastNumberUtils.formatToDouble(get(attr));
    }

    /**
     * 获得Double值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @return Double
     */
    public double getDouble(String attr, double defaultValue) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue);
    }

    /**
     * 获得Double值
     *
     * @param attr  属性名
     * @param digit 精度
     * @return Double
     */
    public double getDouble(String attr, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), digit);
    }

    /**
     * 获得Double值
     *
     * @param attr         属性名
     * @param digit        精度
     * @param roundingMode 四舍五入的方式
     * @return Double
     */
    public double getDouble(String attr, int digit, RoundingMode roundingMode) {
        return FastNumberUtils.formatToDouble(get(attr), digit, roundingMode);
    }

    /**
     * 获得Double值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @param digit        精度
     * @return Double
     */
    public double getDouble(String attr, double defaultValue, int digit) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue, digit);
    }

    /**
     * 获得Double值
     *
     * @param attr         属性名
     * @param defaultValue 默认值
     * @param digit        精度
     * @param roundingMode 四舍五入的方式
     * @return Double
     */
    public double getDouble(String attr, double defaultValue, int digit, RoundingMode roundingMode) {
        return FastNumberUtils.formatToDouble(get(attr), defaultValue, digit, roundingMode);
    }


    /**
     * 获取枚举值
     *
     * @param attr        属性名
     * @param targetClass 枚举类型
     * @param <T>         继承Enum的泛型类
     * @return 枚举值
     */
    public <T extends Enum<?>> T getEnum(String attr, Class<T> targetClass) {
        return getEnum(attr, targetClass, null);
    }

    /**
     * 获取枚举值
     *
     * @param attr        属性名
     * @param targetClass 枚举类型
     * @param defaultEnum 默认枚举值
     * @param <T>         继承Enum的泛型类
     * @return 枚举值
     */
    public <T extends Enum<?>> T getEnum(String attr, Class<T> targetClass, Enum<?> defaultEnum) {
        return FastEnumUtils.formatToEnum(targetClass, getString(attr), defaultEnum);
    }

    /**
     * 获取Blob值
     *
     * @param attr 属性名
     * @return Blob
     */
    public Blob getBlob(String attr) {
        return (Blob) get(attr);
    }

    /**
     * 获取Timestamp值
     *
     * @param attr 属性名
     * @return Timestamp
     */
    public Timestamp getTimestamp(String attr) {
        return getTimestamp(attr, null);
    }

    /**
     * 获取Timestamp值，注意：此方法
     *
     * @param attr         属性名
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
        } else if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        }
        String guessDateFormat = FastDateUtils.guessDateFormat(value.toString());
        if (FastStringUtils.isNotEmpty(guessDateFormat)) {
            Date parse = FastDateUtils.parse(value.toString(), guessDateFormat);
            if (parse != null) {
                return Timestamp.valueOf(FastDateUtils.format(parse, "yyyy-MM-dd HH:mm:ss"));
            }
        }
        return Timestamp.valueOf(value.toString());
    }

    /**
     * 获取BigDecimal值
     *
     * @param attr 属性名
     * @return BigDecimal
     */
    public BigDecimal getBigDecimal(String attr) {
        return getBigDecimal(attr, null);
    }

    /**
     * 获取BigDecimal值
     *
     * @param attr         属性名
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
     * 设置属性值
     *
     * @param attr  属性名称
     * @param value 属性值
     * @return 当前对象
     */
    public FastMapWrap set(Object attr, Object value) {
        Map map = getMap();
        if (map != null) {
            map.put(attr, value);
        }
        return this;
    }

    /**
     * 设置属性
     *
     * @param data 属性集合
     * @return 当前对象
     */
    public FastMapWrap setAll(Map data) {
        getMap().putAll(data);
        return this;
    }

    /**
     * 设置属性
     *
     * @param data 属性集合
     * @return 当前对象
     */
    public FastMapWrap putAll(Map data) {
        getMap().putAll(data);
        return this;
    }

    /**
     * 检测属性是否存在
     *
     * @param attr 属性名称
     * @return 布尔值
     */
    public boolean containsAttr(String attr) {
        return getMap().containsKey(getRealAttr(attr));
    }

    /**
     * 所有值集合
     *
     * @return 集合数据
     */
    public Collection<?> values() {
        return getMap().values();
    }


    /**
     * 删除属性
     *
     * @param attr 属性名称
     * @return 被删除的数据
     */
    public Object removeAttr(String attr) {
        return getMap().remove(getRealAttr(attr));
    }


    /**
     * key集合
     *
     * @return Set
     */
    public Set<?> keySet() {
        return getMap().keySet();
    }

    /**
     * 清空属性值
     */
    public void clear() {
        getMap().clear();
    }


    public FastMapWrap put(Object attr, Object value) {
        Map map = getMap();
        if (map != null) {
            map.put(attr, value);
        }
        return this;
    }

}