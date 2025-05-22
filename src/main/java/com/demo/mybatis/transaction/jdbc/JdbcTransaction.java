package com.demo.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.demo.mybatis.session.TransactionIsolationLevel;
import com.demo.mybatis.transaction.Transaction;

/**
 * @author: yinchao
 * @ClassName: JdbcTransaction
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/14 22:47
 */
public class JdbcTransaction implements Transaction {

    protected Connection connection;
    protected DataSource dataSource;
    protected TransactionIsolationLevel level = TransactionIsolationLevel.NONE;
    protected boolean autoCommit;

    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        this.dataSource = dataSource;
        this.level = level;
        this.autoCommit = autoCommit;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    public void rollback() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException("事务回滚失败", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("关闭连接失败", e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            connection = dataSource.getConnection();
            connection.setTransactionIsolation(level.getLevel());
            connection.setAutoCommit(autoCommit);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }
}
