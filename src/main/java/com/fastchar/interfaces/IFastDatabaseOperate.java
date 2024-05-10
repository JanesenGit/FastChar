package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;

/**
 * 数据库操作接口
 */
@AFastPriority
public interface IFastDatabaseOperate {


    /**
     * 获取连接驱动类
     * @param databaseInfo 数据库对象
     * @throws Exception 异常
     */
    String getConnectionDriverClass(FastDatabaseInfo databaseInfo) throws Exception;

    /**
     * 获取连接数据库的端口
     * @param databaseInfo 数据库对象
     * @throws Exception 异常
     */
    int getConnectionPort(FastDatabaseInfo databaseInfo) throws Exception;

    /**
     * 获取连接的url地址
     * @param databaseInfo 数据库对象
     * @throws Exception 异常
     */
    String getConnectionUrl(FastDatabaseInfo databaseInfo) throws Exception;


    /**
     * 从数据库中抓取信息并同步到对象中
     * @param databaseInfo 数据库对象
     * @throws Exception 异常
     */
    void fetchDatabaseInfo(FastDatabaseInfo databaseInfo) throws Exception;

    /**
     * 创建数据库
     * @param databaseInfo 数据库信息
     * @throws Exception 异常
     */
    void createDatabase(FastDatabaseInfo databaseInfo) throws Exception;

    /**
     * 检测表格是否存在
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @return 布尔值
     * @throws Exception 异常
     */
    boolean checkTableExists(FastDatabaseInfo databaseInfo, FastTableInfo<?> tableInfo) throws Exception;

    /**
     * 创建表格
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @throws Exception 异常
     */
    void createTable(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo) throws Exception;

    /**
     * 检测表格列是否已存在
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @return 布尔值
     * @throws Exception 异常
     */
    boolean checkColumnExists(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo, FastColumnInfo<?> columnInfo) throws Exception;

    /**
     * 添加表格列
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @throws Exception 异常
     */
    void addColumn(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo,FastColumnInfo<?> columnInfo) throws Exception;

    /**
     * 修改表格信息
     * @param databaseInfo 数据库信息
     * @param tableInfo 表格信息
     * @param columnInfo 列信息
     * @throws Exception 异常
     */
    void alterColumn(FastDatabaseInfo databaseInfo,FastTableInfo<?> tableInfo,FastColumnInfo<?> columnInfo) throws Exception;

}
