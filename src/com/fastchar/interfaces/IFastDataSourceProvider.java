package com.fastchar.interfaces;

import com.fastchar.database.info.FastDatabaseInfo;

import javax.sql.DataSource;

public interface IFastDataSourceProvider {

    DataSource getDataSource(FastDatabaseInfo databaseInfo);



}
