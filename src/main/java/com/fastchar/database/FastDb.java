package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;

import com.fastchar.database.sql.FastSql;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 数据库sql操作
 */
public class FastDb {
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
    private String database;
    private long inTime;

    private FastDatabaseInfo getDatabaseInfo() {
        return FastChar.getDatabases().get(database);
    }

    public Connection getConnection() throws SQLException {
        FastDatabaseInfo databaseInfo = getDatabaseInfo();
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
        return buildCacheTag(getDatabaseInfo().getAllTables(sqlStr).toArray(new String[]{}));
    }


    /**
     * 执行查询（带参数）
     *
     * @param sqlStr
     * @param params
     * @return
     */
    public List<FastEntity<?>> select(String sqlStr, Object... params) throws Exception {
        String cacheKey = null;
        String cacheTag = null;
        inTime = System.currentTimeMillis();
        if (getDatabaseInfo().isCache() && isUseCache()) {
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
            List<FastEntity<?>> listResult = new FastResultSet(resultSet).getListResult();
            if (getDatabaseInfo().isCache() && isUseCache()) {
                IFastCache iFastCacheProvider = FastChar.getCache();
                iFastCacheProvider.set(cacheTag, cacheKey, listResult);
            }
            return listResult;
        } finally {
            close(connection, preparedStatement, resultSet);
            log(sqlStr, params);
        }
    }


    public FastPage<FastEntity<?>> select(int page, int pageSize, String sqlStr, Object... params) throws Exception {
        inTime = System.currentTimeMillis();
        FastPage<FastEntity<?>> fastPage = new FastPage<>();
        String type = getDatabaseInfo().getType();
        if (page > 0) {
            String countSql = FastSql.getInstance(type).getCountSql(sqlStr, "ct");

            FastEntity countResult = selectFirst(countSql, params);
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
     * 执行查询（带参数）
     *
     * @param sqlStr
     * @param params
     * @return
     */
    public FastEntity<?> selectFirst(String sqlStr, Object... params) throws Exception {
        inTime = System.currentTimeMillis();
        String cacheKey = null;
        String cacheTag = null;
        if (getDatabaseInfo().isCache() && isUseCache()) {
            cacheKey = buildCacheKeyBySql(sqlStr, params);
            cacheTag = buildCacheTagBySql(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            if (iFastCacheProvider.exists(cacheTag, cacheKey)) {
                FastEntity cache = iFastCacheProvider.get(cacheTag, cacheKey);
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
            FastEntity firstResult = new FastResultSet(resultSet).getFirstResult();
            if (getDatabaseInfo().isCache() && isUseCache()) {
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
     * 执行查询（带参数）
     *
     * @param sqlStr
     * @param params
     * @return
     */
    public FastEntity<?> selectLast(String sqlStr, Object... params) throws Exception {
        inTime = System.currentTimeMillis();
        String cacheKey = null;
        String cacheTag = null;
        if (getDatabaseInfo().isCache() && isUseCache()) {
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
            FastEntity lastResult = new FastResultSet(resultSet).getLastResult();
            if (getDatabaseInfo().isCache() && isUseCache()) {
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
     * @param sqlStr
     * @param params
     * @return 更新成功的数量
     */
    public int update(String sqlStr, Object... params) throws Exception {
        inTime = System.currentTimeMillis();
        if (getDatabaseInfo().isCache() && isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            for (String table : allTables) {
                Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                for (String tag : tags) {
                    iFastCacheProvider.delete(tag);
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
     * @param sqlStr
     * @param params
     * @return 主键
     */
    public int insert(String sqlStr, Object... params) throws Exception {
        inTime = System.currentTimeMillis();
        if (getDatabaseInfo().isCache() && isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            for (String table : allTables) {
                Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                for (String tag : tags) {
                    iFastCacheProvider.delete(tag);
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
        if (getDatabaseInfo().isCache() && isUseCache()) {
            for (String sqlStr : sqlList) {
                List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
                IFastCache iFastCacheProvider = FastChar.getCache();
                for (String table : allTables) {
                    Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                    for (String tag : tags) {
                        iFastCacheProvider.delete(tag);
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
                    int[] executeBatch = statement.executeBatch();
                    Integer[] integers = FastArrayUtils.toObject(executeBatch);
                    if (integers != null) {
                        result.addAll(Arrays.asList(integers));
                    }
                    statement.clearBatch();
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
            for (String sql : sqlList) {
                log(sql, null);
            }
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
        if (getDatabaseInfo().isCache() && isUseCache()) {
            List<String> allTables = getDatabaseInfo().getAllTables(sqlStr);
            IFastCache iFastCacheProvider = FastChar.getCache();
            for (String table : allTables) {
                Set<String> tags = iFastCacheProvider.getTags("*" + buildCacheTag(table) + "*");
                for (String tag : tags) {
                    iFastCacheProvider.delete(tag);
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
                        int[] executeBatch = preparedStatement.executeBatch();
                        Integer[] integers = FastArrayUtils.toObject(executeBatch);
                        if (integers != null) {
                            result.addAll(Arrays.asList(integers));
                        }
                        preparedStatement.clearBatch();
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


    public int[] batchSaveEntity(List<? extends FastEntity> entities, int batchSize, String... checks) throws Exception {
        if (entities.size() == 0) {
            return new int[0];
        }
        LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
        for (FastEntity entity : entities) {
            entity.setDefaultValue();
            FastSqlInfo sqlInfo = entity.toInsertSql(checks);
            if (sqlInfo == null) {
                continue;
            }
            if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
            }
            batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
        }
        List<Integer> result = new ArrayList<>();
        for (String sql : batchSqlMap.keySet()) {
            int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
            for (int i : batch) {
                result.add(i);
            }
        }
        return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
    }

    public int[] batchUpdateEntity(List<? extends FastEntity> entities, int batchSize) throws Exception {
        if (entities.size() == 0) {
            return new int[0];
        }
        LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
        for (FastEntity entity : entities) {
            FastSqlInfo sqlInfo = entity.toUpdateSql();
            if (sqlInfo == null) {
                continue;
            }
            if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
            }
            batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
        }
        List<Integer> result = new ArrayList<>();
        for (String sql : batchSqlMap.keySet()) {
            int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
            for (int i : batch) {
                result.add(i);
            }
        }
        return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
    }

    public int[] batchDeleteEntity(List<? extends FastEntity> entities, int batchSize) throws Exception {
        if (entities.size() == 0) {
            return new int[0];
        }
        LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
        for (FastEntity entity : entities) {
            FastSqlInfo sqlInfo = entity.toDeleteSql();
            if (sqlInfo == null) {
                continue;
            }
            if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
            }
            batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
        }
        List<Integer> result = new ArrayList<>();
        for (String sql : batchSqlMap.keySet()) {
            int[] batch = this.batch(sql, batchSqlMap.get(sql), batchSize);
            for (int i : batch) {
                result.add(i);
            }
        }
        return FastArrayUtils.toPrimitive(result.toArray(new Integer[]{}));
    }

    public int[] batchUpdate(List<FastSqlInfo> sqlInfos, int batchSize) throws Exception {
        if (sqlInfos.size() == 0) {
            return new int[0];
        }
        LinkedHashMap<String, List<Object[]>> batchSqlMap = new LinkedHashMap<>();
        for (FastSqlInfo sqlInfo : sqlInfos) {
            if (!batchSqlMap.containsKey(sqlInfo.getSql())) {
                batchSqlMap.put(sqlInfo.getSql(), new ArrayList<Object[]>());
            }
            batchSqlMap.get(sqlInfo.getSql()).add(sqlInfo.toParams());
            setLog(sqlInfo.isLog());
        }
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


    public void log(String sql, Object params) {
        if (FastChar.getConstant().isLogSql() && isLog()) {
            if (FastStringUtils.isEmpty(sql)) {
                return;
            }
            sql = sql.replace("\n", " ");
            LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
            printMap.put("Sql", sql);
            if (params != null) {
                if (params instanceof Object[]) {
                    printMap.put("Params", Arrays.toString((Object[]) params));
                }
                if (params instanceof List) {
                    List list = (List) params;
                    for (int i = 0; i < list.size(); i++) {
                        printMap.put("Params-" + (i + 1), Arrays.toString((Object[]) list.get(i)));
                    }
                }
            }

            float useTotal = FastNumberUtils.formatToFloat((System.currentTimeMillis() - inTime) / 1000.0, 6);
            printMap.put("Total", useTotal + " seconds");
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
}
