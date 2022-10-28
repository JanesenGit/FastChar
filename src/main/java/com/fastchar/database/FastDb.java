package com.fastchar.database;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.core.FastHandler;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.sql.FastSql;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastSqlListener;
import com.fastchar.interfaces.IFastSqlOperateListener;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 数据库sql操作
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class FastDB {
    private static final ConcurrentSkipListSet<String> CACHE_TABLE = new ConcurrentSkipListSet<>();

    private FastDatabaseTransaction fastTransaction;

    /**
     * 是否开启了事务
     * @return 布尔值
     */
    public boolean isTransaction() {
        return (fastTransaction != null && fastTransaction.isValid());
    }

    /**
     * 开启事务
     */
    public synchronized void beginTransaction() {
        if (isTransaction()) {
            return;
        }
        fastTransaction = new FastDatabaseTransaction();
    }

    /**
     * 结束事务
     */
    public synchronized void endTransaction() {
        if (fastTransaction != null && fastTransaction.isValid()) {
            fastTransaction.commit();
        }
    }

    /**
     * 回滚事务
     */
    public synchronized void rollbackTransaction() {
        if (fastTransaction != null && fastTransaction.isValid()) {
            fastTransaction.rollback();
        }
    }


    private boolean log = true;
    private boolean listener = true;
    private boolean operateListener = true;
    private boolean useCache = true;
    private boolean cache;
    private String database;
    private long inTime;
    private long firstBuildTime;
    private long lastBuildTime;
    private boolean ignoreCase = false;
    private volatile FastDatabaseInfo databaseInfo;

    private FastDatabaseInfo getDatabaseInfo() {
        if (databaseInfo == null) {
            synchronized (FastDB.class) {
                if (databaseInfo == null) {
                    databaseInfo = FastChar.getDatabases().get(database);
                }
            }
        }
        return databaseInfo;
    }

    public Connection getConnection() throws SQLException {
        FastDatabaseInfo databaseInfo = getDatabaseInfo();
        if (databaseInfo == null) {
            return null;
        }
        DataSource dataSource = databaseInfo.getDataSource();
        if (dataSource == null) {
            return null;
        }

        Connection connection = dataSource.getConnection();

        if (fastTransaction != null && fastTransaction.isValid()) {
            fastTransaction.registerConnection(connection);
        }

        FastDatabaseTransaction threadTransaction = FastDatabaseTransaction.getThreadTransaction();
        if (threadTransaction != null && threadTransaction.isValid()) {
            threadTransaction.registerConnection(connection);
        }
        return connection;
    }


    private String buildCacheKeyBySql(String sqlStr, Object... params) {
        FastDatabaseInfo fastDatabaseInfo = getDatabaseInfo();
        return FastChar.getSecurity().MD5_Encrypt(fastDatabaseInfo.getCode() + sqlStr + Arrays.toString(params));
    }

    private String buildCacheTag(String... tableNames) {
        FastDatabaseInfo fastDatabaseInfo = getDatabaseInfo();
        for (int i = 0; i < tableNames.length; i++) {
            tableNames[i] = fastDatabaseInfo.getCode() + "@" + tableNames[i];
        }
        return "@" + FastStringUtils.join(tableNames, "@") + "@";
    }

    private String buildCacheTagBySql(String sqlStr) {
        List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
        CACHE_TABLE.addAll(allTables);
        return buildCacheTag(allTables.toArray(new String[]{}));
    }


    /**
     * 执行查询（带参数）
     *
     * @param sqlStr 查询语句
     * @param params 语句参数
     * @return FastEntity集合
     */
    public List<FastEntity<?>> select(String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                List<FastEntity<?>> result = iFastSqlOperateListener.select(handler, sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return new ArrayList<>();
        }
        String cacheKey = null;
        String cacheTag = null;
        inTime = System.currentTimeMillis();
        if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
            cacheKey = buildCacheKeyBySql(sqlStr, params);
            cacheTag = buildCacheTagBySql(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            if (iFastCacheProvider.exists(cacheTag, cacheKey)) {
                List<FastEntity<?>> cache = iFastCacheProvider.get(cacheTag, cacheKey);
                if (cache != null) {
                    cacheLog(sqlStr);
                    return cache;
                }
            }
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int fetchSize = -1;
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            preparedStatement = connection.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value != null) {
                    preparedStatement.setObject(i + 1, value);
                } else {
                    preparedStatement.setNull(i + 1, Types.NULL);
                }
            }
            resultSet = preparedStatement.executeQuery();
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).setIgnoreCase(this.ignoreCase).getListResult();
            if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                iFastCacheProvider.set(cacheTag, cacheKey, listResult);
            }
            fetchSize = listResult.size();
            return listResult;
        } finally {
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params, fetchSize);
        }
    }


    /**
     * 查询数据
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @param sqlStr   查询语句
     * @param params   语句参数
     * @return FastEntity 分页数据
     * @throws Exception 异常信息
     */
    public FastPage<FastEntity<?>> select(int page, int pageSize, String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                FastPage<FastEntity<?>> result = iFastSqlOperateListener.select(handler, page,pageSize,sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return new FastPage<>();
        }
        inTime = System.currentTimeMillis();
        FastPage<FastEntity<?>> fastPage = new FastPage<>();
        String type = getDatabaseInfo().getType();
        if (page > 0) {
            String countSql = FastSql.getInstance(type).getCountSql(sqlStr, "ct");

            int countRow = 0;
            FastEntity<?> countResult = selectFirst(countSql, params);
            if (countResult != null) {
                countRow = countResult.getInt("ct");
            }
            fastPage.setTotalRow(countRow);
            fastPage.setPage(page);
            fastPage.setPageSize(pageSize);
            fastPage.setTotalPage(countRow % pageSize == 0 ? countRow / pageSize : countRow / pageSize + 1);
        } else {
            fastPage.setTotalPage(1);
            fastPage.setPage(1);
        }
        String pageSql = FastSql.getInstance(type).buildPageSql(sqlStr, page, pageSize);
        FastSqlInfo fastSqlInfo = FastSqlInfo.newInstance().setType(type).setSql(sqlStr).setParams(params);
        fastPage.setSqlInfo(fastSqlInfo);
        fastPage.setList(select(pageSql, params));
        if (page < 0) {
            fastPage.setTotalRow(fastPage.getList().size());
            fastPage.setPageSize(fastPage.getTotalRow());
        }
        return fastPage;
    }


    /**
     * 执行查询，返回第一条数据（带参数）
     *
     * @param sqlStr 查询语句
     * @param params 语句参数
     * @return FastEntity
     */
    public FastEntity<?> selectFirst(String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                FastEntity<?> result = iFastSqlOperateListener.selectFirst(handler,sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return null;
        }
        inTime = System.currentTimeMillis();
        String cacheKey = null;
        String cacheTag = null;
        if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
            cacheKey = buildCacheKeyBySql(sqlStr, params);
            cacheTag = buildCacheTagBySql(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            if (iFastCacheProvider.exists(cacheTag, cacheKey)) {
                FastEntity<?> cache = iFastCacheProvider.get(cacheTag, cacheKey);
                if (cache != null) {
                    cacheLog(sqlStr);
                    return cache;
                }
            }
        }
        String type = getDatabaseInfo().getType();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }

            preparedStatement = connection.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value != null) {
                    preparedStatement.setObject(i + 1, value);
                } else {
                    preparedStatement.setNull(i + 1, Types.NULL);
                }
            }
            resultSet = preparedStatement.executeQuery();
            FastEntity<?> firstResult = new FastResultSet(resultSet).setIgnoreCase(this.ignoreCase).getFirstResult();
            if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                iFastCacheProvider.set(cacheTag, cacheKey, firstResult);
            }
            return firstResult;
        } finally {
            int fetchSize = 0;
            try {
                fetchSize = -1;
                if (resultSet != null && FastChar.getConstant().isLogSql() && isLog()
                        && (getDatabaseInfo().isMySql() || getDatabaseInfo().isOracle())) {
                    if (!resultSet.isLast()) {
                        resultSet.last();
                    }
                    fetchSize = resultSet.getRow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params, fetchSize);
        }
    }


    /**
     * 执行查询，返回最后一条数据（带参数）
     *
     * @param sqlStr 查询语句
     * @param params 语句参数
     * @return FastEntity
     */
    public FastEntity<?> selectLast(String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                FastEntity<?> result = iFastSqlOperateListener.selectLast(handler,sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return null;
        }
        inTime = System.currentTimeMillis();
        String cacheKey = null;
        String cacheTag = null;
        if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
            cacheKey = buildCacheKeyBySql(sqlStr, params);
            cacheTag = buildCacheTagBySql(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            if (iFastCacheProvider.exists(cacheTag, cacheKey)) {
                FastEntity<?> cache = iFastCacheProvider.get(cacheTag, cacheKey);
                if (cache != null) {
                    cacheLog(sqlStr);
                    return cache;
                }
            }
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            preparedStatement = connection.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value != null) {
                    preparedStatement.setObject(i + 1, value);
                } else {
                    preparedStatement.setNull(i + 1, Types.NULL);
                }
            }
            resultSet = preparedStatement.executeQuery();
            FastEntity<?> lastResult = new FastResultSet(resultSet).setIgnoreCase(this.ignoreCase).getLastResult();
            if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                iFastCacheProvider.set(cacheTag, cacheKey, lastResult);
            }
            return lastResult;
        } finally {
            int fetchSize = 0;
            try {
                fetchSize = -1;
                if (resultSet != null && FastChar.getConstant().isLogSql() && isLog()
                        && (getDatabaseInfo().isMySql() || getDatabaseInfo().isOracle())) {
                    if (!resultSet.isLast()) {
                        resultSet.last();
                    }
                    fetchSize = resultSet.getRow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params, fetchSize);
        }
    }

    /**
     * 清除表格的缓存数据
     *
     * @param tables 表格名称
     */
    public void clearCache(String... tables) {
        for (String table : tables) {
            if (CACHE_TABLE.contains(table)) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                for (String tag : tags) {
                    iFastCacheProvider.delete(tag);
                }
                CACHE_TABLE.remove(table);
            }
        }
    }

    /**
     * 执行更新sql
     *
     * @param sqlStr 更新语句
     * @param params 语句参数
     * @return 更新成功的数量
     */
    public int update(String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                int result = iFastSqlOperateListener.update(handler,sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return 0;
        }
        inTime = System.currentTimeMillis();
        if (isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            clearCache(allTables.toArray(new String[]{}));
        }

        int count = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }

            preparedStatement = connection.prepareStatement(sqlStr);
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value != null) {
                    preparedStatement.setObject(i + 1, value);
                } else {
                    preparedStatement.setNull(i + 1, Types.NULL);
                }
            }
            count = preparedStatement.executeUpdate();
        } finally {
            close(connection, preparedStatement);
            log(sqlStr, params, count);
        }
        return count;
    }


    /**
     * 执行插入数据
     *
     * @param sqlStr 添加语句
     * @param params 语句参数
     * @return 主键
     */
    public int insert(String sqlStr, Object... params) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                int result = iFastSqlOperateListener.insert(handler,sqlStr, params);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return 0;
        }
        inTime = System.currentTimeMillis();
        if (isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            clearCache(allTables.toArray(new String[]{}));
        }

        int primary = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return primary;
            }
            preparedStatement = connection.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value != null) {
                    preparedStatement.setObject(i + 1, value);
                } else {
                    preparedStatement.setNull(i + 1, Types.NULL);
                }
            }

            primary = preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()) {
                primary = resultSet.getInt(1);
            }
        } finally {
            log(sqlStr, params, 1);
            close(connection, preparedStatement, resultSet);
        }
        return primary;
    }


    /**
     * 执行复杂Sql，注意：sql文件的执行，请使用FastScriptRunner 运行
     *
     * @param sqlStr 语句
     * @return 是否成功
     * @throws Exception 异常信息
     */
    public boolean run(String sqlStr) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                boolean result = iFastSqlOperateListener.run(handler,sqlStr);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        inTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        int count = 0;
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.createStatement();
            return statement.execute(sqlStr);
        } finally {
            close(connection, statement);
            log(sqlStr, null, -1);
        }
    }


    /**
     * 批量执行sql
     *
     * @param sqlArray sql数组
     * @return 执行结果
     */
    public int[] batch(String[] sqlArray, int batchSize) throws Exception {
        return batch(Arrays.asList(sqlArray), batchSize);
    }


    /**
     * 批量执行sql
     *
     * @param sqlList sql数组
     * @return 执行结果
     */
    public int[] batch(List<String> sqlList, int batchSize) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                int[] result = iFastSqlOperateListener.batch(handler,sqlList,batchSize);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }
        }

        if (sqlList == null) {
            return new int[0];
        }
        inTime = System.currentTimeMillis();
        if (isUseCache()) {
            for (String sqlStr : sqlList) {
                List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
                clearCache(allTables.toArray(new String[]{}));
            }
        }

        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return new int[0];
            }
            List<Integer> result = new ArrayList<>();
            int count = 0;
            statement = connection.createStatement();
            for (String sql : sqlList) {
                statement.addBatch(sql);
                if (++count % batchSize == 0) {
                    try {
                        int[] executeBatch = statement.executeBatch();
                        Integer[] integers = FastArrayUtils.toObject(executeBatch);
                        if (integers != null) {
                            result.addAll(Arrays.asList(integers));
                        }
                    } finally {
                        statement.clearBatch();
                    }
                }
            }
            int[] executeBatch = statement.executeBatch();
            Integer[] integers = FastArrayUtils.toObject(executeBatch);
            if (integers != null) {
                result.addAll(Arrays.asList(integers));
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        } finally {
            close(connection, statement);
            log(sqlList, null, -1);
        }
    }


    /**
     * 批量执行sql
     *
     * @param sqlStr 共同的sql语句
     * @param params 参数数据的数组
     */
    public int[] batch(String sqlStr, List<Object[]> params, int batchSize) throws Exception {
        if (isOperateListener()) {
            List<IFastSqlOperateListener> iFastSqlOperateListeners = FastChar.getOverrides().singleInstances(false, IFastSqlOperateListener.class);
            for (IFastSqlOperateListener iFastSqlOperateListener : iFastSqlOperateListeners) {
                FastHandler handler = new FastHandler();
                handler.setCode(-1);
                handler.put("database", getDatabase());
                int[] result = iFastSqlOperateListener.batch(handler, sqlStr, params, batchSize);
                if (handler.getCode() == 0) {//拦截执行
                    return result;
                }
            }

        }

        if (FastStringUtils.isEmpty(sqlStr)) {
            return new int[0];
        }
        inTime = System.currentTimeMillis();
        if (isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            for (String table : allTables) {
                if (CACHE_TABLE.contains(table)) {
                    IFastCache iFastCacheProvider = FastChar.getCache();
                    Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                    for (String tag : tags) {
                        iFastCacheProvider.delete(tag);
                    }
                    CACHE_TABLE.remove(table);
                }
            }
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return new int[0];
            }
            int count = 0;
            List<Integer> result = new ArrayList<>();
            preparedStatement = connection.prepareStatement(sqlStr);
            if (params != null) {
                for (Object[] param : params) {
                    if (param == null) {
                        continue;
                    }
                    for (int i = 0; i < param.length; i++) {
                        Object value = param[i];
                        if (value != null) {
                            preparedStatement.setObject(i + 1, value);
                        } else {
                            preparedStatement.setNull(i + 1, Types.NULL);
                        }
                    }

                    preparedStatement.addBatch();
                    if (++count % batchSize == 0) {
                        try {
                            int[] executeBatch = preparedStatement.executeBatch();
                            Integer[] integers = FastArrayUtils.toObject(executeBatch);
                            if (integers != null) {
                                result.addAll(Arrays.asList(integers));
                            }
                        } finally {
                            preparedStatement.clearBatch();
                        }
                    }
                }
            }
            int[] executeBatch = preparedStatement.executeBatch();
            Integer[] integers = FastArrayUtils.toObject(executeBatch);
            if (integers != null) {
                result.addAll(Arrays.asList(integers));
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        } finally {
            close(connection, preparedStatement);
            log(sqlStr, params, -1);
        }
    }



    /**
     * 批量添加FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchSaveEntity(List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        return batchSaveEntity(false, entities, batchSize, checks);
    }

    /**
     * 批量添加FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchSaveEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities == null || entities.size() == 0) {
            return new int[0];
        }
        if (staticSql) {
            firstBuildTime = System.currentTimeMillis();
            List<String> sqlList = new ArrayList<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toInsertSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                sqlList.add(sqlInfo.toStaticSql());
            }
            lastBuildTime = System.currentTimeMillis();
            return this.batch(sqlList, batchSize);
        } else {
            firstBuildTime = System.currentTimeMillis();
            LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toInsertSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                    batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
                }
                batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
            }
            lastBuildTime = System.currentTimeMillis();
            List<Integer> result = new ArrayList<>();
            for (Map.Entry<String, List<Object[]>> stringListEntry : batchSqlMap.entrySet()) {
                int[] batch = this.batch(stringListEntry.getKey(), stringListEntry.getValue(), batchSize);
                for (int i : batch) {
                    result.add(i);
                }
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        }
    }

    /**
     * 批量更新FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchUpdateEntity(List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        return batchUpdateEntity(false, entities, batchSize, checks);
    }

    /**
     * 批量更新FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchUpdateEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities == null || entities.size() == 0) {
            return new int[0];
        }
        if (staticSql) {
            firstBuildTime = System.currentTimeMillis();
            List<String> sqlList = new ArrayList<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toUpdateSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                sqlList.add(sqlInfo.toStaticSql());
            }
            lastBuildTime = System.currentTimeMillis();
            return this.batch(sqlList, batchSize);
        } else {
            firstBuildTime = System.currentTimeMillis();
            LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toUpdateSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                    batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
                }
                batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
            }
            lastBuildTime = System.currentTimeMillis();
            List<Integer> result = new ArrayList<>();
            for (Map.Entry<String, List<Object[]>> stringListEntry : batchSqlMap.entrySet()) {
                int[] batch = this.batch(stringListEntry.getKey(), stringListEntry.getValue(), batchSize);
                for (int i : batch) {
                    result.add(i);
                }
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        }
    }

    /**
     * 批量删除FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchDeleteEntity(List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        return batchDeleteEntity(false, entities, batchSize, checks);
    }

    /**
     * 批量删除FastEntity实体集合，注意：如果数据量超过百万级别，建议自主拼接静态SQL语句，避免多余逻辑照成耗时过长
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchDeleteEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities == null || entities.size() == 0) {
            return new int[0];
        }
        if (staticSql) {
            firstBuildTime = System.currentTimeMillis();
            List<String> sqlList = new ArrayList<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toDeleteSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                sqlList.add(sqlInfo.toStaticSql());
            }
            lastBuildTime = System.currentTimeMillis();
            return this.batch(sqlList, batchSize);
        } else {
            firstBuildTime = System.currentTimeMillis();
            LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
            for (FastEntity<?> entity : entities) {
                FastSqlInfo sqlInfo = entity.toDeleteSql(checks);
                if (sqlInfo == null) {
                    continue;
                }
                if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                    batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
                }
                batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
            }
            lastBuildTime = System.currentTimeMillis();
            List<Integer> result = new ArrayList<>();
            for (Map.Entry<String, List<Object[]>> stringListEntry : batchSqlMap.entrySet()) {
                int[] batch = this.batch(stringListEntry.getKey(), stringListEntry.getValue(), batchSize);
                for (int i : batch) {
                    result.add(i);
                }
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        }
    }

    /**
     * 批量执行更新的sql语句集合
     *
     * @param sqlInfos  sql语句信息
     * @param batchSize 单次批量提交的数据大小
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchUpdate(List<FastSqlInfo> sqlInfos, int batchSize) throws Exception {
        if (sqlInfos == null || sqlInfos.size() == 0) {
            return new int[0];
        }
        firstBuildTime = System.currentTimeMillis();
        LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
        for (FastSqlInfo sqlInfo : sqlInfos) {
            if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
            }
            batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
            setLog(sqlInfo.isLog());
        }
        lastBuildTime = System.currentTimeMillis();
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<String, List<Object[]>> stringListEntry : batchSqlMap.entrySet()) {
            int[] batch = this.batch(stringListEntry.getKey(), stringListEntry.getValue(), batchSize);
            for (int i : batch) {
                result.add(i);
            }
        }
        return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
    }

    public void close(Connection connection) {
        if (isTransaction() || FastDatabaseTransaction.isThreadTransaction()) {
            return;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void close(Connection connection, ResultSet resultSet) {
        close(resultSet);
        close(connection);
    }

    public void close(PreparedStatement preparedStatement, ResultSet resultSet) {
        close(resultSet);
        close(preparedStatement);
    }

    public void close(Connection connection, PreparedStatement preparedStatement) {
        close(preparedStatement);
        close(connection);
    }

    public void close(Connection connection, PreparedStatement preparedStatement,
                      ResultSet resultSet, Statement statement) {
        close(resultSet);
        close(statement);
        close(preparedStatement);
        close(connection);
    }

    public void close(Connection connection, PreparedStatement preparedStatement,
                      ResultSet resultSet) {
        close(resultSet);
        close(preparedStatement);
        close(connection);
    }

    public void close(Connection connection, Statement statement) {
        close(statement);
        close(connection);
    }


    public void log(Object sql, Object params, int resultCount) {
        if (isListener()) {
            List<IFastSqlListener> iFastSqlListeners = FastChar.getOverrides().singleInstances(false, IFastSqlListener.class);
            for (IFastSqlListener iFastSqlListener : iFastSqlListeners) {
                if (iFastSqlListener != null) {
                    iFastSqlListener.onRunSql(this, sql, params, resultCount);
                }
            }
        }

        if (FastChar.getConstant().isLogSql() && isLog()) {
            if (FastStringUtils.isEmpty(String.valueOf(sql))) {
                return;
            }
            LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
            if (FastStringUtils.isNotEmpty(database)) {
                printMap.put("database", database);
            }
            if (sql instanceof List) {
                List<?> list = (List<?>) sql;
                if (list.size() < 100) {
                    for (int i = 0; i < list.size(); i++) {
                        printMap.put("Sql-" + (i + 1), String.valueOf(list.get(i)));
                    }
                } else {
                    printMap.put("Sql-1", String.valueOf(list.get(0)));
                    printMap.put("Sql-*", "……");
                    printMap.put("Sql-" + list.size(), String.valueOf(list.get(list.size() - 1)));
                }
            } else {
                sql = String.valueOf(sql).replace("\n", " ");
                printMap.put("Sql", String.valueOf(sql));
            }
            if (params != null) {
                if (params instanceof Object[]) {
                    Object[] arrayParams = (Object[]) params;
                    if (arrayParams.length < 100) {
                        printMap.put("Params", Arrays.toString(arrayParams));
                    } else {
                        printMap.put("Params", arrayParams.length + " data");
                    }
                }
                if (params instanceof List) {
                    List<?> list = (List<?>) params;
                    if (list.size() < 100) {
                        for (int i = 0; i < list.size(); i++) {
                            printMap.put("Params-" + (i + 1), Arrays.toString((Object[]) list.get(i)));
                        }
                    } else {
                        printMap.put("Params", list.size() + " data");
                    }
                }
            }
            float useTotal = FastNumberUtils.formatToFloat((System.currentTimeMillis() - inTime) / 1000.0, 6);
            float buildTotal = FastNumberUtils.formatToFloat((lastBuildTime - firstBuildTime) / 1000.0, 6);
            if (buildTotal > 0) {
                printMap.put("Build-Total", buildTotal + " seconds");
                printMap.put("Run-Total", useTotal + " seconds");
            }
            if (resultCount >= 0) {
                printMap.put("Result", String.valueOf(resultCount));
            }
            printMap.put("Total", FastNumberUtils.formatToFloat((useTotal + buildTotal), 6) + " seconds");
            printMap.put("Time", FastDateUtils.getDateString("yyyy-MM-dd HH:mm:ss:SSS"));
            int maxKeyLength = 0;
            for (Map.Entry<String, String> stringStringEntry : printMap.entrySet()) {
                maxKeyLength = Math.max(maxKeyLength, stringStringEntry.getKey().length() + 1);
            }
            FastAction threadLocalAction = FastChar.getThreadLocalAction();
            if (threadLocalAction != null) {
                if (!threadLocalAction.getAttribute().containsAttr("sqlLogList")) {
                    threadLocalAction.getAttribute().put("sqlLogList", new ArrayList<>());
                }
                ArrayList<Map<String, String>> sqlLogList = threadLocalAction.getAttribute().getObject("sqlLogList");
                sqlLogList.add(printMap);
                return;
            }

            StringBuilder print = new StringBuilder();
            for (Map.Entry<String, String> stringStringEntry : printMap.entrySet()) {
                String text = stringStringEntry.getValue();
                if (FastStringUtils.isEmpty(text)) {
                    continue;
                }
                print.append("\n").append(formatString(stringStringEntry.getKey(), maxKeyLength)).append(text);
            }
            System.out.println(color(print.toString()));
        }
    }

    private void cacheLog(String sqlStr) {
        if (isLog() && FastChar.getConstant().isDebug()) {
            System.out.println(color("Cached ：" + sqlStr));
        }
    }


    private static String color(String content) {
        return FastChar.getLog().colorStyle(13, content);
    }


    private static String formatString(String target, int targetLength) {
        StringBuilder targetBuilder = new StringBuilder(target);
        for (int i = 0; i < targetLength; i++) {
            if (i >= targetBuilder.length()) {
                targetBuilder.append(" ");
            }
        }
        target = targetBuilder.toString();
        return target + "：";
    }


    public boolean isLog() {
        return log;
    }

    public FastDB setLog(boolean log) {
        this.log = log;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public FastDB setUseCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public FastDB setDatabase(String database) {
        this.database = database;
        return this;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public FastDB setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    public boolean isCache() {
        return cache;
    }

    public FastDB setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public void setDatabaseInfo(FastDatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public long getInTime() {
        return inTime;
    }

    public long getFirstBuildTime() {
        return firstBuildTime;
    }

    public long getLastBuildTime() {
        return lastBuildTime;
    }

    public boolean isListener() {
        return listener;
    }

    public FastDB setListener(boolean listener) {
        this.listener = listener;
        return this;
    }

    public boolean isOperateListener() {
        return operateListener;
    }

    public FastDB setOperateListener(boolean operateListener) {
        this.operateListener = operateListener;
        return this;
    }
}
