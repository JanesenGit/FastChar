package com.fastchar.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class FastTransaction {
    private int isolation = -1;
    private boolean commit;
    private boolean rollback;
    private ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public int getIsolation() {
        return isolation;
    }

    public FastTransaction setIsolation(int isolation) {
        this.isolation = isolation;
        return this;
    }

    public FastTransaction setConnection(String database, Connection connection) throws Exception {
        if (!connectionMap.containsKey(database)) {
            connection.setAutoCommit(false);
            connectionMap.put(database, connection);
        }
        return this;
    }

    public boolean isCommit() {
        return commit;
    }


    public boolean isRollback() {
        return rollback;
    }


    public Connection getConnection(String database) {
        return connectionMap.get(database);
    }

    public boolean contains(String database) {
        return connectionMap.containsKey(database);
    }


    public void commit() throws Exception {
        for (Connection value : connectionMap.values()) {
            if (isolation != -1) {
                value.setTransactionIsolation(isolation);
            }
            value.commit();
        }
        commit = true;
    }

    public void rollback() {
        for (Connection value : connectionMap.values()) {
            try {
                value.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        rollback = true;
    }

    public boolean isValid() {
        return !commit && !rollback;
    }


    public void close() {
        for (Connection value : connectionMap.values()) {
            try {
                if (value != null) {
                    value.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
