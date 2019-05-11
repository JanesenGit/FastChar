package com.fastchar.extend.jdbc;

import com.fastchar.core.FastChar;
import com.fastchar.database.FastDataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.Validator;

import java.util.Properties;

@SuppressWarnings("UnusedReturnValue")
public class FastJdbcConfig {

    private volatile Boolean defaultAutoCommit = null;
    private volatile Boolean defaultReadOnly = null;
    private volatile int defaultTransactionIsolation = -1;
    private volatile String defaultCatalog = null;
    private volatile String connectionProperties;
    private volatile int initialSize = 10;
    private volatile int maxActive = 100;
    private volatile int maxIdle;
    private volatile int minIdle;
    private volatile int maxWait;
    private volatile int validationQueryTimeout;
    private volatile String validatorClassName;
    private volatile Validator validator;
    private volatile boolean testOnBorrow;
    private volatile boolean testOnReturn;
    private volatile boolean testWhileIdle;
    private volatile int timeBetweenEvictionRunsMillis;
    private volatile int numTestsPerEvictionRun;
    private volatile int minEvictableIdleTimeMillis;
    private volatile boolean accessToUnderlyingConnectionAllowed;
    private volatile boolean removeAbandoned;
    private volatile int removeAbandonedTimeout;
    private volatile boolean logAbandoned;
    private volatile long validationInterval;
    private volatile boolean jmxEnabled;
    private volatile String initSQL;
    private volatile boolean testOnConnect;
    private volatile String jdbcInterceptors;
    private volatile boolean fairQueue;
    private volatile boolean useEquals;
    private volatile int abandonWhenPercentageFull;
    private volatile long maxAge;
    private volatile boolean useLock;
    private volatile PoolProperties.InterceptorDefinition[] interceptors;
    private volatile int suspectTimeout;
    private volatile boolean alternateUsernameAllowed;
    private volatile boolean commitOnReturn;
    private volatile boolean rollbackOnReturn;
    private volatile boolean useDisposableConnectionFacade;
    private volatile boolean logValidationErrors;
    private volatile boolean propagateInterruptState;
    private volatile boolean ignoreExceptionOnPreLoad;


    public FastJdbcConfig() {
        setJmxEnabled(true);
        setTestWhileIdle(false);
        setTestOnBorrow(true);
        setTestOnReturn(false);
        setValidationInterval(30000);
        setTimeBetweenEvictionRunsMillis(30000);
        setMaxActive(100);
        setInitialSize(10);
        setMaxWait(10000);
        setRemoveAbandonedTimeout(60);
        setMinEvictableIdleTimeMillis(30000);
        setMaxIdle(20);
        setMinIdle(10);
        setLogAbandoned(true);
        setRemoveAbandoned(true);
        setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    }

    public Boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public FastJdbcConfig setDefaultAutoCommit(Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
        return this;
    }

    public Boolean getDefaultReadOnly() {
        return defaultReadOnly;
    }

    public FastJdbcConfig setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
        return this;
    }

    public int getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    public FastJdbcConfig setDefaultTransactionIsolation(int defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
        return this;
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public FastJdbcConfig setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
        return this;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public FastJdbcConfig setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
        return this;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public FastJdbcConfig setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public FastJdbcConfig setMaxActive(int maxActive) {
        this.maxActive = maxActive;
        return this;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public FastJdbcConfig setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public FastJdbcConfig setMinIdle(int minIdle) {
        this.minIdle = minIdle;
        return this;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public FastJdbcConfig setMaxWait(int maxWait) {
        this.maxWait = maxWait;
        return this;
    }


    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public FastJdbcConfig setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
        return this;
    }

    public String getValidatorClassName() {
        return validatorClassName;
    }

    public FastJdbcConfig setValidatorClassName(String validatorClassName) {
        this.validatorClassName = validatorClassName;
        return this;
    }

    public Validator getValidator() {
        return validator;
    }

    public FastJdbcConfig setValidator(Validator validator) {
        this.validator = validator;
        return this;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public FastJdbcConfig setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
        return this;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public FastJdbcConfig setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
        return this;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public FastJdbcConfig setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        return this;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public FastJdbcConfig setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        return this;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public FastJdbcConfig setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        return this;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public FastJdbcConfig setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        return this;
    }

    public boolean isAccessToUnderlyingConnectionAllowed() {
        return accessToUnderlyingConnectionAllowed;
    }

    public FastJdbcConfig setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
        return this;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public FastJdbcConfig setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
        return this;
    }

    public int getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public FastJdbcConfig setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
        return this;
    }

    public boolean isLogAbandoned() {
        return logAbandoned;
    }

    public FastJdbcConfig setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
        return this;
    }

    public long getValidationInterval() {
        return validationInterval;
    }

    public FastJdbcConfig setValidationInterval(long validationInterval) {
        this.validationInterval = validationInterval;
        return this;
    }

    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    public FastJdbcConfig setJmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
        return this;
    }

    public String getInitSQL() {
        return initSQL;
    }

    public FastJdbcConfig setInitSQL(String initSQL) {
        this.initSQL = initSQL;
        return this;
    }

    public boolean isTestOnConnect() {
        return testOnConnect;
    }

    public FastJdbcConfig setTestOnConnect(boolean testOnConnect) {
        this.testOnConnect = testOnConnect;
        return this;
    }

    public String getJdbcInterceptors() {
        return jdbcInterceptors;
    }

    public FastJdbcConfig setJdbcInterceptors(String jdbcInterceptors) {
        this.jdbcInterceptors = jdbcInterceptors;
        return this;
    }

    public boolean isFairQueue() {
        return fairQueue;
    }

    public FastJdbcConfig setFairQueue(boolean fairQueue) {
        this.fairQueue = fairQueue;
        return this;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public FastJdbcConfig setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
        return this;
    }

    public int getAbandonWhenPercentageFull() {
        return abandonWhenPercentageFull;
    }

    public FastJdbcConfig setAbandonWhenPercentageFull(int abandonWhenPercentageFull) {
        this.abandonWhenPercentageFull = abandonWhenPercentageFull;
        return this;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public FastJdbcConfig setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isUseLock() {
        return useLock;
    }

    public FastJdbcConfig setUseLock(boolean useLock) {
        this.useLock = useLock;
        return this;
    }

    public PoolProperties.InterceptorDefinition[] getInterceptors() {
        return interceptors;
    }

    public FastJdbcConfig setInterceptors(PoolProperties.InterceptorDefinition[] interceptors) {
        this.interceptors = interceptors;
        return this;
    }

    public int getSuspectTimeout() {
        return suspectTimeout;
    }

    public FastJdbcConfig setSuspectTimeout(int suspectTimeout) {
        this.suspectTimeout = suspectTimeout;
        return this;
    }

    public boolean isAlternateUsernameAllowed() {
        return alternateUsernameAllowed;
    }

    public FastJdbcConfig setAlternateUsernameAllowed(boolean alternateUsernameAllowed) {
        this.alternateUsernameAllowed = alternateUsernameAllowed;
        return this;
    }

    public boolean isCommitOnReturn() {
        return commitOnReturn;
    }

    public FastJdbcConfig setCommitOnReturn(boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
        return this;
    }

    public boolean isRollbackOnReturn() {
        return rollbackOnReturn;
    }

    public FastJdbcConfig setRollbackOnReturn(boolean rollbackOnReturn) {
        this.rollbackOnReturn = rollbackOnReturn;
        return this;
    }

    public boolean isUseDisposableConnectionFacade() {
        return useDisposableConnectionFacade;
    }

    public FastJdbcConfig setUseDisposableConnectionFacade(boolean useDisposableConnectionFacade) {
        this.useDisposableConnectionFacade = useDisposableConnectionFacade;
        return this;
    }

    public boolean isLogValidationErrors() {
        return logValidationErrors;
    }

    public FastJdbcConfig setLogValidationErrors(boolean logValidationErrors) {
        this.logValidationErrors = logValidationErrors;
        return this;
    }

    public boolean isPropagateInterruptState() {
        return propagateInterruptState;
    }

    public FastJdbcConfig setPropagateInterruptState(boolean propagateInterruptState) {
        this.propagateInterruptState = propagateInterruptState;
        return this;
    }

    public boolean isIgnoreExceptionOnPreLoad() {
        return ignoreExceptionOnPreLoad;
    }

    public FastJdbcConfig setIgnoreExceptionOnPreLoad(boolean ignoreExceptionOnPreLoad) {
        this.ignoreExceptionOnPreLoad = ignoreExceptionOnPreLoad;
        return this;
    }
}
