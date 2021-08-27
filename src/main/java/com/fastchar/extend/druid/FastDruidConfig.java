package com.fastchar.extend.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.FilterManager;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastConfig;

import javax.servlet.ServletRegistration;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FastDruidConfig implements IFastConfig {
    private int initialSize = 10;
    private int minIdle = 10;
    private int maxActive = 100;
    private int maxWait;
    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private int removeAbandonedTimeoutMillis = 600 * 1000;
    private boolean removeAbandoned = true;
    private boolean logAbandoned = true;
    private boolean keepAlive = true;

    private boolean clearFiltersEnable;

    private long timeBetweenEvictionRunsMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private long minEvictableIdleTimeMillis = DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private long timeBetweenConnectErrorMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS;
    private boolean poolPreparedStatements = false;
    private int maxPoolPreparedStatementPerConnectionSize = -1;
    private int maxOpenPreparedStatements = 20;


    private List<Filter> filters = new ArrayList<>();

    private int validationQueryTimeout;

    public int getInitialSize() {
        return initialSize;
    }

    public FastDruidConfig setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public FastDruidConfig setMinIdle(int minIdle) {
        this.minIdle = minIdle;
        return this;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public FastDruidConfig setMaxActive(int maxActive) {
        this.maxActive = maxActive;
        return this;
    }

    public int getRemoveAbandonedTimeoutMillis() {
        return removeAbandonedTimeoutMillis;
    }

    public FastDruidConfig setRemoveAbandonedTimeoutMillis(int removeAbandonedTimeoutMillis) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
        return this;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public FastDruidConfig setMaxWait(int maxWait) {
        this.maxWait = maxWait;
        return this;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public FastDruidConfig setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        return this;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public FastDruidConfig setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
        return this;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public FastDruidConfig setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
        return this;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public FastDruidConfig setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
        return this;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public FastDruidConfig setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public FastDruidConfig setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        return this;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public FastDruidConfig setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        return this;
    }

    public long getTimeBetweenConnectErrorMillis() {
        return timeBetweenConnectErrorMillis;
    }

    public FastDruidConfig setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
        this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
        return this;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public FastDruidConfig setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
        return this;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public FastDruidConfig setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
        return this;
    }

    public int getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public FastDruidConfig setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
        return this;
    }

    public boolean isLogAbandoned() {
        return logAbandoned;
    }

    public FastDruidConfig setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
        return this;
    }

    public boolean isClearFiltersEnable() {
        return clearFiltersEnable;
    }

    public FastDruidConfig setClearFiltersEnable(boolean clearFiltersEnable) {
        this.clearFiltersEnable = clearFiltersEnable;
        return this;
    }

    public FastDruidConfig addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public FastDruidConfig setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
        return this;
    }

    public void clearFilters() {
        if (this.isClearFiltersEnable()) {
            this.filters.clear();
        }
    }

    public FastDruidConfig setFilters(String filters) {
        if (filters != null && filters.startsWith("!")) {
            filters = filters.substring(1);
            this.clearFilters();
        }
        this.addFilters(filters);
        return this;
    }

    public void addFilters(String filters) {
        try {
            if (filters != null && filters.length() != 0) {

                if (FastChar.getServletContext() != null) {
                    ServletRegistration.Dynamic druidStatView = FastChar.getServletContext().addServlet("DruidStatView", StatViewServlet.class);
                    druidStatView.addMapping("/druid/*");
                    FastChar.getActions().addExcludeUrls("/druid/*");
                }

                String[] filterArray = filters.split("\\,");
                String[] var3 = filterArray;
                int var4 = filterArray.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    String item = var3[var5];
                    FilterManager.loadFilter(this.filters, item.trim());
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
