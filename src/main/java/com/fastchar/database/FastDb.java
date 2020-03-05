package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEngine;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;

import com.fastchar.database.sql.FastSql;
import com.fastchar.interfaces.IFastCache;
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
public class FastDb {
    private static final ConcurrentSkipListSet<String> CACHE_TABLE = new ConcurrentSkipListSet<>();
    private static final ThreadLocal<FastTransaction> TRANSACTION_THREAD_LOCAL = new ThreadLocal<FastTransaction>();

    public static boolean isTransaction() {
        FastTransaction fastTransaction = TRANSACTION_THREAD_LOCAL.get();
        return fastTransaction != null && fastTransaction.isValid();
    }

    public static FastTransaction doTransaction() {
        FastTransaction fastTransaction = new FastTransaction();
        TRANSACTION_THREAD_LOCAL.set(fastTransaction);
        return fastTransaction;
    }


    private boolean log = true;
    private boolean useCache = true;
    private boolean cache;
    private String database;
    private long inTime;
    private long firstBuildTime;
    private long lastBuildTime;
    private boolean ignoreCase = false;
    private FastDatabaseInfo databaseInfo;

    private FastDatabaseInfo getDatabaseInfo() {
        if (databaseInfo == null) {
            databaseInfo = FastChar.getDatabases().get(database);
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
        FastTransaction fastTransaction = TRANSACTION_THREAD_LOCAL.get();
        if (fastTransaction != null) {
            if (fastTransaction.isValid()) {
                if (!fastTransaction.contains(databaseInfo.getName())) {
                    fastTransaction.setConnection(databaseInfo.getName(), dataSource.getConnection());
                }
                return fastTransaction.getConnection(databaseInfo.getName());
            }
            TRANSACTION_THREAD_LOCAL.remove();
        }
        return dataSource.getConnection();
    }

    private String buildCacheKeyBySql(String sqlStr, Object... params) {
        FastDatabaseInfo fastDatabaseInfo = getDatabaseInfo();
        return FastChar.getSecurity().MD5_Encrypt(fastDatabaseInfo.getName() + sqlStr + Arrays.toString(params));
    }

    private String buildCacheTag(String... tableNames) {
        FastDatabaseInfo fastDatabaseInfo = getDatabaseInfo();
        for (int i = 0; i < tableNames.length; i++) {
            tableNames[i] = fastDatabaseInfo.getName() + "@" + tableNames[i];
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
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
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
            resultSet = preparedStatement.executeQuery();
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).setIgnoreCase(this.ignoreCase).getListResult();
            if ((getDatabaseInfo().isCache() || isCache()) && isUseCache()) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                iFastCacheProvider.set(cacheTag, cacheKey, listResult);
            }
            return listResult;
        } finally {
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params);
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
        inTime = System.currentTimeMillis();
        FastPage<FastEntity<?>> fastPage = new FastPage<>();
        String type = getDatabaseInfo().getType();
        if (page > 0) {
            String countSql = FastSql.getInstance(type).getCountSql(sqlStr, "ct");

            FastEntity<?> countResult = selectFirst(countSql, params);
            int countRow = countResult.getInt("ct");
            fastPage.setTotalRow(countRow);
            fastPage.setPage(page);
            fastPage.setPageSize(pageSize);
            fastPage.setTotalPage(countRow % pageSize == 0 ? countRow / pageSize : countRow / pageSize + 1);
        } else {
            fastPage.setTotalPage(1);
            fastPage.setPage(1);
        }
        String pageSql = FastSql.getInstance(type).buildPageSql(sqlStr, page, pageSize);
        FastSqlInfo fastSqlInfo = FastSqlInfo.newInstance().setSql(sqlStr).setParams(params);
        fastSqlInfo.fromProperty();
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
            preparedStatement = connection.prepareStatement(sqlStr);
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
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params);
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
            preparedStatement = connection.prepareStatement(sqlStr);
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
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params);
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

        int count;
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
            log(sqlStr, params);
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
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params);
        }
        return primary;
    }


    /**
     * 执行复杂Sql
     *
     * @param sql 语句
     * @return 是否成功
     * @throws Exception 异常信息
     */
    public boolean run(String sql) throws Exception {
        inTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.createStatement();
            return statement.execute(sql);
        } finally {
            close(connection, statement);
            log(sql, null);
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
        inTime = System.currentTimeMillis();
        if (isUseCache()) {
            for (String sqlStr : sqlList) {
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
        }

        Connection connection = null;
        Statement statement = null;
        try {
            List<Integer> result = new ArrayList<>();
            int count = 0;
            connection = getConnection();
            if (connection == null) {
                return new int[0];
            }
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
                    } catch (Exception ignored) {
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
            log(sqlList, null);
        }
    }


    /**
     * 批量执行sql
     *
     * @param sqlStr 共同的sql语句
     * @param params 参数数据的数组
     */
    public int[] batch(String sqlStr, List<Object[]> params, int batchSize) throws Exception {
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
            int count = 0;
            connection = getConnection();
            if (connection == null) {
                return new int[0];
            }
            List<Integer> result = new ArrayList<>();
            preparedStatement = connection.prepareStatement(sqlStr);
            if (params != null) {
                for (Object[] param : params) {
                    if (param == null) continue;
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
                        } catch (Exception ignored) {
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
            log(sqlStr, params);
        }
    }

    /**
     * 批量添加FastEntity实体集合
     *
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果结婚
     * @throws Exception 异常信息
     */
    public int[] batchSaveEntity(List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        return batchSaveEntity(false, entities, batchSize, checks);
    }

    /**
     * 批量添加FastEntity实体集合
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchSaveEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities.size() == 0) {
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
            for (String sql : batchSqlMap.keySet()) {
                int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
                for (int i : batch) {
                    result.add(i);
                }
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        }
    }

    /**
     * 批量更新FastEntity实体集合
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
     * 批量更新FastEntity实体集合
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchUpdateEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities.size() == 0) {
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
            for (String sql : batchSqlMap.keySet()) {
                int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
                for (int i : batch) {
                    result.add(i);
                }
            }
            return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
        }
    }

    /**
     * 批量删除FastEntity实体集合
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
     * 批量删除FastEntity实体集合
     *
     * @param staticSql 是否转为静态语句执行
     * @param entities  FastEntity实体集合
     * @param batchSize 单次批量提交的数据大小
     * @param checks    检测属性值
     * @return 结果集合
     * @throws Exception 异常信息
     */
    public int[] batchDeleteEntity(boolean staticSql, List<? extends FastEntity<?>> entities, int batchSize, String... checks) throws Exception {
        if (entities.size() == 0) {
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
            for (String sql : batchSqlMap.keySet()) {
                int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
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
        if (sqlInfos.size() == 0) {
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
        for (String sql : batchSqlMap.keySet()) {
            int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
            for (int i : batch) {
                result.add(i);
            }
        }
        return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
    }

    public void close(Connection connection) {
        if (isTransaction()) {
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


    public void log(Object sql, Object params) {
        if (FastChar.getConstant().isLogSql() && isLog()) {
            if (FastStringUtils.isEmpty(String.valueOf(sql))) {
                return;
            }
            LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
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
            printMap.put("Total", FastNumberUtils.formatToFloat((useTotal + buildTotal), 6) + " seconds");
            printMap.put("Time", FastDateUtils.getDateString("yyyy-MM-dd HH:mm:ss:SSS"));
            int maxKeyLength = 0;
            for (String key : printMap.keySet()) {
                maxKeyLength = Math.max(maxKeyLength, key.length() + 1);
            }
            StringBuilder print = new StringBuilder();
            for (String key : printMap.keySet()) {
                String text = printMap.get(key);
                if (FastStringUtils.isEmpty(text)) {
                    continue;
                }
                print.append("\n").append(formatString(key, maxKeyLength)).append(text);
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
        return FastChar.getLog().tipStyle(content);
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

    public FastDb setLog(boolean log) {
        this.log = log;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public FastDb setUseCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public FastDb setDatabase(String database) {
        this.database = database;
        return this;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public FastDb setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    public boolean isCache() {
        return cache;
    }

    public FastDb setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public void setDatabaseInfo(FastDatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }
}
