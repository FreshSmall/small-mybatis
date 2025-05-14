package com.demo.mybatis.transaction.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import com.demo.mybatis.session.TransactionIsolationLevel;
import com.demo.mybatis.transaction.Transaction;
import com.demo.mybatis.transaction.TransactionFactory;

/**
 * @author: yinchao
 * @ClassName: JdbcTransactionFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/14 22:48
 */
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
