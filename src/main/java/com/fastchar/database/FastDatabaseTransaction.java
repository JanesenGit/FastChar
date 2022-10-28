package com.fastchar.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FastDatabaseTransaction {
    private static final ThreadLocal<FastDatabaseTransaction> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();

    public static boolean isThreadTransaction() {
        FastDatabaseTransaction fastTransaction = TRANSACTION_THREAD_LOCAL.get();
        return fastTransaction != null && fastTransaction.isValid();
    }

    public synchronized static void beginThreadTransaction() {
        if (isThreadTransaction()) {
            return;
        }
        TRANSACTION_THREAD_LOCAL.set(new FastDatabaseTransaction());
    }

    public synchronized static void endThreadTransaction() {
        FastDatabaseTransaction fastDatabaseTransaction = TRANSACTION_THREAD_LOCAL.get();
        if (fastDatabaseTransaction != null && fastDatabaseTransaction.isValid()) {
            fastDatabaseTransaction.commit();
        }
    }

    public synchronized static void rollbackThreadTransaction() {
        FastDatabaseTransaction fastDatabaseTransaction = TRANSACTION_THREAD_LOCAL.get();
        if (fastDatabaseTransaction != null && fastDatabaseTransaction.isValid()) {
            fastDatabaseTransaction.rollback();
        }
    }

    public static FastDatabaseTransaction getThreadTransaction() {
        return TRANSACTION_THREAD_LOCAL.get();
    }


    private int isolation = -1;
    private boolean commit;
    private boolean rollback;

    private final List<Connection> connections = new ArrayList<>(8);

    public int getIsolation() {
        return isolation;
    }

    public FastDatabaseTransaction setIsolation(int isolation) {
        this.isolation = isolation;
        return this;
    }

    public FastDatabaseTransaction registerConnection(Connection connection) throws SQLException {
        if (connections.contains(connection)) {
            return this;
        }
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
        connections.add(connection);
        return this;
    }


    public boolean isCommit() {
        return commit;
    }


    public boolean isRollback() {
        return rollback;
    }


    public void commit() {
        for (Connection value : connections) {
            try {
                if (isolation != -1) {
                    value.setTransactionIsolation(isolation);
                }
                value.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        commit = true;
    }

    public void rollback() {
        for (Connection value : connections) {
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
        for (Connection value : connections) {
            try {
                if (value != null) {
                    value.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
