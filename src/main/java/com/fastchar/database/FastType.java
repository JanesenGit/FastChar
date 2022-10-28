package com.fastchar.database;

import com.fastchar.enums.FastDatabaseType;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastType {
    private static final List<String> TYPE_RELATION = new ArrayList<>(16);
    static {
        //mysql_sqlsever_oracle_
        TYPE_RELATION.add("LONGTEXT_NTEXT_LONG_");
        TYPE_RELATION.add("DOUBLE_DECIMAL_FLOAT_");
    }


    private static final Map<String, String> MYSQL_TYPES = new HashMap<>(16);
    static {
        MYSQL_TYPES.put("VARCHAR", "java.lang.String");
        MYSQL_TYPES.put("CHAR", "java.lang.String");
        MYSQL_TYPES.put("BLOB", "[B");
        MYSQL_TYPES.put("LONGBLOB", "[B");
        MYSQL_TYPES.put("MEDIUMBLOB", "[B");
        MYSQL_TYPES.put("TINYBLOB", "[B");
        MYSQL_TYPES.put("TEXT", "java.lang.String");
        MYSQL_TYPES.put("LONGTEXT", "java.lang.String");
        MYSQL_TYPES.put("FULLTEXT", "java.lang.String");
        MYSQL_TYPES.put("MEDIUMTEXT", "java.lang.String");
        MYSQL_TYPES.put("INTEGER", "java.lang.Long");
        MYSQL_TYPES.put("TINYINT", "java.lang.Integer");
        MYSQL_TYPES.put("SMALLINT", "java.lang.Integer");
        MYSQL_TYPES.put("MEDIUMINT", "java.lang.Integer");
        MYSQL_TYPES.put("BIT", "java.lang.Boolean");
        MYSQL_TYPES.put("BIGINT", "java.math.BigInteger");
        MYSQL_TYPES.put("FLOAT", "java.lang.Float");
        MYSQL_TYPES.put("DOUBLE", "java.lang.Double");
        MYSQL_TYPES.put("DECIMAL", "java.math.BigDecimal");
        MYSQL_TYPES.put("BOOLEAN", "java.lang.Integer");
        MYSQL_TYPES.put("ID", "java.lang.Long");
        MYSQL_TYPES.put("DATE", "java.sql.Date");
        MYSQL_TYPES.put("TIME", "java.sql.Time");
        MYSQL_TYPES.put("DATETIME", "java.sql.Timestamp");
        MYSQL_TYPES.put("TIMESTAMP", "java.sql.Timestamp");
        MYSQL_TYPES.put("YEAR", "java.sql.Date");
    }


    private static final Map<String, String> SQL_SEVER_TYPES = new HashMap<>(16);
    static {
        SQL_SEVER_TYPES.put("BIGINT", "java.lang.Long");
        SQL_SEVER_TYPES.put("TIMESTAMP", "[B");
        SQL_SEVER_TYPES.put("BINARY", "[B");
        SQL_SEVER_TYPES.put("BIT", "java.lang.Boolean");
        SQL_SEVER_TYPES.put("CHAR", "java.lang.String");
        SQL_SEVER_TYPES.put("DECIMAL", "java.math.BigDecimal");
        SQL_SEVER_TYPES.put("MONEY", "java.math.BigDecimal");
        SQL_SEVER_TYPES.put("SMALLMONEY", "java.math.BigDecimal");
        SQL_SEVER_TYPES.put("FLOAT", "java.lang.Double");
        SQL_SEVER_TYPES.put("INT", "java.lang.Integer");
        SQL_SEVER_TYPES.put("IMAGE", "[B");
        SQL_SEVER_TYPES.put("VARBINARY", "[B");
        SQL_SEVER_TYPES.put("VARCHAR", "java.lang.String");
        SQL_SEVER_TYPES.put("TEXT", "java.lang.String");
        SQL_SEVER_TYPES.put("NCHAR", "java.lang.String");
        SQL_SEVER_TYPES.put("NVARCHAR", "java.lang.String");
        SQL_SEVER_TYPES.put("NTEXT", "java.lang.String");
        SQL_SEVER_TYPES.put("NUMERIC", "java.math.BigDecimal");
        SQL_SEVER_TYPES.put("REAL", "java.lang.Float");
        SQL_SEVER_TYPES.put("SMALLINT", "java.lang.Short");
        SQL_SEVER_TYPES.put("DATETIME", "java.sql.Timestamp");
        SQL_SEVER_TYPES.put("SMALLDATETIME", "java.sql.Timestamp");
        SQL_SEVER_TYPES.put("UDT", "[B");
        SQL_SEVER_TYPES.put("TINYINT", "java.lang.Short");
        SQL_SEVER_TYPES.put("XML", "java.lang.String");
        SQL_SEVER_TYPES.put("TIME", "java.sql.Time");
        SQL_SEVER_TYPES.put("DATE", "java.sql.Date");
        SQL_SEVER_TYPES.put("DATETIME2", "java.sql.Timestamp");
    }


    private static final Map<String, String> ORACLE_TYPES = new HashMap<>(16);
    static {
        ORACLE_TYPES.put("CHAR", "java.lang.String");
        ORACLE_TYPES.put("VARCHAR2", "java.lang.String");
        ORACLE_TYPES.put("LONG", "java.lang.String");
        ORACLE_TYPES.put("FLOAT", "java.lang.Float");
        ORACLE_TYPES.put("FLOAT(24)", "java.lang.Double");
        ORACLE_TYPES.put("NUMBER(3,0)", "java.lang.Boolean");
        ORACLE_TYPES.put("NUMBER", "java.lang.Integer");
        ORACLE_TYPES.put("DATE", "java.sql.Date");
        ORACLE_TYPES.put("RAW", "[B");
        ORACLE_TYPES.put("BLOB RAW", "[B");
        ORACLE_TYPES.put("CLOB RAW", "java.lang.String");
    }


    public static boolean isMySqlType(String type) {
        return MYSQL_TYPES.containsKey(type.toUpperCase());
    }

    public static boolean isSqlServerType(String type) {
        return SQL_SEVER_TYPES.containsKey(type.toUpperCase());
    }

    public static boolean isOracleType(String type) {
        return ORACLE_TYPES.containsKey(type.toUpperCase());
    }

    public static Class<?> getTypeClass(String type) {
        String javaClassName = null;
        if (isMySqlType(type)) {
            javaClassName = MYSQL_TYPES.get(type.toUpperCase());
        }else if (isOracleType(type)) {
            javaClassName = ORACLE_TYPES.get(type.toUpperCase());
        }else if (isSqlServerType(type)) {
            javaClassName = SQL_SEVER_TYPES.get(type.toUpperCase());
        }
        if (FastStringUtils.isEmpty(javaClassName)) {
            return null;
        }
        return FastClassUtils.getClass(javaClassName);
    }
    
    
    public static boolean isNumberType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Number.class.isAssignableFrom(aClass);
    }

    public static boolean isIntType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Integer.class.isAssignableFrom(aClass);
    }

    public static boolean isFloatType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Float.class.isAssignableFrom(aClass);
    }

    public static boolean isDoubleType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Double.class.isAssignableFrom(aClass);
    }


    public static boolean isStringType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return String.class.isAssignableFrom(aClass);
    }

    public static boolean isBigStringType(String type) {
        return "longtext".equalsIgnoreCase(type)
                || "ntext".equalsIgnoreCase(type)
                || "long".equalsIgnoreCase(type);
    }


    public static boolean isByteArrayType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return byte[].class.isAssignableFrom(aClass);
    }

    public static boolean isSqlDateType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Date.class.isAssignableFrom(aClass);
    }


    public static boolean isSqlTimeType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Time.class.isAssignableFrom(aClass);
    }

    public static boolean isTimestampType(String type) {
        Class<?> aClass = getTypeClass(type);
        if (aClass == null) {
            return false;
        }
        return Timestamp.class.isAssignableFrom(aClass);
    }


    public static String convertType(String dbType, String type) {
        int index = -1;
        if (FastDatabaseType.MYSQL.name().equalsIgnoreCase(dbType)) {
            if (isMySqlType(type)) {
                return type;
            }
            index = 0;
        } else if (FastDatabaseType.SQL_SERVER.name().equalsIgnoreCase(dbType)) {
            if (isSqlServerType(type)) {
                return type;
            }
            index = 1;
        }else if (FastDatabaseType.ORACLE.name().equalsIgnoreCase(dbType)) {
            if (isOracleType(type)) {
                return type;
            }
            index = 2;
        }

        if (index != -1) {
            for (String convertType : TYPE_RELATION) {
                if (convertType.contains(type.toUpperCase() + "_")) {
                    String[] strings = FastStringUtils.splitByWholeSeparator(convertType,"_");
                    return strings[index].toLowerCase();
                }
            }
        }
        return type;
    }


}

