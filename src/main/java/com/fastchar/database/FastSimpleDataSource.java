package com.fastchar.database;

import com.alibaba.druid.util.StringUtils;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

/**
 * 简单的数据库链接池
 */
public class FastSimpleDataSource implements DataSource {

    private volatile String url;

    private volatile String username;
    private volatile String password;

    private volatile int loginTimeout = 30;

    private volatile int initPoolSize = 10;

    private volatile int maxActive = 100;

    private volatile int validationQueryTimeout = -1;

    private volatile PrintWriter printWriter;

    private volatile String driverClassName;

    private volatile String validationQuery;


    private final BlockingDeque<Connection> connectionPool = new LinkedBlockingDeque<>();

    private transient boolean closed;

    public void initPool() {
        for (int i = 0; i < initPoolSize; i++) {
            connectionPool.add(createConnection());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        while (true) {
            Connection connection = connectionPool.poll();
            if (connection == null) {
                connectionPool.add(createConnection());
                continue;
            }
            if (connection.isClosed()) {
                continue;
            }
            return connection;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (this.username == null
                && this.password == null
                && username != null
                && password != null) {
            this.username = username;
            this.password = password;

            return getConnection();
        }

        if (!StringUtils.equals(username, this.username)) {
            throw new UnsupportedOperationException("Not supported by FastCharDataSource");
        }

        if (!StringUtils.equals(password, this.password)) {
            throw new UnsupportedOperationException("Not supported by FastCharDataSource");
        }

        return getConnection();
    }

    private Connection createConnection() {
        try {
            synchronized (FastSimpleDataSource.class) {
                Class.forName(driverClassName);
                Connection connection = DriverManager.getConnection(url, username, password);
                if (validate(connection)) {
                    return (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                            new Class[]{Connection.class}, new SimpleInvocationHandler(connection));
                }
            }
            throw new SQLException("Validation Query Failed.");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new ExceptionInInitializerError(e);
        }
    }

    private boolean validate(Connection connection) {
        if (FastStringUtils.isNotEmpty(validationQuery)) {
            try (Statement stmt = connection.createStatement()) {
                if (validationQueryTimeout > 0) {
                    stmt.setQueryTimeout(validationQueryTimeout);
                }
                stmt.execute(validationQuery);
                stmt.close();
                return true;
            } catch (Exception e) {
                FastChar.getLogger().error(this.getClass(), e);
            }
            return false;
        }
        return true;
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return printWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.printWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }


    public String getUrl() {
        return url;
    }

    public FastSimpleDataSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public FastSimpleDataSource setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FastSimpleDataSource setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getInitPoolSize() {
        return initPoolSize;
    }

    public FastSimpleDataSource setInitPoolSize(int initPoolSize) {
        this.initPoolSize = initPoolSize;
        return this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public FastSimpleDataSource setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public FastSimpleDataSource setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
        return this;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public FastSimpleDataSource setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
        return this;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public FastSimpleDataSource setMaxActive(int maxActive) {
        this.maxActive = maxActive;
        return this;
    }

    public void close() {
        this.closed = true;
        for (Connection connection : this.connectionPool) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        this.connectionPool.clear();
    }

    public boolean isClosed() {
        return closed;
    }

    private class SimpleInvocationHandler implements InvocationHandler {

        public SimpleInvocationHandler(Connection target) {
            this.connection = target;
        }

        private final Connection connection;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!isClosed()) {
                if (method.getName().equalsIgnoreCase("close")) {
                    if (connectionPool.size() < maxActive) {
                        connectionPool.addLast((Connection) proxy);
                        return null;
                    }
                }
            }
            return method.invoke(connection, args);
        }
    }


}
