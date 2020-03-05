package com.fastchar.database;

import com.fastchar.annotation.AFastEntity;
import com.fastchar.core.FastEntity;

@AFastEntity(false)
public class FastRecord extends FastEntity<FastRecord> {
    private static final long serialVersionUID = -5746145908981682188L;
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
