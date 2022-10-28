package com.fastchar.database.provider;

import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastChar;
import com.fastchar.database.FastSimpleDataSource;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.interfaces.IFastDataSource;
import com.fastchar.local.FastCharLocal;

import javax.sql.DataSource;

@AFastObserver(priority = -9)//数据源监听关闭，放到最终
@AFastPriority
public class FastSimpleDataSourceProvider implements IFastDataSource {
    private volatile FastSimpleDataSource datasource;
    private FastDatabaseInfo databaseInfo;

    @Override
    public DataSource getDataSource(FastDatabaseInfo databaseInfo) {
        if (datasource == null) {
            synchronized (FastSimpleDataSourceProvider.class) {
                if (datasource == null) {
                    datasource = new FastSimpleDataSource();
                    datasource.setUrl(databaseInfo.toUrl());
                    datasource.setUsername(databaseInfo.getUser());
                    datasource.setPassword(databaseInfo.getPassword());
                    datasource.setDriverClassName(databaseInfo.getDriver());
                    if (databaseInfo.isValidate()) {
                        datasource.setValidationQuery(buildValidationQuery(databaseInfo.toUrl()));
                    }
                }
                datasource.initPool();
                this.databaseInfo = databaseInfo;
                String poolInfo = "FastChar jdbc pool of " + databaseInfo.toSimpleInfo();
                if (FastChar.getConstant().isDebug()) {
                    String info = FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO1, poolInfo);
                    FastChar.getLog().info(info);
                }
                FastChar.getValues().put("jdbcPool", "FastChar jdbc pool");
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


    public synchronized void onWebStop() {
        try {
            if (datasource != null) {
                datasource.close();
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.DATASOURCE_INFO2, "FastChar jdbc pool of " + databaseInfo.toSimpleInfo()));
                }
            }
        } finally {
            datasource = null;
        }
    }

}
