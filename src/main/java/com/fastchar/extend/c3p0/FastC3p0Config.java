package com.fastchar.extend.c3p0;

import com.fastchar.interfaces.IFastConfig;

public class FastC3p0Config implements IFastConfig {

    private int acquireIncrement = 3;
    private int idleConnectionTestPeriod = 30;
    private int checkoutTimeout = 30000;
    private int initialPoolSize = 10;
    private int maxIdleTime = 30;
    private int maxPoolSize = 100;
    private int minPoolSize = 10;
    private int maxStatements = 200;
    private int numHelperThreads = 5;
    private int unreturnedConnectionTimeout;
    private boolean debugUnreturnedConnectionStackTraces;
    private boolean autoCommitOnClose;
    private boolean testConnectionOnCheckin;
    private boolean testConnectionOnCheckout;

    public int getIdleConnectionTestPeriod() {
        return idleConnectionTestPeriod;
    }

    public FastC3p0Config setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
        this.idleConnectionTestPeriod = idleConnectionTestPeriod;
        return this;
    }

    public int getCheckoutTimeout() {
        return checkoutTimeout;
    }

    public FastC3p0Config setCheckoutTimeout(int checkoutTimeout) {
        this.checkoutTimeout = checkoutTimeout;
        return this;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public FastC3p0Config setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
        return this;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public FastC3p0Config setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public FastC3p0Config setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public FastC3p0Config setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
        return this;
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public FastC3p0Config setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
        return this;
    }

    public int getAcquireIncrement() {
        return acquireIncrement;
    }

    public FastC3p0Config setAcquireIncrement(int acquireIncrement) {
        this.acquireIncrement = acquireIncrement;
        return this;
    }

    public boolean isAutoCommitOnClose() {
        return autoCommitOnClose;
    }

    public FastC3p0Config setAutoCommitOnClose(boolean autoCommitOnClose) {
        this.autoCommitOnClose = autoCommitOnClose;
        return this;
    }

    public int getNumHelperThreads() {
        return numHelperThreads;
    }

    public FastC3p0Config setNumHelperThreads(int numHelperThreads) {
        this.numHelperThreads = numHelperThreads;
        return this;
    }

    public int getUnreturnedConnectionTimeout() {
        return unreturnedConnectionTimeout;
    }

    public FastC3p0Config setUnreturnedConnectionTimeout(int unreturnedConnectionTimeout) {
        this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
        return this;
    }

    public boolean isDebugUnreturnedConnectionStackTraces() {
        return debugUnreturnedConnectionStackTraces;
    }

    public FastC3p0Config setDebugUnreturnedConnectionStackTraces(boolean debugUnreturnedConnectionStackTraces) {
        this.debugUnreturnedConnectionStackTraces = debugUnreturnedConnectionStackTraces;
        return this;
    }

    public boolean isTestConnectionOnCheckin() {
        return testConnectionOnCheckin;
    }

    public FastC3p0Config setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
        this.testConnectionOnCheckin = testConnectionOnCheckin;
        return this;
    }

    public boolean isTestConnectionOnCheckout() {
        return testConnectionOnCheckout;
    }

    public FastC3p0Config setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
        this.testConnectionOnCheckout = testConnectionOnCheckout;
        return this;
    }
}
