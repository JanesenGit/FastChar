package com.fastchar.database;

import com.fastchar.annotation.AFastEntity;
import com.fastchar.core.FastEntity;

@AFastEntity(false)
public class FastRecord extends FastEntity<FastRecord> {
    private String tableName;
    @Override
    public String getTableName() {
        return tableName;
    }

    public FastRecord setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
}
