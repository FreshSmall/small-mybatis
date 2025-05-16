package com.demo.mybatis.session.defaults;

import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.mapping.Environment;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.TransactionIsolationLevel;
import com.demo.mybatis.transaction.Transaction;
import com.demo.mybatis.transaction.TransactionFactory;

/**
 * @author: yinchao
 * @ClassName: DefaultSqlSessionFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:29
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Environment environment = configuration.getEnvironment();
        TransactionFactory transactionFactory = environment.getTransactionFactory();
        Transaction tx = transactionFactory.newTransaction(environment.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
        Executor executor = configuration.newExecutor(tx);
        return new DefaultSqlSession(configuration, executor);
    }
}
