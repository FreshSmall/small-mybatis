package com.spring.mybatis.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import com.demo.mybatis.session.TransactionIsolationLevel;
import com.demo.mybatis.transaction.Transaction;
import com.demo.mybatis.transaction.TransactionFactory;

/**
 * @author: yinchao
 * @ClassName: SpringManagedTransactionFactory
 * @Description: Spring管理的事务工厂，创建SpringManagedTransaction实例
 * 实现small-mybatis的TransactionFactory接口
 * @team wuhan operational dev.
 * @date: 2025/5/6 13:30
 */
public class SpringManagedTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        throw new UnsupportedOperationException("SpringManagedTransactionFactory does not support creating transaction from Connection");
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new SpringManagedTransaction(dataSource);
    }
} 