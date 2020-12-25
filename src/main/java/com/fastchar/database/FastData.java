package com.fastchar.database;

import com.alibaba.druid.sql.ast.expr.SQLFlashbackExpr;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.core.FastHandler;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.sql.FastSql;
import com.fastchar.exception.FastSqlException;
import com.fastchar.interfaces.IFastColumnSecurity;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * FastEntity的数据库操作
 */
@SuppressWarnings("unchecked")
public class FastData<T extends FastEntity<?>> {

    protected FastEntity<?> target;

    public FastData(T target) {
        try {
            this.target = target;
        } catch (Exception e) {
            throw new FastSqlException(e);
        }
    }

    protected String getDatabaseType() {
        return FastChar.getDatabases().get(target.getDatabase()).getType();
    }

    protected void convertValue(FastEntity<?> entity) {
        if (entity.getTable() == null) {
            return;
        }
        List<FastColumnInfo<?>> columns = entity.getTable().getColumns();
        for (FastColumnInfo<?> column : columns) {
            if (FastStringUtils.isNotEmpty(column.getEncrypt())) {
                String decryptValue = FastChar.getOverrides()
                        .singleInstance(IFastColumnSecurity.class)
                        .decrypt(column, entity.getString(column.getName()));
                entity.put(column.getName(), decryptValue);
            }
        }

    }

    public T selectById(Object... ids) {
        return selectById(false, ids);
    }

    public T selectById(boolean cache,Object... ids) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildSelectSqlByIds(target, ids);
            if (sqlInfo == null) {
                return null;
            }
            FastEntity<?> fastEntity = FastChar.getDb()
                    .setCache(cache)
                    .setDatabase(target.getDatabase())
                    .setIgnoreCase(target.isIgnoreCase())
                    .setLog(sqlInfo.isLog()).selectFirst(sqlInfo.getSql(), sqlInfo.toParams());
            if (fastEntity != null) {
                T newInstance = (T) FastClassUtils.newInstance(target.getClass());
                if (newInstance == null) {
                    return null;
                }
                newInstance.putAll(fastEntity);
                convertValue(newInstance);
                newInstance.convertValue();
                fastEntity.clear();
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }

    public List<T> selectBySql(String sqlStr, Object... params) {
        return selectBySql(false, sqlStr, params);
    }

    public List<T> selectBySql(boolean cache,String sqlStr, Object... params) {
        try {
            List<T> list = new ArrayList<>();
            List<FastEntity<?>> fastEntities = FastChar.getDb()
                    .setCache(cache)
                    .setDatabase(target.getDatabase())
                    .setIgnoreCase(target.isIgnoreCase())
                    .setLog(target.getBoolean("log", true))
                    .select(sqlStr, params);
            for (FastEntity<?> fastEntity : fastEntities) {
                T newInstance = (T) FastClassUtils.newInstance(target.getClass());
                if (newInstance == null) {
                    continue;
                }
                newInstance.putAll(fastEntity);
                convertValue(newInstance);
                newInstance.convertValue();
                fastEntity.clear();
                list.add(newInstance);
            }
            fastEntities.clear();
            return list;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }

    public T selectFirstBySql(String sqlStr, Object... params) {
        return selectFirstBySql(false, sqlStr, params);
    }
    public T selectFirstBySql(boolean cache,String sqlStr, Object... params) {
        try {
            FastEntity<?> fastEntity = FastChar.getDb()
                    .setCache(cache)
                    .setDatabase(target.getDatabase())
                    .setIgnoreCase(target.isIgnoreCase())
                    .setLog(target.getBoolean("log", true))
                    .selectFirst(sqlStr, params);
            if (fastEntity != null) {
                T newInstance = (T) FastClassUtils.newInstance(target.getClass());
                if (newInstance == null) {
                    return null;
                }
                newInstance.putAll(fastEntity);
                convertValue(newInstance);
                newInstance.convertValue();
                fastEntity.clear();
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }

    public T selectLastBySql(String sqlStr, Object... params) {
        return selectLastBySql(false, sqlStr, params);
    }
    public T selectLastBySql(boolean cache,String sqlStr, Object... params) {
        try {
            FastEntity<?> fastEntity = FastChar.getDb()
                    .setCache(cache)
                    .setDatabase(target.getDatabase())
                    .setIgnoreCase(target.isIgnoreCase())
                    .setLog(target.getBoolean("log", true))
                    .selectLast(sqlStr, params);
            if (fastEntity != null) {
                T newInstance = (T) FastClassUtils.newInstance(target.getClass());
                if (newInstance == null) {
                    return null;
                }
                newInstance.putAll(fastEntity);
                convertValue(newInstance);
                newInstance.convertValue();
                fastEntity.clear();
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }


    public FastPage<T> selectBySql(int page, int pageSize, String sqlStr, Object... params) {
        return selectBySql(false, page, pageSize, sqlStr, params);
    }
    public FastPage<T> selectBySql(boolean cache,int page, int pageSize, String sqlStr, Object... params) {
        try {
            FastPage<FastEntity<?>> result = FastChar.getDb()
                    .setCache(cache)
                    .setDatabase(target.getDatabase())
                    .setIgnoreCase(target.isIgnoreCase())
                    .setLog(target.getBoolean("log", true))
                    .select(page, pageSize, sqlStr, params);

            FastPage<T> fastPage = new FastPage<>();
            fastPage.setPage(result.getPage());
            fastPage.setTotalPage(result.getTotalPage());
            fastPage.setTotalRow(result.getTotalRow());
            fastPage.setPageSize(result.getPageSize());
            fastPage.setSqlInfo(result.getSqlInfo());

            List<T> list = new ArrayList<>();
            for (FastEntity<?> fastEntity : result.getList()) {
                T newInstance = (T) FastClassUtils.newInstance(target.getClass());
                if (newInstance == null) {
                    continue;
                }
                newInstance.putAll(fastEntity);
                convertValue(newInstance);
                newInstance.convertValue();
                fastEntity.clear();
                list.add(newInstance);
            }
            fastPage.setList(list);
            result.release();
            return fastPage;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean delete(String...checks) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildDeleteSql(target, checks);
            if (sqlInfo == null) {
                return false;
            }
            return FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .update(sqlInfo.getSql(), sqlInfo.toParams()) > 0;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean deleteById(Object... ids) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildDeleteSqlByIds(target, ids);
            if (sqlInfo == null) {
                return false;
            }
            return FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .update(sqlInfo.getSql(), sqlInfo.toParams()) > 0;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean save(String... checks) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildInsertSql(target, checks);
            if (sqlInfo == null) {
                return false;
            }
            int insert = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .insert(sqlInfo.getSql(), sqlInfo.toParams());
            boolean result = insert > 0;
            if (result) {
                target.getModified().clear();
                for (FastColumnInfo<?> primary : target.getPrimaries()) {
                    if (primary.isAutoincrement()) {
                        target.put(primary.getName(), insert);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean copySave() {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildCopySql(target);
            if (sqlInfo == null) {
                return false;
            }
            int insert = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .insert(sqlInfo.getSql(), sqlInfo.toParams());
            boolean result = insert > 0;
            if (result) {
                target.getModified().clear();
                for (FastColumnInfo<?> primary : target.getPrimaries()) {
                    if (primary.isAutoincrement()) {
                        target.put(primary.getName(), insert);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean push(String... checks) {
        if (checks.length == 0) {
            throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR4));
        }
        if (!target.save(checks)) {
            return target.update(checks);
        }
        return true;
    }

    public boolean push(FastHandler handler,String... checks) {
        if (checks.length == 0) {
            throw new FastSqlException(FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR4));
        }
        if (handler != null) {
            handler.setCode(0);
        }
        if (!target.save(checks)) {
            if (handler != null) {
                handler.setCode(1);
            }
            return target.update(checks);
        }
        return true;
    }

    public int count(String... checks) {
        FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildCountSql(target, checks);
        if (sqlInfo == null) {
            return 0;
        }
        try {
            FastEntity<?> fastEntity = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .selectFirst(sqlInfo.getSql(), sqlInfo.toParams());
            return fastEntity.getInt("ct");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }



    protected void setError(Exception e) {
        if (target == null) {
            return;
        }
        Throwable throwable = e;
        if (e.getCause() != null) {
            throwable = e.getCause();
        }
        target.setError(throwable.toString());
    }


    public boolean update(String...checks) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildUpdateSql(target, checks);
            if (sqlInfo == null) {
                target.getModified().clear();
                target.setError(FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR3));
                return false;
            }
            boolean result = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .update(sqlInfo.getSql(), sqlInfo.toParams()) > 0;
            if (result) {
                target.getModified().clear();
            }
            return result;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }

    public boolean updateById(Object... ids) {
        try {
            FastSqlInfo sqlInfo = FastSql.getInstance(getDatabaseType()).buildUpdateSqlByIds(target, ids);
            if (sqlInfo == null) {
                target.getModified().clear();
                target.setError(FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR3));
                return false;
            }
            boolean result = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .update(sqlInfo.getSql(), sqlInfo.toParams()) > 0;
            if (result) {
                target.getModified().clear();
            }
            return result;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }

    public int updateBySql(String sql, Object... params) {
        try {
            return FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(target.getBoolean("log", true))
                    .update(sql, params);
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }

}
