package com.fastchar.extend.jdbc;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.database.FastDataSource;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.interfaces.IFastDataSourceProvider;
import com.fastchar.utils.FastClassUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.lang.reflect.Field;

@AFastObserver
@AFastClassFind("org.apache.tomcat.jdbc.pool.DataSource")
public class FastJdbcDataSourceProvider implements IFastDataSourceProvider {
    private DataSource datasource;

    @Override
    public DataSource getDataSource(FastDatabaseInfo databaseInfo) {
        if (datasource == null) {
            datasource = new DataSource();
            PoolProperties poolProperties = new PoolProperties();
            FastDataSource dataSource = new FastDataSource();
            dataSource.setUsername(databaseInfo.getUser());
            dataSource.setPassword(databaseInfo.getPassword());
            dataSource.setDriverClassName(databaseInfo.getDriver());
            dataSource.setUrl(databaseInfo.toUrl());

            poolProperties.setDataSource(dataSource);
            poolProperties.setValidationQuery(buildValidationQuery(databaseInfo.toUrl()));

            try {
                FastJdbcConfig jdbc = FastChar.getConfigs().getJdbcConfig();
                for (Field field : FastJdbcConfig.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object o = field.get(jdbc);
                    if (o != null) {
                        Field declaredField = FastClassUtils.getDeclaredField(PoolProperties.class, field.getName());
                        if (declaredField != null) {
                            declaredField.setAccessible(true);
                            declaredField.set(poolProperties, o);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            datasource.setPoolProperties(poolProperties);
        }
        return datasource;
    }

    private String buildValidationQuery(String url) {
        if (url.startsWith("jdbc:oracle")) {
            return "select 1 from dual";
        } else if (url.startsWith("jdbc:db2")) {
            return "select 1 from sysibm.sysdummy1";
        } else if (url.startsWith("jdbc:hsqldb")) {
            return "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
        } else if (url.startsWith("jdbc:derby")) {
            return "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
        }
        return "select 1";
    }

    public void onWebStop() {
        if (datasource != null) {
            datasource.close();
        }
    }

}
