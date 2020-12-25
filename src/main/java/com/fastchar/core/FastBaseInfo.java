package com.fastchar.core;

import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 便捷的信息存储类
 * @author 沈建（Janesen）
 */
public class FastBaseInfo extends LinkedHashMap<String, Object> {

    private transient static final long serialVersionUID = -8188484441789855067L;
    private transient int lineNumber = 1;
    private transient List<Field> fields;
    private transient String tagName;
    private transient String fileName;
    private transient FastMapWrap mapWrap;

    public FastBaseInfo() {
        try {
            fields = new ArrayList<>();
            Class<?> tempClass = this.getClass();
            while (tempClass != null) {
                Field[] fieldList = tempClass.getDeclaredFields();
                for (Field field : fieldList) {
                    if (Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    fields.add(field);
                }
                if (FastBaseInfo.class.isAssignableFrom(tempClass.getSuperclass())) {
                    tempClass = tempClass.getSuperclass();
                }else{
                    tempClass = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void clear() {
        super.clear();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(this, null);
            } catch (Exception ignored) {}
        }
    }


    /**
     * 批量设置属性值
     */
    public void setAll(Map<String, Object> sources) {
        for (String s : sources.keySet()) {
            set(s, sources.get(s));
        }
    }

    /**
     * 设置属性值
     * @param attr 属性名
     * @param value 属性值
     */
    public void set(String attr, Object value) {
        try {
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getName().equals(attr)) {
                    field.setAccessible(true);
                    field.set(this, value);
                }
            }
            put(attr, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除属性
     * @param attr 属性名
     */
    public void delete(String attr) {
        try {
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getName().equals(attr)) {
                    field.setAccessible(true);
                    field.set(this, null);
                    break;
                }
            }
            remove(attr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 将自定义的属性填充到map中
     */
    public void fromProperty() {
        try {
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    super.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置自定义属性值，从map中
     */
    public void toProperty() {
        try {
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (this.containsKey(field.getName())) {
                    field.setAccessible(true);
                    field.set(this, this.get(field.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected String buildErrorInfo(String info,String attr) {
        String error = info;
        StackTraceElement stackTrace = getStackTrace(attr);
        if (stackTrace != null) {
            error += "\n\tat " + stackTrace;
        }
        return error;
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


    public int getInt(String attr) {
        return FastNumberUtils.formatToInt(get(attr));
    }

    public int getInt(String attr, int defaultValue) {
        return FastNumberUtils.formatToInt(get(attr), defaultValue);
    }

    public String getString(String attr) {
        return getString(attr, null);
    }

    public String getString(String attr,String defaultValue) {
        return FastStringUtils.defaultValue(get(attr), defaultValue);
    }

    public boolean getBoolean(String attr) {
        return FastBooleanUtils.formatToBoolean(get(attr));
    }

    public boolean getBoolean(String attr, boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(get(attr), defaultValue);
    }

    public String toJson() {
        fromProperty();
        return  FastChar.getJson().toJson(this);
    }


    @Override
    public String toString() {
        return toJson();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public FastMapWrap getMapWrap() {
        if (mapWrap == null) {
            mapWrap = FastMapWrap.newInstance(this);
        }
        return mapWrap;
    }

    public boolean isFromXml() {
        return getBoolean("fromXml", false);
    }

    public void setFromXml(boolean fromXml) {
        put("fromXml", fromXml);
    }
}
