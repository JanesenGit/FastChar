package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.exception.FastSqlException;
import com.fastchar.utils.FastIOUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * from ibatis ScriptRunner
 * http://www.mybatis.org/mybatis-3
 */
public class FastScriptRunner {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", 2);
    private final Connection connection;
    private boolean stopOnError;
    private boolean throwWarning;
    private boolean autoCommit;
    private boolean sendFullScript;
    private boolean removeCRs;
    private boolean escapeProcessing = true;
    private String delimiter;
    private boolean fullLineDelimiter;

    private int maxFullLineCount = 10000;

    public FastScriptRunner(Connection connection) {
        this.delimiter = ";";
        this.connection = connection;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public void setThrowWarning(boolean throwWarning) {
        this.throwWarning = throwWarning;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void setSendFullScript(boolean sendFullScript) {
        this.sendFullScript = sendFullScript;
    }

    public void setRemoveCRs(boolean removeCRs) {
        this.removeCRs = removeCRs;
    }

    public void setEscapeProcessing(boolean escapeProcessing) {
        this.escapeProcessing = escapeProcessing;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setFullLineDelimiter(boolean fullLineDelimiter) {
        this.fullLineDelimiter = fullLineDelimiter;
    }

    public int getMaxFullLineCount() {
        return maxFullLineCount;
    }

    public FastScriptRunner setMaxFullLineCount(int maxFullLineCount) {
        this.maxFullLineCount = maxFullLineCount;
        return this;
    }

    public void runScript(Reader reader) {
        this.setAutoCommit();

        try {
            if (this.sendFullScript) {
                this.executeFullScript(reader);
            } else {
                this.executeLineByLine(reader);
            }
        } finally {
            this.rollbackConnection();
            FastIOUtils.closeQuietly(reader);
        }

    }

    private void executeFullScript(Reader reader) {
        StringBuilder script = new StringBuilder();

        String line;
        try {
            BufferedReader lineReader = new BufferedReader(reader);

            int lineCount = 0;
            while ((line = lineReader.readLine()) != null) {
                lineCount++;
                script.append(line);
                script.append(LINE_SEPARATOR);
                if (lineCount >= maxFullLineCount) {
                    String command = script.toString();
                    this.executeStatement(command);
                    script = new StringBuilder();
                    lineCount = 0;
                }
            }

            String command = script.toString();
            this.executeStatement(command);
            this.commitConnection();
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    private void executeLineByLine(Reader reader) {
        StringBuilder command = new StringBuilder();

        String line;
        try {
            BufferedReader lineReader = new BufferedReader(reader);

            while ((line = lineReader.readLine()) != null) {
                this.handleLine(command, line);
            }

            this.commitConnection();
            this.checkForMissingLineTerminator(command);
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (Exception ignored) {
        }

    }

    private void setAutoCommit() {
        try {
            if (this.autoCommit != this.connection.getAutoCommit()) {
                this.connection.setAutoCommit(this.autoCommit);
            }

        } catch (Throwable var2) {
            throw new FastSqlException("Could not set AutoCommit to " + this.autoCommit + ". Cause: " + var2, var2);
        }
    }

    private void commitConnection() {
        try {
            if (!this.connection.getAutoCommit()) {
                this.connection.commit();
            }

        } catch (Throwable var2) {
            throw new FastSqlException("Could not commit transaction. Cause: " + var2, var2);
        }
    }

    private void rollbackConnection() {
        try {
            if (!this.connection.getAutoCommit()) {
                this.connection.rollback();
            }
        } catch (Throwable ignored) {
        }

    }

    private void checkForMissingLineTerminator(StringBuilder command) {
        if (command != null && command.toString().trim().length() > 0) {
            throw new FastSqlException("Line missing end-of-line terminator (" + this.delimiter + ") => " + command);
        }
    }

    private void handleLine(StringBuilder command, String line) throws SQLException {
        String trimmedLine = line.trim();
        if (this.lineIsComment(trimmedLine)) {
            Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);
            if (matcher.find()) {
                this.delimiter = matcher.group(5);
            }

        } else if (this.commandReadyToExecute(trimmedLine)) {
            command.append(line.substring(0, line.lastIndexOf(this.delimiter)));
            command.append(LINE_SEPARATOR);
            this.executeStatement(command.toString());
            command.setLength(0);
        } else if (trimmedLine.length() > 0) {
            command.append(line);
            command.append(LINE_SEPARATOR);
        }

    }

    private boolean lineIsComment(String trimmedLine) {
        return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
    }

    private boolean commandReadyToExecute(String trimmedLine) {
        return !this.fullLineDelimiter && trimmedLine.contains(this.delimiter) || this.fullLineDelimiter && trimmedLine.equals(this.delimiter);
    }

    private void executeStatement(String command) throws SQLException {
        Statement statement = this.connection.createStatement();

        try {
            statement.setEscapeProcessing(this.escapeProcessing);
            String sql = command;
            if (this.removeCRs) {
                sql = command.replaceAll("\r\n", "\n");
            }

            try {
                for (boolean hasResults = statement.execute(sql); hasResults || statement.getUpdateCount() != -1; hasResults = statement.getMoreResults()) {
                    this.checkWarnings(statement);
                    this.printResults(statement, hasResults);
                }
            } catch (SQLWarning var14) {
                throw var14;
            } catch (SQLException e) {
                FastChar.getLogger().error(this.getClass(), e);
            }
        } finally {
            try {
                statement.close();
            } catch (Exception ignored) {
            }

        }

    }

    private void checkWarnings(Statement statement) throws SQLException {
        if (this.throwWarning) {
            SQLWarning warning = statement.getWarnings();
            if (warning != null) {
                throw warning;
            }
        }
    }

    private void printResults(Statement statement, boolean hasResults) {
        if (hasResults) {
            try {
                ResultSet rs = statement.getResultSet();

                try {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    int i = 0;

                    label50:
                    while (true) {
                        String value;
                        if (i >= cols) {
                            while (true) {
                                if (!rs.next()) {
                                    break label50;
                                }

                                for (i = 0; i < cols; ++i) {
                                    value = rs.getString(i + 1);
                                }
                            }
                        }

                        value = md.getColumnLabel(i + 1);
                        ++i;
                    }
                } catch (Throwable var9) {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }
                    throw var9;
                }
                rs.close();
            } catch (SQLException e) {
                FastChar.getLogger().error(this.getClass(), e);
            }

        }
    }

}
