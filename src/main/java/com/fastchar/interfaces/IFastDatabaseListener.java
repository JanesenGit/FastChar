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
    Boolean onBeforeCreateDatabase(FastDatabaseInfo databaseInfo);


    /**
     * 创建数据库回调
     * @param databaseInfo 数据库信息
     * @return 布尔值，true：允许，false：不允许
     */
    void onAfterCreateDatabase(FastDatabaseInfo databaseInfo);

    /**
     * 创建表格回调
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @return 布尔值，true：允许，false：不允许
     */
    Boolean onBeforeCreateTable(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo);


    /**
     * 创建表格回调
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @return 布尔值，true：允许，false：不允许
     */
    void onAfterCreateTable(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo);


    /**
     * 添加表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    Boolean onBeforeAddTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);

    /**
     * 添加表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    void onAfterAddTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);


    /**
     * 修改表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    Boolean onBeforeAlterTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);

    /**
     * 修改表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值，true：允许，false：不允许
     */
    void onAfterAlterTableColumn(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo);


}
