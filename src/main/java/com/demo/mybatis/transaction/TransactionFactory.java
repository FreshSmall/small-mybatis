package com.demo.mybatis.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import com.demo.mybatis.session.TransactionIsolationLevel;

/**
 * @author: yinchao
 * @ClassName: TransactionFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/14 22:47
 */
public interface TransactionFactory {
    Transaction newTransaction(Connection conn);

    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
