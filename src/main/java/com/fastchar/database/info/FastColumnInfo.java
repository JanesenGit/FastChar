package com.fastchar.database.info;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.exception.FastDatabaseException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.LinkedHashMap;

public class FastColumnInfo<T> extends LinkedHashMap<String, Object> {

    public static FastColumnInfo<?> newInstance() {
        return FastChar.getOverrides().newInstance(FastColumnInfo.class);
    }

    protected transient FastMapWrap mapWrap;

    protected FastColumnInfo() {
        super(16);
        put("tagName", "column");
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
        return  mapWrap.getString("fileName");
    }

    public void setFileName(String fileName) {
        put("fileName", fileName);
    }

    public String getTagName() {
        return  mapWrap.getString("tagName");
    }

    public boolean isPrimary() {
        return mapWrap.getBoolean("primary", false);
    }

    public boolean isAutoincrement() {
        return mapWrap.getBoolean("autoincrement", false);
    }

    public boolean isPassword() {
        return mapWrap.getBoolean("password", false);
    }

    public boolean isNotNull() {
        return "notnull".equalsIgnoreCase(mapWrap.getString("nullable", "null").replace(" ", ""));
    }

    public boolean isEncrypt() {
        String encrypt = mapWrap.getString("encrypt");
        if (FastStringUtils.isNotEmpty(encrypt)) {
            if ("md5".equalsIgnoreCase(encrypt)) {
                return true;
            }
        }
        return mapWrap.getBoolean("encrypt", false);
    }

    public boolean isEnable() {
        return mapWrap.getBoolean("enable", true);
    }


    public String getPrimary() {
        return mapWrap.getString("primary");
    }

    public T setPrimary(String primary) {
        put("primary", primary);
        return (T) this;
    }

    public String getName() {
        return mapWrap.getString("name");
    }

    public T setName(String name) {
        put("name", name);
        return (T) this;
    }

    public String getType() {
        return mapWrap.getString("type");
    }

    public T setType(String type) {
        put("type", type);
        return (T) this;
    }

    public String getComment() {
        return mapWrap.getString("comment", getName());
    }

    public T setComment(String comment) {
        put("comment", comment);
        return (T) this;
    }

    public String getLength() {
        return mapWrap.getString("length");
    }

    public T setLength(String length) {
        put("length", length);
        return (T) this;
    }

    public String getAutoincrement() {
        return mapWrap.getString("autoincrement");
    }

    public T setAutoincrement(String autoincrement) {
        put("autoincrement", autoincrement);
        return (T) this;
    }

    public String getIndex() {
        return mapWrap.getString("index");
    }

    public T setIndex(String index) {
        put("index", index);
        return (T) this;
    }

    public String getEncrypt() {
        if (isPassword()) {
            return "md5";
        }
        return mapWrap.getString("encrypt");
    }

    public T setEncrypt(String encrypt) {
        put("encrypt", encrypt);
        return (T) this;
    }

    public String getNullable() {
        return mapWrap.getString("nullable");
    }

    public T setNullable(String nullable) {
        put("nullable", nullable);
        return (T) this;
    }

    public String getCharset() {
        return mapWrap.getString("charset");
    }

    public T setCharset(String charset) {
        put("charset", charset);
        return (T) this;
    }

    public String getValue() {
        return mapWrap.getString("value");
    }

    public T setValue(String value) {
        put("value", value);
        return (T) this;
    }

    public String getPassword() {
        return mapWrap.getString("password");
    }

    public T setPassword(String password) {
        put("password", password);
        return (T) this;
    }

    public String getDatabase() {
        return mapWrap.getString("database");
    }

    public T setDatabase(String databaseName) {
        put("database", databaseName);
        return (T) this;
    }

    public String getTableName() {
        return mapWrap.getString("tableName");
    }

    public T setTableName(String tableName) {
        put("tableName", tableName);
        return (T) this;
    }

    public int getDisplaySize() {
        return mapWrap.getInt("displaySize");
    }

    public T setDisplaySize(int displaySize) {
        put("displaySize", displaySize);
        return (T) this;
    }

    public boolean isExist() {
        return mapWrap.getBoolean("exist");
    }

    public T setExist(boolean exist) {
        put("exist", exist);
        return (T) this;
    }

    public int getSortIndex() {
        return mapWrap.getInt("sort_index", 0);
    }

    public T setSortIndex(int sortIndex) {
        put("sort_index", sortIndex);
        return (T) this;
    }




    /**
     * 校验必须属性值配置
     */
    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(getName())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_COLUMN_ERROR1)
                    + "\n\tat " + getStackTrace("name"));
        }
        if (FastStringUtils.isEmpty(getType())) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_COLUMN_ERROR2, "'" + getName() + "'")

                    + "\n\tat " + getStackTrace("type"));
        }
        put("validated", true);
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


    public String getPlainTick() {
        String baseTick = getName()
                + getType()
                + getLength()
                + getNullable()
                + getAutoincrement()
                + getIndex()
                + getCharset()
                + getPrimary()
                + getValue()
                + getComment();
        if (containsKey("version")) {
            baseTick = baseTick + mapWrap.getString("version");
        }
        return baseTick;
    }

    public String getModifyTicket() {
        String modifyTicket = mapWrap.getString("modifyTicket");
        if (FastStringUtils.isEmpty(modifyTicket)) {
            modifyTicket = FastChar.getSecurity().MD5_Encrypt(getPlainTick());
            put("modifyTicket", modifyTicket);
        }
        return modifyTicket;
    }


    /**
     * 合并
     */
    public FastColumnInfo<?> merge(FastColumnInfo<?> info) {
        this.putAll(info);
        return this;
    }


    public FastColumnInfo<?> copy() {
        FastColumnInfo<?> fastColumnInfo = newInstance();
        fastColumnInfo.putAll(this);
        fastColumnInfo.setFileName(this.getFileName());
        fastColumnInfo.setLineNumber(this.getLineNumber());
        return fastColumnInfo;
    }


    public String toSimpleInfo() {
        return getClass().getSimpleName() + " { name = '" + getName() + "' ,table = '" + getTableName() + "', database = " + FastChar.getDatabases().get(getDatabase()).toSimpleInfo() + " } ";
    }


}
