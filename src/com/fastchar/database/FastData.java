package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEntity;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.exception.FastSqlException;

import java.util.ArrayList;
import java.util.List;

/**
 * FastEntity的数据库操作
 */
@SuppressWarnings("unchecked")
public class FastData<T extends FastEntity> {

    protected FastEntity<?> target;

    public FastData(T target) {
        try {
            this.target = target;
        } catch (Exception e) {
            throw new FastSqlException(e);
        }
    }


    public T selectById(Object... ids) {
        try {
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases().get(target.getDatabase()).getType()).toSelectSql(target, ids);
            if (sqlInfo == null) {
                return null;
            }
            FastEntity<?> fastEntity = FastChar.getDb().setDatabase(target.getDatabase()).setLog(sqlInfo.isLog()).selectFirst(sqlInfo.getSql(), sqlInfo.toParams());
            if (fastEntity != null) {
                T newInstance = (T) target.getClass().newInstance();
                newInstance.putAll(fastEntity);
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }

    public List<T> select(String sqlStr, Object... params) {
        try {
            List<T> list = new ArrayList<>();
            List<FastEntity<?>> fastEntities = FastChar.getDb().setDatabase(target.getDatabase()).setLog(target.getBoolean("log", true)).select(sqlStr, params);
            for (FastEntity<?> fastEntity : fastEntities) {
                T newInstance = (T) target.getClass().newInstance();
                newInstance.putAll(fastEntity);
                list.add(newInstance);
            }
            return list;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }

    public T selectFirst(String sqlStr, Object... params) {
        try {
            FastEntity<?> fastEntity = FastChar.getDb().setDatabase(target.getDatabase()).setLog(target.getBoolean("log", true)).selectFirst(sqlStr, params);
            if (fastEntity != null) {
                T newInstance = (T) target.getClass().newInstance();
                newInstance.putAll(fastEntity);
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }

    public T selectLast(String sqlStr, Object... params) {
        try {
            FastEntity<?> fastEntity = FastChar.getDb().setDatabase(target.getDatabase()).setLog(target.getBoolean("log", true)).selectLast(sqlStr, params);
            if (fastEntity != null) {
                T newInstance = (T) target.getClass().newInstance();
                newInstance.putAll(fastEntity);
                return newInstance;
            }
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
        return null;
    }


    public FastPage<T> select(int page, int pageSize, String sqlStr, Object... params) {
        try {
            FastPage<FastEntity<?>> result = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(target.getBoolean("log", true))
                    .select(page, pageSize, sqlStr, params);

            FastPage<T> fastPage = new FastPage<>();
            fastPage.setPage(result.getPage());
            fastPage.setTotalPage(result.getTotalPage());
            fastPage.setTotalRow(result.getTotalRow());
            fastPage.setPageSize(result.getPageSize());

            List<T> list = new ArrayList<>();
            for (FastEntity<?> fastEntity : result.getList()) {
                T newInstance = (T) target.getClass().newInstance();
                newInstance.putAll(fastEntity);
                list.add(newInstance);
            }
            fastPage.setList(list);
            return fastPage;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
    }


    public boolean delete() {
        try {
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases().get(target.getDatabase()).getType()).toDeleteSql(target);
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
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases().get(target.getDatabase()).getType()).toDeleteSql(target, ids);
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


    public boolean save() {
        try {
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases().get(target.getDatabase()).getType()).toInsertSql(target);
            if (sqlInfo == null) {
                return false;
            }
            int insert = FastChar.getDb()
                    .setDatabase(target.getDatabase())
                    .setLog(sqlInfo.isLog())
                    .insert(sqlInfo.getSql(), sqlInfo.toParams());
            for (FastColumnInfo primary : target.getPrimaries()) {
                if (primary.isAutoincrement()) {
                    target.put(primary.getName(), insert);
                }
            }
            return insert > 0;
        } catch (Exception e) {
            setError(e);
            throw new FastSqlException(e);
        }
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


    public boolean update() {
        try {
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases()
                    .get(target.getDatabase()).getType()).toUpdateSql(target);
            if (sqlInfo == null) {
                target.setError(FastChar.getLocal().getInfo("Entity_Error3"));
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

    public boolean updateById(Object... ids) {
        try {
            FastSqlInfo sqlInfo = FastSql.newInstance(FastChar.getDatabases().get(target.getDatabase()).getType()).toUpdateSql(target, ids);
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

    public int update(String sql, Object... params) {
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
