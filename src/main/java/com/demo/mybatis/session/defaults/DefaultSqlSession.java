package com.demo.mybatis.session.defaults;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.demo.mybatis.binding.MapperRegistry;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author: yinchao
 * @ClassName: DefaultSqlSession
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:29
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你的操作被代理了！" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        try {
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            Environment environment = configuration.getEnvironment();

            Connection connection = environment.getDataSource().getConnection();

            BoundSql boundSql = mappedStatement.getBoundSql();
            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
            return objList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
