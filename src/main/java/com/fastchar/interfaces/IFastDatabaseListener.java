package com.fastchar.interfaces;

import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;

/**
 * 数据库操作的监听类
 * @author 沈建（Janesen）
 * @date 2020/7/15 10:05
 */
public interface IFastDatabaseListener {

    /**
     * 创建数据库回调
     * @param databaseInfo 数据库信息
     * @return 布尔值，true：允许，false：不允许
     */
    boolean onCreateDatabase(FastDatabaseInfo databaseInfo);

    /**
     * 创建表格回调
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @return 布尔值，true：允许，false：不允许
     */
    boolean onCreateTable(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo);

    /**
     * 添加表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    boolean onAddTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);

    /**
     * 修改表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    boolean onAlterTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);
}
