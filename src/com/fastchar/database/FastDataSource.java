package com.fastchar.database;

import com.fastchar.utils.FastStringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastDataSource implements DataSource {
    private static final String MYSQL_REG = "jdbc:mysql://(.*):(\\d{2,4})/([^?&;=]*)";
    private static final String SQL_SERVER_REG = "jdbc:sqlserver://(.*):(\\d{2,4});databaseName=([^?&;=]*)";
    private static final String ORACLE_REG = "jdbc:oracle:thin:@[/]{0,2}(.*):(\\d{2,4})[:/]([^?&;=]*)";


    private String url;
    private String username;
    private String password;
    private String catalog;
    private String schema;
    private String driverClassName;

    public String getUrl() {
        return url;
    }

    public FastDataSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public FastDataSource setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FastDataSource setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public FastDataSource setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public FastDataSource setSchema(String schema) {
        this.schema = schema;
        return this;
    }


    public FastDataSource setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(this.getUsername(), this.getPassword());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnectionFromDriver(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        } else {
            throw new SQLException("DataSource of type [" + this.getClass().getName() + "] cannot be unwrapped as [" + iface.getName() + "]");
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger("global");
    }


    private Connection getConnectionFromDriver(String user, String password) throws SQLException {
        try {
            Class.forName(driverClassName);
            Connection con = DriverManager.getConnection(getUrl(), user, password);
            if (this.catalog != null) {
                con.setCatalog(this.catalog);
            }

            if (this.schema != null) {
                con.setSchema(this.schema);
            }
            return con;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
