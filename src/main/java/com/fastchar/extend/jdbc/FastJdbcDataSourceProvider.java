package com.fastchar.extend.jdbc;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.utils.FastClassUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.lang.reflect.Field;

@AFastObserver
@AFastPriority
@AFastClassFind("org.apache.tomcat.jdbc.pool.DataSource")
public class FastJdbcDataSourceProvider implements IFastDataSource {
    private DataSource datasource;
    private FastDatabaseInfo databaseInfo;


    @Override
    public DataSource getDataSource(FastDatabaseInfo databaseInfo) {
        if (datasource == null) {
            datasource = new DataSource();
            PoolProperties poolProperties = new PoolProperties();
            poolProperties.setValidationQuery(buildValidationQuery(databaseInfo.toUrl()));
            poolProperties.setUrl(databaseInfo.toUrl());
            poolProperties.setDriverClassName(databaseInfo.getDriver());
            poolProperties.setUsername(databaseInfo.getUser());
            poolProperties.setPassword(databaseInfo.getPassword());
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
            this.databaseInfo = databaseInfo;

            if (FastChar.getConstant().isDebug()) {
                FastChar.getLog().info(FastChar.getLocal().getInfo("DataSource_Info1", "Tomcat jdbc pool of " + databaseInfo.getName()));
            }
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
        try {
            if (datasource != null) {
                datasource.close();
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo("DataSource_Info2", "Tomcat jdbc pool of " + databaseInfo.getName()));
                }
            }
        } finally {
            datasource = null;
        }
    }

}
