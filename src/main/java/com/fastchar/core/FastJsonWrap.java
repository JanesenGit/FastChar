package com.fastchar.core;

import com.fastchar.object.FastObjectExecute;
import com.fastchar.object.FastObjectProperty;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.math.RoundingMode;

/**
 * Json对象快速获取属性象值，兼容fastjson和gson
 *
 * @author 沈建（Janesen）
 * @date 2021/8/19 14:01
 */
public class FastJsonWrap {

    public static FastJsonWrap newInstance(String json) {
        return FastChar.getOverrides().newInstance(FastJsonWrap.class, json);
    }


    private Object jsonObject;
    private FastObjectExecute objectExecute;

    public FastJsonWrap(String json) {
        if (FastStringUtils.isNotEmpty(json)) {
            this.jsonObject = FastChar.getJson().fromJson(json, Object.class);
        }
    }

    private FastJsonWrap() {

    }

    private FastJsonWrap setJsonObject(Object jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    /**
     * 设置属性表达式的值
     *
     * @param value 值
     * @return 当前对象
     */
    public FastJsonWrap setValue(Object value) {
        return setValue(null, value);
    }

    /**
     * 设置属性表达式的值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @param value      值
     * @return 当前对象
     */
    public FastJsonWrap setValue(String attr, Object value) {
        getProperty(attr).setValue(value);
        return this;
    }

    /**
     * 添加属性表达式的值
     *
     * @param value 值
     * @return 当前对象
     */
    public FastJsonWrap addValue(Object value) {
        return addValue(null, value);
    }

    /**
     * 添加属性表达式的值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @param value      值
     * @return 当前对象
     */
    public FastJsonWrap addValue(String attr, Object value) {
        getProperty(attr).addValue(value);
        return this;
    }

    /**
     * 添加属性表达式的值
     *
     * @param index 索引位置
     * @param value 值
     * @return 当前对象
     */
    public FastJsonWrap addValue(int index, Object value) {
        return addValue(null, index, value);
    }

    /**
     * 添加属性表达式的值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @param index      添加到指定索引位置
     * @param value      值
     * @return 当前对象
     */
    public FastJsonWrap addValue(String attr, int index, Object value) {
        getProperty(attr).addValue(index, value);
        return this;
    }

    /**
     * 添加属性表达式的值
     *
     * @param key   键
     * @param value 值
     * @return 当前对象
     */
    public FastJsonWrap addKeyValue(String key, Object value) {
        return addKeyValue(null, key, value);
    }

    /**
     * 添加属性表达式的值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @param key        键
     * @param value      值
     * @return 当前对象
     */
    public FastJsonWrap addKeyValue(String attr, String key, Object value) {
        getProperty(attr).addKeyValue(key, value);
        return this;
    }

    /**
     * 获取json对象里的值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @return 获取的值
     */
    public Object get(String attr) {
        if (objectExecute == null) {
            objectExecute = new FastObjectExecute(jsonObject);
        }
        if (FastStringUtils.isNotEmpty(attr)) {
            return objectExecute.execute("${" + attr + "}");
        }
        return jsonObject;
    }

    /**
     * 获取json对象里的属性值
     *
     * @param attr 属性表达式，支持层级获取，例如：user.userId 或 user.userNames[0]
     * @return 获取的值
     */
    private FastObjectProperty getProperty(String attr) {
        if (objectExecute == null) {
            objectExecute = new FastObjectExecute(jsonObject);
        }
        if (FastStringUtils.isNotEmpty(attr)) {
            return objectExecute.executeProperty("${" + attr + "}");
        }
        return objectExecute.executeProperty(null);
    }

    /**
     * 获取FastJsonWrap对象
     *
     * @param attr 表达式
     * @return FastJsonWrap
     */
    public FastJsonWrap getJsonWrap(String attr) {
        return newInstance(null).setJsonObject(get(attr));
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
     * 转为json字符串
     *
     * @return 字符串
     */
    public String toJson() {
        return FastChar.getJson().toJson(jsonObject);
    }

    public Object getJsonObject() {
        return this.jsonObject;
    }
}
