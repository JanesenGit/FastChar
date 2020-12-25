package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastDatabaseException;

import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

public class FastColumnInfo<T> extends FastBaseInfo {

    public static FastColumnInfo<?> newInstance() {
        return FastChar.getOverrides().newInstance(FastColumnInfo.class);
    }

    protected FastColumnInfo() {
    }


    private String databaseName;
    private String tableName;
    private String modifyTick;

    private String primary;
    private String name;
    private String type;
    private String comment;
    private String length;
    private String autoincrement;
    private String index;//true 默认为 normal 或者直接设置 "normal","fulltext","spatial","unique"
    private String encrypt;
    private String nullable;
    private String charset;
    private String value;
    private String password;//是否为密码字段

    public boolean isPrimary() {
        return FastBooleanUtils.formatToBoolean(primary, false);
    }

    public boolean isAutoincrement() {
        return FastBooleanUtils.formatToBoolean(autoincrement, false);
    }

    public boolean isPassword() {
        return FastBooleanUtils.formatToBoolean(password, false);
    }

    public boolean isNotNull() {
        return "notnull".equalsIgnoreCase(FastStringUtils.defaultValue(nullable, "null").replace(" ", ""));
    }

    public boolean isEncrypt() {
        if (FastStringUtils.isNotEmpty(encrypt)) {
            if ("md5".equalsIgnoreCase(encrypt)) {
                return true;
            }
        }
        return FastBooleanUtils.formatToBoolean(encrypt, false);
    }

    public String getPrimary() {
        return primary;
    }

    public T setPrimary(String primary) {
        this.primary = primary;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getType() {
        return type;
    }

    public T setType(String type) {
        this.type = type;
        return (T) this;
    }

    public String getComment() {
        return comment;
    }

    public T setComment(String comment) {
        this.comment = comment;
        return (T) this;
    }

    public String getLength() {
        return length;
    }

    public T setLength(String length) {
        this.length = length;
        return (T) this;
    }

    public String getAutoincrement() {
        return autoincrement;
    }

    public T setAutoincrement(String autoincrement) {
        this.autoincrement = autoincrement;
        return (T) this;
    }

    public String getIndex() {
        return index;
    }

    public T setIndex(String index) {
        this.index = index;
        return (T) this;
    }

    public String getEncrypt() {
        if (isPassword()) {
            return "md5";
        }
        return encrypt;
    }

    public T setEncrypt(String encrypt) {
        this.encrypt = encrypt;
        return (T) this;
    }

    public String getNullable() {
        return nullable;
    }

    public T setNullable(String nullable) {
        this.nullable = nullable;
        return (T) this;
    }

    public String getCharset() {
        return charset;
    }

    public T setCharset(String charset) {
        this.charset = charset;
        return (T) this;
    }

    public String getValue() {
        return value;
    }

    public T setValue(String value) {
        this.value = value;
        return (T) this;
    }

    public String getPassword() {
        return password;
    }

    public T setPassword(String password) {
        this.password = password;
        return (T) this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public FastColumnInfo<T> setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public FastColumnInfo<T> setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public boolean isIntType() {
        return "int".equalsIgnoreCase(type);
    }

    public boolean isFloatType() {
        return "float".equalsIgnoreCase(type)
                || "decimal".equalsIgnoreCase(type)
                || "money".equalsIgnoreCase(type)
                || "numeric".equalsIgnoreCase(type)
                || "real".equalsIgnoreCase(type)
                || "smallmoney".equalsIgnoreCase(type);
    }


    /**
     * 校验必须属性值配置
     */
    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(name)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_COLUMN_ERROR1)
                    + "\n\tat " + getStackTrace("name"));
        }
        if (FastStringUtils.isEmpty(type)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo(FastCharLocal.DB_COLUMN_ERROR2, "'" + name + "'")

                    + "\n\tat " + getStackTrace("type"));
        }
    }



    public String getModifyTick() {
        if (FastStringUtils.isEmpty(modifyTick)) {
            String tick = this.name
                    + this.type
                    + this.length
                    + this.nullable
                    + this.autoincrement
                    + this.index
                    + this.charset
                    + this.primary
                    + this.value
                    + this.comment;
            modifyTick = FastChar.getSecurity().MD5_Encrypt(tick);
        }
        return modifyTick;
    }


    /**
     * 合并
     *
     * @param info
     */
    public FastColumnInfo<?> merge(FastColumnInfo<?> info) {
        for (Object key : info.keySet()) {
            this.set(String.valueOf(key), info.get(key));
        }
        if (FastStringUtils.isNotEmpty(info.getFileName())) {
            setFileName(info.getFileName());
        }
        if (info.getLineNumber() != 0) {
            setLineNumber(info.getLineNumber());
        }
        return this;
    }


    public FastColumnInfo<?> copy() {
        FastColumnInfo<?> fastColumnInfo = newInstance();
        for (Object key : keySet()) {
            fastColumnInfo.set(String.valueOf(key), get(key));
        }
        fastColumnInfo.setFileName(this.getFileName());
        fastColumnInfo.setLineNumber(this.getLineNumber());
        fastColumnInfo.setTagName(this.getTagName());
        fastColumnInfo.fromProperty();
        return fastColumnInfo;
    }


}
