package com.fastchar.extend.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;

//具体详细配置介绍，请参看官方文档 https://github.com/alibaba/druid/wiki
@AFastObserver(priority = -9)//数据源监听关闭，放到最终
@AFastPriority(AFastPriority.P_HIGH)
@AFastClassFind("com.alibaba.druid.pool.DruidDataSource")
public class FastDruidDataSourceProvider implements IFastDataSource {
    private volatile DruidDataSource dataSource = null;
    private FastDatabaseInfo databaseInfo;

    public FastDruidDataSourceProvider() {

    }

    @Override
    public DataSource getDataSource(FastDatabaseInfo databaseInfo) {
        if (dataSource == null) {
            synchronized (FastDruidDataSourceProvider.class) {
                if (dataSource == null) {
                    dataSource = new DruidDataSource();
                    dataSource.setUrl(databaseInfo.toUrl());
                    dataSource.setUsername(databaseInfo.getUser());
                    dataSource.setPassword(databaseInfo.getPassword());
                    dataSource.setDriverClassName(databaseInfo.getDriver());
                    if (databaseInfo.isValidate()) {
                        dataSource.setValidationQuery(buildValidationQuery(databaseInfo.toUrl()));
                    }
                    this.databaseInfo = databaseInfo;

                    try {
                        FastDruidConfig druid = FastChar.getConfigs().getDruidConfig();
                        for (Field field : FastDruidConfig.class.getDeclaredFields()) {
                            field.setAccessible(true);
                            Object o = field.get(druid);
                            if (o != null) {
                                Field declaredField = FastClassUtils.getDeclaredField(DruidDataSource.class, field.getName());
                                if (declaredField != null) {
                                    declaredField.setAccessible(true);
                                    declaredField.set(dataSource, o);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String poolInfo = "Druid jdbc pool of " +databaseInfo.toSimpleInfo();
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLog().info(this.getClass(),FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO1, poolInfo));
                    }
                    FastChar.getValues().put("jdbcPool", "Druid jdbc pool");
                }
            }
        }
        return dataSource;
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
        return "select 1 ";
    }


    public synchronized void onWebStop() {
        try {
            if (dataSource != null) {
                dataSource.close();
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info(this.getClass(),FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO2, "Druid jdbc pool of " + databaseInfo.toSimpleInfo()));
                }
            }
        } finally {
            dataSource = null;
        }
    }

}
