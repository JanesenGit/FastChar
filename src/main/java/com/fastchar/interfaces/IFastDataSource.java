package com.fastchar.interfaces;

import com.fastchar.database.info.FastDatabaseInfo;

import javax.sql.DataSource;

/**
 * 数据源接口
 */
public interface IFastDataSource  {

    /**
     * 获取数据源
     * @param databaseInfo 数据库信息
     * @return 数据源
     */
    DataSource getDataSource(FastDatabaseInfo databaseInfo);


}
