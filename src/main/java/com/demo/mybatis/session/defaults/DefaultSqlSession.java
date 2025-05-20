package com.demo.mybatis.session.defaults;

import java.util.List;

import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;


/**
 * @author: yinchao
 * @ClassName: DefaultSqlSession
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:29
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你的操作被代理了！" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        List<T> result = executor.query(mappedStatement, parameter, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        return result.get(0);
    }


    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
