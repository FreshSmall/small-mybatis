package com.demo.mybatis.executor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.demo.mybatis.executor.statement.StatementHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.ResultHandler;
import com.demo.mybatis.transaction.Transaction;

public class SimpleExecutor implements Executor {

    private final Configuration configuration;
    private final Transaction transaction;

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        try {
            Statement stmt = null;
            try {
                Configuration configuration = ms.getConfiguration();
                StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, resultHandler,
                        boundSql);
                stmt = handler.prepare(transaction.getConnection());
                handler.parameterize(stmt);
                return handler.query(stmt, resultHandler);
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database.  Cause: " + e);
        }
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, Executor.NO_RESULT_HANDLER,
                    ms.getSqlSource().getBoundSql(parameter));
            stmt = handler.prepare(transaction.getConnection());
            handler.parameterize(stmt);
            return handler.update(stmt);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public void commit(boolean required) throws SQLException {
        transaction.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        transaction.rollback();
    }

    @Override
    public void close(boolean forceRollback) throws SQLException {
        transaction.close();
    }

}
