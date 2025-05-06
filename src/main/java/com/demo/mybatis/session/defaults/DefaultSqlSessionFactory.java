package com.demo.mybatis.session.defaults;

import com.demo.mybatis.binding.MapperRegistry;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;

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
        return new DefaultSqlSession(configuration);
    }
}
