package com.fastchar.interfaces;

import com.fastchar.core.FastEntity;
import com.fastchar.core.FastHandler;
import com.fastchar.database.FastPage;

import java.util.List;

public interface IFastSqlOperateListener {

    List<FastEntity<?>> select(FastHandler handler, String sqlStr, Object... params) throws Exception;

    FastPage<FastEntity<?>> select(FastHandler handler, int page, int pageSize, String sqlStr, Object... params) throws Exception;

    FastEntity<?> selectFirst(FastHandler handler, String sqlStr, Object... params) throws Exception;

    FastEntity<?> selectLast(FastHandler handler, String sqlStr, Object... params) throws Exception;

    int update(FastHandler handler,String sqlStr, Object... params) throws Exception;

    int insert(FastHandler handler,String sqlStr, Object... params) throws Exception;

    boolean run(FastHandler handler,String sql) throws Exception;

    int[] batch(FastHandler handler, List<String> sqlList, int batchSize) throws Exception;

    int[] batch(FastHandler handler, String sqlStr, List<Object[]> params, int batchSize) throws Exception;

}
