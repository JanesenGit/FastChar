package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastConstant;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;

/**
 * 数据库操作
 */
@AFastPriority
public interface IFastDatabaseOperateProvider {

    void fetchDatabaseInfo(FastDatabaseInfo databaseInfo) throws Exception;

    void createDatabase(FastDatabaseInfo databaseInfo) throws Exception;

    boolean checkTableExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception;

    void createTable(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo) throws Exception;

    boolean checkColumnExists(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception;

    void addColumn(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo,FastColumnInfo<?> columnInfo) throws Exception;

    void alterColumn(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo,FastColumnInfo<?> columnInfo) throws Exception;

}
