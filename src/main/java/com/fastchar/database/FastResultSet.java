package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastResultSet {

    private final ResultSet resultSet;
    private boolean ignoreCase;

    public FastResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }


    private Object getValue(String key) throws SQLException {
        Object object = resultSet.getObject(key);
        if (FastChar.getConstant().isJdbcParseToTimestamp()) {
            if (object instanceof LocalDateTime) {
                return Timestamp.valueOf((LocalDateTime) object);
            }
        }
        return object;
    }

    public FastEntity<?> getFirstResult() {
        if (resultSet != null) {
            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final String tableName = resultSetMetaData.getTableName(1);
                if (resultSet.next()) {
                    FastRecord fastRecord = new FastRecord();
                    fastRecord.setTableName(tableName);
                    fastRecord.setIgnoreCase(this.ignoreCase);
                    int columnCount = resultSetMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        try {
                            String key = resultSetMetaData.getColumnLabel(i);
                            Object value = getValue(key);
                            fastRecord.put(key, value);
                        } catch (Exception ignored) {
                        }
                    }
                    return fastRecord;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public FastEntity<?> getLastResult() {
        if (resultSet != null) {
            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final String tableName = resultSetMetaData.getTableName(1);
                if (resultSet.last()) {
                    FastRecord fastRecord = new FastRecord();
                    fastRecord.setTableName(tableName);
                    int columnCount = resultSetMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        try {
                            String key = resultSetMetaData.getColumnLabel(i);
                            Object value = getValue(key);
                            fastRecord.put(key, value);
                        } catch (Exception ignored) {
                        }
                    }
                    return fastRecord;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public List<FastEntity<?>> getListResult() {
        if (resultSet != null) {
            try {
                List<FastEntity<?>> list = new ArrayList<>();
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final String tableName = resultSetMetaData.getTableName(1);
                int columnCount = resultSetMetaData.getColumnCount();
                while (resultSet.next()) {
                    FastRecord fastRecord = new FastRecord();
                    fastRecord.setTableName(tableName);
                    fastRecord.setIgnoreCase(this.ignoreCase);
                    Map<String, Integer> keyCount = new HashMap<>(16);
                    for (int i = 1; i <= columnCount; i++) {
                        try {
                            String key = resultSetMetaData.getColumnLabel(i);
                            Object value = getValue(key);
                            if (keyCount.containsKey(key)) {
                                keyCount.put(key, keyCount.get(key) + 1);
                                key = key + "(" + keyCount.get(key) + ")";
                            } else {
                                keyCount.put(key, 0);
                            }
                            fastRecord.put(key, value);
                        } catch (Exception ignored) {
                        }
                    }
                    list.add(fastRecord);
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Map<String, Object> getMap() {
        if (resultSet != null) {
            try {
                Map<String, Object> map = new HashMap<>(16);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                if (resultSet.last()) {
                    int columnCount = resultSetMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        try {
                            String key = resultSetMetaData.getColumnLabel(i);
                            Object value = getValue(key);
                            map.put(key, value);
                        } catch (Exception ignored) {
                        }
                    }
                }
                return map;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public FastResultSet setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }
}
