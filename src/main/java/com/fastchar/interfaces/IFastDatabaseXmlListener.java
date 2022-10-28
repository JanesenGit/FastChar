package com.fastchar.interfaces;

import com.fastchar.database.FastDatabaseXml;

import java.io.File;

/**
 * 解析fast-database-*.xml监听类
 */
public interface IFastDatabaseXmlListener {

    /**
     * 当解析fast-database-*.xml时触发
     *
     * @param databaseXml 文件
     * @return 布尔值，true或null：允许，false：不允许
     */
    Boolean onBeforeParseDatabaseXml(File databaseXml);


    /**
     * 当解析fast-database-*.xml后触发
     *
     * @param databaseInfoHandler 解析对象
     */
    void onAfterParseDatabaseXml(FastDatabaseXml.DatabaseInfoHandler databaseInfoHandler);

}
