package com.fastchar.interfaces;

import com.fastchar.database.FastDb;

/**
 * 当使用FastDb执行Sql语句时的全局监听
 * @author 沈建（Janesen）
 * @date 2021/6/3 16:59
 */
public interface IFastSqlListener {

    void onRunSql(FastDb fastDb, Object sql, Object params, int resultCount);

}
