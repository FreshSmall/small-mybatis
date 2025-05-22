package com.demo.mybatis.executor.statement;

/*
 * @Author: yinchao
 * @Date: 2025-05-15 22:59:47
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:39:55
 * @Description:
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.demo.mybatis.executor.keygen.KeyGenerator;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.SqlCommandType;
import com.demo.mybatis.session.ResultHandler;

/**
 * 预处理语句处理器
 */
public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject,
            ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();

        // 处理自增主键
        if (mappedStatement.isUseGeneratedKeys() && mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            KeyGenerator keyGenerator = new Jdbc3KeyGenerator();
            keyGenerator.processGeneratedKeys(ps, parameterObject, mappedStatement);
        }

        return rows;
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();

        // 检查是否需要返回自增主键
        if (mappedStatement.isUseGeneratedKeys() && mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(sql);
        }
    }

}
