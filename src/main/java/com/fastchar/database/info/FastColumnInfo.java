package com.fastchar.database.info;

import com.fastchar.core.FastBaseInfo;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastDatabaseException;

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
        return FastStringUtils.defaultValue(nullable, "null").replace(" ", "").equalsIgnoreCase("notnull");
    }

    public boolean isEncrypt() {
        if (FastStringUtils.isNotEmpty(encrypt)) {
            if (encrypt.equalsIgnoreCase("md5")) {
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
        return type.equalsIgnoreCase("int");
    }

    public boolean isFloatType() {
        return type.equalsIgnoreCase("float")
                || type.equalsIgnoreCase("decimal")
                || type.equalsIgnoreCase("money")
                || type.equalsIgnoreCase("numeric")
                || type.equalsIgnoreCase("real")
                || type.equalsIgnoreCase("smallmoney");
    }


    /**
     * 校验必须属性值配置
     */
    public void validate() throws FastDatabaseException {
        if (FastStringUtils.isEmpty(name)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo("Db_Column_Error1")
                    + "\n\tat " + getStackTrace("name"));
        }
        if (FastStringUtils.isEmpty(type)) {
            throw new FastDatabaseException(FastChar.getLocal().getInfo("Db_Column_Error2", "'" + name + "'")

                    + "\n\tat " + getStackTrace("type"));
        }

    }


    public String getModifyTick() {
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
        return FastChar.getSecurity().MD5_Encrypt(tick);
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
