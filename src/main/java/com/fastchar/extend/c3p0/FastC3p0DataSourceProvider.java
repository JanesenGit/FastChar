package com.fastchar.extend.c3p0;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.annotation.AFastObserver;
import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.interfaces.IFastDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@AFastObserver
@AFastPriority(AFastPriority.P_NORMAL)
@AFastClassFind("com.mchange.v2.c3p0.ComboPooledDataSource")
public class FastC3p0DataSourceProvider implements IFastDataSource {
    private ComboPooledDataSource dataSource = null;
    private FastDatabaseInfo databaseInfo;

    @Override
    public DataSource getDataSource(FastDatabaseInfo databaseInfo) {
        if (dataSource == null) {
            try {
                dataSource = new ComboPooledDataSource();
                dataSource.setDriverClass(databaseInfo.getDriver());
                dataSource.setJdbcUrl(databaseInfo.toUrl());
                dataSource.setUser(databaseInfo.getUser());
                dataSource.setPassword(databaseInfo.getPassword());

                FastC3p0Config c3p0Config = FastChar.getConfigs().getC3p0Config();
                dataSource.setPreferredTestQuery(buildValidationQuery(databaseInfo.toUrl()));
                dataSource.setIdleConnectionTestPeriod(c3p0Config.getIdleConnectionTestPeriod());
                dataSource.setMaxIdleTime(c3p0Config.getMaxIdleTime());
                dataSource.setInitialPoolSize(c3p0Config.getInitialPoolSize());
                dataSource.setMaxPoolSize(c3p0Config.getMaxPoolSize());
                dataSource.setMinPoolSize(c3p0Config.getMinPoolSize());
                dataSource.setAcquireIncrement(c3p0Config.getAcquireIncrement());
                dataSource.setCheckoutTimeout(c3p0Config.getCheckoutTimeout());
                dataSource.setMaxStatements(c3p0Config.getMaxStatements());
                dataSource.setAutoCommitOnClose(c3p0Config.isAutoCommitOnClose());
                dataSource.setNumHelperThreads(c3p0Config.getNumHelperThreads());
                dataSource.setUnreturnedConnectionTimeout(c3p0Config.getUnreturnedConnectionTimeout());
                dataSource.setDebugUnreturnedConnectionStackTraces(c3p0Config.isDebugUnreturnedConnectionStackTraces());
                dataSource.setTestConnectionOnCheckin(c3p0Config.isTestConnectionOnCheckin());
                dataSource.setTestConnectionOnCheckout(c3p0Config.isTestConnectionOnCheckout());
                this.databaseInfo = databaseInfo;

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo("DataSource_Info1", "C3P0 of " + databaseInfo.getName()));
                }

            } catch (PropertyVetoException e) {
                e.printStackTrace();
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
        return "select 1";
    }

    public void onWebStop() {
        try {
            if (dataSource != null) {
                dataSource.close();
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo("DataSource_Info2", "C3P0 of " + databaseInfo.getName()));
                }
            }
        } finally {
            dataSource = null;
        }
    }

}
